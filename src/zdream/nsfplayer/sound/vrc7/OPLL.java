package zdream.nsfplayer.sound.vrc7;

import static zdream.nsfplayer.sound.vrc7.VRC7Static.*;

/**
 * 只在 VRC7 中使用
 * @author Zdream
 */
public class OPLL {
	
	int out;

	/** unsigned */
	int realstep;
	/** unsigned */
	int oplltime;
	/** unsigned */
	int opllstep;
	int prev, next;
	int[] sprev = new int[2], snext = new int[2];
	/** unsigned */
	int[] pans = new int[16];

	// Register
	/** unsigned */
	public int[] regs = new int[0x40];
	int[] slot_on_flag = new int[18];

	// Pitch Modulator
	/** unsigned */
	int pm_phase;
	int lfo_pm;

	// Amp Modulator
	int am_phase;
	int lfo_am;

	/** unsigned */
	int quality;

	// Noise Generator
	/** unsigned */
	int noise_seed;

	// Channel Data
	int[] patch_number = new int[9];
	int[] key_status = new int[9];

	/** Slot */
	public OPLLSlot[] slots = new OPLLSlot[18];

	// Voice Data
	/** 19 x 2 = 38 */
	OPLLPatch[] patches = new OPLLPatch[38];
	/** flag for check patch update */
	int[] patch_update = new int[2];

	/** unsigned */
	int mask;
	
	// 上面是原工程 OPLL 的变量
	
	/** Input clock, unsigned */
	int clk = 844451141;
	/** Sampling rate, unsigned */
	int rate = 3354932;

	// WaveTable for each envelope amp
	/** unsigned */
	int[][] waveform = { new int[PG_WIDTH], new int[PG_WIDTH] };
	int[] fullsintable = waveform[0], halfsintable = waveform[1];

	// LFO Table
	/** unsigned */
	int[] pmtable = new int[PM_PG_WIDTH], amtable = new int[AM_PG_WIDTH];

	// Phase delta for LFO
	/** unsigned */
	int pm_dphase, am_dphase;

	// dB to Liner table
	/** int - 16bit */
	int[] DB2LIN_TABLE = new int[(DB_MUTE + DB_MUTE) * 2];

	// Liner to Log curve conversion table (for Attack rate).
	/** unsigned - 16bit */
	int[] AR_ADJUST_TABLE = new int[1 << EG_BITS];

	// Empty voice data
	OPLLPatch null_patch = new OPLLPatch();

	// Basic voice Data
	OPLLPatch[][] default_patch = new OPLLPatch[OPLL_TONE_NUM][(16 + 3) * 2];

	/** Phase incr table for Attack, unsigned */
	int[][] dphaseARTable = new int[16][16];
	/** Phase incr table for Decay and Release, unsigned */
	int[][] dphaseDRTable = new int[16][16];

	/** KSL + TL Table, unsigned */
	int[][][][] tllTable = new int[16][8][1 << TL_BITS][4];
	int[][][] rksTable = new int[2][8][2];

	/** Phase incr table for PG, unsigned */
	int[][][] dphaseTable = new int[512][8][16];
	
	{
		for (int i = 0; i < default_patch.length; i++) {
			OPLLPatch[] os = default_patch[i];
			for (int j = 0; j < os.length; j++) {
				os[j] = new OPLLPatch();
			}
		}
	}
	
	private void dump2patch(short[] dump, int offset, OPLLPatch patch0, OPLLPatch patch1) {
		patch0.AM = (dump[offset] >> 7) & 1;
		patch1.AM = (dump[1 + offset] >> 7) & 1;
		patch0.PM = (dump[offset] >> 6) & 1;
		patch1.PM = (dump[1 + offset] >> 6) & 1;
		patch0.EG = (dump[offset] >> 5) & 1;
		patch1.EG = (dump[1 + offset] >> 5) & 1;
		patch0.KR = (dump[offset] >> 4) & 1;
		patch1.KR = (dump[1 + offset] >> 4) & 1;
		patch0.ML = (dump[offset]) & 15;
		patch1.ML = (dump[1 + offset]) & 15;
		patch0.KL = (dump[2 + offset] >> 6) & 3;
		patch1.KL = (dump[3 + offset] >> 6) & 3;
		patch0.TL = (dump[2 + offset]) & 63;
		patch0.FB = (dump[3 + offset]) & 7;
		patch0.WF = (dump[3 + offset] >> 3) & 1;
		patch1.WF = (dump[3 + offset] >> 4) & 1;
		patch0.AR = (dump[4 + offset] >> 4) & 15;
		patch1.AR = (dump[5 + offset] >> 4) & 15;
		patch0.DR = (dump[4 + offset]) & 15;
		patch1.DR = (dump[5 + offset]) & 15;
		patch0.SL = (dump[6 + offset] >> 4) & 15;
		patch1.SL = (dump[7 + offset] >> 4) & 15;
		patch0.RR = (dump[6 + offset]) & 15;
		patch1.RR = (dump[7 + offset]) & 15;
	}
	
	/**
	 * 仅初始化时调用
	 */
	private void makeDefaultPatch() {
		for (int i = 0; i < OPLL_TONE_NUM; i++) {
			for (int j = 0; j < 19; j++) {
				dump2patch(default_inst[i], j * 16,
						default_patch[i][j * 2], default_patch[i][j * 2 + 1]);
			}
		}
	}

	// unused
	public void setPatch (short[] dump) {
		OPLLPatch[] patch = { new OPLLPatch(), new OPLLPatch() };
		int i;

		for (i = 0; i < 19; i++) {
			dump2patch(dump, i * 16, patch[0], patch[1]);
			patch[i * 2] = patch[0].clone();
			patch[i * 2 + 1] = patch[1].clone();
		}
	}

