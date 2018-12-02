package zdream.nsfplayer.ftm.renderer;

import static zdream.nsfplayer.core.NsfStatic.BASE_FREQ_NTSC;

import java.util.Set;

import zdream.nsfplayer.core.AbstractNsfRenderer;
import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.core.NsfCommonParameter;
import zdream.nsfplayer.core.NsfPlayerApplication;
import zdream.nsfplayer.core.NsfPlayerException;
import zdream.nsfplayer.core.NsfRateConverter;
import zdream.nsfplayer.ftm.audio.FtmAudio;
import zdream.nsfplayer.ftm.executor.FamiTrackerExecutor;
import zdream.nsfplayer.ftm.executor.FamiTrackerParameter;
import zdream.nsfplayer.ftm.executor.hook.IFtmExecutedListener;
import zdream.nsfplayer.ftm.executor.hook.IFtmFetchListener;
import zdream.nsfplayer.mixer.IMixerChannel;
import zdream.nsfplayer.mixer.IMixerConfig;
import zdream.nsfplayer.mixer.IMixerHandler;
import zdream.nsfplayer.mixer.ISoundMixer;
import zdream.nsfplayer.mixer.xgm.XgmMixerConfig;
import zdream.nsfplayer.sound.AbstractNsfSound;

/**
 * <p>默认 FamiTracker 部分的音频渲染器.
 * <p>来源于原 C++ 工程的 SoundGen 类.
 * <p>该渲染器是线程不安全的, 请注意不要在渲染途中设置参数.
 * </p>
 * 
 * @version <b>v0.2.4</b>
 * <br>抽出抽象渲染器, 将部分方法移交至父类抽象渲染器中.
 *   
 * @version <b>v0.3.0</b>
 * <br>将原来执行相关的构件移至 {@link FamiTrackerExecutor} 中.
 * 
 * @author Zdream
 * @since v0.2.1
 */
public class FamiTrackerRenderer extends AbstractNsfRenderer<FtmAudio> {
	
	/**
	 * 执行器
	 */
	private final FamiTrackerExecutor executor = new FamiTrackerExecutor();
	
	/**
	 * 速率转换器
	 */
	private final NsfRateConverter rate;
	
	/**
	 * 音频混音器
	 */
	private ISoundMixer mixer;
	
	private FamiTrackerConfig config;
	
	private NsfCommonParameter param = new NsfCommonParameter();
	
	/**
	 * 利用默认配置产生一个音频渲染器
	 */
	public FamiTrackerRenderer() {
		this(null);
	}
	
	public FamiTrackerRenderer(FamiTrackerConfig config) {
		if (config == null) {
			this.config = new FamiTrackerConfig();
		} else {
			this.config = config.clone();
		}
		
		// 采样率数据只有渲染构建需要
		param.sampleRate = this.config.sampleRate;
		
		// 音量参数只有渲染构建需要
		param.levels.copyFrom(this.config.channelLevels);
		
		rate = new NsfRateConverter(param);
		initMixer();
	}
	
	private void initMixer() {
		IMixerConfig mixerConfig = config.mixerConfig;
		if (mixerConfig == null) {
			mixerConfig = new XgmMixerConfig();
		}
		
		this.mixer = NsfPlayerApplication.app.mixerFactory.create(mixerConfig, param);
	}
	
	/* **********
	 * 准备部分 *
	 ********** */
	
	/**
	 * <p>让该渲染器读取对应的 audio 数据.
	 * <p>设置播放暂停位置为第 1 个曲目 (曲目 0) 的第一段 (段 0)
	 * </p>
	 * @param audio
	 */
	public void ready(FtmAudio audio) throws NsfPlayerException {
		ready(audio, 0, 0);
	}
	
