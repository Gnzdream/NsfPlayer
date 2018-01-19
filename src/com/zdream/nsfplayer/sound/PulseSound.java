package com.zdream.nsfplayer.sound;

/**
 * 矩形波发声器
 * @author Zdream
 * @date 2017-12-06
 */
public class PulseSound implements INsfSound, IResetable {
	
	static final boolean[][] SQRT_BL = {
			{ false, false,  true,  true, false, false, false, false, false, false, false, false, false, false, false, false },
			{ false, false,  true,  true,  true,  true, false, false, false, false, false, false, false, false, false, false },
			{ false, false,  true,  true,  true,  true,  true,  true,  true,  true, false, false, false, false, false, false },
			{  true,  true, false, false, false, false,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true }
	};
	
	/**
	 * 音量大小, 0-15
	 */
	public int volume;
	/**
	 * <p>Envelope Fix
	 * <p>它是一个开关, 将控制实际输出的音量大小是根据 {@link #volume} 还是 ???
	 */
	public boolean envelope_disable;
	/**
	 * 
	 */
	public boolean envelope_loop;
	/**
	 * 
	 */
	public int duty;
	
	
	/**
	 * 
	 */
	public boolean sweep_enable;
	/**
	 * 
	 */
	public int sweep_div_period;
	/**
	 * 
	 */
	public boolean sweep_mode;
	/**
	 * 
	 */
	public int sweep_amount;
	
	
	/**
	 * 
	 */
	public int period;
	
	
	/**
	 * 不知道干什么用
	 */
	public int length_counter;
	
	// 补充
	/**
	 * 
	 */
	public int envelope_div_period;
	/**
	 * 当 {@link #sweep_enable}, {@link #sweep_div_period}, {@link #sweep_mode}, {@link #sweep_amount}
	 * 等数据被重新写入时, 这个数据会被置为 true, 表示这些数据已经更新.
	 */
	public boolean sweep_write;
	/**
	 * 
	 */
	public int sweep_freq;

	@Override
	public String name() {
		return "Pulse";
	}

	@Override
	public void reset() {
		
		
	}
	
	/**
	 * <p>计算目标扫描频率. 扫描频率将会放在成员变量 {@link #sweep_freq} 中.</p>
	 * calculates target sweep frequency
	 */
	public void sweepUpdate(boolean isChannel0) {
		int shifted = period >> sweep_amount;
		if (isChannel0 && sweep_mode) shifted += 1;
		sweep_freq = period + (sweep_mode ? -shifted : shifted);
	}
	
}
