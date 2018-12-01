package zdream.nsfplayer.mixer.blip;

public class BufferContext {

	/**
	 * <p>输出的采样是带符号的 16 位浮点数, 范围在 [-32767, 32767]
	 * <p>Output samples are 16-bit signed, with a range of -32767 to 32767
	 */
	static final int blip_sample_max = 32767;

	static final int buffer_extra = 18;
	
	static final int blip_max_length = 0;
	
	static final int blip_default_length = 250;
	
	static final int blip_sample_bits = 30;
	
	static final int blip_widest_impulse_ = 16;
	
	static final int blip_res = 64;
	
	static void gen_sinc( float[] out, int offset, int count, double oversample, double treble, double cutoff ) {
		if ( cutoff >= 0.999 )
			cutoff = 0.999;
		
		if ( treble < -300.0 )
			treble = -300.0;
		if ( treble > 5.0 )
			treble = 5.0;
		
		final double maxh = 4096.0;
		final double rolloff = Math.pow( 10.0, 1.0 / (maxh * 20.0) * treble / (1.0 - cutoff) );
		final double pow_a_n = Math.pow( rolloff, maxh - maxh * cutoff );
		final double to_angle = Math.PI / 2 / maxh / oversample;
		for ( int i = 0; i < count; i++ )
		{
			double angle = ((i - count) * 2 + 1) * to_angle;
			double c = rolloff * Math.cos( (maxh - 1.0) * angle ) - Math.cos( maxh * angle );
			double cos_nc_angle = Math.cos( maxh * cutoff * angle );
			double cos_nc1_angle = Math.cos( (maxh * cutoff - 1.0) * angle );
			double cos_angle = Math.cos( angle );
			
			c = c * pow_a_n - rolloff * cos_nc1_angle + cos_nc_angle;
			double d = 1.0 + rolloff * (rolloff - cos_angle - cos_angle);
			double b = 2.0 - cos_angle - cos_angle;
			double a = 1.0 - cos_angle - cos_nc_angle + cos_nc1_angle;
			
			out[offset + i] = (float) ((a * d + c * b) / (b * d)); // a / b + c / d
		}
	}

}
