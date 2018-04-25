package zdream.nsfplayer.xgm.device.audio;

public class FilterTools {
	
	public static final int DEFAULT_RATE = 48000;
	
	public static double HAMMING_window(int n, int M) {
		return 0.54 + 0.46 * Math.cos(Math.PI * n / M);
	}

	public static double HANNING_window(int n, int M) {
		return 0.5 * (1.0 + Math.cos(Math.PI * n / M));
	}

	public static double BERTLET_window(int n, int M) {
		return 1.0 - (double) n / M;
	}

	public static double SQR_window(int n, int M) {
		return 1.0;
	}
	
	public static double window(int n, int m) {
		// rectangular window
		// return 1.0;

		// hanning window
		// return 0.5 + 0.5 * Math.cos(Math.PI*(double)(n)/(double)(m));

		// hamming window
		return 0.54 + 0.46 * Math.cos(Math.PI * (double) (n) / (double) (m));
	}
}