	/**
	 * <p>让该渲染器读取对应的 audio 数据.
	 * <p>设置播放暂停位置为指定曲目的第一段 (段 0)
	 * </p>
	 * @param audio
	 * @param track
	 *   曲目号, 从 0 开始
	 */
	public void ready(FtmAudio audio, int track) throws NsfPlayerException {
		ready(audio, track, 0);
	}
	
	/**
	 * <p>让该渲染器读取对应的 audio 数据.
	 * <p>设置播放暂停位置为指定曲目的指定段
	 * </p>
	 * @param audio
	 * @param track
	 *   曲目号, 从 0 开始
	 * @param section
	 *   段号, 从 0 开始
	 */
	public void ready(
			FtmAudio audio,
			int track,
			int section)
			throws NsfPlayerException {
		executor.ready(audio, track, section);
		
		// 重置播放相关的数据
		int frameRate = executor.getFrameRate();
		resetCounterParam(frameRate, param.sampleRate);
		clearBuffer();
		rate.onParamUpdate(frameRate, BASE_FREQ_NTSC);
		
		reloadMixer();
		connectChannels();
	}
	
	/**
	 * <p>在不更改 Ftm 音频的同时, 重置当前曲目, 让播放的位置重置到曲目开头
	 * <p>第一次播放时需要指定 Ftm 音频数据.
	 * 因此第一次需要调用含 {@link FtmAudio} 参数的重载方法
	 * </p>
	 * @throws NullPointerException
	 *   当调用该方法前未指定 {@link FtmAudio} 音频时
	 */
	public void ready() throws NsfPlayerException {
		executor.ready();
		resetMixer();
	}
	
	/**
	 * <p>在不更改 Ftm 文件的同时, 切换到指定曲目的开头.
	 * <p>第一次播放时需要指定 Ftm 音频数据.
	 * 因此第一次需要调用含 {@link FtmAudio} 参数的重载方法
	 * </p>
	 * @param track
	 *   曲目号, 从 0 开始
	 * @throws NullPointerException
	 *   当调用该方法前未指定 {@link FtmAudio} 音频时
	 */
	public void ready(int track) throws NsfPlayerException {
		ready(track, 0);
	}
	
	/**
	 * <p>在不更改 Ftm 文件的同时, 切换曲目、段号
	 * <p>第一次播放时需要指定 Ftm 音频数据.
	 * 因此第一次需要调用含 {@link FtmAudio} 参数的重载方法
	 * </p>
	 * @param track
	 *   曲目号, 从 0 开始
	 * @param section
	 *   段号, 从 0 开始
	 * @throws NullPointerException
	 *   当调用该方法前未指定 {@link FtmAudio} 音频时
	 */
	public void ready(int track, int section) throws NsfPlayerException {
		executor.ready(track, section);
		resetMixer();
	}
	
	/**
	 * <p>不改变各个轨道参数的情况下, 切换到指定播放位置.
	 * 切换时, 各轨道的播放音高、音量、效果等均不改变, 这也包括延迟效果 Gxx.
	 * 混音器不会重置, 这也意味着上一帧播放的音可能继续延长播放下去.
	 * 而 FTM 文档的播放速度（不是播放速度 speed）会重新根据 tempo 等数值重置.
	 * <p>请谨慎使用该方法. 如果前面使用了颤音 4xy 或者其它效果, 而没有消除时,
	 * 切换位置后, 这些效果会仍然保留下来, 导致后面播放会很奇怪.
	 * 如果想使用更加稳健的方式切换播放位置, 而不会使播放效果发生较大变化,
	 * 请使用 {@link #ready(int, int)} 或 {@link #skip(int)} 方法.
	 * <p>需要在调用前确定该渲染器已经成功加载了 {@link FtmAudio} 音频.
	 * </p>
	 * @param track
	 *   曲目号, 从 0 开始
	 * @param section
	 *   段号, 从 0 开始
	 * @throws NullPointerException
	 *   当调用该方法前未成功加载 {@link FtmAudio} 音频时
	 * @see #ready(int, int)
	 * @see #skip(int)
	 * @since v0.2.9
	 */
	public void switchTo(int track, int section) {
		executor.switchTo(track, section);
	}
	
