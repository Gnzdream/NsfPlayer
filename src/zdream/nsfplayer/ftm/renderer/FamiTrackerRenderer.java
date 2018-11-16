package zdream.nsfplayer.ftm.renderer;

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
import zdream.nsfplayer.ftm.renderer.effect.DefaultFtmEffectConverter;
import zdream.nsfplayer.ftm.renderer.effect.FtmEffectType;
import zdream.nsfplayer.ftm.renderer.effect.IFtmEffect;
import zdream.nsfplayer.ftm.renderer.effect.IFtmEffectConverter;
import zdream.nsfplayer.sound.AbstractNsfSound;
import zdream.nsfplayer.sound.mixer.IMixerChannel;

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
	
	final FtmRowFetcher fetcher = new FtmRowFetcher(runtime);
	
	final IFtmEffectConverter converter = new DefaultFtmEffectConverter(runtime);
	
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
		
		this.runtime.init();
		
		runtime.param.sampleRate = this.runtime.config.sampleRate;
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
		fetcher.ready(audio, track, section);
		
		// 重置播放相关的数据
		int frameRate = fetcher.getFrameRate();
		resetCounterParam(frameRate, runtime.config.sampleRate);
		runtime.param.calcFreq(frameRate);
		
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
		ready(fetcher.trackIdx, fetcher.curSection);
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
	 * @param track
	 *   曲目号, 从 0 开始
	 * @param section
	 *   段号, 从 0 开始
	 * @throws NullPointerException
	 *   当调用该方法前未指定 {@link FtmAudio} 音频时
	 */
	public void ready(int track, int section) throws FamiTrackerException {
		if (fetcher.querier == null) {
			throw new NullPointerException("FtmAudio = null");
		}
		
		fetcher.ready(track, section);
		runtime.resetAllChannels();
	}
	
	/* **********
	 * 渲染部分 *
	 ********** */
	
	/**
	 * 渲染一帧
	 * <br>SoundGen.playFrame
	 * @return
	 *  本函数已渲染的采样数 (按单声道计算)
	 */
	protected int renderFrame() {
		int ret = countNextFrame();
		runtime.param.sampleInCurFrame = ret;
		mixerReady();
		
		fetcher.runFrame();
		updateChannels();
		
		fetcher.updateState();
		
		// 从 mixer 中读取数据
		readMixer();
		
		log();
		
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
		return fetcher.isFinished();
	}
	
	/**
	 * 获取正在播放的曲目号
	 * @return
	 *   {@link FtmRowFetcher#trackIdx}
	 */
	public int getCurrentTrack() {
		return fetcher.trackIdx;
	}

	/**
	 * 获取正在播放的段号
	 * @return
	 *   {@link FtmRowFetcher#getCurrentSection()}
	 */
	public int getCurrentSection() {
		return fetcher.getCurrentSection();
	}
	
	/**
	 * 获取正在播放的行号
	 * @return
	 *   {@link FtmRowFetcher#getCurrentRow()}
	 */
	public int getCurrentRow() {
		return fetcher.getCurrentRow();
	}
	
	/**
	 * 询问当前行是否播放完毕, 需要跳到下一行 (不是询问当前帧是否播放完)
	 * @return
	 *   true, 如果当前行已经播放完毕
	 * @since v0.2.2
	 */
	public boolean currentRowRunOut() {
		return fetcher.needRowUpdate();
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

	/* **********
	 * 仪表盘区 *
	 ********** */
	/*
	 * 用于控制实际播放数据的部分.
	 * 其中有: 控制音量、控制是否播放
	 */
	
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
	
	/* **********
	 *  初始化  *
	 ********** */
	
	/**
	 * 初始化 / 重置音频合成器 (混音器)
	 */
	private void initMixer() {
		runtime.mixer.detachAll();
		runtime.mixer.reset();
		
		// TODO
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
	 */
	private void updateChannels() {
		final FamiTrackerQuerier querier = runtime.querier;
		
		// 全局效果
		globalEffectsExecute();
		
		// 局部效果
		final int len = querier.channelCount();
		for (int i = 0; i < len; i++) {
			byte code = querier.channelCode(i);
			AbstractFtmChannel channel = runtime.channels.get(code);
			
			channel.playNote();
		}
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
	 * <p>通知混音器, 当前帧的渲染开始了.
	 * <p>这个方法原本用于通知混音器, 如果本帧的渲染速度需要变化,
	 * 可以通过该方法, 让混音器提前对此做好准备, 修改存储的采样数容量, 从而调节播放速度.
	 * </p>
	 * @since v0.2.7
	 */
	private void mixerReady() {
		
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
		b.append(String.format("%02d:%03d", fetcher.curSection, fetcher.curRow));
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
		b.append(String.format("%02d:%03d ", fetcher.curSection, fetcher.curRow));
		
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
