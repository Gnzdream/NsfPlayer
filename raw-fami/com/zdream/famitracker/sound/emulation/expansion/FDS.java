package com.zdream.famitracker.sound.emulation.expansion;

import com.zdream.famitracker.sound.emulation.APU;
import com.zdream.famitracker.sound.emulation.Mixer;

import static com.zdream.famitracker.sound.emulation.Types.*;

import java.util.Arrays;

public class FDS extends ExChannel implements External {

	public FDS(Mixer pMixer) {
		super(pMixer, SNDCHIP_FDS, CHANID_FDS);
		
		FDSSoundInstall3();
	}

	@Override
	public void reset() {
		FDSSoundReset();
		FDSSoundVolume(0);
	}

	@Override
	public void process(int time) {
		if (time == 0)
			return;

		while ((time--) > 0) {
			int value = FDSSoundRender() >> 12;
			mix(value);
			++m_iTime;
		}
	}

	@Override
	public void write(int address, int value) {
		FDSSoundWrite(address, value);
	}

	@Override
	public int read(int address) {
//		Mapped = ((0x4040 <= Address && Address <= 0x407f) || (0x4090 == Address) || (0x4092 == Address));
//		return FDSSoundRead(Address);
		
//		throw new IllegalStateException("不允许调用 read 方法");
		return FDSSoundRead(address);
	}

	@Override
	public boolean isMapped(int address) {
		return ((0x4040 <= address && address <= 0x407f) || (0x4090 == address) || (0x4092 == address));
	}

	@Override
	public Mixer getMixer() {
		return super.m_pMixer;
	}
	
	// ///////////// sound 部分 /////////////
	
	static final int LOG_BITS = 12;
	static final int LIN_BITS = 7;
	static final int LOG_LIN_BITS = 30;
	
//	/**
//	 * void FDSSoundInstall3(void);
//	 */
//	static void FDSSoundInstall3() {
//		
//	}
	
	/**
	 * uint32 LinearToLog(int32 l);
	 * 并没有使用
	 * @param l
	 * @return
	 */
	/*static int linearToLog(int l) {
		return (l < 0) ? (lineartbl[-l] + 1) : lineartbl[l];
	}*/
	
	/**
	 * int32 LogToLinear(uint32 l, uint32 sft);
	 * @param l
	 * @param sft
	 * @return
	 */
	static int logToLinear(int l, int sft) {
		int ret;
		int ofs; // unsigned
		l += sft << (LOG_BITS + 1);
		sft = l >> (LOG_BITS + 1);
		if (sft >= LOG_LIN_BITS) return 0;
		ofs = (l >> 1) & ((1 << LOG_BITS) - 1);
		ret = logtbl[ofs] >> sft;
		return (l & 1) != 0 ? -ret : ret;
	}
	
	/**
	 * void LogTableInitialize(void);
	 */
	static void logTableInitialize() {
		/*
		 * static volatile uint32 initialized = 0;
		 */
//		int initialized = 0;
		int i; // unsigned
		double a;
//		if (initialized) return;
		
		synchronized (FDS.class) {
			if (initFlag) {
				return;
			}
			
			for (i = 0; i < (1 << LOG_BITS); i++)
			{
				a = (1 << LOG_LIN_BITS) / Math.pow(2, i / (double)(1 << LOG_BITS));
				logtbl[i] = (int) a;
			}
			/*lineartbl[0] = LOG_LIN_BITS << LOG_BITS;
			for (i = 1; i < (1 << LIN_BITS) + 1; i++)
			{
				int ua; // unsigned
				a = i << (LOG_LIN_BITS - LIN_BITS);
				ua = (int)((LOG_LIN_BITS - ((double)(Math.log(a)) / (double)(Math.log(2.0)))) * (1 << LOG_BITS));
				lineartbl[i] = ua << 1;
			}*/
			
			initFlag = true;
		}
	}

