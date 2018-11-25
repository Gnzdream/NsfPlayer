package zdream.nsfplayer.ftm.renderer;

import static zdream.nsfplayer.core.NsfStatic.BASE_FREQ_NTSC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import zdream.nsfplayer.core.AbstractNsfRenderer;
import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.ftm.audio.FamiTrackerException;
import zdream.nsfplayer.ftm.audio.FamiTrackerQuerier;
import zdream.nsfplayer.ftm.audio.FtmAudio;
import zdream.nsfplayer.ftm.renderer.effect.FtmEffectType;
import zdream.nsfplayer.ftm.renderer.effect.IFtmEffect;
import zdream.nsfplayer.sound.AbstractNsfSound;
import zdream.nsfplayer.sound.mixer.IMixerChannel;
import zdream.nsfplayer.sound.mixer.IMixerHandler;

/**
 * <p>默认 FamiTracker 部分的音频渲染器.
 * <p>来源于原 C++ 工程的 SoundGen 类
 * </p>
 * 
 * @version v0.2.4
 *   抽出抽象渲染器, 将部分方法移交至父类抽象渲染器中.
 * 
 * @author Zdream
 * @since v0.2.1
 */
public class FamiTrackerRenderer extends AbstractNsfRenderer<FtmAudio> {
	
	final FamiTrackerRuntime runtime = new FamiTrackerRuntime();
	
	/**
	 * 利用默认配置产生一个音频渲染器
	 */
	public FamiTrackerRenderer() {
		this(null);
	}
	
