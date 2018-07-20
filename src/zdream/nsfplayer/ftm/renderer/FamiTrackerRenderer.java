package zdream.nsfplayer.ftm.renderer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import zdream.nsfplayer.ftm.FamiTrackerSetting;
import zdream.nsfplayer.ftm.document.FamiTrackerException;
import zdream.nsfplayer.ftm.document.FamiTrackerQuerier;
import zdream.nsfplayer.ftm.document.FtmAudio;
import zdream.nsfplayer.ftm.renderer.channel.ChannalFactory;
import zdream.nsfplayer.ftm.renderer.effect.DefaultFtmEffectConverter;
import zdream.nsfplayer.ftm.renderer.effect.FtmEffectType;
import zdream.nsfplayer.ftm.renderer.effect.IFtmEffect;
import zdream.nsfplayer.ftm.renderer.effect.IFtmEffectConverter;

/**
 * <p>默认 FamiTracker 部分的音频渲染器.
 * <p>来源于原 C++ 工程的 SoundGen 类
 * </p>
 * @author Zdream
 * @since v0.2.1
 */
public class FamiTrackerRenderer {
	
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
	 * @param pattern
	 *   段号, 从 0 开始
	 */
	public void ready(
			FtmAudio audio,
			int track,
			int pattern)
			throws FamiTrackerException {
		fetcher.ready(audio, track, pattern);
		
		initChannels();
		
		// TODO 重置播放相关的数据
		
		// TODO SoundGen.loadMachineSettings()
	}
	
	// TODO 测试使用
	boolean b = false;
	
	/**
	 * 询问是否已经播放完毕
	 * @return
	 */
	public boolean isFinished() {
		// TODO
		
		boolean bb = b;
		b = !b;
		return bb;
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
		}
		
		return ret; // (现单位 byte)
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
	 * 获取正在播放的段号
	 * @return
	 *   {@link FtmRowFetcher#getCurrentSection()}
	 */
	public int getCurrentSection() {
		return fetcher.getCurrentSection();
	}

	/* **********
	 * 所含数据 *
	 ********** */
	
	/**
	 * 配置
	 */
	final FamiTrackerSetting setting = new FamiTrackerSetting();
	
	final FamiTrackerRuntime runtime = new FamiTrackerRuntime();
	
	final FtmRowFetcher fetcher = new FtmRowFetcher(runtime);
	
	final IFtmEffectConverter converter = new DefaultFtmEffectConverter(runtime);
	
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
		
//		fetcher.runFrame();
//		updateChannels();
		
		for (int i = 0; i < 1600; i++) {
			fetcher.runFrame();
			updateChannels();
			
			fetcher.updateState();
			
			// 测试方法
			log();
		}
		
		
		
		return ret;
	}
	
	/**
	 * 计算下一帧需要的采样数
	 * <br>并修改 {@link #sampleCount} 和 {@link #frameCount} 的数据
	 */
	private int countNextFrame() {
		int maxFrameCount = fetcher.getFrameRate();
		int maxSampleCount = setting.sampleRate;
		
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
			
			AbstractFtmChannel ch = ChannalFactory.create(code);
			ch.setRuntime(runtime);
			runtime.channels.put(code, ch);
			runtime.effects.put(code, new HashMap<>());
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
	
	/* **********
	 * 测试方法 *
	 ********** */
	private void log() {
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
	
}
