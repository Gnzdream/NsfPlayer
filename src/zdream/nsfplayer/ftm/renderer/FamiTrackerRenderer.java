package zdream.nsfplayer.ftm.renderer;

import java.util.Arrays;

import zdream.nsfplayer.ftm.FamiTrackerSetting;
import zdream.nsfplayer.ftm.document.FamiTrackerException;
import zdream.nsfplayer.ftm.document.FtmAudio;

/**
 * <p>默认 FamiTracker 部分的音频渲染器.
 * <p>来源于原 C++ 工程的 SoundGen 类
 * </p>
 * @author Zdream
 * @since v0.2.1
 */
public class FamiTrackerRenderer {
	
	/* **********
	 * 播放参数 *
	 ********** */
	/**
	 * 播放音频数据
	 */
	FtmAudio audio;
	
	/**
	 * 正播放的曲目号
	 */
	int trackIdx;
	
	/**
	 * 正播放的段号 (pattern)
	 */
	int sectionIdx;
	
	/**
	 * 配置
	 */
	final FamiTrackerSetting setting = new FamiTrackerSetting();
	
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
		this.audio = audio;
		this.trackIdx = track;
		this.sectionIdx = pattern;
		
		// TODO 重置播放相关的数据
		
		// TODO SoundGen.loadMachineSettings()
	}
	
	/**
	 * 询问是否已经播放完毕
	 * @return
	 */
	public boolean isFinished() {
		// TODO
		return true;
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
		// 实际需要填充的采样数
		final int sampleLen = length / 2;
		
		int bRemain = sampleLen; // bs 的 remain (单位 byte)
		int bOffset = offset; // bs 的 offset (单位 采样)
		int ret = 0; // (现单位 采样)
		boolean full = false;
		
		// 前面渲染剩余的采样、还没有被返回的
		int dRemain = this.length - this.offset; // data 中剩下的 (单位 采样)
		if (dRemain != 0) {
			if (bRemain <= dRemain) {
				// TODO 将 data 的数据填充到 bs 中
				// bs 填满了
				
				this.offset += bRemain;
				ret += bRemain;
				full = true;
			} else {
				// TODO 将 data 的数据填充到 bs 中
				// data 用完了
				
				ret += dRemain;
				bOffset += dRemain * 2;
				bRemain -= dRemain;
			}
		}
		
		while (!full) {
			renderFrame();
			// data 数据已经就绪
			
			dRemain = this.length - this.offset; // data 中剩下的 (单位 采样)
			if (bRemain <= dRemain) {
				// TODO 将 data 的数据填充到 bs 中
				// bs 填满了
				
				this.offset += bRemain;
				ret += bRemain;
				full = true;
			} else {
				// TODO 将 data 的数据填充到 bs 中
				// data 用完了
				
				ret += dRemain;
				bOffset += dRemain * 2;
				bRemain -= dRemain;
			}
		}
		
		return ret * 2; // (现单位 byte)
	}
	
	/* **********
	 * 播放部分 *
	 ********** */
	
	/*
	 * 渲染参数 
	 */
	
	/**
	 * 已渲染的采样数
	 * <br>渲染完一秒的所有采样后, 就会清零.
	 * <br>所以, 该数据值域为 [0, setting.sampleRate]
	 */
	int sampleCount;
	
	/**
	 * 已渲染的帧数
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
	 * 渲染一帧
	 * <br>SoundGen.playFrame
	 * @return
	 *  本函数已渲染的采样数 (按单声道计算)
	 */
	int renderFrame() {
		int ret = countNextFrame();
		
		
		
		
		
		return ret;
	}
	
	/**
	 * 计算下一帧需要的采样数
	 * <br>并修改 {@link #sampleCount} 和 {@link #frameCount} 的数据
	 */
	private int countNextFrame() {
		int maxFrameCount = audio.getFramerate();
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
	
}
