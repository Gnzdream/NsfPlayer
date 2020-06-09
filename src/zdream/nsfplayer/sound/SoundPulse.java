package zdream.nsfplayer.sound;

/**
 * 矩形波发声器
 * @author Zdream
 * @since v0.2.1
 */
public class SoundPulse extends Sound2A03 {
	
	protected static final boolean[][] DUTY_TABLE = {
			{ false, false,  true,  true, false, false, false, false, false, false, false, false, false, false, false, false },
			{ false, false,  true,  true,  true,  true, false, false, false, false, false, false, false, false, false, false },
			{ false, false,  true,  true,  true,  true,  true,  true,  true,  true, false, false, false, false, false, false },
			{  true,  true, false, false, false, false,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true }
	};
	
	public SoundPulse() {
		reset();
	}
	
	/* **********
	 *   参数   *
	 ********** */
	/*
	 * 原始记录参数
	 */
	
	/**
	 * <p>0 号位: xx000000
	 * <p>unsigned, 值域 [0, 3]. 指示音色的值, 指向 DUTY_TABLE 的一级索引
	 * </p>
	 */
	public int dutyLength;

	/**
	 * <p>0 号位: 0000xxxx
	 * <p>unsigned, 值域 [0, 15]
	 * </p>
	 */
	public int fixedVolume;
	
	/*
	 * 1 号位的和 sweep 扫音相关的参数, 以及 0 号位的 envelopeFix,
	 * 已经移动到 SweepSoundPulse 子类中.
	 * 
	 * 在该类默认认为 envelopeFix = true 且不会改变
	 */
	
	/**
	 * <p>2 号位: xxxxxxxx (低八位), 3 号位: 00000xxx (高三位) 共 11 位
	 * <p>为波长值的 16 分之一, 即 16 个该值为波长值, 单位为 CPU 时钟周期;
	 * <p>unsigned, 值域 [0, 2047]
	 * </p>
	 */
	public int period;
	
	/**
	 * <p>3 号位: xxxxx000
	 * <p>查找索引. 设值的时候请设值为 LENGTH_TABLE[?].
	 * </p>
	 */
	public int lengthCounter;
	
	/*
	 * 辅助参数
	 * 
	 * 注意, 0x4015 位: (Pulse 1) 0000000x, (Pulse 2) 000000x0 是 enable, 在超类中
	 */
	
	/**
	 * 记录当前周期（时钟周期数为 period, 16 分之一的真实波长数）
	 * 没放完的时钟周期数.
	 */
	private int counter;
	
	/**
	 * 真实波长分成 16 个部分,
	 * 该参数记录当前正在播放的周期是这 16 个中的第几个.
	 * 值域 [0, 15]
	 */
	protected int dutyCycle; // m_iStepGen
	
	/* **********
	 * 公共方法 *
	 ********** */
	
	/**
	 * 重置相关数据
	 */
	public void reset() {
		// 原始记录参数
		
		dutyLength = 0;
		fixedVolume = 0;
		period = 0;
		
		lengthCounter = LENGTH_TABLE[0];
		counter = 0;
		dutyCycle = 0;
		
		super.reset();
	}
	
	@Override
	protected void onProcess(int time) {
		if (period < 8) {
			dutyCycle = 0;
			mix(0);
			return;
		}
		
		boolean valid = isStatusValid();
		
		while (time >= counter) {
			time -= counter;
			this.time += counter;
			counter = period + 1;
			
			int value = processStep(counter);
			mix(valid ? value : 0);
			
			dutyCycle = (dutyCycle + 1) & 0x0F;
		}

		counter -= time;
	}
	
	protected int processStep(int period) {
		int volume = fixedVolume;
		if (DUTY_TABLE[dutyLength][dutyCycle]) {
			return volume;
		} else {
			return 0;
		}
	}
	
	protected boolean isStatusValid() {
		return (period > 7) && isEnable() && (lengthCounter > 0)/* && (sweepResult < 0x800)*/;
	}

}
