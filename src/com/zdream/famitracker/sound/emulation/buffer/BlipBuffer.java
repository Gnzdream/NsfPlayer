package com.zdream.famitracker.sound.emulation.buffer;

import static com.zdream.famitracker.sound.emulation.buffer.BufferContext.*;

import java.util.Arrays;

/**
 * <p>源文件 Blip_Buffer 0.4.0
 * <p>Band-limited sound synthesis and buffering
 * @author Zdream
 */
public class BlipBuffer {
	
	/**
	 * <p>将输出采样速率和缓冲区长度 (需要换算成毫秒数, 默认为 1/4 秒, 即 250 毫秒), 然后清除缓冲区.<br>
	 * 如果成功返回 null, 如果内存不足, 抛出错误, 但不影响当前缓冲设置。
	 * <p>Set output sample rate and buffer length in milliseconds (1/1000 sec, defaults
	 * to 1/4 second), then clear buffer.<br>
	 * If there isn't enough memory, throw error without affecting current buffer setup.
	 * @param new_rate
	 *   输出的采样速率, 每秒多少个采样点
	 * @param msec
	 *   缓冲区长度, 以毫秒为单位, 调用的默认值是 1000 / 4 = 250
	 * @throws RuntimeException
	 *   内存不足时
	 */
	public void setSampleRate(int new_rate, int msec) throws RuntimeException {
		// 原本是 (0xFFFFFFFFUL >> 16) - buffer_extra - 64;
		int new_size = 65535 - buffer_extra - 64;
		if ( msec != blip_max_length ) {
			int s = (new_rate * (msec + 1) + 999) / 1000;
			if ( s < new_size )
				new_size = s;
			else
				throw new RuntimeException("要求的缓冲区长度已经超过最大限度");
		}
		
		if ( buffer_size_ != new_size ) {
			buffer_ = new long[new_size + buffer_extra];
		}
		
		buffer_size_ = new_size;
		
		// update things based on the sample rate
		sample_rate_ = new_rate;
		length_ = new_size * 1000 / new_rate - 1;
		
		if (msec != 0)
			assert( length_ == msec ); // ensure length is same as that passed in
		if (clock_rate_ != 0)
			clockRate( clock_rate_ );
		bassFreq( bass_freq_ );
		
		clear(true);
		
		// success and return
	}
	
	/**
	 * <p>设置每秒的时钟数
	 * <p>Set number of source time units per second
	 * @param rate
	 */
	public void clockRate(int cps) {
		factor_ = clockRateFactor(clock_rate_ = cps);
	}
	
	/**
	 * <p>根据特定的持续时间, 结束当前时间帧,
	 * 使采样数据能够在调用 {@link #read_samples()} 方法时能够获取到（连同任何还未读的采样）.
	 * 在当前帧的结尾处开始一个新的帧。
	 * <p>End current time frame of specified duration and make its samples available
	 * (along with any still-unread samples) for reading with read_samples(). Begins
	 * a new time frame at the end of the current frame.
	 * @param time
	 *   所指定的持续时间, 单位 TODO
	 */
	public void endFrame(int time) {
		offset_ += time * factor_;
		assert(samplesAvail() <= buffer_size_); // time outside buffer length
	}
	
