package zdream.nsfplayer.sound;

/**
 * 三角波发声器
 * @author Zdream
 * @since v0.2.2
 */
public class SoundTriangle extends Sound2A03 {
	
	/**
	 * 一个周期内音波的高低. 实际上就是等腰三角形
	 */
	private static final byte[] TRIANGLE_WAVE = {
			0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 
			0x0F, 0x0E, 0x0D, 0x0C, 0x0B, 0x0A, 0x09, 0x08, 0x07, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01, 0x00
	};
	
	public SoundTriangle() {
		reset();
	}
	
	/* **********
	 *   参数   *
	 ********** */
	/*
	 * 0 号位: (0x4008)
	 * 1 号位: (0x4009)
	 * 2 号位: (0x400A)
	 * 3 号位: (0x400B)
	 */
	
	/*
	 * 0 号位的 looping 和 linearLoad,
	 * 已经移动到 LinearSoundTriangle 子类中.
	 * 
	 * 在该类默认认为 looping = true 且不会改变
	 */
	
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
	
	/**
	 * <p>记录上一个 period 的值.
	 * <p>NSF 播放中, 部分曲目会将 period 设置为 0, 表示立刻停止播放.
	 * 此时如果三角波在周期 (16 / 32) 附近时, 它的音量很大, 由于 period 重置,
	 * 它将快速降到 0, 导致发出很大的干扰音.
	 * <p>这个问题的解决方法是, 缓存上一个 period 的值.
	 * 如果 period 被意外重置到 0, 则在渲染时用 lastPeriod 作为替代值,
	 * 等到三角波完成一个周期之后再关闭播放. 我称这个方法为“软着陆”.
	 * </p>
	 * 
	 * @since v0.2.10
	 */
	protected int lastPeriod;
	
	/* **********
	 * 公共方法 *
	 ********** */
	
	@Override
	public void reset() {
		// 原始记录参数
		period = 0;
		lengthCounter = LENGTH_TABLE[0];
		
		// 辅助参数
		counter = 0;
		dutyCycle = 0;
		lastPeriod = 0;
		
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
		if (period >= 8) {
			lastPeriod = period;
		}
		if (lastPeriod < 7) {
			lastPeriod = 7;
		}
		
		while (time >= counter) {
			time -= counter;
			this.time += counter;
			
			if (period < 8) {
				counter = lastPeriod + 1;
			} else {
				counter = period + 1;
			}
			
			int value = processStep(counter);
			mix(value);
		}
		
		counter -= time;
	}
	
	protected int processStep(int period) {
		if (isStatusValid()) {
			int ret = TRIANGLE_WAVE[dutyCycle];
			dutyCycle = (dutyCycle + 1) & 0x1F;
			return ret;
		} else {
			if (dutyCycle == 0) {
				if (this.period == 0) {
					lastPeriod = 7;
				}
				return 0;
			}
			int ret = TRIANGLE_WAVE[dutyCycle];
			dutyCycle = (dutyCycle + 1) & 0x1F;
			return ret;
		}
	}
	
	protected boolean isStatusValid() {
		return isEnable() && lengthCounter > 0 && period > 1;
	}

}
