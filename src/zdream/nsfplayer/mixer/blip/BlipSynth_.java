package zdream.nsfplayer.mixer.blip;

import static zdream.nsfplayer.mixer.blip.BufferContext.*;

public class BlipSynth_ {
	
	double volume_unit_;
	final short[] impulses;
	final int width;
	long kernel_unit;
	int impulses_size() { return blip_res / 2 * width + 1; }
	
	void adjust_impulse() {
		// sum pairs for each phase and add error correction to end of first half
		final int size = impulses_size();
		for (int p = blip_res; p-- >= blip_res / 2;) {
			int p2 = blip_res - 2 - p;
			long error = kernel_unit;
			for (int i = 1; i < size; i += blip_res) {
				error -= impulses[i + p];
				error -= impulses[i + p2];
			}
			if (p == p2)
				error /= 2; // phase = 0.5 impulse uses same half for both sides
			impulses[size - blip_res + p] += (short) error;
			// printf( "error: %ld\n", error );
		}
	}
	
	public BlipBuffer buf;
	public int last_amp;
	public int delta_factor;
	
	BlipSynth_( short[] impulses, int width ) {
		this.impulses = impulses;
		this.width = width;
	}
	
	public void trebleEq(final BlipEQ eq) {
		float[] fimpulse = new float[blip_res / 2 * (blip_widest_impulse_ - 1) + blip_res * 2];

		final int half_size = blip_res / 2 * (width - 1);
		eq.generate(fimpulse, blip_res, half_size);

		int i;

		// need mirror slightly past center for calculation
		for (i = blip_res; i-- > 0;)
			fimpulse[blip_res + half_size + i] = fimpulse[blip_res + half_size - 1 - i];

		// starts at 0
		for (i = 0; i < blip_res; i++)
			fimpulse[i] = 0.0f;

		// find rescale factor
		double total = 0.0;
		for (i = 0; i < half_size; i++)
			total += fimpulse[blip_res + i];

		// double const base_unit = 44800.0 - 128 * 18; // allows treble up to +0 dB
		// double const base_unit = 37888.0; // allows treble to +5 dB
		final double base_unit = 32768.0; // necessary for blip_unscaled to work
		double rescale = base_unit / 2 / total;
		kernel_unit = (long) base_unit;

		// integrate, first difference, rescale, convert to int
		double sum = 0.0;
		double next = 0.0;
		final int impulses_size = this.impulses_size();
		for (i = 0; i < impulses_size; i++) {
			impulses[i] = (short) Math.floor((next - sum) * rescale + 0.5);
			sum += fimpulse[i];
			next += fimpulse[i + blip_res];
		}
		adjust_impulse();

		// volume might require rescaling
		double vol = volume_unit_;
		if (vol != 0) {
			volume_unit_ = 0.0;
			volume_unit(vol);
		}
	}
	
	public void volume_unit(double new_unit) {
		if (new_unit != volume_unit_) {
			// use default eq if it hasn't been set yet
			if (kernel_unit == 0)
				trebleEq(new BlipEQ(-8.0));

			volume_unit_ = new_unit;
			double factor = new_unit * (1L << blip_sample_bits) / kernel_unit;

			if (factor > 0.0) {
				int shift = 0;

				// if unit is really small, might need to attenuate kernel
				while (factor < 2.0) {
					shift++;
					factor *= 2.0;
				}

				if (shift != 0) {
					kernel_unit >>= shift;
					assert (kernel_unit > 0); // fails if volume unit is too low

					// keep values positive to avoid round-towards-zero of sign-preserving
					// right shift for negative values
					long offset = 0x8000 + (1 << (shift - 1));
					long offset2 = 0x8000 >> shift;
					for (int i = impulses_size(); i-- > 0;)
						impulses[i] = (short) (((impulses[i] + offset) >> shift) - offset2);
					adjust_impulse();
				}
			}
			delta_factor = (int) Math.floor(factor + 0.5);
		}
	}

}
