package zdream.nsfplayer.xgm.device.sound.legacy;

/**
 * in emu2149.h | emu2149.c
 * @author Zdream
 */
public final class PSG {
	
	private static final int
			EMU2149_VOL_DEFAULT = 1,
			EMU2149_VOL_YM2149 = 0,
			EMU2149_VOL_AY_3_8910 = 1;
	
	// Volume Table
	/** unsigned */
	public int[] voltbl;

	/** unsigned byte */
	int[] reg = new int[0x20];
	int out;
	public int[] cout = new int[3];

	/** unsigned */
	public int clk;

	int rate;

	int base_incr;

	int quality;

	/** unsigned */
	public int[] count = new int[3];
	/** unsigned */
	public int[] volume = new int[3];
	/** unsigned */
	public int[] freq = new int[3];
	/** unsigned */
	public int[] edge = new int[3];
	/** unsigned */
	public int[] tmask = new int[3];
	/** unsigned */
	public int[] nmask = new int[3];
	/** unsigned */
	public int mask;

	/** unsigned */
	int base_count;

	/** unsigned */
	int env_volume;
	/** unsigned */
	public int env_ptr;
	/** unsigned */
	int env_face;

	/** unsigned */
	public int env_continue;
	/** unsigned */
	public int env_attack;
	/** unsigned */
	public int env_alternate;
	/** unsigned */
	public int env_hold;
	/** unsigned */
	int env_pause;
	/** unsigned */
	int env_reset;

	/** unsigned */
	public int env_freq;
	/** unsigned */
	public int env_count;

	/** unsigned */
	public int noise_seed;
	/** unsigned */
	public int noise_count;
	/** unsigned */
	public int noise_freq;

	// rate converter
	/** unsigned */
	int realstep;
	/** unsigned */
	int psgtime;
	/** unsigned */
	int psgstep;

	// I/O Ctrl
	/** unsigned */
	int adr;
	
	private static final int[][] VOLT_BL = { // len: 2,32
			{ 0x00, 0x01, 0x01, 0x02, 0x02, 0x03, 0x03, 0x04, 0x05, 0x06, 0x07, 0x09, 0x0B, 0x0D, 0x0F, 0x12,
			  0x16, 0x1A, 0x1F, 0x25, 0x2D, 0x35, 0x3F, 0x4C, 0x5A, 0x6A, 0x7F, 0x97, 0xB4, 0xD6, 0xEB, 0xFF },
			{ 0x00, 0x00, 0x01, 0x01, 0x02, 0x02, 0x03, 0x03, 0x05, 0x05, 0x07, 0x07, 0x0B, 0x0B, 0x0F, 0x0F,
			  0x16, 0x16, 0x1F, 0x1F, 0x2D, 0x2D, 0x3F, 0x3F, 0x5A, 0x5A, 0x7F, 0x7F, 0xB4, 0xB4, 0xFF, 0xFF }
			};
	private static final int GETA_BITS = 24;
	
	public void internal_refresh() {
		if (quality != 0) {
			base_incr = 1 << GETA_BITS;
			realstep = (int) ((1 << 31) / rate);
			psgstep = (int) ((1 << 31) / (clk / 16));
			psgtime = 0;
		} else {
			base_incr = (int) ((double) clk * (1 << GETA_BITS) / (16 * rate));
		}
	}
	
	/**
	 * 
	 * @param c
	 *   unsigned
	 * @param r
	 *   unsigned
	 */
	public PSG(int c, int r) {
		setVolumeMode(EMU2149_VOL_DEFAULT);
		clk = c;
		rate = r != 0 ? r : 44100;
		setQuality(0);
	}
	
	/**
	 * 
	 * @param r
	 *   unsigned
	 */
	public void setRate(int r) {
		rate = r != 0 ? r : 44100;
		internal_refresh();
	}
	
	/**
	 * 
	 * @param q
	 *   unsigned
	 */
	public void setQuality(int q) {
		quality = q;
		internal_refresh();
	}
	
	public void setVolumeMode(int type) {
		switch (type) {
		case 1:
			voltbl = VOLT_BL[EMU2149_VOL_YM2149];
			break;
		case 2:
			voltbl = VOLT_BL[EMU2149_VOL_AY_3_8910];
			break;
		default:
			voltbl = VOLT_BL[EMU2149_VOL_DEFAULT];
			break;
		}
	}
	
	/**
	 * 更新 mask 值, 并将原有的 mask 值返回
	 * @param m
	 *   unsigned
	 * @return
	 *   替换下的 mask 值
	 */
	public int setMask(int m) {
		int ret = 0;
		ret = mask;
		mask = m;
		return ret;
	}
	
	/**
	 * 更新 mask 值(mask = mask ^ m), 并将原有的 mask 值返回
	 * @param m
	 *   unsigned
	 * @return
	 *   原来的 mask 值
	 */
	public int toggleMask(int m) {
		int ret = 0;
		ret = mask;
		mask ^= m;
		return ret;
	}
	