	// unused
	public void patch2dump(OPLLPatch patch0, OPLLPatch patch1, int[] dump, int offset) {
		dump[offset] = 0xFF & ((patch0.AM << 7) + (patch0.PM << 6) + (patch0.EG << 5) + (patch0.KR << 4) + patch0.ML);
		dump[1 + offset] = 0xFF
				& ((patch1.AM << 7) + (patch1.PM << 6) + (patch1.EG << 5) + (patch1.KR << 4) + patch1.ML);
		dump[2 + offset] = 0xFF & ((patch0.KL << 6) + patch0.TL);
		dump[3 + offset] = 0xFF & ((patch1.KL << 6) + (patch1.WF << 4) + (patch0.WF << 3) + patch0.FB);
		dump[4 + offset] = 0xFF & ((patch0.AR << 4) + patch0.DR);
		dump[5 + offset] = 0xFF & ((patch1.AR << 4) + patch1.DR);
		dump[6 + offset] = 0xFF & ((patch0.SL << 4) + patch0.RR);
		dump[7 + offset] = 0xFF & ((patch1.SL << 4) + patch1.RR);
		dump[8 + offset] = 0;
		dump[9 + offset] = 0;
		dump[10 + offset] = 0;
		dump[11 + offset] = 0;
		dump[12 + offset] = 0;
		dump[13 + offset] = 0;
		dump[14 + offset] = 0;
		dump[15 + offset] = 0;
	}
	
/* ***********************************************************
    OPLL internal interfaces
*********************************************************** */
	
	/**
	 * Slot key on without reseting the phase
	 * @param slot
	 */
	private void slotOn2(OPLLSlot slot) {
		slot.eg_mode = ATTACK;
		slot.eg_phase = 0;
		slot.eg_dphase = slot.calc_eg_dphase(); // UPDATE_EG(slot);
	}
	
	/**
	 * Channel key on
	 */
	private void keyOn(int i) {
		if (slot_on_flag[i * 2] == 0) {
			slots[(i) << 1].slotOn(); // MOD(opll,i)
		}

		if ((slot_on_flag[i * 2 + 1]) == 0) {
			slots[((i) << 1) | 1].slotOn(); // CAR(opll,i)
		}
		key_status[i] = 1;
	}
	
	/**
	 * Channel key off
	 */
	private void keyOff(int i) {
		if (slot_on_flag[i * 2 + 1] != 0)
			slots[((i) << 1) | 1].slotOff(); // CAR(opll,i)
		key_status[i] = 0;
	}
	
	private void keyOn_BD() {
		keyOn(6);
	}
	
	private void keyOn_SD() {
		if (slot_on_flag[SLOT_SD] == 0)
			slots[((7) << 1) | 1].slotOn(); // CAR(opll,7)
	}

	private void keyOn_TOM() {
		if (slot_on_flag[SLOT_TOM] == 0)
			slots[(8) << 1].slotOn(); // MOD(opll,8)
	}

	private void keyOn_HH() {
		if (slot_on_flag[SLOT_HH] == 0)
			slotOn2(slots[(7) << 1]); // MOD(opll,7)
	}
	
	private void keyOn_CYM() {
		if (slot_on_flag[SLOT_CYM] == 0)
			slotOn2((slots[((8) << 1) | 1])); // CAR(opll,8)
	}
	
	// Drum key off
	private void keyOff_BD() {
		keyOff(6);
	}
	
	private void keyOff_SD() {
		if (slot_on_flag[SLOT_SD] != 0)
			slots[((7) << 1) | 1].slotOff(); // CAR(opll,7)
	}
	
	private void keyOff_TOM() {
		if (slot_on_flag[SLOT_TOM] != 0)
			slots[(8) << 1].slotOff(); // MOD(opll,8)
	}
	
	private void keyOff_HH() {
		if (slot_on_flag[SLOT_HH] != 0)
			slots[(7) << 1].slotOff(); // MOD(opll,7)
	}
	
	private void keyOff_CYM() {
		if (slot_on_flag[SLOT_CYM] != 0)
			slots[((8) << 1) | 1].slotOff(); // CAR(opll,8)
	}
	
	/**
	 *  Change a voice
	 */
	private void setPatch(int i, int num) {
		patch_number[i] = num;
		slots[i << 1].patch = patches[num * 2]; // MOD(opll,i)
		slots[((i) << 1) | 1].patch = patches[num * 2 + 1]; // CAR(opll,i)
	}
	
	/**
	 * Set sustine parameter
	 */
	private void setSustine(int c, int sustine) {
		slots[(c << 1) | 1].sustine = sustine; // CAR(opll,c)
		if (slots[c << 1].type != 0) // MOD(opll,c)
			slots[c << 1].sustine = sustine; // MOD(opll,c)
	}
	
	/**
	 * Volume : 6bit ( Volume register << 2 )
	 */
	private void setVolume(int c, int volume) {
		slots[(c << 1) | 1].volume = volume; // CAR(opll,c)
	}
	
	/**
	 * Set F-Number ( fnum : 9bit ) 
	 */
	private void setFnumber(int c, int fnum) {
		slots[(c << 1) | 1].fnum = fnum; // CAR(opll,c)
		slots[c << 1].fnum = fnum; // MOD(opll,c)
	}
	
	/**
	 * Set Block data (block : 3bit )
	 */
	private void setBlock(int c, int block) {
		slots[(c << 1) | 1].block = block; // CAR(opll,c)
		slots[c << 1].block = block; // MOD(opll,c)
	}
	