	/* **********
	 * 渲染部分 *
	 ********** */
	
	/**
	 * <p>渲染一帧.
	 * <p>这个方法在 v0.3.0 版本中有了新的解释, 即: 执行构件执行一帧, 渲染构件也执行一帧.
	 * </p>
	 * @return
	 *   本函数已渲染的采样数 (按单声道计算)
	 */
	protected int renderFrame() {
		int ret = countNextFrame();
		param.sampleInCurFrame = ret;
		rate.doConvert();
		mixer.readyBuffer();
		
		handleDelay();
		executor.tick();
		triggerSounds();
		
		// 从 mixer 中读取数据
		readMixer();
		
		return ret;
	}
	
	/**
	 * <p>跳过一帧.
	 * <p>这个方法在 v0.3.0 版本中有了新的解释, 即: 执行构件执行一帧, 渲染构件不执行.
	 * </p>
	 * @return
	 *   本函数已跳过的采样数 (按单声道计算)
	 */
	protected int skipFrame() {
		int ret = countNextFrame();
		param.sampleInCurFrame = ret;
		rate.doConvert();

		executor.tick();
		triggerSounds();
		
		return ret;
	}
	
	/**
	 * <p>询问是否已经播放完毕
	 * <p>如果已经播放完毕的 Ftm 音频尝试再调用 {@link #render(byte[], int, int)}
	 * 或者 {@link #renderOneFrame(byte[], int, int)}, 则会忽略停止符号,
	 * 强制再向下播放.
	 * </p>
	 * @return
	 */
	public boolean isFinished() {
		return executor.isFinished();
	}

	/* **********
	 * 仪表盘区 *
	 ********** */
	/*
	 * 用于控制实际播放数据的部分.
	 * 其中有: 控制音量、控制是否播放
	 */
	
	/**
	 * @return
	 *   获取正在播放的曲目号
	 */
	public int getCurrentTrack() {
		return executor.getCurrentTrack();
	}

	/**
	 * @return
	 *   获取正在播放的段号
	 */
	public int getCurrentSection() {
		return executor.getCurrentSection();
	}
	
	/**
	 * @return
	 *   获取正在播放的行号
	 */
	public int getCurrentRow() {
		return executor.getCurrentRow();
	}
	
	/**
	 * 询问当前行是否播放完毕, 需要跳到下一行 (不是询问当前帧是否播放完)
	 * @return
	 *   true, 如果当前行已经播放完毕
	 * @since v0.2.2
	 */
	public boolean currentRowRunOut() {
		return executor.currentRowRunOut();
	}

	/**
	 * <p>获取如果跳到下一行（不是下一帧）, 跳到的位置所对应的段号.
	 * <p>如果侦测到有跳转的效果正在触发, 按触发后的结果返回.
	 * </p>
	 * @return
	 *   下一行对应的段号
	 * @since v0.2.9
	 */
	public int getNextSection() {
		return executor.getNextSection();
	}
	
	/**
	 * <p>获取如果跳到下一行（不是下一帧）, 跳到的位置所对应的行号.
	 * <p>如果侦测到有跳转的效果正在触发, 按触发后的结果返回.
	 * </p>
	 * @return
	 *   下一行对应的段号
	 * @since v0.2.9
	 */
	public int getNextRow() {
		return executor.getNextRow();
	}
	
	/**
	 * 返回所有的轨道号的集合. 轨道号的参数在 {@link INsfChannelCode} 里面写出
	 * @return
	 *   所有的轨道号的集合. 如果没有调用 ready(...) 方法时, 返回空集合.
	 * @since v0.2.2
	 */
	public Set<Byte> allChannelSet() {
		return executor.allChannelSet();
	}
	