	public void reset() {
		int i;

		base_count = 0;

		for (i = 0; i < 3; i++) {
			cout[i] = 0;
			count[i] = 0x1000;
			freq[i] = 0;
			edge[i] = 0;
			volume[i] = 0;
		}

		mask = 0;

		for (i = 0; i < 16; i++)
			reg[i] = 0;
		adr = 0;

		noise_seed = 0xffff;
		noise_count = 0x40;
		noise_freq = 0;

		env_volume = 0;
		env_ptr = 0;
		env_freq = 0;
		env_count = 0;
		env_pause = 1;

		out = 0;
	}
	
	/**
	 * 
	 * @return
	 *   unsigned byte
	 */
	public int readIO() {
		return (reg[adr]);
	}
	
	/**
	 * 
	 * @param reg
	 *   unsigned
	 * @return
	 *   unsigned byte
	 */
	public int readReg(int r) {
		return (reg[r & 0x1f]);
	}
	
	/**
	 * 
	 * @param adr
	 *   unsigned
	 * @param val
	 *   unsigned
	 */
	public void writeIO(int adr, int val) {
		if ((adr & 1) != 0)
			writeReg(this.adr, val);
		else
			this.adr = val & 0x1f;
	}
	
	public int calc0() {
		int i, noise;
		int incr; // unsigned
		int mix = 0;

		base_count += base_incr;
		incr = (base_count >> GETA_BITS);
		base_count &= (1 << GETA_BITS) - 1;

		/* Envelope */
		env_count += incr;
		while (env_count >= 0x10000 && env_freq != 0) {
			if (env_pause == 0) {
				if (env_face != 0)
					env_ptr = (env_ptr + 1) & 0x3f;
				else
					env_ptr = (env_ptr + 0x3f) & 0x3f;
			}

			if ((env_ptr & 0x20) != 0) /* if carry or borrow */
			{
				if (env_continue != 0) {
					if ((env_alternate ^ env_hold) != 0)
						env_face ^= 1;
					if (env_hold != 0)
						env_pause = 1;
					env_ptr = (env_face != 0) ? 0 : 0x1f;
				} else {
					env_pause = 1;
					env_ptr = 0;
				}
			}

			env_count -= env_freq;
		}

		/* Noise */
		noise_count += incr;
		if ((noise_count & 0x40) != 0) {
			if ((noise_seed & 1) != 0)
				noise_seed ^= 0x24000;
			noise_seed >>= 1;
			noise_count -= noise_freq;
		}
		noise = noise_seed & 1;

		/* Tone */
		for (i = 0; i < 3; i++) {
			count[i] += incr;
			if ((count[i] & 0x1000) != 0) {
				if (freq[i] > 1) {
					edge[i] = (edge[i] == 0) ? 1 : 0; // ?
					count[i] -= freq[i];
				} else {
					edge[i] = 1;
				}
			}

			cout[i] = 0; // maintaining cout for stereo mix

			if ((mask & (1 << (i))) != 0)
				continue;

			if ((tmask[i] != 0 || edge[i] != 0) && (nmask[i] != 0 || noise != 0)) {
				if ((volume[i] & 32) == 0)
					cout[i] = voltbl[volume[i] & 31];
				else
					cout[i] = voltbl[env_ptr];

				mix += cout[i];
			}
		}

		return mix & 0xFFFF;
	}
	
	public int calc1() {
		if (quality == 0)
			return (calc0() << 4);

		/* Simple rate converter */
		while (realstep > psgtime) {
			psgtime += psgstep;
			out += calc0();
			out >>= 1;
		}

		psgtime = psgtime - realstep;

		return (out << 4);
	}
	
	/**
	 * 
	 * @param reg
	 *   unsigned
	 * @param val
	 *   unsigned
	 */
	public void writeReg(int reg, int val) {
		int c;

		if (reg > 15)
			return;

		this.reg[reg] = (val & 0xff);
		switch (reg) {
		case 0:
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
			c = reg >> 1;
			freq[c] = ((this.reg[c * 2 + 1] & 15) << 8) + this.reg[c * 2];
			break;

		case 6:
			noise_freq = (val == 0) ? 1 : ((val & 31) << 1);
			break;

		case 7:
			tmask[0] = (val & 1);
			tmask[1] = (val & 2);
			tmask[2] = (val & 4);
			nmask[0] = (val & 8);
			nmask[1] = (val & 16);
			nmask[2] = (val & 32);
			break;

		case 8:
		case 9:
		case 10:
			volume[reg - 8] = val << 1;
			break;

		case 11:
		case 12:
			env_freq = (this.reg[12] << 8) + this.reg[11];
			break;

		case 13:
			env_continue = (val >> 3) & 1;
			env_attack = (val >> 2) & 1;
			env_alternate = (val >> 1) & 1;
			env_hold = val & 1;
			env_face = env_attack;
			env_pause = 0;
			env_count = 0x10000 - env_freq;
			env_ptr = (env_face != 0) ? 0 : 0x1f;
			break;

		case 14:
		case 15:
		default:
			break;
		}

		return;
	}
	
}