	/**
	 * static uint32 lineartbl[(1 << LIN_BITS) + 1];
	 */
	//static int[] lineartbl = new int[(1 << LIN_BITS) + 1];
	
	/**
	 * static uint32 logtbl[1 << LOG_BITS];
	 */
	static int[] logtbl = new int[1 << LOG_BITS];
	
	/**
	 * 上面的数组只需要初始化一次. 初始化后 initFlag = true
	 */
	static boolean initFlag = false;
	
	static final int FM_DEPTH = 0; /* 0,1,2 */
	static final int NES_BASECYCLES = (21477270);
	static final int PGCPS_BITS = (32-16-6);
	static final int EGCPS_BITS = (12);
	static final int VOL_BITS = 12;
	
	static class FDS_EG {
		/**
		 * uint8
		 */
		int spd;
		/**
		 * uint8
		 */
		int cnt;
		/**
		 * uint8
		 */
		int mode;
		/**
		 * uint8
		 */
		int volume;
		
		public void clear() {
			spd = 0;
			cnt = 0;
			mode = 0;
			volume = 0;
		}

		@Override
		public String toString() {
			return "{spd=" + spd + ", cnt=" + cnt + ", mode=" + mode + ", volume=" + volume + "}";
		}
	}
	
	static class FDS_PG {
		/**
		 * uint32
		 */
		int spdbase;
		/**
		 * uint32
		 */
		int spd;
		/**
		 * uint32
		 */
		int freq;
		
		public void clear() {
			spdbase = 0;
			spd = 0;
			freq = 0;
		}

		@Override
		public String toString() {
			return "{spdbase=" + spdbase + ", spd=" + spd + ", freq=" + freq + "}";
		}
	}
	
	static class FDS_WG {
		/**
		 * uint32
		 */
		int phase;
		
		byte[] wave = new byte[0x40]; // 64 个单位
//		/**
//		 * uint8
//		 */
//		int wavptr;
		
		byte output;
		/**
		 * uint8
		 */
		int disable;
		/**
		 * uint8
		 */
		int disable2;
		
		public void clear() {
			phase = 0;
			Arrays.fill(wave, (byte) 0);
//			wavptr = 0;
			output = 0;
			disable = 0;
			disable2 = 0;
		}

		@Override
		public String toString() {
			return "{phase=" + phase + ", wave=" + Arrays.toString(wave) + /*", wavptr=" + wavptr + */", output="
					+ output + ", disable=" + disable + ", disable2=" + disable2 + "}";
		}
	}
	
	static class FDS_OP {
		FDS_EG eg = new FDS_EG();
		FDS_PG pg = new FDS_PG();
		FDS_WG wg = new FDS_WG();
		int bias;
		/**
		 * uint8
		 */
		int wavebase;
		/**
		 * uint8
		 */
		int[] d = new int[2];
		
		public void clear() {
			eg.clear();
			pg.clear();
			wg.clear();
			bias = 0;
			wavebase = 0;
			d[0] = 0;
			d[1] = 0;
		}

		@Override
		public String toString() {
			return "{eg=" + eg + ", pg=" + pg + ", wg=" + wg + ", bias=" + bias + ", wavebase=" + wavebase
					+ ", d=" + Arrays.toString(d) + "}";
		}
	}

	/**
	 * 也叫 FDSSOUND_tag
	 */
	static class FDSSOUND {
		FDS_OP[] op = new FDS_OP[2];
		/**
		 * uint32
		 */
		int phasecps;
		/**
		 * uint32
		 */
		int envcnt;
		/**
		 * uint32
		 */
		int envspd;
		/**
		 * uint32
		 */
		int envcps;
		/**
		 * uint8
		 */
		int envdisable;
		/**
		 * uint8 []
		 */
		int[] d = new int[3];
		/**
		 * uint32
		 */
		int lvl;
		int[] mastervolumel = new int[4];
		/**
		 * uint32
		 */
		int mastervolume;
		/**
		 * uint32
		 * APU.freq()
		 */
		int srate;
		/**
		 * uint8 []
		 */
		int[] reg = new int[0x10];
		
