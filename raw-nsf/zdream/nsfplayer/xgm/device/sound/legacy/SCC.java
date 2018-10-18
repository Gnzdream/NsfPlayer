package zdream.nsfplayer.xgm.device.sound.legacy;

import java.util.Arrays;

public class SCC {

	/** unsigned */
	public int clk, rate, base_incr, quality;

	public int out, prev, next;
	/** unsigned */
	public int type;
	/** unsigned */
	public int mode;
	/** unsigned */
	public int active;
	/** unsigned */
	public int base_adr;
	/** unsigned */
	public int mask;

	/** unsigned */
	public int realstep;
	/** unsigned */
	public int scctime;
	/** unsigned */
	public int sccstep;

	/** unsigned */
	public int[] incr = new int[5];

	public int[][] wave = new int[5][32];

	/** unsigned */
	public int[] count = new int[5];
	/** unsigned */
	public int[] freq = new int[5];
	/** unsigned */
	public int[] phase = new int[5];
	/** unsigned */
	public int[] volume = new int[5];
	/** unsigned */
	public int[] offset = new int[5];
	/** unsigned */
	public int[] reg = new int[0x100 - 0xC0];

	public int ch_enable;
	public int ch_enable_next;

	public int cycle_4bit;
	public int cycle_8bit;
	public int refresh;
	public int[] rotate = new int[5];
	
	public static final int
			SCC_STANDARD = 0,
			SCC_ENHANCED = 1;
	
	public static final int GETA_BITS = 22;
	
	public void internal_refresh() {
		if (quality != 0) {
			base_incr = 2 << GETA_BITS;
			realstep = (int) ((1 << 31) / rate);
			sccstep = (int) ((1 << 31) / (clk / 2));
			scctime = 0;
		} else {
			base_incr = (int) ((double) clk * (1 << GETA_BITS) / rate);
		}
	}