	/**
	 * 设置某个轨道的音量
	 * @param channelCode
	 *   轨道号
	 * @param level
	 *   音量. 范围 [0, 1]
	 * @since v0.2.2
	 */
	public void setLevel(byte channelCode, float level) {
		if (level < 0) {
			level = 0;
		} else if (level > 1) {
			level = 1;
		}
		
		int id = findMixerChannelByCode(channelCode);
		if (id != -1) {
			mixer.setLevel(id, level);
		}
	}
	
	/**
	 * 获得某个轨道的音量
	 * @param channelCode
	 *   轨道号
	 * @return
	 *   音量. 范围 [0, 1]
	 * @throws NullPointerException
	 *   当不存在 <code>channelCode</code> 对应的轨道时
	 * @since v0.2.3
	 */
	public float getLevel(byte channelCode) throws NullPointerException {
		int id = findMixerChannelByCode(channelCode);
		if (id != -1) {
			return mixer.getLevel(id);
		}
		throw new NullPointerException("不存在 " + channelCode + " 对应的轨道");
	}
	
	/**
	 * 设置轨道是否发出声音
	 * @param channelCode
	 *   轨道号
	 * @param mask
	 *   false, 使该轨道发声; true, 则静音
	 * @since v0.2.2
	 */
	public void setChannelMask(byte channelCode, boolean mask) {
		AbstractNsfSound sound = executor.getSound(channelCode);
		if (sound != null) {
			sound.setMask(mask);
		}
	}
	
	/**
	 * 查看轨道是否能发出声音
	 * @param channelCode
	 *   轨道号
	 * @return
	 *   false, 说明该轨道没有被屏蔽; true, 则已经被屏蔽
	 * @throws NullPointerException
	 *   当不存在 <code>channelCode</code> 对应的轨道时
	 * @since v0.2.3
	 */
	public boolean isChannelMask(byte channelCode) throws NullPointerException {
		return executor.getSound(channelCode).isMask();
	}
	
	@Override
	public void setSpeed(float speed) {
		if (speed > 10) {
			speed = 10;
		} else if (speed < 0.1f) {
			speed = 0.1f;
		}
		
		param.speed = speed;
		
		int frameRate = executor.getFrameRate();
		resetCounterParam(frameRate, param.sampleRate);
		rate.onParamUpdate();
	}
	
	@Override
	public float getSpeed() {
		return param.speed;
	}
	
	/**
	 * 获得混音器的操作者（工具类）. 通过它可以对所使用的混音器进行简单的操作.
	 * @return
	 *   混音器的操作者
	 * @since v0.2.10
	 */
	public IMixerHandler getMixerHandler() {
		return mixer.getHandler();
	}
	
	/* **********
	 *  初始化  *
	 ********** */
	
	/**
	 * 初始化 / 重置音频合成器 (混音器)
	 */
	private void reloadMixer() {
		mixer.detachAll();
		mixer.reset();
	}
	
	/**
	 * <p>将执行构件中的发声器取出, 与混音器相连接.
	 * </p>
	 */
	private void connectChannels() {
		Set<Byte> channels = executor.allChannelSet();
		this.channels = new ChannelParam[channels.size()];
		
		int index = 0;
		int mixerChannel = -1;
		for (byte channelCode: channels) {
			AbstractNsfSound sound = executor.getSound(channelCode);
			if (sound != null) {
				mixerChannel = mixer.allocateChannel(channelCode);
				IMixerChannel mix = mixer.getMixerChannel(mixerChannel);
				sound.setOut(mix);
				
				// 音量
				mix.setLevel(getInitLevel(channelCode));
				
				// TODO 告诉混音器更多的信息, 包括发声器的输出采样率 (NSF 的为 177万, mpeg 的为 44100 或 48000 等)
				
			}
			
			// channel param
			ChannelParam p = new ChannelParam();
			this.channels[index] = p;
			p.channelCode = channelCode;
			p.delay = index * 100;
			p.mixerChannel = mixerChannel;
			
			index++;
		}
	}
	