	/**
	 * <p>从缓冲区中读取 {@code max_samples} 个采样点的数据到 {@code dest} 中, 这些数据在读完后将从缓冲区中移除.<br>
	 * 返回真正读了多少采样点的数据 (不是数组长度).<br>
	 * 如果设置为立体声 ({@code stereo = true}), 那么会写完一个采样点的数据之后,
	 * 在 {@code dest} 上跳过一个采样点的数据的位置, 将下一个数据写到第三个采样点数据的位置.<br>
	 * 这样能够更容易地将两个声道的数据写在一个数组中.
	 * <p>Read at most {@code max_samples} out of buffer into {@code dest}, removing them from
	 * the buffer. Returns number of samples actually read and removed. If stereo is
	 * true, increments 'dest' one extra time after writing each sample, to allow
	 * easy interleving of two channels into a stereo output buffer.
	 * <p>另外注意, 因为要将 16 位的采样数据写到每个单元 8 位的 byte 数组中, 所以一个采样数据占两个 byte 位.
	 * @param dest
	 *   已经转成 byte 数组
	 * @param offset
	 *   默认为 0, 表示 dest 这个数组从哪里开始写入数据.<br>
	 *   单声道 (stereo = false) 时, 一般为 0<br>
	 *   立体声 (stereo = true) 时, 一号声道一般为 0, 二号声道为 2
	 * @param max_samples
	 *   这是 16 位的采样, 那应该小于等于 dest.length / 2
	 * @param stereo
	 *   默认 false. 如果为 true, 说明为立体声
	 * @return
	 */
	public int readSamples(byte[] dest, int offset, int max_samples, boolean stereo) {
		int count = samplesAvail();
		if (count > max_samples)
			count = max_samples;
		
		if (count != 0) {
			final int sample_shift = blip_sample_bits - 16;
			long accum = reader_accum;
			int ptr = 0;
			int outptr = offset;
			
			if (!stereo) {
				for ( int n = count; (--n) >= 0;) {
					long s = (accum >> sample_shift);
					accum -= accum >> bass_shift;
					accum += buffer_[ptr++];
					
					short out = (short) s;

					// clamp sample
					if ( out != s )
						out = (short) (0x7FFF - (s >> 24));
					
					dest[outptr++] = (byte) out; // 低位
					dest[outptr++] = (byte) ((out & 0xFF00) >> 8); // 高位
				}
			} else {
				for ( int n = count; (--n) >= 0;) {
					long s = (accum >> sample_shift);
					accum -= accum >> bass_shift;
					accum += buffer_[ptr++];
					
					short out = (short) s;

					// clamp sample
					if ( out != s )
						out = (short) (0x7FFF - (s >> 24));
					
					dest[outptr++] = (byte) out; // 低位
					dest[outptr++] = (byte) ((out & 0xFF00) >> 8); // 高位
					
					outptr += 2;
				}
			}
			
			reader_accum = accum;
			removeSamples( count );
		}
		return count;
	}
	
// Additional optional features

	/**
	 * <p>采样率
	 * <p>Current output sample rate, const function
	 * @return
	 */
	public final int sampleRate() {
		return sample_rate_;
	}
	
	public void sampleRate(int r) {
		setSampleRate(r, 250);
	}

	public void sampleRate(int r, int msec) {
		setSampleRate(r, msec);
	}
	
	/**
	 * <p>Buffer 的长度, 以毫秒为单位
	 * <p>Length of buffer, in milliseconds
	 * @return
	 */
	public final int length() {
		return length_;
	}
	
	/**
	 * <p>获取每秒的时钟数
	 * <p>Number of source time units per second
	 * @return
	 */
	public final int clockRate() {
		return clock_rate_;
	}
	
	/**
	 * <p>设置频率高通滤波器频率，其中较高的值减少低音更多.
	 * <p>Set frequency high-pass filter frequency, where higher values reduce bass more
	 * @param freq
	 */
	public void bassFreq(int freq) {
		bass_freq_ = freq;
		int shift = 31;
		if ( freq > 0 ) {
			shift = 13;
			long f = (freq << 16) / sample_rate_;
			while ( (f >>= 1) > 0 && (--shift > 0) ) { }
		}
		bass_shift = shift;
	}
	
	/**
	 * <p>从合成到读出的样本延迟数
	 * <p>Number of samples delay from synthesis to samples read out
	 * @return
	 */
	public int output_latency() {
		return blip_widest_impulse_ / 2;
	}
	