	/**
	 * Change Rhythm Mode
	 */
	private void update_rhythm_mode() {
		if ((patch_number[6] & 0x10) != 0) {
			if ((slot_on_flag[SLOT_BD2] | (regs[0x0e] & 32)) == 0) {
				slots[SLOT_BD1].eg_mode = FINISH;
				slots[SLOT_BD2].eg_mode = FINISH;
				setPatch(6, regs[0x36] >> 4);
			}
		} else if ((regs[0x0e] & 32) != 0) {
			patch_number[6] = 16;
			slots[SLOT_BD1].eg_mode = FINISH;
			slots[SLOT_BD2].eg_mode = FINISH;
			slots[SLOT_BD1].setPatch(patches[16 * 2 + 0]);
			slots[SLOT_BD2].setPatch(patches[16 * 2 + 1]);
		}

		if ((patch_number[7] & 0x10) != 0) {
			if (!((slot_on_flag[SLOT_HH] != 0 && slot_on_flag[SLOT_SD] != 0) || (regs[0x0e] & 32) != 0)) {
				slots[SLOT_HH].type = 0;
				slots[SLOT_HH].eg_mode = FINISH;
				slots[SLOT_SD].eg_mode = FINISH;
				setPatch(7, regs[0x37] >> 4);
			}
		} else if ((regs[0x0e] & 32) != 0) {
			patch_number[7] = 17;
			slots[SLOT_HH].type = 1;
			slots[SLOT_HH].eg_mode = FINISH;
			slots[SLOT_SD].eg_mode = FINISH;
			slots[SLOT_HH].setPatch(patches[17 * 2 + 0]);
			slots[SLOT_SD].setPatch(patches[17 * 2 + 1]);
		}

		if ((patch_number[8] & 0x10) != 0) {
			if (!((slot_on_flag[SLOT_CYM] != 0 && slot_on_flag[SLOT_TOM] != 0)
					|| (regs[0x0e] & 32) != 0)) {
				slots[SLOT_TOM].type = 0;
				slots[SLOT_TOM].eg_mode = FINISH;
				slots[SLOT_CYM].eg_mode = FINISH;
				setPatch(8, regs[0x38] >> 4);
			}
		} else if ((regs[0x0e] & 32) != 0) {
			patch_number[8] = 18;
			slots[SLOT_TOM].type = 1;
			slots[SLOT_TOM].eg_mode = FINISH;
			slots[SLOT_CYM].eg_mode = FINISH;
			slots[SLOT_TOM].setPatch(patches[18 * 2 + 0]);
			slots[SLOT_CYM].setPatch(patches[18 * 2 + 1]);
		}
	}
	
	private void update_key_status() {
		int ch;

		for (ch = 0; ch < 9; ch++)
			slot_on_flag[ch * 2] = slot_on_flag[ch * 2 + 1] = (regs[0x20 + ch]) & 0x10;

		if ((regs[0x0e] & 32) != 0) {
			slot_on_flag[SLOT_BD1] |= (regs[0x0e] & 0x10);
			slot_on_flag[SLOT_BD2] |= (regs[0x0e] & 0x10);
			slot_on_flag[SLOT_SD] |= (regs[0x0e] & 0x08);
			slot_on_flag[SLOT_HH] |= (regs[0x0e] & 0x01);
			slot_on_flag[SLOT_TOM] |= (regs[0x0e] & 0x04);
			slot_on_flag[SLOT_CYM] |= (regs[0x0e] & 0x02);
		}
	}
	
	private void copyPatch(int num, OPLLPatch patch) {
		this.patches[num] = patch.clone();
	}
	
/* ***********************************************************
    Initializing
*********************************************************** */
	
	private void internal_refresh() {

		// makeDphaseTable()
		{
			int fnum, block, ML;
			int[] mltable = { 1, 1 * 2, 2 * 2, 3 * 2, 4 * 2, 5 * 2, 6 * 2, 7 * 2, 8 * 2, 9 * 2, 10 * 2, 10 * 2, 12 * 2,
					12 * 2, 15 * 2, 15 * 2 };

			for (fnum = 0; fnum < 512; fnum++) {
				for (block = 0; block < 8; block++) {
					// #define RATE_ADJUST(x) (rate==49716?x:(e_uint32)((double)(x)*clk/72/rate + 0.5))
					for (ML = 0; ML < 16; ML++) {
						int x = ((fnum * mltable[ML]) << block) >> (20 - DP_BITS);
						dphaseTable[fnum][block][ML] = (rate == 49716 ? x
								: (int) ((double) (x) * clk / 72 / rate + 0.5));
					}
				}
			}
		}

		// makeDphaseARTable();
		/* Rate Table for Attack */
		{
			int AR, Rks, RM, RL;
			for (AR = 0; AR < 16; AR++) {
				for (Rks = 0; Rks < 16; Rks++) {
					RM = AR + (Rks >> 2);
					RL = Rks & 3;
					if (RM > 15)
						RM = 15;
					switch (AR) {
					case 0:
						dphaseARTable[AR][Rks] = 0;
						break;
					case 15:
						dphaseARTable[AR][Rks] = 0;/* EG_DP_WIDTH; */
						break;
					default:
						int x = 3 * (RL + 4) << (RM + 1);
						dphaseARTable[AR][Rks] = (rate == 49716 ? x : (int) ((double) (x) * clk / 72 / rate + 0.5));
						break;
					}
				}
			}
		}
		
		// makeDphaseDRTable()
		{
			int DR, Rks, RM, RL;

			for (DR = 0; DR < 16; DR++) {
				for (Rks = 0; Rks < 16; Rks++) {
					RM = DR + (Rks >> 2);
					RL = Rks & 3;
					if (RM > 15)
						RM = 15;
					switch (DR) {
					case 0:
						dphaseDRTable[DR][Rks] = 0;
						break;
					default: {
						int x = (RL + 4) << (RM - 1);
						dphaseDRTable[DR][Rks] = (rate == 49716 ? x : (int) ((double) (x) * clk / 72 / rate + 0.5));
					} break;
					}
				}
			}
		}

		double x = (PM_SPEED * PM_DP_WIDTH / (clk / 72));
		pm_dphase = (rate == 49716 ? (int) x : (int) (x * clk / 72 / rate + 0.5)) & 0x7FFFFFFF;
		x = AM_SPEED * AM_DP_WIDTH / (clk / 72);
		am_dphase = (rate == 49716 ? (int) x : (int) (x * clk / 72 / rate + 0.5)) & 0x7FFFFFFF;
	}
	