	/**
	 * 获得某个轨道的原始音量. 这个值要从 {@link FamiTrackerParameter} 中取
	 * @param channelCode
	 *   轨道号
	 * @return
	 *   范围 [0, 1]
	 * @since v0.2.4
	 */
	private float getInitLevel(byte channelCode) {
		float level = 0;
		switch (channelCode) {
		case CHANNEL_2A03_PULSE1: level = config.channelLevels.level2A03Pules1; break;
		case CHANNEL_2A03_PULSE2: level = config.channelLevels.level2A03Pules2; break;
		case CHANNEL_2A03_TRIANGLE: level = config.channelLevels.level2A03Triangle; break;
		case CHANNEL_2A03_NOISE: level = config.channelLevels.level2A03Noise; break;
		case CHANNEL_2A03_DPCM: level = config.channelLevels.level2A03DPCM; break;

		case CHANNEL_VRC6_PULSE1: level = config.channelLevels.levelVRC6Pules1; break;
		case CHANNEL_VRC6_PULSE2: level = config.channelLevels.levelVRC6Pules2; break;
		case CHANNEL_VRC6_SAWTOOTH: level = config.channelLevels.levelVRC6Sawtooth; break;

		case CHANNEL_MMC5_PULSE1: level = config.channelLevels.levelMMC5Pules1; break;
		case CHANNEL_MMC5_PULSE2: level = config.channelLevels.levelMMC5Pules2; break;
		
		case CHANNEL_FDS: level = config.channelLevels.levelFDS; break;
		
		case CHANNEL_N163_1: level = config.channelLevels.levelN163Namco1; break;
		case CHANNEL_N163_2: level = config.channelLevels.levelN163Namco2; break;
		case CHANNEL_N163_3: level = config.channelLevels.levelN163Namco3; break;
		case CHANNEL_N163_4: level = config.channelLevels.levelN163Namco4; break;
		case CHANNEL_N163_5: level = config.channelLevels.levelN163Namco5; break;
		case CHANNEL_N163_6: level = config.channelLevels.levelN163Namco6; break;
		case CHANNEL_N163_7: level = config.channelLevels.levelN163Namco7; break;
		case CHANNEL_N163_8: level = config.channelLevels.levelN163Namco8; break;
		
		case CHANNEL_VRC7_FM1: level = config.channelLevels.levelVRC7FM1; break;
		case CHANNEL_VRC7_FM2: level = config.channelLevels.levelVRC7FM2; break;
		case CHANNEL_VRC7_FM3: level = config.channelLevels.levelVRC7FM3; break;
		case CHANNEL_VRC7_FM4: level = config.channelLevels.levelVRC7FM4; break;
		case CHANNEL_VRC7_FM5: level = config.channelLevels.levelVRC7FM5; break;
		case CHANNEL_VRC7_FM6: level = config.channelLevels.levelVRC7FM6; break;
		
		case CHANNEL_S5B_SQUARE1: level = config.channelLevels.levelS5BSquare1; break;
		case CHANNEL_S5B_SQUARE2: level = config.channelLevels.levelS5BSquare2; break;
		case CHANNEL_S5B_SQUARE3: level = config.channelLevels.levelS5BSquare3; break;
		
		default: level = 1.0f; break;
		}
		
		if (level > 1) {
			level = 1.0f;
		} else if (level < 0) {
			level = 0;
		}
		
		return level;
	}
	
