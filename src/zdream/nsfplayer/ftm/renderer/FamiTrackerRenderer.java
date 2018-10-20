package zdream.nsfplayer.ftm.renderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.ftm.FamiTrackerSetting;
import zdream.nsfplayer.ftm.document.FamiTrackerException;
import zdream.nsfplayer.ftm.document.FamiTrackerQuerier;
import zdream.nsfplayer.ftm.document.FtmAudio;
import zdream.nsfplayer.ftm.renderer.effect.DefaultFtmEffectConverter;
import zdream.nsfplayer.ftm.renderer.effect.FtmEffectType;
import zdream.nsfplayer.ftm.renderer.effect.IFtmEffect;
import zdream.nsfplayer.ftm.renderer.effect.IFtmEffectConverter;
import zdream.nsfplayer.ftm.renderer.tools.ChannalDeviceSelector;
import zdream.nsfplayer.sound.AbstractNsfSound;
import zdream.nsfplayer.sound.mixer.IMixerChannel;

/**
 * <p>默认 FamiTracker 部分的音频渲染器.
 * <p>来源于原 C++ 工程的 SoundGen 类
 * </p>
 * @author Zdream
 * @since v0.2.1
 */
public class FamiTrackerRenderer implements INsfChannelCode {
	
	/**
	 * 利用默认配置产生一个音频渲染器
	 */
	public FamiTrackerRenderer() {
		this(new FamiTrackerSetting());
	}
	
	public FamiTrackerRenderer(FamiTrackerSetting setting) {
		this.runtime.setting = setting;
		this.runtime.init();
	}
	
	/* **********
	 * 公共接口 *
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
		
		runtime.param.calcFreq();
		initMixer();
		initChannels();
		
		// TODO 重置播放相关的数据
		
		// TODO SoundGen.loadMachineSettings()
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
	 * 渲染
	 * <br>线程不安全的方法
	 * @param bs
	 * @param offset
	 *   bs 存放数据的起始位置
	 * @param length
	 *   bs 存放的数据总量, 以 byte 为单位.
	 *   <br>这里是单声道、16 位深度, 该数据需要是 2 的倍数.
	 * @return
	 *   真正填充的数组元素个数
	 */
	public int render(byte[] bs, int offset, int length) {
		int bOffset = offset; // bs 的 offset
		int bLength = length / 2 * 2; // 化成 2 的倍数
		int ret = 0; // 已完成的采样数
		
		// 前面渲染剩余的采样、还没有被返回的
		int v = fillSample(bs, bOffset, bLength) * 2;
		ret += v;
		bOffset += v;
		bLength -= v;
		
		while (ret < length) {
			renderFrame();
			// data 数据已经就绪
			
			v = fillSample(bs, bOffset, bLength) * 2;
			ret += v;
			bOffset += v;
			bLength -= v;
			
			if (isFinished()) {
				break;
			}
		}
		
		return ret; // (现单位 byte)
	}
	
