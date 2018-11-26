package zdream.nsfplayer.xgm.device.sound;

import zdream.nsfplayer.nsf.device.IDevice;
import zdream.nsfplayer.nsf.device.cpu.IntHolder;
import zdream.nsfplayer.xgm.device.ISoundChip;
import zdream.nsfplayer.xgm.device.ITrackInfo;
import zdream.nsfplayer.xgm.device.TrackInfoBasic;

/**
 * Bottom Half of APU
 * @author Zdream
 */
public class NesDMC implements ISoundChip, IFrameSequencer, IDeviceValue {
	
	public static final int
			OPT_ENABLE_4011 = 0,
			OPT_ENABLE_PNOISE = 1,
			OPT_UNMUTE_ON_RESET = 2,
			OPT_DPCM_ANTI_CLICK = 3,
			OPT_NONLINEAR_MIXER = 4,
			OPT_RANDOMIZE_NOISE = 5,
			OPT_TRI_MUTE = 6,
			OPT_END = 7;
	
	protected static final int wavlen_table[][] = { // [2][16]
			  { // NTSC
				4, 8, 16, 32, 64, 96, 128, 160, 202, 254, 380, 508, 762, 1016, 2034, 4068
			  },
			  { // PAL
				4, 8, 14, 30, 60, 88, 118, 148, 188, 236, 354, 472, 708,  944, 1890, 3778
			  }};

	protected static final int freq_table[][] = { // [2][16]
			  { // NTSC
				428, 380, 340, 320, 286, 254, 226, 214, 190, 160, 142, 128, 106, 84, 72, 54
			  },
			  { // PAL
				398, 354, 316, 298, 276, 236, 210, 198, 176, 148, 132, 118,  98, 78, 66, 50
			  }};
	
	/**
	 * A: NTSC=0, PAL=1
	 */
	protected int[][][][] tnd_table = new int[2][16][16][128];
	
	protected int[] option = new int[OPT_END];
	protected int mask;
	protected int[][] sm = new int[2][3];
	/** unsigned byte */
	protected int[] reg = new int[0x10];
	protected int len_reg;
	protected int adr_reg;
	IDevice memory;
	/** unsigned */
	protected int[] out = new int[3];
	protected int daddress;
	/** unsigned */
	protected int length;
	/** unsigned */
	protected int data;
	protected int damp;
	protected int dac_lsb;
	protected boolean dmc_pop;
	protected int dmc_pop_offset;
	protected int dmc_pop_follow;
	protected double clock;
	/** unsigned */
	protected int rate;
	protected int mode;
	protected boolean irq;
	protected boolean active;

	/**
	 * frequency dividers<br>
	 * unsigned
	 */
	protected int[] counter = new int[3];
	/**
	 * triangle phase
	 */
	protected int tphase;
	/**
	 * noise frequency<br>
	 * unsigned
	 */
	protected int nfreq;
	/**
	 * DPCM frequency<br>
	 * unsigned
	 */
	protected int dfreq;

	/** unsigned */
	protected int tri_freq;
	protected int linear_counter;
	protected int linear_counter_reload;
	protected boolean linear_counter_halt;
	protected boolean linear_counter_control;

	protected int noise_volume;
	/** unsigned */
	protected int noise, noise_tap;

	// noise envelope
	protected boolean envelope_loop;
	protected boolean envelope_disable;
	protected boolean envelope_write;
	protected int envelope_div_period;
	protected int envelope_div;
	protected int envelope_counter;

	protected boolean[] enable = new boolean[3];
	/**
	 * 0=tri, 1=noise
	 */
	protected int[] length_counter = new int[2];

	protected TrackInfoBasic[] trkinfo = new TrackInfoBasic[3];

	// frame sequencer
	/**
	 * apu is clocked by DMC's frame sequencer
	 */
//	protected NesAPU apu;
	/**
	 * current cycle count
	 */
	//protected int frame_sequence_count;
	/**
	 * CPU cycles per FrameSequence
	 */
	//protected int frame_sequence_length;
	/**
	 * current step of frame sequence
	 */
	//protected int frame_sequence_step;
	/**
	 * 4/5 steps per frame
	 */
	//protected int frame_sequence_steps;
//	protected boolean frame_irq;
//	protected boolean frame_irq_enable;
	
	/**
	 * frame sequencer 的大部分参数已经封装成 {@link FrameSequenceCounter} 这个类中.
	 */
	FrameSequenceCounter frameCounter;
	
//	private Random rand = new Random();
	