	/**
	 * 仅初始化时调用
	 * @param c
	 *   unsigned
	 * @param r
	 *   unsigned, 48000
	 */
	private void maketables(int c, int r) {
		if (c != clk) {
			clk = c;
			
			// makePmTable();
			{
				for (int i = 0; i < PM_PG_WIDTH; i++) {
					double phase = 2.0 * Math.PI * i / PM_PG_WIDTH, d;
					// saw begin - inline
					if (phase <= Math.PI / 2)
						d = phase * 2 / Math.PI;
					else if (phase <= Math.PI * 3 / 2)
						d = 2.0 - (phase * 2 / Math.PI);
					else
						d = -4.0 + phase * 2 / Math.PI;
					// saw end - inline
					pmtable[i] = (int) ((double) PM_AMP * Math.pow(2, (double) PM_DEPTH * d / 1200));
				}
			}
			// makeAmTable();
			{
				for (int i = 0; i < AM_PG_WIDTH; i++) {
					double phase = 2.0 * Math.PI * i / PM_PG_WIDTH, d;
					// saw begin - inline
					if (phase <= Math.PI / 2)
						d = phase * 2 / Math.PI;
					else if (phase <= Math.PI * 3 / 2)
						d = 2.0 - (phase * 2 / Math.PI);
					else
						d = -4.0 + phase * 2 / Math.PI;
					// saw end - inline
					amtable[i] = (int) ((double) AM_DEPTH / 2 / DB_STEP * (1.0 + d));
				}
			}
			// makeDB2LinTable();
			{
				for (int i = 0; i < DB_MUTE + DB_MUTE; i++) {
					DB2LIN_TABLE[i] = (int) ((double) ((1 << DB2LIN_AMP_BITS) - 1)
							* Math.pow(10, -(double) i * DB_STEP / 20));
					if (i >= DB_MUTE)
						DB2LIN_TABLE[i] = 0;
					DB2LIN_TABLE[i + DB_MUTE + DB_MUTE] = (int) (-DB2LIN_TABLE[i]);
				}
			}
			// makeAdjustTable();
			{
				AR_ADJUST_TABLE[0] = (1 << EG_BITS) - 1;
				for (int i = 1; i < (1 << EG_BITS); i++) {
					AR_ADJUST_TABLE[i] = (int) ((double) (1 << EG_BITS) - 1
							- ((1 << EG_BITS) - 1) * Math.log(i) / Math.log(127));
				}
			}
			// makeTllTable();
			{
				double kltable[] = { 
						0.00, 18.00, 24.00, 27.75, 30.00, 32.25, 33.75, 35.25,
						36.00, 37.50, 38.25, 39.00, 39.75, 40.50, 41.25, 42.00
						};

				int tmp;
				for (int fnum = 0; fnum < 16; fnum++)
					for (int block = 0; block < 8; block++)
						for (int TL = 0; TL < 64; TL++)
							for (int KL = 0; KL < 4; KL++) {
								if (KL == 0) {
									tllTable[fnum][block][TL][KL] = (TL * (int) (TL_STEP / EG_STEP));
								} else {
									tmp = (int) (kltable[fnum] - (6.00) * (7 - block));
									if (tmp <= 0)
										tllTable[fnum][block][TL][KL] = (TL) * (int) (TL_STEP / EG_STEP);
									else
										tllTable[fnum][block][TL][KL] = (int) ((tmp >> (3 - KL)) / EG_STEP)
												+ (TL) * (int) (TL_STEP / EG_STEP);
								}
							}
			}
			// makeRksTable();
			{
				int fnum8, block, KR;

				for (fnum8 = 0; fnum8 < 2; fnum8++)
					for (block = 0; block < 8; block++)
						for (KR = 0; KR < 2; KR++) {
							if (KR != 0)
								rksTable[fnum8][block][KR] = (block << 1) + fnum8;
							else
								rksTable[fnum8][block][KR] = block >> 1;
						}
			}
			// makeSinTable();
			{
				int i;

				for (i = 0; i < PG_WIDTH / 4; i++) {
					double d = Math.sin(2.0 * Math.PI * i / PG_WIDTH), v;
					// lin2db begin - inline
					if (d == 0)
						v = (DB_MUTE - 1);
					else
						v = Math.min(-(int) (20.0 * Math.log10(d) / DB_STEP), DB_MUTE - 1); /* 0 -- 127 */
					// lin2db end
					fullsintable[i] = (int) v;
				}

				for (i = 0; i < PG_WIDTH / 4; i++) {
					fullsintable[PG_WIDTH / 2 - 1 - i] = fullsintable[i];
				}

				for (i = 0; i < PG_WIDTH / 2; i++) {
					fullsintable[PG_WIDTH / 2 + i] = (int) (DB_MUTE + DB_MUTE + fullsintable[i]);
				}

				for (i = 0; i < PG_WIDTH / 2; i++)
					halfsintable[i] = fullsintable[i];
				for (i = PG_WIDTH / 2; i < PG_WIDTH; i++)
					halfsintable[i] = fullsintable[0];
			}
			
			makeDefaultPatch();
		}

		if (r != rate) {
			rate = r;
			internal_refresh();
		}
	}