	/**
	 * 更新 mask 值, 并将原有的 mask 值返回
	 * @param m
	 *   unsigned
	 * @return
	 *   替换下的 mask 值
	 */
	public int setMask(int mask) {
		int ret = 0;
		ret = this.mask;
		this.mask = mask;
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

	/**
	 * 
	 * @param q
	 *   unsigned
	 */
	public void set_quality (int q) {
		quality = q;
		internal_refresh ();
	}

	/**
	 * 
	 * @param r
	 *   unsigned
	 */
	public void set_rate(int r) {
		rate = r != 0 ? r : 44100;
		internal_refresh();
	}
	
	/**
	 * 
	 * @param c
	 *   unsigned
	 * @param r
	 *   unsigned
	 */
	public SCC(int c, int r) {
		clk = c;
		rate = r != 0 ? r : 44100;
		set_quality (0);
		type = SCC_ENHANCED;
	}

	public void reset() {
		int i, j;

		mode = 0;
		active = 0;
		base_adr = 0x9000;

		for (i = 0; i < 5; i++) {
			for (j = 0; j < 5; j++)
				wave[i][j] = 0;
			count[i] = 0;
			freq[i] = 0;
			phase[i] = 0;
			volume[i] = 0;
			offset[i] = 0;
			rotate[i] = 0;
		}

		Arrays.fill(this.reg, 0);

		mask = 0;

		ch_enable = 0xff;
		ch_enable_next = 0xff;

		cycle_4bit = 0;
		cycle_8bit = 0;
		refresh = 0;

		out = 0;
		prev = 0;
		next = 0;

		return;
	}

	public int calc0() {
		int i;
		int mix = 0;

		for (i = 0; i < 5; i++) {
			count[i] = (count[i] + incr[i]);

			if ((count[i] & (1 << (GETA_BITS + 5))) != 0) {
				count[i] &= ((1 << (GETA_BITS + 5)) - 1);
				offset[i] = (offset[i] + 31) & rotate[i];
				ch_enable &= ~(1 << i);
				ch_enable |= ch_enable_next & (1 << i);
			}

			if ((ch_enable & (1 << i)) != 0) {
				phase[i] = ((count[i] >> (GETA_BITS)) + offset[i]) & 0x1F;
				if ((mask & (1 << i)) == 0)
					mix += (((wave[i][phase[i]]) * volume[i]) & 0xFF) >> 4;
			}
		}

		return (mix << 4) & 0xFFFF;
	}

	public int calc1() {
		if (quality == 0)
			return calc0();

		while (realstep > scctime) {
			scctime += sccstep;
			prev = next;
			next = calc0();
		}

		scctime -= realstep;
		out = (int) (((double) next * (sccstep - scctime) + (double) prev * scctime) / sccstep) & 0xFFFF;

		return (out & 0xFFFF);
	}

	/**
	 * @param adr
	 *   unsigned
	 * @return
	 *   unsigned
	 */
	public int readReg(int adr) {
		if (adr < 0xA0)
			return wave[adr >> 5][adr & 0x1f];
		else if (0xC0 < adr && adr < 0xF0)
			return reg[adr - 0xC0];
		else
			return 0;
	}

	/**
	 * 
	 * @param adr
	 *   unsigned
	 * @param val
	 *   unsigned
	 */
	public void writeReg(int adr, int val) {
		int ch;
		int freq;

		adr &= 0xFF;

		if (adr < 0xA0) {
			ch = (adr & 0xF0) >> 5;
			if (rotate[ch] == 0) {
				wave[ch][adr & 0x1F] = val & 0xFF; // byte
				if (mode == 0 && ch == 3)
					wave[4][adr & 0x1F] = val & 0xFF; // byte
			}
		} else if (0xC0 <= adr && adr <= 0xC9) {
			reg[adr - 0xC0] = val;
			ch = (adr & 0x0F) >> 1;
			if ((adr & 1) != 0)
				this.freq[ch] = ((val & 0xF) << 8) | (this.freq[ch] & 0xFF);
			else
				this.freq[ch] = (this.freq[ch] & 0xF00) | (val & 0xFF);

			if (refresh != 0)
				count[ch] = 0;
			freq = this.freq[ch];
			if (cycle_8bit != 0)
				freq &= 0xFF;
			if (cycle_4bit != 0)
				freq >>= 8;
			if (freq <= 8)
				incr[ch] = 0;
			else
				incr[ch] = base_incr / (freq + 1);
		} else if (0xD0 <= adr && adr <= 0xD4) {
			reg[adr - 0xC0] = val;
			volume[adr & 0x0F] = (val & 0xF);
		} else if (adr == 0xE0) {
			reg[adr - 0xC0] = val;
			mode = val & 1;
		} else if (adr == 0xE1) {
			reg[adr - 0xC0] = val;
			ch_enable_next = val & 0x1F;
		} else if (adr == 0xE2) {
			reg[adr - 0xC0] = val;
			cycle_4bit = val & 1;
			cycle_8bit = val & 2;
			refresh = val & 32;
			if ((val & 64) != 0)
				for (ch = 0; ch < 5; ch++)
					rotate[ch] = 0x1F;
			else
				for (ch = 0; ch < 5; ch++)
					rotate[ch] = 0;
			if ((val & 128) != 0)
				rotate[3] = rotate[4] = 0x1F;
		}

		return;
	}

	/**
	 * 
	 * @param adr
	 *   unsigned
	 * @param val
	 *   unsigned
	 */
	public void write_standard(int adr, int val) {
		adr &= 0xFF;

		if (adr < 0x80) { /* wave */
			writeReg(adr, val);
		} else if (adr < 0x8A) { /* freq */
			writeReg(adr + 0xC0 - 0x80, val);
		} else if (adr < 0x8F) { /* volume */
			writeReg(adr + 0xD0 - 0x8A, val);
		} else if (adr == 0x8F) { /* ch enable */
			writeReg(0xE1, val);
		} else if (0xE0 <= adr) { /* flags */
			writeReg(0xE2, val);
		}
	}

	/**
	 * 
	 * @param adr
	 *   unsigned
	 * @param val
	 *   unsigned
	 */
	public void write_enhanced(int adr, int val) {
		adr &= 0xFF;

		if (adr < 0xA0) { /* wave */
			writeReg(adr, val);
		} else if (adr < 0xAA) { /* freq */
			writeReg(adr + 0xC0 - 0xA0, val);
		} else if (adr < 0xAF) { /* volume */
			writeReg(adr + 0xD0 - 0xAA, val);
		} else if (adr == 0xAF) { /* ch enable */
			writeReg(0xE1, val);
		} else if (0xC0 <= adr && adr <= 0xDF) { /* flags */
			writeReg(0xE2, val);
		}
	}

	/**
	 * 
	 * @param adr
	 *   unsigned
	 * @return
	 *   unsigned
	 */
	public int read_enhanced(int adr) {
		adr &= 0xFF;
		if (adr < 0xA0)
			return readReg(adr);
		else if (adr < 0xAA)
			return readReg(adr + 0xC0 - 0xA0);
		else if (adr < 0xAF)
			return readReg(adr + 0xD0 - 0xAA);
		else if (adr == 0xAF)
			return readReg(0xE1);
		else if (0xC0 <= adr && adr <= 0xDF)
			return readReg(0xE2);
		else
			return 0;
	}

	/**
	 * 
	 * @param adr
	 *   unsigned
	 * @return
	 *   unsigned
	 */
	public int read_standard(int adr) {
		adr &= 0xFF;
		if (adr < 0x80)
			return readReg(adr);
		else if (0xA0 <= adr && adr <= 0xBF)
			return readReg(0x80 + (adr & 0x1F));
		else if (adr < 0x8A)
			return readReg(adr + 0xC0 - 0x80);
		else if (adr < 0x8F)
			return readReg(adr + 0xD0 - 0x8A);
		else if (adr == 0x8F)
			return readReg(0xE1);
		else if (0xE0 <= adr)
			return readReg(0xE2);
		else
			return 0;
	}

	/**
	 * 
	 * @param adr
	 *   unsigned
	 * @return
	 *   unsigned
	 */
	public int read(int adr) {
		if (type == SCC_ENHANCED && (adr & 0xFFFE) == 0xBFFE)
			return (base_adr >> 8) & 0x20;

		if (adr < base_adr)
			return 0;
		adr -= base_adr;

		if (adr == 0) {
			if (mode != 0)
				return 0x80;
			else
				return 0x3F;
		}

		if (active == 0 || adr < 0x800 || 0x8FF < adr)
			return 0;

		switch (type) {
		case SCC_STANDARD:
			return read_standard(adr);
		case SCC_ENHANCED:
			if (mode == 0)
				return read_standard(adr);
			else
				return read_enhanced(adr);
		default:
			break;
		}

		return 0;
	}

	/**
	 * 
	 * @param adr
	 *   unsigned
	 * @param val
	 *   unsigned
	 */
	public void write(int adr, int val) {
		val = val & 0xFF;

		if (type == SCC_ENHANCED && (adr & 0xFFFE) == 0xBFFE) {
			base_adr = 0x9000 | ((val & 0x20) << 8);
			return;
		}

		if (adr < base_adr)
			return;
		adr -= base_adr;

		if (adr == 0) {
			if (val == 0x3F) {
				mode = 0;
				active = 1;
			} else if ((val & 0x80) != 0 && type == SCC_ENHANCED) {
				mode = 1;
				active = 1;
			} else {
				mode = 0;
				active = 0;
			}
			return;
		}
	  
		if (active == 0 || adr < 0x800 || 0x8FF < adr)
			return;

		switch (type) {
		case SCC_STANDARD:
			write_standard(adr, val);
			break;
		case SCC_ENHANCED:
			if (mode != 0)
				write_enhanced(adr, val);
			else
				write_standard(adr, val);
		default:
			break;
		}

		return;
	}

	/**
	 * 
	 * @param type
	 *   unsigned
	 */
	public void set_type(int type) {
		this.type = type;
	}

}