	/**
	 * <p>清除所有采样数据, 清空缓冲区.<br>
	 * 如果 {@code entire = false}, 仅清除在等待中的采样数据, 而不是整个缓冲区. 
	 * <p>Remove all available samples and clear buffer to silence.<br>
	 * If {@code entire} is false, just clears out any samples waiting rather than the entire buffer.
	 * @param entire
	 *   entire_buffer, 是否选择清除整个缓冲区的数据. 默认 true
	 */
	public void clear(boolean entire) {
		offset_ = 0;
		reader_accum = 0;
		if ( buffer_ != null ) {
			int count = (entire ? buffer_size_ : samplesAvail());
			Arrays.fill(buffer_, 0, count + buffer_extra, 0);
		}
	}
	
	/**
	 * <p>返回有多少采样数据能够被 {@link #readSamples(byte[], int, boolean, int)} 读取.
	 * <p>Number of samples available for reading with read_samples()
	 * @return
	 */
	public final int samplesAvail() {
		return offset_ >> 16;
	}
	
	/**
	 * <p>Remove 'count' samples from those waiting to be read
	 * @param count
	 */
	public void removeSamples(int count) {
		removeSilence(count);
		
		// copy remaining samples to beginning and clear old samples
		int remain = samplesAvail() + buffer_extra;
		long[] buffer2 = new long[buffer_.length];
		System.arraycopy(buffer_, count, buffer2, 0, remain);
		
		buffer_ = buffer2;
	}
	
// Experimental features
	
	/**
	 * <p>Number of raw samples that can be mixed within frame of specified duration.
	 * @param duration
	 * @return
	 */
	public final int countSamples( int duration ) {
		int last_sample  = resampledTime( duration ) >> 16;
		int first_sample = offset_ >> 16;
		return last_sample - first_sample;
	}
	
	/**
	 * <p>Mix 'count' samples from 'buf' into buffer.
	 */
	public void mixSamples(short[] buf, int offset, int count) {
		int outptr = (offset_ >> 16) + blip_widest_impulse_ / 2;
		int inptr = offset;
		
		final int sample_shift = blip_sample_bits - 16;
		long prev = 0;
		while ( count-- > 0 ) {
			long s = buf[inptr++] << sample_shift;
			buffer_[outptr] += s - prev;
			prev = s;
			++outptr;
		}
		buffer_[outptr] -= prev;
	}
	
	/**
	 * <p>Count number of clocks needed until 'count' samples will be available.
	 * If buffer can't even hold 'count' samples, returns number of clocks until
	 * buffer becomes full.
	 * @param count
	 * @return
	 */
	public final int countClocks( int count ) {
		if ( count > buffer_size_ )
			count = buffer_size_;
		int time = count << 16;
		return ((time - offset_ + factor_ - 1) / factor_);
	}
	
	// not documented yet
	public void removeSilence( int count ) {
		assert( count <= samplesAvail() ); // tried to remove more samples than available
		offset_ -= count << 16;
	}
	
	public final int resampledDuration(int t) {
		return t * factor_;
	}
	
	public final int resampledTime(int t) {
		return t * factor_ + offset_;
	}

	private int clockRateFactor( int clock_rate ) {
		double ratio = (double) sample_rate_ / clock_rate;
		int factor = (int) Math.floor( ratio * (1L << 16) + 0.5 );
		assert( factor > 0 || sample_rate_ == 0 ); // fails if clock/output ratio is too large
		return factor;
	}
	
	public BlipBuffer() {
		factor_ = Integer.MAX_VALUE;
		bass_freq_ = 16;
	}
	
	/**
	 * 据我猜测, 毫秒数 * factor_ = 采样数
	 */
	public int factor_;
	public int offset_;
	
	/**
	 * 这个是采样数据存储的位置, 为计算出来的采样数据
	 */
	long[] buffer_;
	int buffer_size_;
	
	/**
	 * 据我猜测, 每个计算出来的采样数据值, 和实际播放的采样数据值不同,
	 * 下一个采样点播放的数据实际和上一个采样点计算值和下一个采样点计算值共同决定.
	 * 因此需要存储上一个采样点计算值
	 */
	long reader_accum; // 确定为 long
	int bass_shift;
	int sample_rate_;
	int clock_rate_;
	private int bass_freq_;
	int length_;
}