	/**
	 * <p>处理延迟写. 后一个轨道比前一个轨道晚 100 时钟写入数据.
	 * <p>由于每个轨道的触发时间不同可以有效避免轨道之间共振情况的发生,
	 * 因此这里需要采用轨道先后写入数据的方式.
	 * <p>在版本 v0.2.9 与 v0.2.10 时, 延迟写是由 AbstractFtmChannel 完成的.
	 * 现在由于执行构件的分离, 延迟写的任务现在由渲染器承担.
	 * </p>
	 * @see #triggerSounds()
	 * @since v0.3.0
	 */
	private void handleDelay() {
		for (int i = 0; i < channels.length; i++) {
			ChannelParam p = channels[i];
			
			byte channelCode = p.channelCode;
			AbstractNsfSound s = executor.getSound(channelCode);
			s.process(p.delay);
		}
	}
	
	/**
	 * <p>让发声器逐个进行工作.
	 * <p>工作的时钟数, 为该帧需要工作的时钟数, 减去延迟时钟数.
	 * </p>
	 * @see #handleDelay()
	 * @since v0.3.0
	 */
	private void triggerSounds() {
		final int clock = param.freqPerFrame;
		for (int i = 0; i < channels.length; i++) {
			ChannelParam p = channels[i];
			
			byte channelCode = p.channelCode;
			AbstractNsfSound s = executor.getSound(channelCode);
			s.process(clock - p.delay);
			s.endFrame();
		}
	}
	
	/**
	 * 重置 Mixer
	 */
	private void resetMixer() {
		mixer.reset();
	}
	
	/**
	 * 从 Mixer 中读取音频数据
	 */
	private void readMixer() {
		mixer.finishBuffer();
		mixer.readBuffer(data, 0, data.length);
	}
	
	class ChannelParam {
		/**
		 * 轨道号
		 */
		byte channelCode;
		/**
		 * 延迟写时钟数
		 */
		int delay;
		/**
		 * Mixer 轨道标识号
		 */
		int mixerChannel;
	}
	private ChannelParam[] channels;
	
	/**
	 * 根据轨道号, 找到 Mixer 中的轨道标识号
	 * @param channelCode
	 *   NSF 定义的轨道号
	 * @return
	 *   Mixer 的轨道标识号
	 * @since v0.3.0
	 */
	private int findMixerChannelByCode(byte channelCode) {
		for (ChannelParam p : channels) {
			if (p == null) {
				continue;
			}
			if (p.channelCode == channelCode) {
				return p.mixerChannel;
			}
		}
		return -1;
	}
	

	
	/* **********
	 *  监听器  *
	 ********** */
	
	/**
	 * 添加获取音键的监听器
	 * @param l
	 *   获取音键的监听器
	 * @throws NullPointerException
	 *   当监听器 <code>l == null</code> 时
	 * @since v0.3.0
	 */
	public void addFetchListener(IFtmFetchListener l) {
		executor.addFetchListener(l);
	}
	
	/**
	 * 移除获取音键的监听器
	 * @param l
	 *   移除音键的监听器
	 * @since v0.3.0
	 */
	public void removeFetchListener(IFtmFetchListener l) {
		executor.removeFetchListener(l);
	}
	
	/**
	 * 清空所有获取音键的监听器
	 * @since v0.3.0
	 */
	public void clearFetchListener() {
		executor.clearFetchListener();
	}
	
	/**
	 * 添加执行结束的监听器.
	 * 该监听器会在效果执行结束, 但还未写入 sound 时唤醒.
	 * @param l
	 *   执行结束的监听器
	 * @throws NullPointerException
	 *   当监听器 <code>l == null</code> 时
	 * @since v0.3.0
	 */
	public void addExecuteFinishedListener(IFtmExecutedListener l) {
		executor.addExecuteFinishedListener(l);
	}
	
	/**
	 * 移除执行结束的监听器
	 * @param l
	 *   执行结束的监听器
	 * @since v0.3.0
	 */
	public void removeExecuteFinishedListener(IFtmExecutedListener l) {
		executor.removeExecuteFinishedListener(l);
	}
	
	/**
	 * 清空所有执行结束的监听器
	 * @since v0.3.0
	 */
	public void clearExecuteFinishedListener() {
		executor.clearExecuteFinishedListener();
	}
	
}
