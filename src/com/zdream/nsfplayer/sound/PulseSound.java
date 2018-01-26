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
	 * <p>音色
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
	public int sweep_div;
	
	
	/**
	 * <p>反映指定矩形波中音符音调的值, 一般而言指的是波长的概念
	 */
	public int period;
	/**
	 * frequency divider
	 */
	public int scounter;
	
	
	/**
	 * 
	 */
	public boolean envelope_write;
	/**
	 * 
	 */
	public int envelope_counter;
	/**
	 * 
	 */
	public int envelope_div;
	
	
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
	 * sfreq, 扫描频率
	 */
	public int sweep_freq;
	/**
	 * <p>相位计数器. 缓存矩形波渲染到一个周期的哪个位置.
	 * <p>该数值只有低 4 位有效.
	 * <p>phase counter
	 */
	public int sphase;
	
	/**
	 * 预输出的数据存放位置
	 */
	public int out;

	@Override
	public String name() {
		return "Pulse";
	}

	@Override
	public void reset() {
		scounter = 0;
		sphase = 0;
		
		sweep_div = 0;
		envelope_div = 0;
		length_counter = 0;
		envelope_counter = 0;
		
		volume = 0;
		envelope_disable = false;
		envelope_loop = false;
		duty = 0;
		
		sweep_enable = false;
		sweep_div_period = 0;
		sweep_mode = false;
		sweep_amount = 0;
		sweep_div = 0;
		
		period = 0;
		
		envelope_write = false;
		envelope_div_period = 0;
		sweep_write = false;
		sweep_freq = 0;
		
		out = 0;
	}
	
	/**
	 * <p>计算目标扫描频率. 扫描频率将会放在成员变量 {@link #sweep_freq} 中.</p>
	 * calculates target sweep frequency
	 * @param diff
	 *   如果是轨道矩形波 1 的, diff = true, 矩形波 2 的为 false
	 */
	public void sweepUpdate(boolean diff) {
		int shifted = period >> sweep_amount;
		if (sweep_mode && diff) shifted += 1;
		sweep_freq = period + (sweep_mode ? -shifted : shifted);
	}
	
	/**
	 * 
	 * @param clocks
	 *   unsigned
	 * @return
	 */
	public void tick(int clocks) {
		scounter += clocks;
		while (scounter > period) {
			sphase = (sphase + 1) & 15; // 16 为一周期
			scounter -= (period + 1);
		}
		
		if (length_counter > 0 && period >= 8 && sweep_freq < 0x800) {
			int v = envelope_disable ? volume : envelope_counter;
			out = (SQRT_BL[duty][sphase]) ? v : 0;
		}

	}
	
	@Override
	public int render() {
		return out;
	}
	
}