	/**
	 * <p>仅渲染一帧. 如果之前有没有渲染完的、上一帧采样数据,
	 * 只将上一帧剩余的采样数据写进数组.
	 * <br>线程不安全的方法
	 * </p>
	 * @param bs
	 * @param offset
	 *   bs 存放数据的起始位置
	 * @param length
	 *   bs 存放的数据总量, 以 byte 为单位.
	 *   <br>这里是单声道、16 位深度, 该数据需要是 2 的倍数.
	 * @return
	 *   真正填充的数组元素个数
	 * @since v0.2.2
	 */
	public int renderOneFrame(byte[] bs, int offset, int length) {
		int bLength = length / 2 * 2; // 化成 2 的倍数
		
		// 前面渲染剩余的采样、还没有被返回的
		int ret = fillSample(bs, offset, bLength) * 2;
		if (ret == 0) {
			renderFrame();
			// data 数据已经就绪
			ret = fillSample(bs, offset, bLength) * 2;
		}
		
		return ret; // (现单位 byte)
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
	 * 所含数据 *
	 ********** */
	
	final FamiTrackerRuntime runtime = new FamiTrackerRuntime();
	
	final FtmRowFetcher fetcher = new FtmRowFetcher(runtime);
	
	final IFtmEffectConverter converter = new DefaultFtmEffectConverter(runtime);

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
	 * 设置轨道是否发出声音
	 * @param channelCode
	 *   轨道号
	 * @param enable
	 *   true, 使该轨道发声; false, 则静音
	 * @since v0.2.2
	 */
	public void setChannelEnable(byte channelCode, boolean enable) {
		AbstractFtmChannel ch = runtime.channels.get(channelCode);
		if (ch != null) {
			ch.getSound().setEnable(enable);
		}
	}
	
	/* **********
	 * 播放部分 *
	 ********** */
	
	/*
	 * 渲染参数 
	 */
	
	/**
	 * 已渲染的采样数, 累加
	 * <br>渲染完一秒的所有采样后, 就会清零.
	 * <br>所以, 该数据值域为 [0, setting.sampleRate]
	 */
	int sampleCount;
	
	/**
	 * 已渲染的帧数, 计数
	 * <br>渲染完一秒的所有采样后, 就会清零.
	 * <br>每秒的帧率是 audio.framerate
	 * <br>该数据值域为 [0, audio.framerate]
	 * @see FtmAudio#getFramerate()
	 */
	int frameCount;
	
	/**
	 * 音频数据.
	 * <br>还没有返回的采样数据在这一块: [offset, length)
	 */
	short[] data;
	int offset = 0;
	int length = 0;
	
	/**
	 * 
	 * @param bs
	 * @param bOffset
	 * @param bLength
	 * @return
	 *   实际填充的采样数
	 */
	private int fillSample(byte[] bs, int bOffset, int bLength) {
		int bRemain = bLength / 2;
		int dRemain = this.length - this.offset; // data 中剩下的 (单位 采样)
		int ret = 0;
		
		if (dRemain != 0) {
			if (bRemain <= dRemain) {
				// 将 data 的数据填充到 bs 中
				fillSample(bs, bOffset, bLength, bRemain);
				// bs 填满了
				
				ret += bRemain;
			} else {
				// 将 data 的数据填充到 bs 中
				fillSample(bs, bOffset, bLength, dRemain);
				// data 用完了
				
				ret += dRemain;
			}
		}
		
		return ret;
	}
	
	private void fillSample(byte[] bs, int bOffset, int bLength, int dLength) {
		int bptr = bOffset;
		int dptr = this.offset;
		for (int i = 0; i < dLength; i++) {
			short sample = this.data[dptr++];
			bs[bptr++] = (byte) sample; // 低位
			bs[bptr++] = (byte) ((sample & 0xFF00) >> 8); // 高位
		}
		
		this.offset += dLength;
	}
	
	/**
	 * 渲染一帧
	 * <br>SoundGen.playFrame
	 * @return
	 *  本函数已渲染的采样数 (按单声道计算)
	 */
	private int renderFrame() {
		int ret = countNextFrame();
		
		fetcher.runFrame();
		updateChannels();
		
		fetcher.updateState();
		
		// 从 mixer 中读取数据
		readMixer();
		
		log();
		
		return ret;
	}
	
	/**
	 * 计算下一帧需要的采样数
	 * <br>并修改 {@link #sampleCount} 和 {@link #frameCount} 的数据
	 */
	private int countNextFrame() {
		int maxFrameCount = fetcher.getFrameRate();
		int maxSampleCount = runtime.setting.sampleRate;
		
		if (frameCount == maxFrameCount) {
			frameCount = 0;
			sampleCount = 0;
		}
		
		frameCount++;
		int oldSampleCount = sampleCount;
		sampleCount = maxSampleCount / maxFrameCount * frameCount;
		
		int ret = sampleCount - oldSampleCount;
		
		if (data == null || data.length < ret) {
			data = new short[ret];
		} else {
			Arrays.fill(data, (byte) 0);
		}
		length = ret;
		offset = 0;
		
		return ret;
	}
	
	/* **********
	 *  初始化  *
	 ********** */
	
	/**
	 * 初始化 / 重置音频合成器
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
		
		final FamiTrackerQuerier querier = runtime.querier;
		
		final int len = querier.channelCount();
		for (int i = 0; i < len; i++) {
			byte code = querier.channelCode(i);
			
			AbstractFtmChannel ch = ChannalDeviceSelector.selectFtmChannel(code);
			ch.setRuntime(runtime);
			runtime.channels.put(code, ch);
			runtime.effects.put(code, new HashMap<>());
			
			AbstractNsfSound sound = ch.getSound();
			if (sound != null) {
				// TODO
				IMixerChannel mix = runtime.mixer.allocateChannel(code);
				sound.setOut(mix);
			}
			
			// TODO 后面的配置
		}
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
	 * 从 Mixer 中读取音频数据
	 */
	private void readMixer() {
		// TODO
		
		runtime.mixer.finishBuffer();
		runtime.mixer.readBuffer(data, 0, data.length);
		
		// 现在音频数据在 data 中
		
//		int SamplesAvail = m_pMixer.finishBuffer(m_iFrameCycles);
//		int ReadSamples	= m_pMixer.readBuffer(SamplesAvail, m_pSoundBuffer, m_bStereoEnabled);
//		m_pParent.flushBuffer(m_pSoundBuffer, 0, ReadSamples);
//		
//		m_iFrameClock= m_iFrameCycleCount;
//		m_iFrameCycles = 0;
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
