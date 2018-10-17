package zdream.nsfplayer.sound;

/**
 * 噪音发声器
 * 
 * @author Zdream
 * @since v0.2.2
 */
public class NoiseSound extends Sound2A03 {
	
	public static final short[] NOISE_PERIODS_NTSC = {
		4, 8, 16, 32, 64, 96, 128, 160, 202, 254, 380, 508, 762, 1016, 2034, 4068
	};

	public static final short[] NOISE_PERIODS_PAL = {
		4, 8, 14, 30, 60, 88, 118, 148, 188, 236, 354, 472, 708,  944, 1890, 3778
	};
	
	public short[] PERIOD_TABLE;

	public NoiseSound() {
		PERIOD_TABLE = NOISE_PERIODS_NTSC;
		reset();
	}
	
	/* **********
	 *   参数   *
	 ********** */
	/*
	 * 原始记录参数
	 * 0 号位: (0x400C)
	 * 1 号位: (0x400D)
	 * 2 号位: (0x400E)
	 * 3 号位: (0x400F)
	 */

	/**
	 * <p>0 号位: 00x00000
	 * <p>为 1 时为 true, 为 0 时为 false
	 * </p>
	 */
	public boolean looping;
	
	/**
	 * <p>0 号位: 000x0000
	 * <p>为 1 时为 true, 为 0 时为 false
	 * </p>
	 */
	public boolean envelopeFix;

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
	 * <p>2 号位: x0000000
	 * <p>为 1 时为 true, 为 0 时为 false
	 * <p>该值为 duty 值
	 * <p><code>sampleRate = (dutyType) ? 8 : 13;</code>
	 * </p>
	 */
	public boolean dutyType;
	
	/**
	 * <p>3 号位: xxxxx000
	 * <p>查找索引
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
	private int shiftReg;
	
	/* **********
	 * 公共方法 *
	 ********** */
	
	@Override
	public void reset() {
		// 原始记录参数
		looping = false;
		envelopeFix = false;
		fixedVolume = 0;
		periodIndex = 0;
		dutyType = false;
		lengthCounter = 0;
		
		// 辅助参数
		counter = 0;
		shiftReg = 1;
		
		super.reset();
	}

	@Override
	protected void onProcess(int time) {
		final int period = PERIOD_TABLE[periodIndex];
		final int sampleRate = (dutyType) ? 8 : 13;
		
		while (time >= counter) {
			time -= counter;
			this.time += counter;
			counter = period;
			//int volume = m_iEnvelopeFix != 0 ? m_iFixedVolume : m_iEnvelopeVolume;
			mix((shiftReg & 1) != 0 ? fixedVolume : 0);
			shiftReg = (((shiftReg << 14) ^ (shiftReg << sampleRate)) & 0x4000) | (shiftReg >> 1);
		}

		counter -= time;
		this.time += time;
	}

}