	public OPLL() {
		this(3579545, 49716); // default
	}
	
	/**
	 * init OPLL
	 * @param clk
	 *   unsigned
	 * @param rate
	 *   unsigned
	 * @return
	 */
	public OPLL (int clk, int rate) {
		for (int i = 0; i < slots.length; i++) {
			slots[i] = new OPLLSlot(this);
		}
		for (int i = 0; i < patches.length; i++) {
			patches[i] = new OPLLPatch();
		}
		
		initSound();
		maketables(clk, rate);

		for (int i = 0; i < 19 * 2; i++) {
			patches[i] = null_patch.clone();
		}

		mask = 0;

		reset();
		reset_patch (0);

	}
	
	/**
	 * Reset patch datas by system default.
	 */
	public void reset_patch(int type) {
		int i;

		for (i = 0; i < 19 * 2; i++) {
			copyPatch(i, default_patch[type % OPLL_TONE_NUM][i]);
		}
	}
	
	/**
	 * Reset whole of OPLL except patch datas.
	 */
	public void reset() {
		int i;

		out = 0;

		pm_phase = 0;
		am_phase = 0;

		noise_seed = 0xffff;
		mask = 0;

		for (i = 0; i < 18; i++)
			slots[i].reset(i % 2);

		for (i = 0; i < 9; i++) {
			key_status[i] = 0;
			setPatch(i, 0);
		}

		for (i = 0; i < 0x40; i++)
			writeReg(i, 0);

		long factor = 1l << 31;
		realstep = (int) (factor / rate);
		if (realstep < 0) {
			System.out.println("opll.realstep < 0");
		}
		opllstep = (int) (factor / (clk / 72));
		oplltime = 0;
		for (i = 0; i < 14; i++)
			pans[i] = 2;
		sprev[0] = sprev[1] = 0;
		snext[0] = snext[1] = 0;
	}
	
	/**
	 * Force Refresh (When external program changes some parameters).
	 */
	public void forceRefresh() {
		int i;

		for (i = 0; i < 9; i++)
			setPatch(i, patch_number[i]);

		for (i = 0; i < 18; i++) {
			OPLLSlot s = slots[i];
			s.dphase = dphaseTable[s.fnum][s.block][s.patch.ML];
			s.rks = rksTable[(s.fnum) >> 8][s.block][s.patch.KR];

			if (s.type == 0) {
				s.tll = tllTable[(s.fnum) >> 5][s.block][s.patch.TL][s.patch.KL];
			} else {
				s.tll = tllTable[(s.fnum) >> 5][s.block][s.volume][s.patch.KL];
			}
			s.sintbl = waveform[s.patch.WF];
			s.eg_dphase = s.calc_eg_dphase();
		}
	}
	
	public void setRate(int r) {
		if (quality != 0)
			rate = 49716;
		else
			rate = r;
		internal_refresh();
		rate = r;
	}
	
	public void setQuality(int q) {
		quality = q;
		setRate(rate);
	}
	
/* ***********************************************************
    Generate wave data
*********************************************************** */
	
	/**
	 * Update AM, PM unit
	 * @param opll
	 */
	private void update_ampm() {
		pm_phase = (pm_phase + pm_dphase) & (PM_DP_WIDTH - 1);
		am_phase = (am_phase + am_dphase) & (AM_DP_WIDTH - 1);
		lfo_am = amtable[(am_phase) >> (AM_DP_BITS - AM_PG_BITS)];
		lfo_pm = pmtable[(pm_phase) >> (PM_DP_BITS - PM_PG_BITS)];
	}
	
	/**
	 * Update Noise unit
	 */
	private void update_noise () {
		if ((noise_seed & 1) != 0)
			noise_seed ^= 0x8003020;
		noise_seed >>= 1;
	}
	
	private int calc1() {
		int inst = 0, perc = 0, out = 0;
		int i;

		update_ampm();
		update_noise();

		for (i = 0; i < 18; i++) {
			slots[i].calc_phase(lfo_pm);
			slots[i].calc_envelope(lfo_am);
		}

		for (i = 0; i < 6; i++)
			if ((mask & (1 << i)) == 0 && (slots[(i << 1) | 1].eg_mode != FINISH))
				inst += slots[(i << 1) | 1].calc_slot_car(slots[i << 1].calc_slot_mod());

		/* CH6 */
		if (patch_number[6] <= 15) {
			if ((mask & (1 << 6)) == 0 && (slots[(6 << 1) | 1].eg_mode != FINISH))
				inst += slots[(6 << 1) | 1].calc_slot_car(slots[6 << 1].calc_slot_mod());
		} else {
			if ((mask & 1 << 13) == 0 && (slots[(6 << 1) | 1].eg_mode != FINISH))
				perc += slots[(6 << 1) | 1].calc_slot_car(slots[6 << 1].calc_slot_mod());
		}

		/* CH7 */
		if (patch_number[7] <= 15) {
			if ((mask & (1 << 7)) == 0 && (slots[(7 << 1) | 1].eg_mode != FINISH))
				inst += slots[(7 << 1) | 1].calc_slot_car(slots[7 << 1].calc_slot_mod());
		} else {
			if ((mask & 1 << 9) == 0 && (slots[7 << 1].eg_mode != FINISH))
				perc += slots[7 << 1].calc_slot_hat(slots[(8 << 1) | 1].pgout, noise_seed & 1);
			if ((mask & 1 << 12) == 0 && (slots[(7 << 1) | 1].eg_mode != FINISH))
				perc -= slots[(7 << 1) | 1].calc_slot_snare(noise_seed & 1);
		}

		/* CH8 */
		if (patch_number[8] <= 15) {
			if ((mask & (1 << 8)) == 0 && (slots[(8 << 1) | 1].eg_mode != FINISH))
				inst += slots[(8 << 1) | 1].calc_slot_car(slots[8 << 1].calc_slot_mod());
		} else {
			if ((mask & 1 << 11) == 0 && (slots[8 << 1].eg_mode != FINISH))
				perc += slots[i << 1].calc_slot_tom();
			if ((mask & 1 << 10) == 0 && (slots[(8 << 1) | 1].eg_mode != FINISH))
				perc -= slots[(8 << 1) | 1].calc_slot_cym(slots[7 << 1].pgout);
		}

		out = inst + (perc << 1);
		return out << 3;
	}
	