	{
		trkinfo[0] = new TrackInfoBasic();
		trkinfo[1] = new TrackInfoBasic();
		trkinfo[2] = new TrackInfoBasic();
	}
	
	public NesDMC() {
		setClock(DEFAULT_CLOCK);
		setRate(DEFAULT_RATE);
		option[OPT_ENABLE_4011] = 1;
		option[OPT_ENABLE_PNOISE] = 1;
		option[OPT_UNMUTE_ON_RESET] = 1;
		option[OPT_DPCM_ANTI_CLICK] = 0;
		option[OPT_NONLINEAR_MIXER] = 1;
		option[OPT_RANDOMIZE_NOISE] = 1;
		option[OPT_TRI_MUTE] = 1;
		tnd_table[0][0][0][0] = 0;
		tnd_table[1][0][0][0] = 0;

		for (int c = 0; c < 2; ++c)
			for (int t = 0; t < 3; ++t)
				sm[c][t] = 128;
	}
	
	public int getDamp() {
		return (damp << 1) | dac_lsb;
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
	
	public void setFrameCounter(FrameSequenceCounter frameCounter) {
		this.frameCounter = frameCounter;
	}

	@Override
	public ITrackInfo getTrackInfo(int trk) {
		int pal = frameCounter.pal;
		
		switch (trk) {
		case 0:
			trkinfo[trk].maxVolume = 255;
			trkinfo[0].key = (linear_counter > 0 && length_counter[0] > 0 && enable[0]);
			trkinfo[0].volume = 0;
			trkinfo[0]._freq = tri_freq;
			if (trkinfo[0]._freq != 0)
				trkinfo[0].freq = clock / 32 / (trkinfo[0]._freq + 1);
			else
				trkinfo[0].freq = 0;
			trkinfo[0].tone = -1;
			trkinfo[0].output = out[0];
			break;
		case 1:
			trkinfo[1].maxVolume = 15;
			trkinfo[1].volume = noise_volume + (envelope_disable ? 0 : 0x10) + (envelope_loop ? 0x20 : 0);
			trkinfo[1].key = length_counter[1] > 0 && enable[1]
					&& (envelope_disable ? (noise_volume > 0) : (envelope_counter > 0));
			trkinfo[1]._freq = reg[0x400e - 0x4008] & 0xF;
			trkinfo[1].freq = clock
					/ (double) (wavlen_table[pal][trkinfo[1]._freq] * (((noise_tap & (1 << 6)) != 0) ? 93 : 1));
			trkinfo[1].tone = noise_tap & (1 << 6);
			trkinfo[1].output = out[1];
			break;
		case 2:
			trkinfo[2].maxVolume = 127;
			trkinfo[2].volume = reg[0x4011 - 0x4008] & 0x7F;
			trkinfo[2].key = active;
			trkinfo[2]._freq = reg[0x4010 - 0x4008] & 0xF;
			trkinfo[2].freq = clock / (double) (freq_table[pal][trkinfo[2]._freq]);
			trkinfo[2].tone = (0xc000 | (adr_reg << 6));
			trkinfo[2].output = (damp << 1) | dac_lsb;
			break;
		default:
			return null;
		}
		return trkinfo[trk];
	}
	
	@Override
	public final void frameSequence(int s) {
		// DEBUG_OUT("FrameSequence: %d\n",s);

		if (s > 3)
			return; // no operation in step 4

		// 240hz clock
		{
			// triangle linear counter
			if (linear_counter_halt) {
				linear_counter = linear_counter_reload;
			} else {
				if (linear_counter > 0)
					--linear_counter;
			}
			if (!linear_counter_control) {
				linear_counter_halt = false;
			}

			// noise envelope
			boolean divider = false;
			if (envelope_write) {
				envelope_write = false;
				envelope_counter = 15;
				envelope_div = 0;
			} else {
				++envelope_div;
				if (envelope_div > envelope_div_period) {
					divider = true;
					envelope_div = 0;
				}
			}
			if (divider) {
				if (envelope_loop && envelope_counter == 0)
					envelope_counter = 15;
				else if (envelope_counter > 0)
					--envelope_counter;
			}
		}

		// 120hz clock
		if ((s & 1) == 0) {
			// triangle length counter
			if (!linear_counter_control && (length_counter[0] > 0))
				--length_counter[0];

			// noise length counter
			if (!envelope_loop && (length_counter[1] > 0))
				--length_counter[1];
		}
	}

	/**
	 * 三角浪频道的计算返回值在 0 - 15 之间
	 */
	public final int calc_tri (int clocks) {
		int tritbl[] = 
		{ // len : 32
		  0, 1, 2, 3, 4, 5, 6, 7,
		  8, 9,10,11,12,13,14,15,
		 15,14,13,12,11,10, 9, 8,
		  7, 6, 5, 4, 3, 2, 1, 0
		};

		if (linear_counter > 0 && length_counter[0] > 0 && (option[OPT_TRI_MUTE] == 0 || tri_freq > 0)) {
			counter[0] += clocks;
			while (counter[0] > tri_freq) {
				tphase = (tphase + 1) & 31;
				counter[0] -= (tri_freq + 1);
			}
		}

		int ret = tritbl[tphase];
		return ret;
	}
	
	/**
	 * 噪声信道的计算值为 0 - 127
	 * 由于低抽样的合成的话，因为被替换的噪音激烈
	 * 只有在这个函数内高时钟合成，简单的抽样率变换。
	 * @param clocks
	 *   unsigned
	 * @return
	 *   unsigned
	 */
	public final int calc_noise(int clocks) {
		int env = envelope_disable ? noise_volume : envelope_counter; // unsigned
		if (length_counter[1] < 1)
			env = 0;

		int last = (noise & 0x4000) != 0 ? env : 0;
		if (clocks < 1)
			return last;

		// simple anti-aliasing (noise requires it, even when oversampling is off)
		int count = 0; // unsigned
		int accum = 0; // unsigned

		counter[1] += clocks;
		assert (nfreq > 0); // prevent infinite loop
		while (counter[1] >= nfreq) {
			// tick the noise generator
			int feedback = (noise & 1) ^ ((noise & noise_tap) != 0 ? 1 : 0); // unsigned
			noise = (noise >> 1) | (feedback << 14);

			++count;
			accum += last;
			last = (noise & 0x4000) != 0 ? env : 0;

			counter[1] -= nfreq;
		}

		if (count < 1) // no change over interval, don't anti-alias
		{
			return last;
		}

		int clocks_accum = clocks - counter[1]; // unsigned
		// count = number of samples in accum
		// counter[1] = number of clocks since last sample

		accum = (accum * clocks_accum) + (last * counter[1] * count);
		// note accum as an average is already premultiplied by count
		
		return accum / (clocks * count);
	}
	
	/**
	 * DMC 频道的计算返回值为 0 - 127
	 * @param clocks
	 *   unsigned
	 * @return
	 *   unsigned
	 */
	public final int calc_dmc(int clocks) {
		counter[2] += clocks;
		assert (dfreq > 0); // prevent infinite loop
		while (counter[2] >= dfreq) {
			if (data != 0x100) { // data = 0x100 是 EMPTY 的意思
				if ((data & 1) != 0 && (damp < 63))
					damp++;
				else if ((data & 1) == 0 && (0 < damp))
					damp--;
				data >>= 1;
			}

			if (data == 0x100 && active) {
				IntHolder val = new IntHolder(data);
				memory.read(daddress, val, 0);
				data = val.val;
				data |= (data & 0xFF) | 0x10000; // 8 个 bit 移动
				if (length > 0) {
					daddress = ((daddress + 1) & 0xFFFF) | 0x8000;
					length--;
				}

			}

			if (length == 0) { // 最后的抽取结束（播放完毕前）马上结束处理
				if ((mode & 1) != 0) {
					daddress = ((adr_reg << 6) | 0xC000);
					length = (len_reg << 4) + 1;
				} else {
					irq = (mode == 2 && active) ? true : false; // 之前有 active 了的时候 IRQ 发动
					active = false;
				}
			}

			counter[2] -= dfreq;
		}

		return (damp << 1) + dac_lsb;
	}
	
	

	@Override
	public void tick(int clocks) {
		out[0] = calc_tri(clocks);
		out[1] = calc_noise(clocks);
		out[2] = calc_dmc(clocks);
	}

	@Override
	public int render(int[] bs) {
		out[0] = (mask & 1) != 0 ? 0 : out[0];
		out[1] = (mask & 2) != 0 ? 0 : out[1];
		out[2] = (mask & 4) != 0 ? 0 : out[2];

		int m0, m1, m2;
		m0 = tnd_table[0][out[0]][0][0];
		m1 = tnd_table[0][0][out[1]][0];
		m2 = tnd_table[0][0][0][out[2]];

		if (option[OPT_NONLINEAR_MIXER] != 0) {
			int ref = m0 + m1 + m2;
			int voltage = tnd_table[1][out[0]][out[1]][out[2]];
			if (ref != 0) {
				m0 = (m0 * voltage) / ref;
				m1 = (m1 * voltage) / ref;
				m2 = (m2 * voltage) / ref;
			} else {
				m0 = voltage;
				m1 = voltage;
				m2 = voltage;
			}
		}

		// anti-click nullifies any 4011 write but preserves nonlinearity
		if (option[OPT_DPCM_ANTI_CLICK] != 0) {
			if (dmc_pop) // $4011 will cause pop this frame
			{
				// adjust offset to counteract pop
				dmc_pop_offset += dmc_pop_follow - m2;
				dmc_pop = false;

				// prevent overflow, keep headspace at edges
				final int OFFSET_MAX = (1 << 30) - (4 << 16);
				if (dmc_pop_offset > OFFSET_MAX)
					dmc_pop_offset = OFFSET_MAX;
				if (dmc_pop_offset < -OFFSET_MAX)
					dmc_pop_offset = -OFFSET_MAX;
			}
			dmc_pop_follow = m2; // remember previous position

			m2 += dmc_pop_offset; // apply offset

			// TODO implement this in a better way
			// roll off offset (not ideal, but prevents overflow)
			if (dmc_pop_offset > 0)
				--dmc_pop_offset;
			else if (dmc_pop_offset < 0)
				++dmc_pop_offset;
		}

		bs[0] = m0 * sm[0][0];
		bs[0] += m1 * sm[0][1];
		bs[0] += m2 * sm[0][2];
		bs[0] >>= 7;

		bs[1] = m0 * sm[1][0];
		bs[1] += m1 * sm[1][1];
		bs[1] += m2 * sm[1][2];
		bs[1] >>= 7;

		return 2;
	}

	@Override
	public void setClock(double c) {
		clock = c;
	}

	@Override
	public void setRate(double r) {
		if (r == 0) {
			rate = DEFAULT_RATE;
		}
		rate = (int) (r > 0 ? r : -r);
	}
	
	public final void initializeTNDTable(double wt, double wn, double wd) {

		// volume adjusted by 0.75 based on empirical measurements
		final double MASTER = 8192.0 * 0.75;
		// truthfully, the nonlinear curve does not appear to match well
		// with my tests, triangle in particular seems too quiet relatively.
		// do more testing of the APU/DMC DAC later

		{ // Linear Mixer
			for (int t = 0; t < 16; t++) {
				for (int n = 0; n < 16; n++) {
					for (int d = 0; d < 128; d++) {
						tnd_table[0][t][n][d] = (int) (MASTER * (3.0 * t + 2.0 * n + d) / 208.0);
					}
				}
			}
		}
		{ // Non-Linear Mixer
			tnd_table[1][0][0][0] = 0;
			for (int t = 0; t < 16; t++) {
				for (int n = 0; n < 16; n++) {
					for (int d = 0; d < 128; d++) {
						if (t != 0 || n != 0 || d != 0)
							tnd_table[1][t][n][d] = (int) ((MASTER * 159.79)
									/ (100.0 + 1.0 / ((double) t / wt + (double) n / wn + (double) d / wd)));
					}
				}
			}
		}

	}

	@Override
	public void reset() {
		int i;
		mask = 0;

		initializeTNDTable(8227, 12241, 22638);

		counter[0] = 0;
		counter[1] = 0;
		counter[2] = 0;
		tphase = 0;
		nfreq = wavlen_table[0][0];
		dfreq = freq_table[0][0];

		envelope_div = 0;
		length_counter[0] = 0;
		length_counter[1] = 0;
		envelope_counter = 0;

		for (i = 0; i < 0x10; i++)
			write(0x4008 + i, 0, 0);

		irq = false;
		write(0x4015, 0x00, 0);
		if (option[OPT_UNMUTE_ON_RESET] != 0)
			write(0x4015, 0x0f, 0);

		out[0] = out[1] = out[2] = 0;
		tri_freq = 0;
		damp = 0;
		dmc_pop = false;
		dmc_pop_offset = 0;
		dmc_pop_follow = 0;
		dac_lsb = 0;
		data = 0x100;
		adr_reg = 0;
		active = false;
		length = 0;
		len_reg = 0;
		daddress = 0;
		noise = 1;
		noise_tap = (1 << 1);
		/*if (option[OPT_RANDOMIZE_NOISE] != 0) {
			noise |= (rand.nextInt() & 0xFFFF);
		}*/

		setRate(rate);
	}
	
	public final void setMemory(IDevice r) {
		memory = r;
	}

	@Override
	public void setOption(int id, int val) {
		if (id < OPT_END) {
			option[id] = val;
			if (id == OPT_NONLINEAR_MIXER)
				initializeTNDTable(8227, 12241, 22638);
		}
	}

	@Override
	public boolean write(int adr, int val, int id) {
		final int length_table[] = { // len : 32
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

		if (adr == 0x4015) {
			enable[0] = (val & 4) != 0 ? true : false;
			enable[1] = (val & 8) != 0 ? true : false;

			if (!enable[0]) {
				length_counter[0] = 0;
			}
			if (!enable[1]) {
				length_counter[1] = 0;
			}

			if ((val & 16) == 0) {
				enable[2] = active = false;
			} else if (!active) {
				enable[2] = active = true;
				daddress = (0xC000 | (adr_reg << 6));
				length = (len_reg << 4) + 1;
				irq = false;
				
				if (length == 769) {
					length += 1;
				}
			}

			reg[adr - 0x4008] = val;
			return true;
		}

		if (adr < 0x4008 || 0x4013 < adr)
			return false;

		reg[adr - 0x4008] = val & 0xff;

		// DEBUG_OUT("$%04X %02X\n", adr, val);

		switch (adr) {

		// tri

		case 0x4008:
			linear_counter_control = ((val >> 7) & 1) != 0;
			linear_counter_reload = val & 0x7F;
			break;

		case 0x4009:
			break;

		case 0x400a:
			tri_freq = val | (tri_freq & 0x700);
			if (counter[0] > tri_freq)
				counter[0] = tri_freq;
			break;

		case 0x400b:
			tri_freq = (tri_freq & 0xff) | ((val & 0x7) << 8);
			if (counter[0] > tri_freq)
				counter[0] = tri_freq;
			linear_counter_halt = true;
			if (enable[0]) {
				length_counter[0] = length_table[(val >> 3) & 0x1f];
			}
			break;

		// noise

		case 0x400c:
			noise_volume = val & 15;
			envelope_div_period = val & 15;
			envelope_disable = ((val >> 4) & 1) != 0;
			envelope_loop = ((val >> 5) & 1) != 0;
			break;

		case 0x400d:
			break;

		case 0x400e:
			if (option[OPT_ENABLE_PNOISE] != 0)
				noise_tap = (val & 0x80) != 0 ? (1 << 6) : (1 << 1);
			else
				noise_tap = (1 << 1);
			nfreq = wavlen_table[frameCounter.pal][val & 15];
			if (counter[1] > nfreq)
				counter[1] = nfreq;
			break;

		case 0x400f:
			if (enable[1]) {
				length_counter[1] = length_table[(val >> 3) & 0x1f];
			}
			envelope_write = true;
			break;

		// dmc

		case 0x4010:
			mode = (val >> 6) & 3;
			dfreq = freq_table[frameCounter.pal][val & 15];
			if (counter[2] > dfreq)
				counter[2] = dfreq;
			break;

		case 0x4011:
			if (option[OPT_ENABLE_4011] != 0) {
				damp = (val >> 1) & 0x3f;
				dac_lsb = val & 1;
				dmc_pop = true;
			}
			break;

		case 0x4012:
			adr_reg = val & 0xff;
			// 在这里 daddress 被更新
			break;

		case 0x4013:
			len_reg = val & 0xff;
			// 在这里length被更新
			break;

		default:
			return false;
		}

		return true;
	}

	@Override
	public boolean read(int adr, IntHolder val, int id) {
		if (adr == 0x4015) {
			val.val |= (irq ? 128 : 0)
					| (frameCounter.frame_irq ? 0x40 : 0)
					| (active ? 16 : 0)
					| (length_counter[1] != 0 ? 8 : 0)
					| (length_counter[0] != 0 ? 4 : 0);
			frameCounter.frame_irq = false;
			return true;
		} else if (0x4008 <= adr && adr <= 0x4014) {
			val.val |= reg[adr - 0x4008];
			return true;
		} else
			return false;
	}

	@Override
	public void setMask(int m) {
		mask = m;
	}

}
