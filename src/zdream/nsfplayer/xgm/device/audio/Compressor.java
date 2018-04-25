package zdream.nsfplayer.xgm.device.audio;

import zdream.nsfplayer.xgm.device.IRenderable0;

public class Compressor implements IRenderable0 {
	
	private int strength, limit, threshold;
	
	public Compressor() {
		setParam(32767, 1.0, 1.0);
	}
	
	public final void reset() {
		// do nothing
	}
	
	public final void setRate(double rate) {
		// do nothing
	}
	
	public void setParam(double lim, double thr, double str) {
		if (lim < 0.0)
			lim = 0.0;
		limit = (lim < 1.0) ? ((int) (lim * 32767)) : 0;
		threshold = (int) (thr * limit);
		strength = (int) (str * (1 << 12));
	}
	
	public final int limiter(int in) {
		if (threshold < in)
			in = threshold + (((in - threshold) * strength) >> 12);
		else if (in < -threshold)
			in = -threshold + (((in + threshold) * strength) >> 12);

		if (limit > 0) {
			if (limit < in)
				in = limit;
			else if (in < -limit)
				in = -limit;
		}

		return in;
	}

	@Override
	public void tick(int clocks) {

	}

	@Override
	public int render(int[] bs) {
		return fastRender(bs);
	}

	/**
	 * @param bs
	 *   int[2]
	 * @return
	 */
	public final int fastRender(int bs[]) {
		bs[0] = limiter(bs[0]);
		bs[1] = limiter(bs[1]);
		return 2;
	}

}
