package zdream.nsfplayer.mixer.blip;

import static zdream.nsfplayer.mixer.blip.BufferContext.*;

/**
 * <p>Blip 轨道输入工具, 用于往 {@link BlipBuffer} 填充音频.
 * </p>
 * @author Zdream
 */
public class BlipSynth {
	
	/**
	 * <p>指定预期的最大变化振幅值,
	 * 通过找出最大和最小期望振幅数值之差（max - min）来计算后面的音频数值
	 * <p>Range specifies the greatest expected change in amplitude.
	 * Calculate it by finding the difference between the maximum and minimum expected amplitudes (max - min).
	 * </p>
	 * @param quality
	 *   输入的音频的质量, 即位率, 每个采样由多少个位组成. 默认 16
	 * @param range
	 *   指定振幅的最大预期变化
	 */
	public BlipSynth(int quality, int range) {
		this.quality = quality;
		this.range = range;
		impulses = new short [blip_res * (quality / 2) + 1];
		impl = new BlipSynth_(impulses, quality);
	}

	/**
	 * <p>设置波形总音量
	 * <p>Set overall volume of waveform
	 * @param v
	 */
	public void volume(double v) {
		impl.volume_unit(v * (1.0 / (range < 0 ? -range : range)));
	}
	
	/**
	 * <p>设置低通过滤器
	 * <p>Configure low-pass filter
	 */
	public void trebleEq(final BlipEQ eq) {
		impl.trebleEq(eq);
	}
	
	/**
	 * <p>获取 BlipBuffer
	 * <p>Get BlipBuffer used for output
	 * @return
	 */
	public final BlipBuffer output() {
		return impl.buf;
	}

	/**
	 * <p>设置 BlipBuffer
	 * <p>Set BlipBuffer used for output
	 * @param b
	 */
	public void output(BlipBuffer b) {
		impl.buf = b;
		impl.last_amp = 0;
	}
	
	/**
	 * <p>在指定的时间范围内更新波形幅度.<br>
	 * 使用这种需要每个波形有一个单独的 BlipSynth.
	 * <p>Update amplitude of waveform at given time.<br>
	 * Using this requires a separate BlipSynth for each waveform.
	 * @param time
	 * @param amplitude
	 */
	public void update( int time, int amplitude ) {
		int delta = amplitude - impl.last_amp;
		impl.last_amp = amplitude;
		offset_resampled( time * ((factor_ != 0) ? factor_ : impl.buf.factor_) + impl.buf.offset_,
				delta, impl.buf );
	}

// Low-level interface

	/**
	 * <p>一个时间点的采样值加一个增量.
	 * <p>Add an amplitude transition of specified delta,
	 * optionally into specified buffer rather than the one set with output().<br>
	 * Delta can be positive or negative.
	 * The actual change in amplitude is delta * (volume / range)
	 * @param time
	 *   时刻, 采用入采样率的时间单位, 对 NSF 轨道而言为每秒时钟数.
	 * @param delta
	 *   音频数据, 可正可负
	 * @param buf
	 */
	public final void offset( int time, int delta, BlipBuffer buf ) {
		offset_resampled( time * ((factor_ != 0) ? factor_ : buf.factor_) + buf.offset_, delta, buf );
	}
	
	public final void offset(int t, int delta) {
		offset(t, delta, impl.buf);
	}
	
