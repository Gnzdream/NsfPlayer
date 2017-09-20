package com.zdream.nsfplayer.xgm.device.sound;

import java.util.Arrays;

import com.zdream.nsfplayer.xgm.device.ISoundChip;
import com.zdream.nsfplayer.xgm.device.ITrackInfo;
import com.zdream.nsfplayer.xgm.device.IntHolder;

public class NesFDS implements ISoundChip {
	
	public static final int
			OPT_CUTOFF = 0,
			OPT_4085_RESET = 1,
			OPT_WRITE_PROTECT = 2,
			OPT_END = 3;
	
	public static final int
			TMOD = 0,
			TWAV = 1;
	public static final int
			EMOD = 0,
			EVOL = 1;
	
	final static int RC_BITS = 12;
	
	protected double rate, clock;
	protected int mask;
	/** stereo mix */
	protected int[] sm = new int[2];
	/** current output */
	protected int fout;
	protected TrackInfoFDS trkinfo;
	protected int[] option = new int[OPT_END];

	protected boolean master_io;
	/** unsigned */
	protected int master_vol;
	/** for trackinfo. unsigned */
	protected int last_freq;
	/** for trackinfo. unsigned */
	protected int last_vol;
	
	// two wavetables
	protected int[][] wave = new int[2][64];
	/** unsigned */
	protected int[] freq = new int[2];
	/** unsigned */
	protected int[] phase = new int[2];
	protected boolean wav_write;
	protected boolean wav_halt;
	protected boolean env_halt;
	protected boolean mod_halt;
	/** unsigned */
	protected int mod_pos;
	/** unsigned */
	protected int mod_write_pos;

	// two ramp envelopes
	protected boolean[] env_mode = new boolean[2];
	protected boolean[] env_disable = new boolean[2];
	/** unsigned */
	protected int[] env_timer = new int[2];
	/** unsigned */
	protected int[] env_speed = new int[2];
	/** unsigned */
	protected int[] env_out = new int[2];
	/** unsigned */
	protected int master_env_speed;

	// 1-pole RC lowpass filter
	protected int rc_accum;
	protected int rc_k;
	protected int rc_l;
	
	public NesFDS() {
		option[OPT_CUTOFF] = 2000;
		option[OPT_4085_RESET] = 0;
		option[OPT_WRITE_PROTECT] = 0; // not used here, see nsfplay.cpp

		rc_k = 0;
		rc_l = (1 << RC_BITS);

		setClock(DEFAULT_CLOCK);
		setRate(DEFAULT_RATE);
		sm[0] = 128;
		sm[1] = 128;

		reset();
	}

	@Override
	public void setStereoMix(int trk, int mixl, int mixr) {
		if (trk < 0)
			return;
		if (trk > 1)
			return;
		sm[0] = mixl;
		sm[1] = mixr;
	}

	@Override
	public ITrackInfo getTrackInfo(int trk) {
		trkinfo.maxVolume = 32;
		trkinfo.volume = last_vol;
		trkinfo.key = last_vol > 0;
		trkinfo._freq = last_freq;
		trkinfo.freq = ((double) (last_freq) * clock) / (65536.0 * 64.0);
		trkinfo.tone = env_out[EMOD];
		for (int i = 0; i < 64; i++)
			trkinfo.wave[i] = wave[TWAV][i];

		return trkinfo;
	}

	@Override
	public void setClock(double c) {
		clock = c;
	}

	@Override
	public void setRate(double r) {
		rate = r;

		// configure lowpass filter
		double cutoff = (double) option[OPT_CUTOFF];
		double leak = 0.0;
		if (cutoff > 0)
			leak = Math.exp(-2.0 * 3.14159 * cutoff / rate);
		rc_k = (int) (leak * (double) (1 << RC_BITS));
		rc_l = (1 << RC_BITS) - rc_k;
	}

	@Override
	public void setOption(int id, int val) {
		if (id < OPT_END)
			option[id] = val;

		// update cutoff immediately
		if (id == OPT_CUTOFF)
			setRate(rate);
	}

	@Override
	public void reset() {
		master_io = true;
		master_vol = 0;
		last_freq = 0;
		last_vol = 0;

		rc_accum = 0;

		for (int i = 0; i < 2; ++i) {
			Arrays.fill(wave[i], 0);
			freq[i] = 0;
			phase[i] = 0;
		}
		wav_write = false;
		wav_halt = true;
		env_halt = true;
		mod_halt = true;
		mod_pos = 0;
		mod_write_pos = 0;

		for (int i = 0; i < 2; ++i) {
			env_mode[i] = false;
			env_disable[i] = true;
			env_timer[i] = 0;
			env_speed[i] = 0;
			env_out[i] = 0;
		}
		master_env_speed = 0xFF;

		// NOTE: the FDS BIOS reset only does the following related to audio:
		// $4023 = $00
		// $4023 = $83 enables master_io
		// $4080 = $80 output volume = 0, envelope disabled
		// $408A = $E8 master envelope speed
		write(0x4023, 0x00, 0);
		write(0x4023, 0x83, 0);
		write(0x4080, 0x80, 0);
		write(0x408A, 0xE8, 0);

		// reset other stuff
		write(0x4082, 0x00, 0); // wav freq 0
		write(0x4083, 0x80, 0); // wav disable
		write(0x4084, 0x80, 0); // mod strength 0
		write(0x4085, 0x00, 0); // mod position 0
		write(0x4086, 0x00, 0); // mod freq 0
		write(0x4087, 0x80, 0); // mod disable
		write(0x4089, 0x00, 0); // wav write disable, max global volume}
	}

