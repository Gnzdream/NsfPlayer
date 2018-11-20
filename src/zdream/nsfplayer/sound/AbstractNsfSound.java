package zdream.nsfplayer.sound;

import zdream.nsfplayer.core.IEnable;
import zdream.nsfplayer.core.IResetable;
import zdream.nsfplayer.sound.mixer.IMixerChannel;

/**
 * 音频发声器的超类
 * @author Zdream
 * @since 0.2.1
 */
public abstract class AbstractNsfSound implements IResetable, IEnable {

	/**
	 * 通向合成器 Buffer 的管道
	 */
	protected IMixerChannel out;
	
	/**
	 * 记录已经渲染的时钟数.
	 * 每帧结束时, 调用 {@link #endFrame()} 重置为 0
	 */
	protected int time;
	
	/**
	 * 是否启用的标志位, 4015 控制位
	 */
	private boolean enable = true;
	
	/**
	 * 控制是否将音频数据发送到合成器管道的标志.
	 * 如果设置成 true, 将不会把数据送往合成器管道 {@link #out}.
	 * 不会被 {@link #reset()} 重置.
	 */
	private boolean mask = false;
	
	/**
	 * 每帧结束时调用
	 */
	public void endFrame() {
		time = 0;
	}
	
	/**
	 * @param out
	 *   {@link #out}
	 */
	public void setOut(IMixerChannel out) {
		this.out = out;
	}
	
	/**
	 * @return
	 *   {@link #enable}
	 */
	public boolean isEnable() {
		return enable;
	}
	
	/**
	 * @param enable
	 *   {@link #enable}
	 */
	public void setEnable(boolean enable) {
		this.enable = enable;
	}
	
	/**
	 * @return
	 *   {@link #mask}
	 */
	public boolean isMask() {
		return mask;
	}

	/**
	 * @param mask
	 *   {@link #mask}
	 */
	public void setMask(boolean mask) {
		this.mask = mask;
	}

	@Override
	public void reset() {
		enable = true;
		this.endFrame();
	}
	
	/**
	 * <p>开始运作 time 的时钟周期数.
	 * <p>如果该发声器启用 ({@link #enable} == true) 则会向 {@link #out} 传递音频相关数据
	 * 否则将直接跳过这段时间.
	 * </p>
	 * @param time
	 */
	public final void process(int time) {
		if (time < 1) {
			return;
		}
		
		int destTime = this.time + time;
		if (enable) {
			onProcess(time);
		}
		this.time = destTime;
	}
	
	/**
	 * 发声器工作, 并向 {@link #out} 传递音频相关数据
	 * @param time
	 */
	protected abstract void onProcess(int time);
	
	protected void mix(int value) {
		if (!mask)
			out.mix(value, time);
	}
	
	/**
	 * 向混音器传递某个时间点的音频值.
	 * @param value
	 *   音频值
	 * @param offset
	 *   时间为 this.time + offset
	 * @since v0.2.9
	 */
	protected void mix(int value, int offset) {
		if (!mask)
			out.mix(value, time + offset);
	}

}
