package zdream.nsfplayer.xgm.device.sound;

import zdream.nsfplayer.nsf.device.cpu.IntHolder;
import zdream.nsfplayer.nsf.device.cpu.NesCPU;
import zdream.nsfplayer.xgm.device.ISoundChip;
import zdream.nsfplayer.xgm.device.ITrackInfo;
import zdream.nsfplayer.xgm.device.TrackInfoBasic;

public class NesMMC5 implements ISoundChip, IDeviceValue {
	
	public static final int
			OPT_NONLINEAR_MIXER = 0,
			OPT_PHASE_REFRESH = 1,
			OPT_END = 2;
	
	protected int[] option = new int[OPT_END];
	protected int mask;
	/** stereo panning */
	protected int[][] sm = new int[2][3];
	/** unsigned byte */
	protected int[] ram = new int[0x6000 - 0x5c00];
	/** unsigned byte */
	protected int[] reg = new int[8];
	/** unsigned byte */
	protected int[] mreg = new int[2];
	/** PCM channel, unsigned byte */
	protected int pcm;
	/** PCM channel */
	protected boolean pcm_mode;
	/** PCM channel reads need CPU access */
	protected NesCPU cpu;

	/** frequency divider, unsigned */
	protected int[] scounter = new int[2];
	/** phase counter, unsigned */
	protected int[] sphase = new int[2];

	/** unsigned */
	protected int[] duty = new int[2];
	/** unsigned */
	protected int[] volume = new int[2];
	/** unsigned */
	protected int[] freq = new int[2];
	protected int[] out = new int[3];
	protected boolean[] enable = new boolean[2];

	protected boolean[] envelope_disable = new boolean[2]; // 包络启用标志
	protected boolean[] envelope_loop = new boolean[2]; // 包络循环标志
	protected boolean[] envelope_write = new boolean[2];
	protected int[] envelope_div_period = new int[2];
	protected int[] envelope_div = new int[2];
	protected int[] envelope_counter = new int[2];

	protected int[] length_counter = new int[2];

	protected int frame_sequence_count;

	protected double clock, rate;
	// int calc_sqr (int i, UINT32 clocks);
	protected int[] square_table = new int[32];
	protected int[] pcm_table = new int[256];
	protected TrackInfoBasic[] trkinfo = new TrackInfoBasic[3];

	{
		for (int i = 0; i < trkinfo.length; i++) {
			trkinfo[i] = new TrackInfoBasic();
		}
	}
	
	public NesMMC5() {
		cpu = null;
		setClock(DEFAULT_CLOCK);
		setRate(DEFAULT_RATE);
		option[OPT_NONLINEAR_MIXER] = 1; // true
		option[OPT_PHASE_REFRESH] = 1; // true
		frame_sequence_count = 0;

		// square nonlinear mix, same as 2A03
		square_table[0] = 0;
		for (int i = 1; i < 32; i++)
			square_table[i] = (int) ((8192.0 * 95.88) / (8128.0 / i + 100));

		// 2A03 style nonlinear pcm mix with double the bits
		// pcm_table[0] = 0;
		// INT32 wd = 22638;
		// for(int d=1;d<256; ++d)
		// pcm_table[d] = (INT32)((8192.0*159.79)/(100.0+1.0/((double)d/wd)));

		// linear pcm mix (actual hardware seems closer to this)
		pcm_table[0] = 0;
		double pcm_scale = 32.0;
		for (int d = 1; d < 256; ++d)
			pcm_table[d] = (int) ((double) d * pcm_scale);

		// stereo mix
		for (int c = 0; c < 2; ++c)
			for (int t = 0; t < 3; ++t)
				sm[c][t] = 128;
	}

	@Override
	public void reset() {
		int i;

		scounter[0] = 0;
		scounter[1] = 0;
		sphase[0] = 0;
		sphase[1] = 0;

		envelope_div[0] = 0;
		envelope_div[1] = 0;
		length_counter[0] = 0;
		length_counter[1] = 0;
		envelope_counter[0] = 0;
		envelope_counter[1] = 0;
		frame_sequence_count = 0;

		for (i = 0; i < 8; i++)
			write(0x5000 + i, 0, 0);

		write(0x5015, 0, 0);

		for (i = 0; i < 3; ++i)
			out[i] = 0;

		mask = 0;
		pcm = 0; // PCM channel
		pcm_mode = false; // write mode

		setRate(rate);
	}

	@Override
	public void setOption(int id, int val) {
		if (id<OPT_END)
			option[id] = val;
	}

	@Override
	public void setClock(double c) {
		clock = c;
	}

