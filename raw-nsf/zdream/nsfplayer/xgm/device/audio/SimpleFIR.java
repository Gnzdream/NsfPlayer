package zdream.nsfplayer.xgm.device.audio;

public class SimpleFIR {
	
	protected int[] tap;
	protected double[] h;
	protected int n, m;
	protected double rate;
	protected double cutoff;
	
	public SimpleFIR(int tapNum) {
		n = tapNum | 1;
		tap = new int[n];
		m = (n - 1) / 2;
		h = new double[m + 1];
	}
	
	public final void setRate(double rate) {
		this.rate = rate;
	}
	
	public final void setCutoff(double cutoff) {
		this.cutoff = cutoff;
	}
	
	public void reset() {
		int i;

		for (i = 0; i < n; i++)
			tap[i] = 0;

		double Wc = 2.0 * Math.PI * (cutoff / rate);
		double gain = 0.0;

		h[0] = Wc / Math.PI * FilterTools.HAMMING_window(0, m);

		for (i = 1; i <= m; i++) {
			h[i] = (1.0 / (Math.PI * i)) * Math.sin(Wc * i) * FilterTools.HAMMING_window(i, m);
			gain += h[i];
		}

		// 输出增益调整
		for (i = 0; i <= m; i++)
			h[i] /= gain;
	}
	
	public final void put(int wav) {
		for (int i = 0; i < n - 1; i++)
			tap[i] = tap[i + 1];
		tap[n - 1] = wav;
	}
	
	public final int get() {
		double temp = h[0] * tap[m];
		for (int i = 1; i <= m; i++)
			temp += h[i] * (tap[m + i] + tap[m - i]);
		return (int) temp;
	}

}
