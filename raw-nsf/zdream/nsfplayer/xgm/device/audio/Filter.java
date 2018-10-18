package zdream.nsfplayer.xgm.device.audio;

import zdream.nsfplayer.xgm.device.IRenderable0;

public class Filter implements IRenderable0 {
	
	protected IRenderable0 target;

	protected int type;
	protected int[] out = new int[2];
	protected double a;
	protected double rate, R, C;
	protected boolean disable;
	protected final int GETA_BITS = 20;
	
	public Filter() {
		target = null;
		rate = FilterTools.DEFAULT_RATE;
		R = 4700;
		C = 10.0E-9;
		disable = false;
		out[0] = out[1] = 0;
	}
	
	public final void attach(IRenderable0 t) {
		target = t;
	}

	public final int fastRender(int[] buf) {
		if (target != null)
			target.render(buf);
		if (a < 1.0) {
			out[0] += (int) (a * (buf[0] - out[0]));
			out[1] += (int) (a * (buf[1] - out[1]));
			buf[0] = out[0];
			buf[1] = out[1];
		}
		return 2;
	}

	@Override
	public void tick(int clocks) {
		if (target != null)
			target.tick(clocks);
	}

	@Override
	public int render(int[] bs) {
		return fastRender(bs);
	}

	public void setParam(int r, int c) {
		// C = 1.0E-10 * c;
		R = r;

		C = Math.pow((double) c / 400.0, 2.0) * 1.0E-10 * 400.0;
		// curved to try to provide useful range of settings
		// LPF = 112 ~ my NES

		updateFactor();
	}
	
	public void setclock() {
		reset();
	}

	public final void setRate(double r) {
		rate = r;
		updateFactor();
	}

	public final void updateFactor() {
		if (R != 0.0 && C != 0.0 && rate != 0.0)
			a = (1.0 / rate) / ((R * C) + (1.0 / rate));
		else
			a = 2.0; // disabled
	}
	
	public double getFactor() {
		return a;
	}

	public void reset() {
		updateFactor();
		out[0] = out[1] = 0;
	}

}