	@Override
	public void setRate(double r) {
		rate = (r != 0) ? r : DEFAULT_RATE;
	}
	
	public final void frameSequence() {
		// 240hz clock
		for (int i = 0; i < 2; ++i) {
			boolean divider = false;
			if (envelope_write[i]) {
				envelope_write[i] = false;
				envelope_counter[i] = 15;
				envelope_div[i] = 0;
			} else {
				++envelope_div[i];
				if (envelope_div[i] > envelope_div_period[i]) {
					divider = true;
					envelope_div[i] = 0;
				}
			}
			if (divider) {
				if (envelope_loop[i] && envelope_counter[i] == 0)
					envelope_counter[i] = 15;
				else if (envelope_counter[i] > 0)
					--envelope_counter[i];
			}
		}

		// MMC5 length counter is clocked at 240hz, unlike 2A03
		for (int i = 0; i < 2; ++i) {
			if (!envelope_loop[i] && (length_counter[i] > 0))
				--length_counter[i];
		}
	}
	
	public final int calc_sqr(int i, int clocks) {
		final int sqrtbl[][] = { { 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0 },
				{ 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }
				};

		scounter[i] += clocks;
		while (scounter[i] > freq[i]) {
			sphase[i] = (sphase[i] + 1) & 15;
			scounter[i] -= (freq[i] + 1);
		}

		int ret = 0;
		if (length_counter[i] > 0) {
			// note MMC5 does not silence the highest 8 frequencies like APU,
			// because this is done by the sweep unit.

			int v = envelope_disable[i] ? volume[i] : envelope_counter[i];
			ret = sqrtbl[duty[i]][sphase[i]] != 0 ? v : 0;
		}

		return ret;
	}
	
	/**
	 * @param clocks
	 *   unsigned
	 */
	public final void tickFrameSequence(int clocks) {
		frame_sequence_count += clocks;
		while (frame_sequence_count > 7458) {
			frameSequence();
			frame_sequence_count -= 7458;
		}
	}

	@Override
	public void tick(int clocks) {
		out[0] = calc_sqr(0, clocks);
	    out[1] = calc_sqr(1, clocks);
	    out[2] = pcm;
	}

	@Override
	public int render(int[] bs) {
		out[0] = (mask & 1) != 0 ? 0 : out[0];
		out[1] = (mask & 2) != 0 ? 0 : out[1];
		out[2] = (mask & 4) != 0 ? 0 : out[2];

		int[] m = new int[3];

		if (option[OPT_NONLINEAR_MIXER] != 0) {
			// squares nonlinear
			int voltage = square_table[out[0] + out[1]];
			m[0] = out[0] << 6;
			m[1] = out[1] << 6;
			int ref = m[0] + m[1];
			if (ref > 0) {
				m[0] = (m[0] * voltage) / ref;
				m[1] = (m[1] * voltage) / ref;
			} else {
				m[0] = voltage;
				m[1] = voltage;
			}

			// pcm nonlinear
			m[2] = pcm_table[out[2]];
		} else {
			// squares
			m[0] = out[0] << 6;
			m[1] = out[1] << 6;

			// pcm channel
			m[2] = out[2] << 5;
		}

		// note polarity is flipped on output

		bs[0] = m[0] * -sm[0][0];
		bs[0] += m[1] * -sm[0][1];
		bs[0] += m[2] * -sm[0][2];
		bs[0] >>= 7;

		bs[1] = m[0] * -sm[1][0];
		bs[1] += m[1] * -sm[1][1];
		bs[1] += m[2] * -sm[1][2];
		bs[1] >>= 7;

		return 2;
	}

