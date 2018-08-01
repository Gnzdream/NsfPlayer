package zdream.nsfplayer.sound;

import zdream.nsfplayer.sound.mixer.IMixerChannel;

/**
 * 旧
 * @author Zdream
 */
public abstract class AbstractNsfSound0 implements INsfSound, IResetable {

	/**
	 * 通向合成器 Buffer 的管道
	 */
	protected IMixerChannel out;

	/**
	 * <p>时钟周期计数器
	 * <p>记录时钟周期数, 会在每调用 {@link #endFrame()} 时重置, 从零开始.
	 */
	protected int mclock;
	
	/**
	 * 上一个合成进 Buffer 的数据.
	 */
	protected int lastValue;

	// Variables used by channels
	/**
	 * enabled
	 */
	protected boolean m_iControlReg;
	/**
	 * unsigned
	 */
	protected byte m_iEnabled;
	/**
	 * unsigned
	 */
	protected int period;
	/**
	 * unsigned
	 */
	protected short m_iLengthCounter;
	/**
	 * unsigned
	 */
	protected int m_iCounter;
	
	/**
	 * 开始一个新的时间节点. 将时钟周期计数清空.
	 */
	public void endFrame() {
		mclock = 0;
	}

	public final int getPeriod() {
		return period;
	}

	protected void mix(int value) {
		if (lastValue != value) {
			out.mix(value, mclock);
			lastValue = value;
		}
	}
	
	public void setOut(IMixerChannel out) {
		this.out = out;
	}

}
