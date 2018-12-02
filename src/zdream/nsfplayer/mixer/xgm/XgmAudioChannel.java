package zdream.nsfplayer.mixer.xgm;

import java.util.Arrays;

import zdream.nsfplayer.sound.AbstractNsfSound;

/**
 * Xgm 的混音器采样整合轨道.
 * 
 * @version v0.2.10
 *   作了大幅度优化. 从以前存储所有时钟的音频数据改为存储所有采样的音频数据,
 *   存储大小大幅度降低以外, 效率也得到了提高.
 * 
 * @author Zdream
 * @since v0.2.1
 */
public final class XgmAudioChannel extends AbstractXgmAudioChannel {
	
	/**
	 * <p>仅存储从 {@link AbstractNsfSound} 通过方法 {@link #mix(int, int)} 输出的音频数据.
	 * 在 buffer 中存储每一个采样的音频值, 得知每个时钟的音频数据,
	 * 按照它占整个采样的比重加到对应采样的音频数据中.
	 * 因此需要在每帧开始知道, 该帧时钟和采样总数.
	 * 
	 * <p>最终处理情况是, 每个采样的数据由多个时钟对应的音频数据作平均值得出.
	 * 在以前的版本是使用 short 作为存储格式.
	 * 但考虑到非常小的数作平均数之后会损失很大的精度,
	 * 因此从版本 v0.2.10 开始使用 float 格式.
	 * 
	 * <p>原设定是 buffer 长度为每一帧的采样数 + 4.
	 * 因为每一帧的采样数会上下浮动, 所以要加 4 这个参数确保数组够用.
	 * </p>
	 */
	float[] buffer;
	
	/**
	 * 上一个存储的数值
	 */
	short lastValue;
	
	/**
	 * 上一个 mix 的 time 值.
	 * 后面要 mix 的位置是 [lastTime, time]
	 */
	int lastTime;
	
	/**
	 * 总时间, 单位: 时钟
	 */
	int maxTime;
	
	// 新的部分
	
	/**
	 * 比值: 采样数 / 时钟数
	 */
	float param;
	
	
	public XgmAudioChannel() {
		
	}

	@Override
	public void reset() {
		if (buffer != null) {
			Arrays.fill(buffer, (short) 0);
		}
		lastValue = 0;
		lastTime = 0;
	}

	@Override
	public void mix(int value, int time) {
		if (value == lastValue) {
			return;
		}
		
		if (buffer == null) {
			this.lastValue = (short) value;
			return;
		}
		
		if (time > this.maxTime) {
			time = maxTime;
		}
		
		mix0(value, time);
	}
	
	private void mix0(int value, int time) {
		// 写入到 buffer 中去
		
		float pstart = lastTime * param;
		float pend = time * param;
		int istart = (int) (pstart) + 1;
		int iend = (int) (pend);
		
		if (istart <= iend) {
			// pstart <= istart <= iend <= pend
			// 起始部分
			buffer[istart - 1] += this.lastValue * (istart - pstart);
			
			// 中间部分
			Arrays.fill(buffer, istart, iend, lastValue);
			
			// 结束部分
			buffer[iend] = this.lastValue * (pend - iend);
		} else {
			// iend <= pstart <= pend <= istart
			buffer[iend] += this.lastValue * (pend - pstart);
		}
		
		this.lastTime = time;
		this.lastValue = (short) value;
	}
	
	@Override
	protected void beforeSubmit() {
		this.mix0(lastValue, maxTime);
		this.lastTime = 0;
	}
	
	@Override
	protected float read(int index) {
		return buffer[index];
	}

	@Override
	protected void checkCapacity(int size, int frame) {
		if (this.buffer != null) {
			int length = this.buffer.length;
			if (length < frame || length > frame + 8) {
				this.buffer = new float[frame + 4];
			} else {
				Arrays.fill(buffer, 0, buffer.length, 0);
			}
		} else {
			this.buffer = new float[frame + 4];
		}
		this.maxTime = size;
		this.param = (float) frame / size;
	}
}
