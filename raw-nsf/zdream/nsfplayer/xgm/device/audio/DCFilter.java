package zdream.nsfplayer.xgm.device.audio;

import zdream.nsfplayer.xgm.device.IRenderable0;

public class DCFilter implements IRenderable0 {
	
	private double r = 0, c = 0;
	private double a;
	private double[] in = new double[2],
			out = new double[2];
	private double rate;
	
	public DCFilter() {
		reset();
	}
	
	public final void updateFactor() {
		if (c == 0.0 || r == 0.0) {
			a = 2.0; // disable
		} else {
			a = (r * c) / ((r * c) + (1.0 / rate));
		}
	}
	
	public final double getFactor() {
		return a;
	}

	public final void setRate(double r) {
		// 截止频率 2 pi * r * c
		this.rate = r;
		updateFactor();
	}

	public final void setParam(int r, int c) {
		this.r = r;
		// C = c;

		if (c > 255)
			this.c = 0.0; // disable
		else
			this.c = 2.0e-4 * (1.0 - Math.pow(1.0 - ((double) (c + 1) / 256.0), 0.05));

		// the goal of this curve is to have a wide range of practical use,
		// though it may look a little complicated. points of interest:
		// HPF = 163 ~ my NES
		// HPF = 228 ~ my Famicom
		// low values vary widely and have an audible effect
		// high values vary slowly and have a visible effect on DC offset

		updateFactor();
	}

	int render_count = 0; // TODO 测试变量

	/**
	 * 不是虚拟的渲染
	 * @param buf
	 */
	public final int fastRender(int[] bs) {
		if (a < 1.0) {
			out[0] = a * (out[0] + bs[0] - in[0]);
			in[0] = bs[0];
			bs[0] = (int) out[0];

			out[1] = a * (out[1] + bs[1] - in[1]);
			in[1] = bs[1];
			bs[1] = (int) out[1];
		}
		render_count++;
		return 2;
	}

	@Override
	public int render(int[] bs) {
		return fastRender(bs);
	}

	@Override
	public void tick(int clocks) {}

	public final void reset() {
		in[0] = in[1] = 0;
		out[0] = out[1] = 0;
	}

}
