package zdream.nsfplayer.sound.blip;

import static zdream.nsfplayer.sound.blip.BufferContext.*;

/**
 * <p>对自定义格式的方式读取采样数据进行优化.
 * <p>Optimized inline sample reader for custom sample formats and mixing of Blip_Buffer samples
 * @author Zdream
 */
public class BlipReader {

	/**
	 * <p>开始从 buffer 中读取采样数据.
	 * <p>Begin reading samples from buffer.<br>
	 * Returns value to pass to next() (can be ignored if default bass_freq is acceptable).
	 * @return
	 */
	public int begin( BlipBuffer _buf ) {
		buf = _buf;
		accum = _buf.reader_accum;
		return _buf.bass_shift;
	}
		
	/**
	 * <p>当前采样值
	 * <p>Current sample
	 * @return
	 */
	public final long read() {
		return accum >> (blip_sample_bits - 16);
	}
	
	/**
	 * <p>没有进行过修改的原始采样值. 这个值是计算后直接得出, 并没有进行高通等优化.
	 * <p>Current raw sample in full internal resolution
	 * @return
	 */
	public final long read_raw() {
		return accum;
	}
	
	/**
	 * <p>下一个样本值
	 * <p>Advance to next sample
	 * @param bass_shift
	 *   默认为 9
	 */
	public void next( int bass_shift )         { accum += buf.buffer_[ptr++] - (accum >> bass_shift); }
	
	/**
	 * <p>结束从 buffer 中读取采样数据.<br>
	 * 读取的采样数据之后, 你需要调用 {@link BlipBuffer#removeSamples(int)}, 将读取采样的个数作为参数传入,
	 * 来将这些数据从缓冲区删除掉.
	 * <p>End reading samples from buffer.<br>
	 * The number of samples read must now be removed using {@link BlipBuffer#removeSamples(int)}.
	 */
	public void end() {
		buf.reader_accum = accum;
	}
	
	private BlipBuffer buf;
	/**
	 * 这个是指向 buf.buffer_ 的索引指针
	 */
	private int ptr;
	private long accum;

}