	@Override
	public boolean write(int adr, int val, int id) {
		int ch;

	    final int length_table[] = {
	        0x0A, 0xFE,
	        0x14, 0x02,
	        0x28, 0x04,
	        0x50, 0x06,
	        0xA0, 0x08,
	        0x3C, 0x0A,
	        0x0E, 0x0C,
	        0x1A, 0x0E,
	        0x0C, 0x10,
	        0x18, 0x12,
	        0x30, 0x14,
	        0x60, 0x16,
	        0xC0, 0x18,
	        0x48, 0x1A,
	        0x10, 0x1C,
	        0x20, 0x1E
	    };

		if ((0x5c00 <= adr) && (adr < 0x5ff0)) {
			ram[adr & 0x3ff] = val;
			return true;
		} else if ((0x5000 <= adr) && (adr < 0x5008)) {
			reg[adr & 0x7] = val;
		}

		switch (adr) {
		case 0x5000:
		case 0x5004:
			ch = (adr >> 2) & 1;
			volume[ch] = val & 15;
			envelope_disable[ch] = ((val >> 4) & 1) != 0;
			envelope_loop[ch] = ((val >> 5) & 1) != 0;
			envelope_div_period[ch] = (val & 15);
			duty[ch] = (val >> 6) & 3;
			break;

		case 0x5002:
		case 0x5006:
			ch = (adr >> 2) & 1;
			freq[ch] = val + (freq[ch] & 0x700);
			if (scounter[ch] > freq[ch])
				scounter[ch] = freq[ch];
			break;

		case 0x5003:
		case 0x5007:
			ch = (adr >> 2) & 1;
			freq[ch] = (freq[ch] & 0xff) + ((val & 7) << 8);
			if (scounter[ch] > freq[ch])
				scounter[ch] = freq[ch];
			// phase reset
			if (option[OPT_PHASE_REFRESH] != 0)
				sphase[ch] = 0;
			envelope_write[ch] = true;
			if (enable[ch]) {
				length_counter[ch] = length_table[(val >> 3) & 0x1f];
			}
			break;

		// PCM channel control
		case 0x5010:
			pcm_mode = ((val & 1) != 0); // 0 = write, 1 = read
			break;

		// PCM channel control
		case 0x5011:
			if (!pcm_mode) {
				val &= 0xFF;
				if (val != 0)
					pcm = val;
			}
			break;

		case 0x5015:
			enable[0] = (val & 1) != 0 ? true : false;
			enable[1] = (val & 2) != 0 ? true : false;
			if (!enable[0])
				length_counter[0] = 0;
			if (!enable[1])
				length_counter[1] = 0;
			break;

		case 0x5205:
			mreg[0] = val;
			break;

		case 0x5206:
			mreg[1] = val;
			break;

		default:
			return false;

		}
		return true;
	}

	@Override
	public boolean read(int adr, IntHolder val, int id) {
		// in PCM read mode, reads from $8000-$C000 automatically load the PCM output
		if (pcm_mode && (0x8000 <= adr) && (adr < 0xC000) && cpu != null) {
			pcm_mode = false; // prevent recursive entry
			IntHolder pcm_read = new IntHolder(0);
			cpu.read(adr, pcm_read, 0);
			pcm_read.val &= 0xFF;
			if (pcm_read.val != 0)
				pcm = pcm_read.val;
			pcm_mode = true;
		}

		if ((0x5000 <= adr) && (adr < 0x5008)) {
			val.val = reg[adr & 0x7];
			return true;
		} else if (adr == 0x5015) {
			val.val = (enable[1] ? 2 : 0) | (enable[0] ? 1 : 0);
			return true;
		}

		if ((0x5c00 <= adr) && (adr < 0x5ff0)) {
			val.val = ram[adr & 0x3ff];
			return true;
		} else if (adr == 0x5205) {
			val.val = (mreg[0] * mreg[1]) & 0xff;
			return true;
		} else if (adr == 0x5206) {
			val.val = (mreg[0] * mreg[1]) >> 8;
			return true;
		}

		return false;
	}

	@Override
	public void setStereoMix(int trk, int mixl, int mixr) {
		if (trk < 0)
			return;
		if (trk > 2)
			return;
		sm[0][trk] = mixl;
		sm[1][trk] = mixr;
	}
	
	@Override
	public ITrackInfo getTrackInfo(int trk) {
		assert (trk < 3);

		if (trk < 2) // square
		{
			trkinfo[trk]._freq = freq[trk];
			if (freq[trk] != 0)
				trkinfo[trk].freq = clock / 16 / (freq[trk] + 1);
			else
				trkinfo[trk].freq = 0;

			trkinfo[trk].output = out[trk];
			trkinfo[trk].maxVolume = 15;
			trkinfo[trk].volume = volume[trk] + (envelope_disable[trk] ? 0 : 0x10);
			trkinfo[trk].key = (envelope_disable[trk] ? (volume[trk] > 0) : (envelope_counter[trk] > 0));
			trkinfo[trk].tone = duty[trk];
		} else // pcm
		{
			trkinfo[trk]._freq = 0;
			trkinfo[trk].freq = 0;
			trkinfo[trk].output = out[2];
			trkinfo[trk].maxVolume = 255;
			trkinfo[trk].volume = pcm;
			trkinfo[trk].key = false;
			trkinfo[trk].tone = pcm_mode ? 1 : 0;
		}

		return trkinfo[trk];
	}

	@Override
	public void setMask(int m) {
		mask = m;
	}
	
	public final void setCPU(NesCPU cpu_) {
		cpu = cpu_;
	}

}