	public int calc0() {
		if (quality == 0)
			return calc1();

		while (realstep > oplltime) {
			oplltime += opllstep;
			prev = next;
			next = calc1();
		}

		oplltime -= realstep;
		out = (int) (((double) next * (opllstep - oplltime) + (double) prev * oplltime)
				/ opllstep);

		return (int) out;
	}
	
	/**
	 * @param mask
	 *   unsigned
	 * @return
	 *   unsigned
	 */
	public int setMask(int m) {
		int ret = mask;
		mask = m;
		return ret;
	}
	
	/**
	 * @param mask
	 *   unsigned
	 * @return
	 *   unsigned
	 */
	public int toggleMask(int mask) {
		int ret = mask;
		mask ^= mask;
		return ret;
	}
	
/* ***********************************************************
    I/O Ctrl
*********************************************************** */
	
	/**
	 * @param reg
	 *   unsigned
	 * @param data
	 *   unsigned
	 */
	public void writeReg(int reg, int data) {
		int i, v, ch;

		data = data & 0xff;
		reg = reg & 0x3f;
		this.regs[reg] = data;

		switch (reg) {
		case 0x00:
			patches[0].AM = (data >> 7) & 1;
			patches[0].PM = (data >> 6) & 1;
			patches[0].EG = (data >> 5) & 1;
			patches[0].KR = (data >> 4) & 1;
			patches[0].ML = (data) & 15;
			for (i = 0; i < 9; i++) {
				if (patch_number[i] == 0) {
					OPLLSlot s = slots[i << 1];
					s.dphase = dphaseTable[s.fnum][s.block][s.patch.ML];
					s.rks = rksTable[(s.fnum) >> 8][s.block][s.patch.KR];
					s.eg_dphase = s.calc_eg_dphase();
				}
			}
			break;

		case 0x01:
			patches[1].AM = (data >> 7) & 1;
			patches[1].PM = (data >> 6) & 1;
			patches[1].EG = (data >> 5) & 1;
			patches[1].KR = (data >> 4) & 1;
			patches[1].ML = (data) & 15;
			for (i = 0; i < 9; i++) {
				if (patch_number[i] == 0) {
					OPLLSlot s = slots[(i << 1) | 1];
					s.dphase = dphaseTable[s.fnum][s.block][s.patch.ML];
					s.rks = rksTable[(s.fnum) >> 8][s.block][s.patch.KR];
					s.eg_dphase = s.calc_eg_dphase();
				}
			}
			break;

		case 0x02:
			patches[0].KL = (data >> 6) & 3;
			patches[0].TL = (data) & 63;
			for (i = 0; i < 9; i++) {
				if (patch_number[i] == 0) {
					OPLLSlot s = slots[i << 1];
					if (s.type == 0) {
						s.tll = tllTable[(s.fnum) >> 5][s.block][s.patch.TL][s.patch.KL];
					} else {
						s.tll = tllTable[(s.fnum) >> 5][s.block][s.volume][s.patch.KL];
					}
				}
			}
			break;

		case 0x03:
			patches[1].KL = (data >> 6) & 3;
			patches[1].WF = (data >> 4) & 1;
			patches[0].WF = (data >> 3) & 1;
			patches[0].FB = (data) & 7;
			for (i = 0; i < 9; i++) {
				if (patch_number[i] == 0) {
					OPLLSlot s = slots[i << 1];
					s.sintbl = waveform[s.patch.WF];
					s = slots[(i << 1) | 1];
					s.sintbl = waveform[s.patch.WF];
				}
			}
			break;

		case 0x04:
			patches[0].AR = (data >> 4) & 15;
			patches[0].DR = (data) & 15;
			for (i = 0; i < 9; i++) {
				if (patch_number[i] == 0) {
					OPLLSlot s = slots[i << 1];
					s.eg_dphase = s.calc_eg_dphase();
				}
			}
			break;

		case 0x05:
			patches[1].AR = (data >> 4) & 15;
			patches[1].DR = (data) & 15;
			for (i = 0; i < 9; i++) {
				if (patch_number[i] == 0) {
					OPLLSlot s = slots[(i << 1) | 1];
					s.eg_dphase = s.calc_eg_dphase();
				}
			}
			break;

		case 0x06:
			patches[0].SL = (data >> 4) & 15;
			patches[0].RR = (data) & 15;
			for (i = 0; i < 9; i++) {
				if (patch_number[i] == 0) {
					OPLLSlot s = slots[i << 1];
					s.eg_dphase = s.calc_eg_dphase();
				}
			}
			break;

		case 0x07:
			patches[1].SL = (data >> 4) & 15;
			patches[1].RR = (data) & 15;
			for (i = 0; i < 9; i++) {
				if (patch_number[i] == 0) {
					OPLLSlot s = slots[(i << 1) | 1];
					s.eg_dphase = s.calc_eg_dphase();
				}
			}
			break;

		case 0x0e:
			update_rhythm_mode();
			if ((data & 32) != 0) {
				if ((data & 0x10) != 0)
					keyOn_BD();
				else
					keyOff_BD();
				if ((data & 0x8) != 0)
					keyOn_SD();
				else
					keyOff_SD();
				if ((data & 0x4) != 0)
					keyOn_TOM();
				else
					keyOff_TOM();
				if ((data & 0x2) != 0)
					keyOn_CYM();
				else
					keyOff_CYM();
				if ((data & 0x1) != 0)
					keyOn_HH();
				else
					keyOff_HH();
			}
			update_key_status();

			for (int j = 0; j < 6; j++) {
				OPLLSlot s;
				switch (j) {
				case 0:
					s = slots[6 << 1];
					break;
				case 1:
					s = slots[(6 << 1) | 1];
					break;
				case 2:
					s = slots[7 << 1];
					break;
				case 3:
					s = slots[(7 << 1) | 1];
					break;
				case 4:
					s = slots[8 << 1];
					break;
				case 5:
					s = slots[(8 << 1) | 1];
					break;

				default:
					s = null;
					break;
				}

				s.dphase = dphaseTable[s.fnum][s.block][s.patch.ML];
				if (s.type == 0) {
					s.tll = tllTable[(s.fnum) >> 5][s.block][s.patch.TL][s.patch.KL];
				} else {
					s.tll = tllTable[(s.fnum) >> 5][s.block][s.volume][s.patch.KL];
				}
				s.rks = rksTable[(s.fnum) >> 8][s.block][s.patch.KR];
				s.sintbl = waveform[s.patch.WF];
				s.eg_dphase = s.calc_eg_dphase();
			}
			break;

		case 0x0f:
			break;

		case 0x10:
		case 0x11:
		case 0x12:
		case 0x13:
		case 0x14:
		case 0x15:
		case 0x16:
		case 0x17:
		case 0x18:
			ch = reg - 0x10;
			setFnumber(ch, data + ((this.regs[0x20 + ch] & 1) << 8));

		{
			OPLLSlot s = slots[ch << 1];
			s.dphase = dphaseTable[s.fnum][s.block][s.patch.ML];
			if (s.type == 0) {
				s.tll = tllTable[(s.fnum) >> 5][s.block][s.patch.TL][s.patch.KL];
			} else {
				s.tll = tllTable[(s.fnum) >> 5][s.block][s.volume][s.patch.KL];
			}
			s.rks = rksTable[(s.fnum) >> 8][s.block][s.patch.KR];
			s.sintbl = waveform[s.patch.WF];
			s.eg_dphase = s.calc_eg_dphase();
			
			s = slots[(ch << 1) | 1];
			s.dphase = dphaseTable[s.fnum][s.block][s.patch.ML];
			if (s.type == 0) {
				s.tll = tllTable[(s.fnum) >> 5][s.block][s.patch.TL][s.patch.KL];
			} else {
				s.tll = tllTable[(s.fnum) >> 5][s.block][s.volume][s.patch.KL];
			}
			s.rks = rksTable[(s.fnum) >> 8][s.block][s.patch.KR];
			s.sintbl = waveform[s.patch.WF];
			s.eg_dphase = s.calc_eg_dphase();
		}
			break;

		case 0x20:
		case 0x21:
		case 0x22:
		case 0x23:
		case 0x24:
		case 0x25:
		case 0x26:
		case 0x27:
		case 0x28:
			ch = reg - 0x20;
			setFnumber(ch, ((data & 1) << 8) + this.regs[0x10 + ch]);
			setBlock(ch, (data >> 1) & 7);
			setSustine(ch, (data >> 5) & 1);
			if ((data & 0x10) != 0)
				keyOn(ch);
			else
				keyOff(ch); {
			OPLLSlot s = slots[ch << 1];
			s.dphase = dphaseTable[s.fnum][s.block][s.patch.ML];
			if (s.type == 0) {
				s.tll = tllTable[(s.fnum) >> 5][s.block][s.patch.TL][s.patch.KL];
			} else {
				s.tll = tllTable[(s.fnum) >> 5][s.block][s.volume][s.patch.KL];
			}
			s.rks = rksTable[(s.fnum) >> 8][s.block][s.patch.KR];
			s.sintbl = waveform[s.patch.WF];
			s.eg_dphase = s.calc_eg_dphase();
			
			s = slots[(ch << 1) | 1];
			s.dphase = dphaseTable[s.fnum][s.block][s.patch.ML];
			if (s.type == 0) {
				s.tll = tllTable[(s.fnum) >> 5][s.block][s.patch.TL][s.patch.KL];
			} else {
				s.tll = tllTable[(s.fnum) >> 5][s.block][s.volume][s.patch.KL];
			}
			s.rks = rksTable[(s.fnum) >> 8][s.block][s.patch.KR];
			s.sintbl = waveform[s.patch.WF];
			s.eg_dphase = s.calc_eg_dphase();
		}
			update_key_status();
			update_rhythm_mode();
			break;

		case 0x30:
		case 0x31:
		case 0x32:
		case 0x33:
		case 0x34:
		case 0x35:
		case 0x36:
		case 0x37:
		case 0x38:
			i = (data >> 4) & 15;
			v = data & 15;
			if ((this.regs[0x0e] & 32) != 0 && (reg >= 0x36)) {
				switch (reg) {
				case 0x37:
					slots[7 << 1].setVolume(i << 2);
					break;
				case 0x38:
					slots[8 << 1].setVolume(i << 2);
					break;
				default:
					break;
				}
			} else {
				setPatch(reg - 0x30, i);
			}
			setVolume(reg - 0x30, v << 2);

		{
			OPLLSlot s = slots[(reg - 0x30) << 1];
			s.dphase = dphaseTable[s.fnum][s.block][s.patch.ML];
			if (s.type == 0) {
				s.tll = tllTable[(s.fnum) >> 5][s.block][s.patch.TL][s.patch.KL];
			} else {
				s.tll = tllTable[(s.fnum) >> 5][s.block][s.volume][s.patch.KL];
			}
			s.rks = rksTable[(s.fnum) >> 8][s.block][s.patch.KR];
			s.sintbl = waveform[s.patch.WF];
			s.eg_dphase = s.calc_eg_dphase();

			s = slots[((reg - 0x30) << 1) | 1];
			s.dphase = dphaseTable[s.fnum][s.block][s.patch.ML];
			if (s.type == 0) {
				s.tll = tllTable[(s.fnum) >> 5][s.block][s.patch.TL][s.patch.KL];
			} else {
				s.tll = tllTable[(s.fnum) >> 5][s.block][s.volume][s.patch.KL];
			}
			s.rks = rksTable[(s.fnum) >> 8][s.block][s.patch.KR];
			s.sintbl = waveform[s.patch.WF];
			s.eg_dphase = s.calc_eg_dphase();
		}
			break;

		default:
			break;
		}
	}
	