	/**
	 * <p>直接使用分数输出样本。
	 * <p>Works directly in terms of fractional output samples.
	 * @param time
	 *   时钟
	 * @param delta
	 * @param buf
	 */
	public final void offset_resampled( int time, int delta, BlipBuffer buf ) {
		// Fails if time is beyond end of Blip_Buffer, due to a bug in caller code or the
		// need for a longer buffer as set by set_sample_rate().
		assert( (long) (time >> 16) < buf.buffer_size_ );
		
		delta *= impl.delta_factor;
		int phase = (int) (time >> (16 - 6) & (blip_res - 1));
		int impptr = (short) (blip_res - phase); // 指向 impulses
		int bufptr = time >> 16; // 指向 buf.buffer_
		long i0 = impulses[impptr];
		
		final int fwd = (blip_widest_impulse_ - quality) / 2;
		final int rev = fwd + quality - 2;
		
		{
			if (bufptr + fwd >= buf.buffer_.length) {
				System.out.println();
			}
			long t0 = i0 * delta + buf.buffer_ [bufptr + fwd];
			long t1 = impulses [impptr + blip_res] * delta + buf.buffer_ [bufptr + fwd + 1];
			i0 = impulses [impptr + blip_res * 2];
			buf.buffer_ [bufptr + fwd] = t0;
			buf.buffer_ [bufptr + fwd + 1] = t1;
		}
		
		if (quality > 8) {
			long t0 = i0 * delta + buf.buffer_ [bufptr + fwd + 2];
			long t1 = impulses [impptr + blip_res * (3)] * delta + buf.buffer_ [bufptr + fwd + 3];
			i0 = impulses [impptr + blip_res * (4)];
			buf.buffer_ [bufptr + fwd + 2] = t0;
			buf.buffer_ [bufptr + fwd + 3] = t1;
		}
		if ( quality > 12 ) {
			long t0 = i0 * delta + buf.buffer_ [bufptr + fwd + 4];
			long t1 = impulses [impptr + blip_res * (4 + 1)] * delta + buf.buffer_ [bufptr + fwd + 1 + 4];
			i0 = impulses [impptr + blip_res * (4 + 2)];
			buf.buffer_ [bufptr + fwd + 4] = t0;
			buf.buffer_ [bufptr + fwd + 5] = t1;
		}
		{
			final int mid = quality / 2 - 1;
			long t0 = i0 * delta + buf.buffer_ [bufptr + fwd + mid - 1];
			long t1 = impulses [impptr + blip_res * mid] * delta + buf.buffer_ [bufptr + fwd + mid];
			impptr = phase;
			i0 = impulses [impptr + blip_res * mid];
			buf.buffer_ [bufptr + fwd + mid - 1] = t0;
			buf.buffer_ [bufptr + fwd + mid] = t1;
		}
		if ( quality > 12 ) { // r = 6
			long t0 = i0 * delta + buf.buffer_ [bufptr + rev - 6];
			long t1 = impulses [impptr + blip_res * 6] * delta + buf.buffer_ [bufptr + rev - 5];
			i0 = impulses [impptr + blip_res * 5];
			buf.buffer_ [bufptr + rev - 6] = t0;
			buf.buffer_ [bufptr + rev - 5] = t1;
		}
		if ( quality > 8  ) { // r = 4
			long t0 = i0 * delta + buf.buffer_ [bufptr + rev - 4];
			long t1 = impulses [impptr + blip_res * 4] * delta + buf.buffer_ [bufptr + rev - 3];
			i0 = impulses [impptr + blip_res * 3];
			buf.buffer_ [bufptr + rev - 4] = t0;
			buf.buffer_ [bufptr + rev - 3] = t1;
		}
		{ // r = 2
			long t0 = i0 * delta + buf.buffer_ [bufptr + rev - 2];
			long t1 = impulses [impptr + blip_res * 2] * delta + buf.buffer_ [bufptr + rev - 1];
			i0 = impulses [impptr + blip_res];
			buf.buffer_ [bufptr + rev - 2] = t0;
			buf.buffer_ [bufptr + rev - 1] = t1;
		}
		
		long t0 = i0 * delta + buf.buffer_ [bufptr + rev];
		long t1 = impulses [impptr] * delta + buf.buffer_ [bufptr + rev + 1];
		buf.buffer_ [bufptr + rev] = t0;
		buf.buffer_ [bufptr + rev + 1] = t1;
	}
	
	public final int quality, range;
		
	private short[] impulses;
	private BlipSynth_ impl;
	
	/**
	 * 默认为 0, 表示不启用;
	 * 如果它不为 0, 则使用该值而不是 BlipBuffer 提供的全局 factor_
	 */
	private int factor_;
	
	/**
	 * 如果有自定义的入采样率, 在这里设置.
	 * @param rate
	 *   输入的采样率.
	 *   如果是 0 则使用全局的入采样率
	 * @since v0.3.0
	 */
	public void in_sample_rate(int rate) {
		if (rate <= 0) {
			factor_ = 0;
		} else {
			this.factor_ = impl.buf.clockRateFactor(rate);
		}
	}
	
	/**
	 * 如果有自定义的入采样率, 在这里设置.
	 * @param rate
	 *   输入的采样率
	 *   如果是 0 则使用全局的入采样率
	 * @param buf
	 *   指定的音频缓冲
	 * @since v0.3.0
	 */
	public void in_sample_rate(int rate, BlipBuffer buf) {
		if (rate <= 0) {
			factor_ = 0;
		} else {
			this.factor_ = buf.clockRateFactor(rate);
		}
	}

}
