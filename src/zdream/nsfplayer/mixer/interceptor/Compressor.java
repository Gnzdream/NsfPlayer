package zdream.nsfplayer.mixer.interceptor;

/**
 * 限制音量在一定的范围内的拦截器
 * 
 * @author Zdream
 * @since v0.2.3
 */
public class Compressor implements ISoundInterceptor {
	
	private int strength, limit, threshold;

	public Compressor() {
		setParam(32767, 1.0, 1.0);
	}

	@Override
	public int execute(int value, int time) {
		if (threshold < value)
			value = threshold + (((value - threshold) * strength) >> 12);
		else if (value < -threshold)
			value = -threshold + (((value + threshold) * strength) >> 12);

		if (limit > 0) {
			if (limit < value)
				value = limit;
			else if (value < -limit)
				value = -limit;
		}

		return value;
	}
	
	/* **********
	 * 参数设置 *
	 ********** */

	@Override
	public void reset() {
		// do nothing
	}
	
	public void setParam(double lim, double thr, double str) {
		if (lim < 0.0)
			lim = 0.0;
		limit = (lim < 1.0) ? ((int) (lim * 32767)) : 0;
		threshold = (int) (thr * limit);
		strength = (int) (str * (1 << 12));
	}
	
	/* **********
	 * 开启状态 *
	 ********** */
	
	boolean enable = true;

	@Override
	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	@Override
	public boolean isEnable() {
		return enable;
	}

}