	/**
	 * STEREO MODE (OPT)
	 * @param ch
	 *   unsigned
	 * @param pan
	 *   unsigned
	 */
	public void set_pan(int ch, int pan) {
		this.pans[ch & 15] = pan & 3;
	}
	
	/**
	 * static calc_stereo
	 * @param out
	 *   len : 2
	 */
	public void calc_stereo0(int[] out) {
		int b[] = { 0, 0, 0, 0 }; /* Ignore, Right, Left, Center */
		int r[] = { 0, 0, 0, 0 }; /* Ignore, Right, Left, Center */
		int i;

		update_ampm();
		update_noise();

		for (i = 0; i < 18; i++) {
			slots[i].calc_phase(lfo_pm);
			slots[i].calc_envelope(lfo_am);
		}

		for (i = 0; i < 6; i++)
			if ((mask & (1 << i)) == 0 && (slots[(i << 1) | 1].eg_mode != FINISH))
				b[pans[i]] += slots[(i << 1) | 1].calc_slot_car(slots[i << 1].calc_slot_mod());

		if (patch_number[6] <= 15) {
			if ((mask & (1 << 6)) == 0 && (slots[(6 << 1) | 1].eg_mode != FINISH))
				b[pans[6]] += slots[(6 << 1) | 1].calc_slot_car(slots[6 << 1].calc_slot_mod());
		} else {
			if ((mask & (1 << 13)) == 0 && (slots[(6 << 1) | 1].eg_mode != FINISH))
				r[pans[9]] += slots[(6 << 1) | 1].calc_slot_car(slots[6 << 1].calc_slot_mod());
		}

		if (patch_number[7] <= 15) {
			if ((mask & (1 << 7)) == 0 && (slots[(7 << 1) | 1].eg_mode != FINISH))
				b[pans[7]] += slots[(7 << 1) | 1].calc_slot_car(slots[7 << 1].calc_slot_mod());
		} else {
			if ((mask & (1 << 9)) == 0 && (slots[7 << 1].eg_mode != FINISH))
				r[pans[10]] += slots[7 << 1].calc_slot_hat(slots[(8 << 1) | 1].pgout, noise_seed & 1);
			if ((mask & (1 << 12)) == 0 && (slots[(7 << 1) | 1].eg_mode != FINISH))
				r[pans[11]] -= slots[(7 << 1) | 1].calc_slot_snare(noise_seed & 1);
		}

		if (patch_number[8] <= 15) {
			if ((mask & (1 << 8)) == 0 && (slots[(8 << 1) | 1].eg_mode != FINISH))
				b[pans[8]] += slots[(8 << 1) | 1].calc_slot_car(slots[8 << 1].calc_slot_mod());
		} else {
			if ((mask & (1 << 11)) == 0 && (slots[8 << 1].eg_mode != FINISH))
				r[pans[12]] += slots[8 << 1].calc_slot_tom();
			if ((mask & (1 << 10)) == 0 && (slots[(8 << 1) | 1].eg_mode != FINISH))
				r[pans[13]] -= slots[(8 << 1) | 1].calc_slot_cym(slots[7 << 1].pgout);
		}

		out[1] = (b[1] + b[3] + ((r[1] + r[3]) << 1)) << 3;
		out[0] = (b[2] + b[3] + ((r[2] + r[3]) << 1)) << 3;
	}
	
	/**
	 * OPLL_calc_stereo
	 * @param out
	 *   len : 2
	 */
	public void calc_stereo1(int[] out) {
		if (quality == 0) {
			calc_stereo0(out);
			return;
		}

		while (realstep > oplltime) {
			oplltime += opllstep;
			sprev[0] = snext[0];
			sprev[1] = snext[1];
			calc_stereo0(snext);
		}

		oplltime -= realstep;
		out[0] = (int) (((double) snext[0] * (opllstep - oplltime)
				+ (double) sprev[0] * oplltime) / opllstep);
		out[1] = (int) (((double) snext[1] * (opllstep - oplltime)
				+ (double) sprev[1] * oplltime) / opllstep);
	}
	
	/* **********
	 *  发声器  *
	 ********** */
	
	final RawSoundVRC7[] sounds = new RawSoundVRC7[6];
	
	private void initSound() {
		for (int i = 0; i < sounds.length; i++) {
			sounds[i] = new RawSoundVRC7(this, i);
		}
	}
	
	/**
	 * 获得发声器实例
	 * @param index
	 *   范围 [0, 5]
	 * @return
	 */
	public RawSoundVRC7 getSound(int index) {
		return sounds[index];
	}

}
