package zdream.nsfplayer.sound;

/**
 * <p>噪音发声器
 * <p>该发声器不处理 Envelope 包络相关的参数. 如果确实需要使用这类数据,
 * 你需要使用 {@link EnvelopeSoundNoise}.
 * </p>
 * 
 * @author Zdream
 * @since v0.2.2
 */
public class SoundNoise extends Sound2A03 {
	
	public static final short[] NOISE_PERIODS_NTSC = {
		4, 8, 16, 32, 64, 96, 128, 160, 202, 254, 380, 508, 762, 1016, 2034, 4068
	};

	public static final short[] NOISE_PERIODS_PAL = {
		4, 8, 14, 30, 60, 88, 118, 148, 188, 236, 354, 472, 708,  944, 1890, 3778
	};
	
	public short[] PERIOD_TABLE;

	public SoundNoise() {
		PERIOD_TABLE = NOISE_PERIODS_NTSC;
		reset();
	}
	
	/* **********
	 *   参数   *
	 ********** */
	
	/**
	 * {@link #dutySampleRate} 的合理取值.
	 * 满足:
	 * <li>duty == 0 时 dutySampleRate = DUTY_SAMPLE_RATE0;
	 * <li>duty == 1 时 dutySampleRate = DUTY_SAMPLE_RATE1;
	 * </li>
	 * @see #dutySampleRate
	 */
	public static final int
			DUTY_SAMPLE_RATE0 = 13,
			DUTY_SAMPLE_RATE1 = 8;
	
	/*
	 * 原始记录参数
	 * 0 号位: (0x400C)
	 * 1 号位: (0x400D)
	 * 2 号位: (0x400E)
	 * 3 号位: (0x400F)
	 */
	
	/*
	 * 0 号位的 envelopeLoop 和 envelopeDisable 两个和 Envelope 包络相关的参数
	 * 已经移动到 EnvelopeSoundNoise 子类中.
	 * 
	 * 在该类默认认为 envelopeLoop = true, envelopeDisable = true 且不会改变
	 */

	/**
	 * <p>0 号位: 0000xxxx
	 * <p>unsigned, 值域 [0, 15]
	 * </p>
	 */
	public int fixedVolume;
	
	// 1 号位忽略
	
	/**
	 * <p>2 号位: 0000xxxx
	 * <p>unsigned, 值域 [0, 15]
	 * <p>用于查询波长的索引. PERIOD_TABLE[periodIndex] 即得到波长的值
	 * </p>
	 */
	public int periodIndex;
	
	/**
	 * <p>2 号位: x0000000 的加工数据
	 * <p>为 1 时为 true, dutySampleRate = 8; 为 0 时为 false, dutySampleRate = 13
	 * <p>该值为 duty 值
	 * </p>
	 * @see #DUTY_SAMPLE_RATE0
	 * @see #DUTY_SAMPLE_RATE1
	 */
	public int dutySampleRate;
	
	/**
	 * <p>3 号位: xxxxx000 的加工数据
	 * <p>不是查找索引. lengthCounter = PulseSound.LENGTH_TABLE[v]
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
	 * 在 onProcess() 方法中使用的
	 */
	protected int shiftReg;
	
	/* **********
	 * 公共方法 *
	 ********** */
	
	@Override
	public void reset() {
		// 原始记录参数
		fixedVolume = 0;
		periodIndex = 0;
		dutySampleRate = DUTY_SAMPLE_RATE0;
		lengthCounter = 0;
		
		// 辅助参数
		counter = 0;
		shiftReg = 1;
		
		super.reset();
	}

	@Override
	protected void onProcess(int time) {
		final int period = PERIOD_TABLE[periodIndex];
		
		while (time >= counter) {
			time -= counter;
			this.time += counter;
			
			int value = processStep(counter);
			mix(value);
			counter = period;
		}

		counter -= time;
		this.time += time;
		processRemainTime(time);
	}
	
	protected int processStep(int period) {
		if (!isEnable()) {
			return 0;
		}
		
		int ret = ((shiftReg & 1) != 0) ? fixedVolume : 0;
		shiftReg = (((shiftReg << 14) ^ (shiftReg << dutySampleRate)) & 0x4000) | (shiftReg >> 1);
		return ret;
	}
	
	protected void processRemainTime(int period) {
		// do nothing
	}

}
