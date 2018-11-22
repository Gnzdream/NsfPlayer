package zdream.nsfplayer.core;

import zdream.nsfplayer.core.IResetable;

/**
 * <p>浮点周期计数器, 工具类
 * <p>这里给出一个数学模型.
 * 一个时间段 T 均匀地分成 N 个部分, 求每个部分的时间段 T1, T2 ...
 * <br>由于 T, N, 还有 T1, T2 等等都要为整数, 因此要满足 sum(Tx) = T,
 * 则会得出 T1 可能不等于 T2 这个结论.
 * <p>本类和 {@link CycleCounter} 不同的是, 这里的总段数允许浮点数
 * </p>
 * 
 * @author Zdream
 * @since v0.2.9
 */
public class FloatCycleCounter implements IResetable {
	
	/**
	 * 总周期数
	 */
	private int cycle;
	
	/**
	 * 总段数, 即分成几段
	 */
	private float maxFrame;
	
	/**
	 * 现在在该周期中, 已经完成的段数
	 */
	private float frameCount;
	
	/**
	 * 现在在该周期中, 已经完成的周期数
	 */
	private int cycleCount;
	
	/**
	 * 缓存上一段的周期数. 如果不存在, 为 -1
	 */
	private int last;

	public FloatCycleCounter() {
		this(100, 10);
	}

	public FloatCycleCounter(int cycle, float maxFrame) {
		setParam(cycle, maxFrame);
	}
	
	/**
	 * 设置总周期数和总段数, 并重置其它所有参数至默认值
	 * @param cycle
	 *   总周期数
	 * @param maxFrame
	 *   总段数
	 */
	public void setParam(int cycle, float maxFrame) {
		this.cycle = cycle;
		this.maxFrame = maxFrame;
		reset();
	}
	
	/**
	 * 时间往前走一个时间段
	 * @return
	 *   该时间段的周期数. 这个周期数会被暂时存储, 在下一次调用 {@link #tick()} 方法或重置前,
	 *   都可以使用 {@link #getLast()} 来获取该值.
	 */
	public int tick() {
		float step = cycle / maxFrame;
		float start = frameCount * step;
		float end = start + step;
		
		cycleCount = (int) end;
		last = cycleCount - (int) (start);
		
		frameCount += 1;
		if (frameCount > maxFrame) {
			// 完成一个周期
			frameCount -= maxFrame;
			cycleCount -= cycle;
		}
		
		return last;
	}

	@Override
	public void reset() {
		frameCount = 0;
		cycleCount = 0;
		last = -1;
	}

	public int getCycle() {
		return cycle;
	}

	public float getMaxFrame() {
		return maxFrame;
	}

	public float getFrameCount() {
		return frameCount;
	}

	public int getCycleCount() {
		return cycleCount;
	}

	public int getLast() {
		return last;
	}

}
