package zdream.nsfplayer.sound.buffer;

import static zdream.nsfplayer.sound.buffer.BufferContext.*;

public class BlipEQ {

	/**
	 * <p>Logarithmic rolloff to treble dB at half sampling rate.
	 * Negative values reduce treble, small positive values (0 to 5.0) increase treble.
	 * @param treble_db
	 *   默认值就是 0
	 */
	public BlipEQ (double treble_db) {
		treble = treble_db;
		rolloff_freq = 0;
		sample_rate = 48000;
		cutoff_freq = 0;
	}
	
	/**
	 * @param treble
	 * @param rolloff_freq
	 * @param sample_rate
	 * @param cutoff_freq
	 *   默认值就是 0
	 */
	public BlipEQ(double treble, int rolloff_freq, int sample_rate, int cutoff_freq) {
		this.treble = treble;
		this.rolloff_freq = rolloff_freq;
		this.sample_rate = sample_rate;
		this.cutoff_freq = cutoff_freq;
	}
	
	double treble;
	int rolloff_freq;
	int sample_rate;
	int cutoff_freq;
	
	void generate(float[] out, int offset, int count) {
		// lower cutoff freq for narrow kernels with their wider transition band
		// (8 points->1.49, 16 points->1.15)
		double oversample = blip_res * 2.25 / count + 0.85;
		double half_rate = sample_rate * 0.5;
		if (cutoff_freq != 0)
			oversample = half_rate / cutoff_freq;
		double cutoff = rolloff_freq * oversample / half_rate;

		gen_sinc(out, offset, count, blip_res * oversample, treble, cutoff);

		// apply (half of) hamming window
		double to_fraction = Math.PI / (count - 1);
		for (int i = count; i-- > 0;)
			out[i + offset] *= 0.53836f - 0.46164f * Math.cos(i * to_fraction);
	}

}