	@Override
	public void tick(int clocks) {
		// clock envelopes
		if (!env_halt && !wav_halt && (master_env_speed != 0)) {
			for (int i = 0; i < 2; ++i) {
				if (!env_disable[i]) {
					env_timer[i] += clocks;
					// unsigned
					int period = ((env_speed[i] + 1) * master_env_speed) << 3;
					while (env_timer[i] >= period) {
						// clock the envelope
						if (env_mode[i]) {
							if (env_out[i] < 32)
								++env_out[i];
						} else {
							if (env_out[i] > 0)
								--env_out[i];
						}
						env_timer[i] -= period;
					}
				}
			}
		}

		// clock the mod table
		if (!mod_halt) {
			// advance phase, adjust for modulator | unsigned
			int start_pos = phase[TMOD] >> 16;
			phase[TMOD] += (clocks * freq[TMOD]);
			// unsigned
			int end_pos = phase[TMOD] >> 16;

			// wrap the phase to the 64-step table (+ 16 bit accumulator)
			phase[TMOD] = phase[TMOD] & 0x3FFFFF;

			// execute all clocked steps
			for (int p = start_pos; p < end_pos; ++p) {
				int wv = wave[TMOD][p & 0x3F];
				if (wv == 4) // 4 resets mod position
					mod_pos = 0;
				else {
					final int BIAS[] = { 0, 1, 2, 4, 0, -4, -2, -1 };
					mod_pos += BIAS[wv];
					mod_pos &= 0x7F; // 7-bit clamp
				}
			}
		}

		// clock the wav table
		if (!wav_halt) {
			// complex mod calculation
			int mod = 0;
			if (env_out[EMOD] != 0) // skip if modulator off
			{
				// convert mod_pos to 7-bit signed
				int pos = (mod_pos < 64) ? mod_pos : (mod_pos - 128);

				// multiply pos by gain,
				// shift off 4 bits but with odd "rounding" behaviour
				int temp = pos * env_out[EMOD];
				int rem = temp & 0x0F;
				temp >>= 4;
				if ((rem > 0) && ((temp & 0x80) == 0)) {
					if (pos < 0)
						temp -= 1;
					else
						temp += 2;
				}

				// wrap if range is exceeded
				while (temp >= 192)
					temp -= 256;
				while (temp < -64)
					temp += 256;

				// multiply result by pitch,
				// shift off 6 bits, round to nearest
				temp = freq[TWAV] * temp;
				rem = temp & 0x3F;
				temp >>= 6;
				if (rem >= 32)
					temp += 1;

				mod = temp;
			}

			// advance wavetable position
			int f = freq[TWAV] + mod;
			phase[TWAV] = phase[TWAV] + (clocks * f);
			phase[TWAV] = phase[TWAV] & 0x3FFFFF; // wrap

			// store for trackinfo
			last_freq = f;
		}

		// output volume caps at 32
		int vol_out = env_out[EVOL];
		if (vol_out > 32)
			vol_out = 32;

		// final output
		if (!wav_write)
			fout = wave[TWAV][(phase[TWAV] >> 16) & 0x3F] * vol_out;

		// NOTE: during wav_halt, the unit still outputs (at phase 0)
		// and volume can affect it if the first sample is nonzero.
		// haven't worked out 100% of the conditions for volume to
		// effect (vol envelope does not seem to run, but am unsure)
		// but this implementation is very close to correct

		// store for trackinfo
		last_vol = vol_out;
	}

	@Override
	public int render(int[] bs) {
		// 8 bit approximation of master volume
		final double MASTER_VOL = 2.4 * 1223.0; // max FDS vol vs max APU square (arbitrarily 1223)
		final double MAX_OUT = 32.0f * 63.0f; // value that should map to master vol
		final int[] MASTER = { (int) ((MASTER_VOL / MAX_OUT) * 256.0 * 2.0f / 2.0f),
				(int) ((MASTER_VOL / MAX_OUT) * 256.0 * 2.0f / 3.0f),
				(int) ((MASTER_VOL / MAX_OUT) * 256.0 * 2.0f / 4.0f),
				(int) ((MASTER_VOL / MAX_OUT) * 256.0 * 2.0f / 5.0f) };

		int v = fout * MASTER[master_vol] >> 8;

		// lowpass RC filter
		int rc_out = ((rc_accum * rc_k) + (v * rc_l)) >> RC_BITS;
		rc_accum = rc_out;
		v = rc_out;

		// output mix
		int m = mask != 0 ? 0 : v;
		bs[0] = (m * sm[0]) >> 7;
		bs[1] = (m * sm[1]) >> 7;
		return 2;
	}

