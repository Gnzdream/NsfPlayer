package zdream.nsfplayer.sound;

/**
 * 三角波发声器
 * @author Zdream
 * @since v0.2.2
 */
public class TriangleSound extends Sound2A03 {
	
	/**
	 * 一个周期内音波的高低. 实际上就是等腰三角形
	 */
	private static final byte[] TRIANGLE_WAVE = {
			0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 
			0x0F, 0x0E, 0x0D, 0x0C, 0x0B, 0x0A, 0x09, 0x08, 0x07, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01, 0x00
		};
	
	/* **********
	 *   参数   *
	 ********** */
	/*
	 * 0 号位: (0x4008)
	 * 1 号位: (0x4009)
	 * 2 号位: (0x400A)
	 * 3 号位: (0x400B)
	 */

	/**
	 * <p>0 号位: x0000000
	 * <p>为 1 时为 true, 为 0 时为 false
	 * <p>这个标志说明该帧这个发声器是否应该发声. 但是实际是否发声请见下面方法 {@link #onProcess(int)}.
	 * </p>
	 */
	public boolean looping;
	
	/**
	 * <p>0 号位: 0xxxxxxx
	 * <p>unsigned, 值域 [0, 127]
	 * </p>
	 */
	public int linearLoad;
	
	// 1 号位忽略
	
	/**
	 * <p>2 号位: xxxxxxxx (低八位), 3 号位: 00000xxx (高三位) 共 11 位
	 * <p>为波长值的 16 分之一, 即 16 个该值为波长值, 单位为 CPU 时钟周期;
	 * <p>unsigned, 值域 [0, 2047]
	 * </p>
	 */
	public int period;
	
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
	 * 真实波长分成 32 个部分. 每个周期的音频高度在 {@link #TRIANGLE_WAVE} 中显示.
	 * 该参数记录当前正在播放的周期是这 32 个中的第几个.
	 * 值域 [0, 31]
	 */
	private int dutyCycle;
	
	/* **********
	 * 公共方法 *
	 ********** */
	
	@Override
	public void reset() {
		// 原始记录参数
		looping = false;
		linearLoad = 0;
		period = 0;
		lengthCounter = 0;
		
		// 辅助参数
		counter = 0;
		dutyCycle = 0;
		
		super.reset();
	}
	
	/**
	 * <p>指导发声器工作.
	 * <p>如果该轨道需要发出声音, 则循环输出三角波数据;
	 * <p>如果该轨道需要停止发出声音, 若上一帧的三角波输出, 还剩余部分周期没有输出完,
	 * 先输出完上一个周期之后, 再停止发出声音.
	 * </p>
	 */
	protected void onProcess(int time) {
		if (!looping) {
			onProcessMute(time);
			return;
		}
		
		if (period <= 1) {
			// 频率太高根本听不见. 这里就直接不播放了
			this.time += time;
			// dutyCycle = 7; 原程序有, 我把它注释了
			mix(TRIANGLE_WAVE[dutyCycle]);
			return;
		}

		while (time >= counter) {
			time -= counter;
			this.time += counter;
			counter = period + 1;
			mix(TRIANGLE_WAVE[dutyCycle]);
			dutyCycle = (dutyCycle + 1) & 0x1F;
		}
		
		counter -= time;
		this.time += time;
	}
	
	/**
	 * 该轨道需要停止发出声音, 这里需要结束上一整个三角波周期之后, 才能完全进入静音状态
	 * @param time
	 */
	private void onProcessMute(int time) {
		if (dutyCycle == 0) {
			// 上一个三角波周期已经结束
			this.time += time;
			return;
		}
		
		if (period <= 1) {
			// 频率太高根本听不见. 这里就直接过掉该周期
			dutyCycle = 0;
			mix(TRIANGLE_WAVE[dutyCycle]);
			this.time += time;
			return;
		}
		
		while (time >= counter) {
			time -= counter;
			this.time += counter;
			counter = period + 1;
			mix(TRIANGLE_WAVE[dutyCycle]);
			dutyCycle = (dutyCycle + 1) & 0x1F;
			
			if (dutyCycle == 0) {
				// 一整个周期结束
				break;
			}
		}

		this.time += time;
	}

}