		{
			op[0] = new FDS_OP();
			op[1] = new FDS_OP();
		}
		
		public void clear() {
			op[0].clear();
			op[1].clear();
			phasecps = 0;
			envcnt = 0;
			envspd = 0;
			envcps = 0;
			envdisable = 0;
			d[0] = 0;
			d[1] = 0;
			d[2] = 0;
			lvl = 0;
			mastervolumel[0] = 0;
			mastervolumel[1] = 0;
			mastervolumel[2] = 0;
			mastervolumel[3] = 0;
			mastervolume = 0;
			srate = 0;
			Arrays.fill(reg, 0);
		}
	}
	
	static FDSSOUND fdssound = new FDSSOUND();
	
	/**
	 * static void FDSSoundWGStep(FDS_WG *pwg)
	 */
	static void FDSSoundWGStep(FDS_WG pwg) {
		if (pwg.disable != 0 || pwg.disable2 != 0) return;
		pwg.output = pwg.wave[(pwg.phase >> (PGCPS_BITS + 16)) & 0x3f];
	}
	
	/**
	 * static void FDSSoundEGStep(FDS_EG *peg)
	 */
	static void FDSSoundEGStep(FDS_EG peg) {
		if ((peg.mode & 0x80) != 0) return;
		if (++peg.cnt <= peg.spd) return;
		peg.cnt = 0;
		if ((peg.mode & 0x40) != 0)
			peg.volume += (peg.volume < 0x1f) ? 1 : 0;
		else
			peg.volume -= (peg.volume > 0) ? 1 : 0;
		
		// test
		if (peg.volume != 0) {
			peg.volume += 0;
		}
	}
	
	/**
	 * int32 __fastcall FDSSoundRender(void)
	 */
	static int FDSSoundRender() {
		int output;
		/* Wave Generator */
		FDSSoundWGStep(fdssound.op[0].wg);
		// EDIT not using FDSSoundWGStep for modulator (op[1]), need to adjust bias when sample changes

		/* Frequency Modulator */
		fdssound.op[1].pg.spd = fdssound.op[1].pg.spdbase;
		if (fdssound.op[1].wg.disable != 0)
			fdssound.op[0].pg.spd = fdssound.op[0].pg.spdbase;
		else {
			// EDIT this step has been entirely rewritten to match FDS.txt by Disch

			// advance the mod table wave and adjust the bias when/if next table entry is reached
			// unsigned
			final int ENTRY_WIDTH = 1 << (PGCPS_BITS + 16);
			// unsigned
			int spd = fdssound.op[1].pg.spd; // phase to add
			while (spd > 0) {
				// unsigned
				int left = ENTRY_WIDTH - (fdssound.op[1].wg.phase & (ENTRY_WIDTH-1));
				// unsigned
				int advance = spd;
				if (spd >= left) // advancing to the next entry
				{
					advance = left;
					fdssound.op[1].wg.phase += advance;
					fdssound.op[1].wg.output = fdssound.op[1].wg.wave[(fdssound.op[1].wg.phase >> (PGCPS_BITS+16)) & 0x3f];

					// adjust bias
					byte value = (byte) (fdssound.op[1].wg.output & 7);
					// length: 8
					final byte[] MOD_ADJUST = { 0, 1, 2, 4, 0, -4, -2, -1 };
					if (value == 4)
						fdssound.op[1].bias = 0;
					else
						fdssound.op[1].bias += MOD_ADJUST[value];
					while (fdssound.op[1].bias >  63) fdssound.op[1].bias -= 128;
					while (fdssound.op[1].bias < -64) fdssound.op[1].bias += 128;
				}
				else // not advancing to the next entry
				{
					fdssound.op[1].wg.phase += advance;
				}
				spd -= advance;
			}

			// modulation calculation
			int mod = fdssound.op[1].bias * (int)(fdssound.op[1].eg.volume);
			mod >>= 4;
			if ((mod & 0x0F) != 0) {
				if (fdssound.op[1].bias < 0) mod -= 1;
				else                         mod += 2;
			}
			if (mod > 193) mod -= 258;
			if (mod < -64) mod += 256;
			mod = (mod * (int)(fdssound.op[0].pg.freq)) >> 6;

			// calculate new frequency with modulation
			int new_freq = fdssound.op[0].pg.freq + mod;
			if (new_freq < 0) new_freq = 0;
			fdssound.op[0].pg.spd = (int)(new_freq) * fdssound.phasecps;
		}

		/* Accumulator */
		output = fdssound.op[0].eg.volume;
		if (output > 0x20) output = 0x20;
		output = (fdssound.op[0].wg.output * output * fdssound.mastervolumel[fdssound.lvl]) >> (VOL_BITS - 4);

		/* Envelope Generator */
		if (fdssound.envdisable == 0 && fdssound.envspd != 0) {
			fdssound.envcnt += fdssound.envcps;
			while (fdssound.envcnt >= fdssound.envspd) {
				fdssound.envcnt -= fdssound.envspd;
				FDSSoundEGStep(fdssound.op[1].eg);
				FDSSoundEGStep(fdssound.op[0].eg);
			}
		}

		/* Phase Generator */
		fdssound.op[0].wg.phase += fdssound.op[0].pg.spd;
		// EDIT modulator op[1] phase now updated above.

		return (fdssound.op[0].pg.freq != 0) ? output : 0;
	}
	