	public FamiTrackerRenderer(FamiTrackerConfig config) {
		if (config == null) {
			this.runtime.config = new FamiTrackerConfig();
		} else {
			this.runtime.config = config.clone();
		}
		
		runtime.param.sampleRate = this.runtime.config.sampleRate;
		
		this.runtime.init();
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
	public void ready(FtmAudio audio) throws FamiTrackerException {
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
	public void ready(FtmAudio audio, int track) throws FamiTrackerException {
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
			throws FamiTrackerException {
		runtime.ready(audio, track, section);
		
		// 重置播放相关的数据
		int frameRate = runtime.fetcher.getFrameRate();
		resetCounterParam(frameRate, runtime.config.sampleRate);
		clearBuffer();
		runtime.rate.onParamUpdate(frameRate, BASE_FREQ_NTSC);
		
		initMixer();
		initChannels();
	}
	
	/**
	 * <p>在不更改 Ftm 音频的同时, 重置当前曲目, 让播放的位置重置到曲目开头
	 * <p>第一次播放时需要指定 Ftm 音频数据.
	 * 因此第一次需要调用含 {@link FtmAudio} 参数的重载方法
	 * </p>
	 * @throws NullPointerException
	 *   当调用该方法前未指定 {@link FtmAudio} 音频时
	 */
	public void ready() throws FamiTrackerException {
		ready(runtime.param.trackIdx, runtime.param.curSection);
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
	public void ready(int track) throws FamiTrackerException {
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
	public void ready(int track, int section) throws FamiTrackerException {
		if (runtime.fetcher.querier == null) {
			throw new NullPointerException("FtmAudio = null");
		}
		
		runtime.ready(track, section);
		runtime.resetAllChannels();
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
		if (runtime.fetcher.querier == null) {
			throw new NullPointerException("FtmAudio = null");
		}
		
		runtime.ready(track, section);
	}
	
	/* **********
	 * 渲染部分 *
	 ********** */
	
	/**
	 * 渲染一帧
	 * <br>SoundGen.playFrame
	 * @return
	 *   本函数已渲染的采样数 (按单声道计算)
	 */
	protected int renderFrame() {
		int ret = countNextFrame();
		runtime.param.sampleInCurFrame = ret;
		runtime.rate.doConvert();
		runtime.mixerReady();
		
		runtime.runFrame();
		updateChannels(true);
		
		runtime.fetcher.updateState();
		
		// 从 mixer 中读取数据
		readMixer();
		
		log();
		
		return ret;
	}
	
	@Override
	protected int skipFrame() {
		int ret = countNextFrame();
		runtime.param.sampleInCurFrame = ret;
		runtime.rate.doConvert();
		
		runtime.runFrame();
		updateChannels(false);
		
		runtime.fetcher.updateState();
		
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
		return runtime.param.finished;
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
		return runtime.param.trackIdx;
	}

	/**
	 * @return
	 *   获取正在播放的段号
	 */
	public int getCurrentSection() {
		return runtime.param.curSection;
	}
	
	/**
	 * @return
	 *   获取正在播放的行号
	 */
	public int getCurrentRow() {
		return runtime.param.curRow;
	}
	
	/**
	 * 询问当前行是否播放完毕, 需要跳到下一行 (不是询问当前帧是否播放完)
	 * @return
	 *   true, 如果当前行已经播放完毕
	 * @since v0.2.2
	 */
	public boolean currentRowRunOut() {
		return runtime.fetcher.needRowUpdate();
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
		return runtime.fetcher.getNextSection();
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
		return runtime.fetcher.getNextRow();
	}
	
	/**
	 * 返回所有的轨道号的集合. 轨道号的参数在 {@link INsfChannelCode} 里面写出
	 * @return
	 *   所有的轨道号的集合. 如果没有调用 ready(...) 方法时, 返回空集合.
	 * @since v0.2.2
	 */
	public Set<Byte> allChannelSet() {
		return new HashSet<>(runtime.effects.keySet());
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
		runtime.mixer.setLevel(channelCode, level);
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
		return runtime.mixer.getLevel(channelCode);
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
		AbstractFtmChannel ch = runtime.channels.get(channelCode);
		if (ch != null) {
			ch.getSound().setMask(mask);
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
		return runtime.channels.get(channelCode).getSound().isMask();
	}
	
	@Override
	public void setSpeed(float speed) {
		if (speed > 10) {
			speed = 10;
		} else if (speed < 0.1f) {
			speed = 0.1f;
		}
		
		runtime.param.speed = speed;
		
		int frameRate = runtime.querier.getFrameRate();
		resetCounterParam(frameRate, runtime.config.sampleRate);
		runtime.rate.onParamUpdate();
	}
	
	@Override
	public float getSpeed() {
		return runtime.param.speed;
	}
	
	/**
	 * 获得混音器的操作者（工具类）. 通过它可以对所使用的混音器进行简单的操作.
	 * @return
	 *   混音器的操作者
	 * @since v0.2.10
	 */
	public IMixerHandler getMixerHandler() {
		return runtime.mixer.getHandler();
	}
	
	/* **********
	 *  初始化  *
	 ********** */
	
	/**
	 * 初始化 / 重置音频合成器 (混音器)
	 */
	private void initMixer() {
		runtime.mixer.detachAll();
		runtime.mixer.reset();
	}
	
	/**
	 * 利用 runtime 已经完成填充的数据, 建立 AbstractFtmChannel, 还有各个轨道的 EffectBatch
	 * 
	 * 包含原工程 SoundGen.createChannels()
	 */
	private void initChannels() {
		runtime.channels.clear();
		runtime.effects.clear();
		runtime.selector.reset();
		
		final FamiTrackerQuerier querier = runtime.querier;
		
		final int len = querier.channelCount();
		for (int i = 0; i < len; i++) {
			byte code = querier.channelCode(i);
			
			AbstractFtmChannel ch = runtime.selector.selectFtmChannel(code);
			ch.setRuntime(runtime);
			ch.setDelay(i * 100);
			runtime.channels.put(code, ch);
			runtime.effects.put(code, new HashMap<>());
			
			AbstractNsfSound sound = ch.getSound();
			if (sound != null) {
				IMixerChannel mix = runtime.mixer.allocateChannel(code);
				sound.setOut(mix);
				
				// 音量
				mix.setLevel(getInitLevel(code));
			}
		}
	}
	
	/**
	 * 获得某个轨道的原始音量. 这个值要从 {@link FamiTrackerConfig} 中取
	 * @param channelCode
	 *   轨道号
	 * @return
	 *   范围 [0, 1]
	 * @since v0.2.4
	 */
	private float getInitLevel(byte channelCode) {
		float level = 0;
		switch (channelCode) {
		case CHANNEL_2A03_PULSE1: level = runtime.config.channelLevels.level2A03Pules1; break;
		case CHANNEL_2A03_PULSE2: level = runtime.config.channelLevels.level2A03Pules2; break;
		case CHANNEL_2A03_TRIANGLE: level = runtime.config.channelLevels.level2A03Triangle; break;
		case CHANNEL_2A03_NOISE: level = runtime.config.channelLevels.level2A03Noise; break;
		case CHANNEL_2A03_DPCM: level = runtime.config.channelLevels.level2A03DPCM; break;

		case CHANNEL_VRC6_PULSE1: level = runtime.config.channelLevels.levelVRC6Pules1; break;
		case CHANNEL_VRC6_PULSE2: level = runtime.config.channelLevels.levelVRC6Pules2; break;
		case CHANNEL_VRC6_SAWTOOTH: level = runtime.config.channelLevels.levelVRC6Sawtooth; break;

		case CHANNEL_MMC5_PULSE1: level = runtime.config.channelLevels.levelMMC5Pules1; break;
		case CHANNEL_MMC5_PULSE2: level = runtime.config.channelLevels.levelMMC5Pules2; break;
		
		case CHANNEL_FDS: level = runtime.config.channelLevels.levelFDS; break;
		
		case CHANNEL_N163_1: level = runtime.config.channelLevels.levelN163Namco1; break;
		case CHANNEL_N163_2: level = runtime.config.channelLevels.levelN163Namco2; break;
		case CHANNEL_N163_3: level = runtime.config.channelLevels.levelN163Namco3; break;
		case CHANNEL_N163_4: level = runtime.config.channelLevels.levelN163Namco4; break;
		case CHANNEL_N163_5: level = runtime.config.channelLevels.levelN163Namco5; break;
		case CHANNEL_N163_6: level = runtime.config.channelLevels.levelN163Namco6; break;
		case CHANNEL_N163_7: level = runtime.config.channelLevels.levelN163Namco7; break;
		case CHANNEL_N163_8: level = runtime.config.channelLevels.levelN163Namco8; break;
		
		case CHANNEL_VRC7_FM1: level = runtime.config.channelLevels.levelVRC7FM1; break;
		case CHANNEL_VRC7_FM2: level = runtime.config.channelLevels.levelVRC7FM2; break;
		case CHANNEL_VRC7_FM3: level = runtime.config.channelLevels.levelVRC7FM3; break;
		case CHANNEL_VRC7_FM4: level = runtime.config.channelLevels.levelVRC7FM4; break;
		case CHANNEL_VRC7_FM5: level = runtime.config.channelLevels.levelVRC7FM5; break;
		case CHANNEL_VRC7_FM6: level = runtime.config.channelLevels.levelVRC7FM6; break;
		
		case CHANNEL_S5B_SQUARE1: level = runtime.config.channelLevels.levelS5BSquare1; break;
		case CHANNEL_S5B_SQUARE2: level = runtime.config.channelLevels.levelS5BSquare2; break;
		case CHANNEL_S5B_SQUARE3: level = runtime.config.channelLevels.levelS5BSquare3; break;
		
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
	 * 让每个 channel 进行播放操作
	 * @param needTriggleSound
	 *   是否需要让发声器工作
	 */
	private void updateChannels(boolean needTriggleSound) {
		final FamiTrackerQuerier querier = runtime.querier;
		
		// 全局效果
		globalEffectsExecute();
		
		// 局部效果
		final int len = querier.channelCount();
		for (int i = 0; i < len; i++) {
			byte code = querier.channelCode(i);
			AbstractFtmChannel channel = runtime.channels.get(code);
			
			channel.playNote();
			channel.triggerSound(needTriggleSound);
		}
	}
	
	/**
	 * 重置 Mixer
	 */
	private void resetMixer() {
		runtime.mixer.reset();
	}
	
	/**
	 * 全局效果的实现
	 */
	private void globalEffectsExecute() {
		for (IFtmEffect eff : runtime.geffect.values()) {
			eff.execute((byte) 0, runtime);
		}
	}
	
	/**
	 * 从 Mixer 中读取音频数据
	 */
	private void readMixer() {
		runtime.mixer.finishBuffer();
		runtime.mixer.readBuffer(data, 0, data.length);
	}
	
	/* **********
	 * 测试方法 *
	 ********** */
	
	public int enableLog = 0;
	
	private void log() {
		switch (enableLog) {
		case 1:
			logEffect();
			break;
		case 2:
			logVolume();
			break;

		default:
			break;
		}
	}
	
	private void logEffect() {
		StringBuilder b = new StringBuilder(128);
		b.append(String.format("%02d:%03d", runtime.param.curSection, runtime.param.curRow));
		for (Iterator<Map.Entry<Byte, Map<FtmEffectType, IFtmEffect>>> it = runtime.effects.entrySet().iterator(); it.hasNext();) {
			Map.Entry<Byte, Map<FtmEffectType, IFtmEffect>> entry = it.next();
			if (entry.getValue().isEmpty()) {
				continue;
			}
			
			b.append(' ').append(Integer.toHexString(entry.getKey())).append('=');
			b.append(entry.getValue().values());
		}
		
		if (!runtime.geffect.isEmpty()) {
			b.append(' ').append("G").append('=').append(runtime.geffect.values());
		}
		System.out.println(b);
	}
	
	private void logVolume() {
		final StringBuilder b = new StringBuilder(64);
		b.append(String.format("%02d:%03d ", runtime.param.curSection, runtime.param.curRow));
		
		List<Byte> bs = new ArrayList<>(runtime.effects.keySet());
		bs.sort(null);
		bs.forEach((channelCode) -> {
			AbstractFtmChannel ch = runtime.channels.get(channelCode);
			int v = ch.getCurrentVolume();
			if (!ch.isPlaying()) {
				v = 0;
			}
			b.append(String.format("%3d|", v));
		});
		
		System.out.println(b);
	}
	
}
