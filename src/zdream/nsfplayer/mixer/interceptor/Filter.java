package zdream.nsfplayer.mixer.interceptor;

/**
 * 音频的过滤器.
 * 
 * 过滤器作用是当前采样的数据按照一定比例合并前面多帧的采样数据, 混合之后得到的采样数据.
 * 
 * @author Zdream
 * @since v0.2.3
 */
public class Filter implements ISoundInterceptor {

	protected int type;
	protected int out;
	protected double a;
	protected double rate, R, C;
	protected boolean disable;
	protected final int GETA_BITS = 20;
	
	public Filter() {
		rate = 48000;
		R = 4700;
		C = 10.0E-9;
		disable = false;
		out = 0;
	}
	
	@Override
	public int execute(int value, int time) {
		if (a < 1.0) {
			out += (int) (a * (value - out));
			value = out;
		}
		return value;
	}
	
	/* **********
	 * 参数设置 *
	 ********** */

	public void setParam(int r, int c) {
		// C = 1.0E-10 * c;
		R = r;

		C = Math.pow((double) c / 400.0, 2.0) * 1.0E-10 * 400.0;
		// curved to try to provide useful range of settings
		// LPF = 112 ~ my NES

		updateFactor();
	}

	/**
	 * @param r
	 *   采样率
	 */
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

	@Override
	public void reset() {
		updateFactor();
		out = 0;
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