	/**
	 * void __fastcall FDSSoundVolume(unsigned int volume)
	 */
	static void FDSSoundVolume(int volume) {
		volume += 196;
		fdssound.mastervolume = (volume << (LOG_BITS - 8)) << 1;
		fdssound.mastervolumel[0] = logToLinear(fdssound.mastervolume, LOG_LIN_BITS - LIN_BITS - VOL_BITS) * 2;
		fdssound.mastervolumel[1] = logToLinear(fdssound.mastervolume, LOG_LIN_BITS - LIN_BITS - VOL_BITS) * 4 / 3;
		fdssound.mastervolumel[2] = logToLinear(fdssound.mastervolume, LOG_LIN_BITS - LIN_BITS - VOL_BITS) * 2 / 2;
		fdssound.mastervolumel[3] = logToLinear(fdssound.mastervolume, LOG_LIN_BITS - LIN_BITS - VOL_BITS) * 8 / 10;
	}

	/**
	 * static const uint8 wave_delta_table[8] = {...}
	 * 并没有使用
	 */
	static final int wave_delta_table[] = {
		0,(1 << FM_DEPTH),(2 << FM_DEPTH),(4 << FM_DEPTH),
		0,256 - (4 << FM_DEPTH),256 - (2 << FM_DEPTH),256 - (1 << FM_DEPTH),
	};

