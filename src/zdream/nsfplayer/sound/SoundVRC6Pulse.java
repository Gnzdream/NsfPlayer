package zdream.nsfplayer.sound;

/**
 * VRC6 矩形轨道的发声器. 共存在两个这类轨道
 * 
 * @author Zdream
 * @since v0.2.3
 */
public class SoundVRC6Pulse extends SoundVRC6 {
	
	public SoundVRC6Pulse() {
		reset();
	}
	
	/* **********
	 *   参数   *
	 ********** */
	/*
	 * 原始记录参数
	 * 0 号位: (0x9000 或 0xA000)
	 * 1 号位: (0x9001 或 0xA001)
	 * 2 号位: (0x9002 或 0xA002)
	 */
	
	/**
	 * <p>0 号位: x0000000
	 * <p>是否需要播放
	 * <p>为 1 时为 true, 为 0 时为 false
	 * </p>
	 */
	public boolean gate;
	
	/**
	 * <p>0 号位: 0xxx0000
	 * <p>音色. 范围 [0, 7]
	 * </p>
	 */
	public int duty;
	
	/**
	 * <p>0 号位: 0000xxxx
	 * <p>音量. 范围 [0, 15]
	 * </p>
	 */
	public int volume;
	
	/**
	 * <p>1 号位: xxxxxxxx 作为低 8 位, 2 号位: 0000xxxx 作为高 4 位, 共 12 位
	 * <p>波长(影响音高). 范围 [0, 0xFFF]
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
	 * 记录当前周期（时钟周期数为 period, 16 分之一的真实波长数）
	 * 没放完的时钟周期数.
	 */
	private int counter;
	
	/**
	 * 将一个波长分为 16 份, 该值记录当前渲染到 16 份中的第几份.
	 * 范围 [0, 15]
	 */
	private int dutyCycleCounter;
	
	/* **********
	 * 公共方法 *
	 ********** */
	
	@Override
	public void reset() {
		// 原始记录参数
		gate = false;
		duty = volume = period = 0;
		
		// 辅助参数
		counter = 0;
		dutyCycleCounter = 0;
		
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
		
			dutyCycleCounter = (dutyCycleCounter + 1) & 0x0F;
			mix((gate || dutyCycleCounter > duty) ? volume : 0);
		}

		counter -= time;
		this.time += time;
	}
	
}