	@Override
	public boolean write(int adr, int val, int id) {
		// $4023 master I/O enable/disable
		if (adr == 0x4023) {
			master_io = ((val & 2) != 0);
			return true;
		}

		if (!master_io)
			return false;
		if (adr < 0x4040 || adr > 0x408A)
			return false;

		if (adr < 0x4080) // $4040-407F wave table write
		{
			if (wav_write)
				wave[TWAV][adr - 0x4040] = val & 0x3F;
			return true;
		}

		switch (adr & 0x00FF) {
		case 0x80: // $4080 volume envelope
			env_disable[EVOL] = ((val & 0x80) != 0);
			env_mode[EVOL] = ((val & 0x40) != 0);
			env_timer[EVOL] = 0;
			env_speed[EVOL] = val & 0x3F;
			if (env_disable[EVOL])
				env_out[EVOL] = env_speed[EVOL];
			return true;
		case 0x81: // $4081 ---
			return false;
		case 0x82: // $4082 wave frequency low
			freq[TWAV] = (freq[TWAV] & 0xF00) | val;
			return true;
		case 0x83: // $4083 wave frequency high / enables
			freq[TWAV] = (freq[TWAV] & 0x0FF) | ((val & 0x0F) << 8);
			wav_halt = ((val & 0x80) != 0);
			env_halt = ((val & 0x40) != 0);
			if (wav_halt)
				phase[TWAV] = 0;
			if (env_halt) {
				env_timer[EMOD] = 0;
				env_timer[EVOL] = 0;
			}
			return true;
		case 0x84: // $4084 mod envelope
			env_disable[EMOD] = ((val & 0x80) != 0);
			env_mode[EMOD] = ((val & 0x40) != 0);
			env_timer[EMOD] = 0;
			env_speed[EMOD] = val & 0x3F;
			if (env_disable[EMOD])
				env_out[EMOD] = env_speed[EMOD];
			return true;
		case 0x85: // $4085 mod position
			mod_pos = val & 0x7F;
			// not hardware accurate., but prevents detune due to cycle inaccuracies
			// (notably in Bio Miracle Bokutte Upa)
			if (option[OPT_4085_RESET] != 0)
				phase[TMOD] = mod_write_pos << 16;
			return true;
		case 0x86: // $4086 mod frequency low
			freq[TMOD] = (freq[TMOD] & 0xF00) | val;
			return true;
		case 0x87: // $4087 mod frequency high / enable
			freq[TMOD] = (freq[TMOD] & 0x0FF) | ((val & 0x0F) << 8);
			mod_halt = ((val & 0x80) != 0);
			if (mod_halt)
				phase[TMOD] = phase[TMOD] & 0x3F0000; // reset accumulator phase
			return true;
		case 0x88: // $4088 mod table write
			if (mod_halt) {
				// writes to current playback position (there is no direct way to set phase)
				wave[TMOD][(phase[TMOD] >> 16) & 0x3F] = val & 0x07;
				phase[TMOD] = (phase[TMOD] + 0x010000) & 0x3FFFFF;
				wave[TMOD][(phase[TMOD] >> 16) & 0x3F] = val & 0x07;
				phase[TMOD] = (phase[TMOD] + 0x010000) & 0x3FFFFF;
				mod_write_pos = phase[TMOD] >> 16; // used by OPT_4085_RESET
			}
			return true;
		case 0x89: // $4089 wave write enable, master volume
			wav_write = ((val & 0x80) != 0);
			master_vol = val & 0x03;
			return true;
		case 0x8A: // $408A envelope speed
			master_env_speed = val;
			// haven't tested whether this register resets phase on hardware,
			// but this ensures my inplementation won't spam envelope clocks
			// if this value suddenly goes low.
			env_timer[EMOD] = 0;
			env_timer[EVOL] = 0;
			return true;
		default:
			return false;
		}
	}

	@Override
	public boolean read(int adr, IntHolder val, int id) {
		if (adr >= 0x4040 && adr <= 0x407F) {
			// TODO: if wav_write is not enabled, the
			// read address may not be reliable? need
			// to test this on hardware.
			val.val = wave[TWAV][adr - 0x4040];
			return true;
		}

		if (adr == 0x4090) // $4090 read volume envelope
		{
			val.val = env_out[EVOL] | 0x40;
			return true;
		}

		if (adr == 0x4092) // $4092 read mod envelope
		{
			val.val = env_out[EMOD] | 0x40;
			return true;
		}

		return false;
	}

	@Override
	public void setMask(int m) {
		mask = m & 1;
	}

}
