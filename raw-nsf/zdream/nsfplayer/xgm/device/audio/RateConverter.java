package zdream.nsfplayer.xgm.device.audio;

import zdream.nsfplayer.xgm.device.IRenderable0;

/**
 * <p>抽样器 / 速率转换器
 * <p>它在进行采样时, 将接上的音频设备进行多个采样,
 * 得到的采样结果最后按照加权平均合并成一个采样交给上级.
 * @author Zdream
 */
public class RateConverter implements IRenderable0 {
	
	protected IRenderable0 target;
	protected double clock = 0, rate = 0;
	protected int mult = 0; // 抽样倍率（奇数）
	
	/**
	 * <p>
	 * <p>原来是 [128][2]
	 */
	protected int[][] tap;
	
	/**
	 * 将多个分采样合并 1 个采样, 需要计算每个分采样的权重.
	 */
	protected double[] hr; // H(z), 给每一个
	protected int clocks = 0; // clocks pending Tick execution
	protected SimpleFIR fir;
	
	public final void attach(IRenderable0 t) {
		target = t;
	}
	
	public final void reset() {
		clocks = 0; // cancel any pending ticks

		if (clock > 0 && rate > 0) {
			mult = (int) (clock / rate);
			if (mult < 2)
				return;

			// generate resampling window
			hr = new double[mult + 1];
			
			hr[0] = FilterTools.window(0, mult);
			double gain = hr[0];
			for (int i = 1; i <= mult; i++) {
				hr[i] = FilterTools.window(i, mult);
				gain += hr[i] * 2;
			}

			// normalize window
			for (int i = 0; i <= mult; i++) {
				hr[i] /= gain;
			}

			int length = mult * 2 + 1;
			tap = new int[length][2];
			for (int i = 0; i < length; i++)
				tap[i][0] = tap[i][1] = 0;
		}
	}
	
	/**
	 * 倍率是奇数倍
	 * @param clock
	 */
	public final void setClock(double clock) {
		this.clock = clock;
	}
	
	public final void setRate(double rate) {
		this.rate = rate;
	}

	@Override
	public void tick(int clocks) {
		this.clocks += clocks;
	}

	@Override
	public int render(int[] bs) {
		return fastRender(bs);
	}
	
	/**
	 * 初始值全部都是无效的
	 * @param bs
	 * @return
	 */
	public final int fastRender(int[] bs) {
		double[] out = new double[2];

		for (int i = 0; i <= mult; i++) {
			tap[i][0] = tap[i + mult][0];
			tap[i][1] = tap[i + mult][1];
		}

		// divide clock ticks among samples evenly
		int mclocks = 0;
		for (int i = 1; i <= mult; i++) {
			mclocks += clocks;
			if (mclocks >= mult) {
				int sub_clocks = mclocks / mult;
				target.tick(sub_clocks);
				mclocks -= (sub_clocks * mult);
			}
			target.render(tap[mult + i]);
		}
		assert (mclocks == 0); // all clocks must be used
		clocks = 0;

		out[0] = hr[0] * tap[mult][0];
		out[1] = hr[0] * tap[mult][1];

		for (int i = 1; i <= mult; i++) {
			out[0] += hr[i] * (tap[mult + i][0] + tap[mult - i][0]);
			out[1] += hr[i] * (tap[mult + i][1] + tap[mult - i][1]);
		}

		bs[0] = (int) out[0];
		bs[1] = (int) out[1];

		return 2;
	}

}
