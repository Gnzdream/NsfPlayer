package zdream.nsfplayer.core;

import java.util.Arrays;

import zdream.nsfplayer.ftm.audio.FtmAudio;

/**
 * 抽象的 NSF 音源的渲染器, 用于输出以 byte 数组组织的 PCM 音频数据
 * 
 * @author Zdream
 * @since v0.2.4
 */
public abstract class AbstractNsfRenderer<T extends AbstractNsfAudio>
		implements INsfChannelCode {
	
	/* **********
	 * 准备部分 *
	 ********** */
	
	/**
	 * <p>让该渲染器读取对应的 audio 数据.
	 * <p>设置播放暂停位置为默认曲目的开头.
	 * </p>
	 * @param audio
	 *   音频数据
	 */
	public abstract void ready(T audio);
	
	/**
	 * <p>让该渲染器读取对应的 audio 数据.
	 * <p>设置播放暂停位置为指定曲目的第一段 (段 0)
	 * </p>
	 * @param audio
	 * @param track
	 *   曲目号, 从 0 开始
	 */
	public abstract void ready(T audio, int track);
	
	/**
	 * 询问是否整个乐曲已经渲染完成
	 * @return
	 */
	public abstract boolean isFinished();
	
	/* **********
	 * 渲染部分 *
	 ********** */
	
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
	
	/*
	 * 渲染参数 
	 */
	
	/**
	 * 已渲染的采样数, 累加
	 * <br>渲染完一秒的所有采样后, 就会清零.
	 * <br>所以, 该数据值域为 [0, setting.sampleRate]
	 */
	protected int sampleCount;
	
	/**
	 * 已渲染的帧数, 计数
	 * <br>渲染完一秒的所有采样后, 就会清零.
	 * <br>每秒的帧率是 audio.framerate
	 * <br>该数据值域为 [0, audio.framerate]
	 * @see FtmAudio#getFramerate()
	 */
	protected int frameCount;
	
	/**
	 * 音频数据.
	 * <br>还没有返回的采样数据在这一块: [offset, length)
	 */
	protected short[] data;
	protected int offset = 0;
	protected int length = 0;
	
	/**
	 * 
	 * @param bs
	 * @param bOffset
	 * @param bLength
	 * @return
	 *   实际填充的采样数
	 */
	protected int fillSample(byte[] bs, int bOffset, int bLength) {
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
	
	protected void fillSample(byte[] bs, int bOffset, int bLength, int dLength) {
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
	 * 计算下一帧需要的采样数 (每个声道)
	 * <br>并修改 {@link #sampleCount} 和 {@link #frameCount} 的数据
	 * @param maxFrameCount
	 *   帧率, 一般为 60
	 * @param maxSampleCount
	 *   采样率, 一般为 48000
	 * @return
	 */
	protected int countNextFrame(int maxFrameCount, int maxSampleCount) {
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
	
	/**
	 * 渲染一帧
	 * @return
	 *  本函数已渲染的采样数 (按单声道计算)
	 */
	protected abstract int renderFrame();

}
