package zdream.nsfplayer.sound;

/**
 * VRC6 锯齿形轨道的发声器
 * 
 * @author Zdream
 * @since v0.2.3
 */
public class SoundVRC6Sawtooth extends SoundVRC6 {

	public SoundVRC6Sawtooth() {
		reset();
	}
	
	/* **********
	 *   参数   *
	 ********** */
	/*
	 * 原始记录参数
	 * 0 号位: (0xB000)
	 * 1 号位: (0xB001)
	 * 2 号位: (0xB002)
	 */
	
	/**
	 * <p>0 号位: 00xxxxxx
	 * <p>音量. 范围 [0, 63], 而不是 [0, 15]
	 * </p>
	 */
	public int volume;
	
	/**
	 * <p>1 号位: xxxxxxxx 作为低 8 位, 2 号位: 0000xxxx 作为高 4 位, 共 12 位
	 * <p>1/14 的波长(影响音高). 范围 [0, 0xFFF]
	 * </p>
	 */
	public int period;
	
	/*
	 * 2 号位: x0000000 即 父类中的 enabled 参数
	 */
	
	/*
	 * 辅助参数
	 */
	/**
	 * 记录当前周期（时钟周期数为 period, 14 分之一的真实波长数）
	 * 没放完的时钟周期数.
	 */
	private int counter;
	
	/**
	 * 将一个波长分为 14 份, 该值记录当前渲染到 14 份中的第几份.
	 * 范围 [1, 14] (注意这里除了初始化后从 0 开始, 其它都从 1 开始)
	 */
	private int cycleCounter;
	
	/**
	 * 计算音像的振幅参数
	 */
	private int phaseAccumulator;
	
	/* **********
	 * 公共方法 *
	 ********** */
	
	@Override
	public void reset() {
		// 原始记录参数
		volume = period = 0;
		
		// 辅助参数
		counter = 0;
		cycleCounter = 0;
		phaseAccumulator = 0;
		
		super.reset();
	}

	@Override
	protected void onProcess(int time) {
		if (period == 0) {
			this.time += time;
			return;
		}

		while (time >= counter) {
			time -= counter;
			this.time += counter;
			counter = period + 1;

			if ((cycleCounter & 1) != 0)
				phaseAccumulator = (phaseAccumulator + volume) & 0xFF;

			cycleCounter++;

			if (cycleCounter == 14) {
				phaseAccumulator = 0;
				cycleCounter = 0;
			}

			// The 5 highest bits of accumulator are sent to the mixer
			mix(phaseAccumulator >> 3);
		}

		counter -= time;
		this.time += time;
	}

}