	/**
	 * void __fastcall FDSSoundWrite(uint16 address, uint8 value)
	 */
	static void FDSSoundWrite(int address, int value) {
		if (0x4040 <= address && address <= 0x407F) {
			fdssound.op[0].wg.wave[address - 0x4040] = (byte) (((int) (value & 0x3f)) - 0x20);
		} else if (0x4080 <= address && address <= 0x408F) {
			FDS_OP pop = fdssound.op[(address & 4) >> 2];
			fdssound.reg[address - 0x4080] = value;
			switch (address & 0xf) {
				case 0:
				case 4:
					pop.eg.mode = value & 0xc0;
					if ((pop.eg.mode & 0x80) != 0) {
						pop.eg.volume = (value & 0x3f);
						if (pop.eg.volume != 0) { // TEST
							value += 0;
						}
					} else {
						pop.eg.spd = value & 0x3f;
					}
					break;
				case 5:
					// EDIT rewrote modulator/bias code
					fdssound.op[1].bias = value & 0x3F;
					if ((value & 0x40) != 0) fdssound.op[1].bias -= 0x40; // extend sign bit
					fdssound.op[1].wg.phase = 0;
					break;
				case 2:	case 6:
					pop.pg.freq &= 0x00000F00;
					pop.pg.freq |= (value & 0xFF) << 0;
					pop.pg.spdbase = pop.pg.freq * fdssound.phasecps;
					break;
				case 3:
					fdssound.envdisable = value & 0x40;
				case 7:
//	#if 0
//					pop->wg.phase = 0;
//	#endif
					pop.pg.freq &= 0x000000FF;
					pop.pg.freq |= (value & 0x0F) << 8;
					pop.pg.spdbase = pop.pg.freq * fdssound.phasecps;
					pop.wg.disable = value & 0x80;
					if (pop.wg.disable != 0) {
						pop.wg.phase = 0;
//						pop.wg.wavptr = 0;
						pop.wavebase = 0;
					}
					break;
				case 8:
					// EDIT rewrote modulator/bias code
					if (fdssound.op[1].wg.disable != 0) {
						byte append = (byte) (value & 0x07);
						for (int i=0; i < 0x3E; ++i) {
							fdssound.op[1].wg.wave[i] = fdssound.op[1].wg.wave[i+2];
						}
						fdssound.op[1].wg.wave[0x3E] = append;
						fdssound.op[1].wg.wave[0x3F] = append;
					}
					break;
				case 9:
					fdssound.lvl = (value & 3);
					fdssound.op[0].wg.disable2 = value & 0x80;
					break;
				case 10:
					fdssound.envspd = value << EGCPS_BITS;
					break;
				default:
					break;
			}
		}
	}

	/**
	 * uint8 __fastcall FDSSoundRead(uint16 address)
	 */
	static int FDSSoundRead(int address) {
		if (0x4040 <= address && address <= 0x407f)
		{
			return fdssound.op[0].wg.wave[address & 0x3f] + 0x20;
		}
		if (0x4090 == address)
			return fdssound.op[0].eg.volume | 0x40;
		if (0x4092 == address) /* 4094? */
			return fdssound.op[1].eg.volume | 0x40;
		return 0;
	}

	/**
	 * static uint32 DivFix(uint32 p1, uint32 p2, uint32 fix)
	 */
	static int divFix(int p1, int p2, int fix) {
		int ret;
		ret = p1 / p2;
		p1  = p1 % p2;/* p1 = p1 - p2 * ret; */
		while ((fix--) != 0) {
			p1 += p1;
			ret += ret;
			if (p1 >= p2)
			{
				p1 -= p2;
				ret++;
			}
		}
		return ret;
	}

	/**
	 * void __fastcall FDSSoundReset(void)
	 */
	static void FDSSoundReset() {
		int i;
		fdssound.clear();
		// TODO: Fix srate
		fdssound.srate = APU.BASE_FREQ_NTSC; ///NESAudioFrequencyGet();
		fdssound.envcps = divFix(NES_BASECYCLES, 12 * fdssound.srate, EGCPS_BITS + 5 - 9 + 1);
		fdssound.envspd = 0xe8 << EGCPS_BITS;
		fdssound.envdisable = 1;
		fdssound.phasecps = divFix(NES_BASECYCLES, 12 * fdssound.srate, PGCPS_BITS);
		for (i = 0; i < 0x40; i++) {
			fdssound.op[0].wg.wave[i] = (byte) ((i < 0x20) ? 0x1f : -0x20);
			fdssound.op[1].wg.wave[i] = 64;
		}
	}

	static void FDSSoundInstall3() {
		logTableInitialize();
	}
}
