package zdream.nsfplayer.mixer.interceptor;

/**
 * DC 过滤器. 需要得到采样率 （采样 / 秒*轨道）
 * 
 * @author Zdream
 * @since v0.2.3
 */
public class DCFilter implements ISoundInterceptor {
	
	private double r = 0, c = 0;
	private double a;
	private double in, out;
	private double rate;

	public DCFilter() {
		reset();
	}

	@Override
	public int execute(int value, int time) {
		if (a < 1.0) {
			out = a * (out + value - in);
			in = value;
			value = (int) out;
		}
		return value;
	}
	
	/* **********
	 * 参数设置 *
	 ********** */

	@Override
	public void reset() {
		in = out = 0;
	}
	
	public final double getFactor() {
		return a;
	}

	/**
	 * @param r
	 *   采样率
	 */
	public final void setRate(double r) {
		// 截止频率 2 pi * r * c
		this.rate = r;
		updateFactor();
	}

	public final void setParam(int r, int c) {
		this.r = r;

		if (c > 255)
			this.c = 0.0; // 禁用
		else
			this.c = 2.0e-4 * (1.0 - Math.pow(1.0 - ((double) (c + 1) / 256.0), 0.05));

		// 以下的大段文本抄自原工程 NsfPlayer:
		// the goal of this curve is to have a wide range of practical use,
		// though it may look a little complicated. points of interest:
		// HPF = 163 ~ my NES
		// HPF = 228 ~ my Famicom
		// low values vary widely and have an audible effect
		// high values vary slowly and have a visible effect on DC offset

		updateFactor();
	}
	
	private final void updateFactor() {
		if (c == 0.0 || r == 0.0) {
			a = 2.0; // 禁用
		} else {
			a = (r * c) / ((r * c) + (1.0 / rate));
		}
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
