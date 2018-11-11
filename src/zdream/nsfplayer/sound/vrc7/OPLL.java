package zdream.nsfplayer.sound.vrc7;

import static zdream.nsfplayer.sound.vrc7.VRC7Static.*;

/**
 * 只在 VRC7 中使用
 * @author Zdream
 */
public class OPLL {

	/** unsigned */
	int realstep;
	/** unsigned */
	int opllstep;

	// Register
	/** unsigned */
	public int[] regs = new int[0x40];

	// Channel Data

	/** Slot */
	public OPLLSlot[] slots = new OPLLSlot[12];

	// Voice Data
	/** 19 x 2 = 38 */
	OPLLPatch[] patches = new OPLLPatch[38];
	/** flag for check patch update */
	
	// 上面是原工程 OPLL 的变量
	
	/** Input clock, unsigned */
	int clk = 0;
	/** Sampling rate, unsigned */
	int rate = 0;

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

	// Basic voice Data
	/**
	 * 默认的 patch 数值. 大小: [8][38]
	 */
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
		patch0.AM = (dump[offset] & 0x80) != 0;
		patch1.AM = (dump[1 + offset] & 0x80) != 0;
		patch0.PM = (dump[offset] & 0x40) != 0;
		patch1.PM = (dump[1 + offset] & 0x40) != 0;
		patch0.EG = (dump[offset] & 0x20) != 0;
		patch1.EG = (dump[1 + offset] & 0x20) != 0;
		patch0.KR = (dump[offset] & 0x10) != 0;
		patch1.KR = (dump[1 + offset] & 0x10) != 0;
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
	
/* ***********************************************************
    OPLL internal interfaces
*********************************************************** */
	
	/**
	 * Channel key on
	 */
	public void keyOn(int i) {
		RawSoundVRC7 sound = sounds[i];
		
		if (!sound.modOn) {
			sound.modulatorSlot.slotOn();
		}
		if (!sound.carOn) {
			sound.carriorSlot.slotOn();
		}
	}
	
	/**
	 * Channel key off
	 */
	public void keyOff(int i) {
		RawSoundVRC7 sound = sounds[i];

		if (sound.carOn) {
			sound.carriorSlot.slotOff();
		}
	}
	
	private void keyOn_BD() {
//		keyOn(6);
	}
	
	private void keyOn_SD() {
//		if (slot_on_flag[SLOT_SD] == 0)
//			slots[((7) << 1) | 1].slotOn(); // CAR(opll,7)
	}

	private void keyOn_TOM() {
//		if (slot_on_flag[SLOT_TOM] == 0)
//			slots[(8) << 1].slotOn(); // MOD(opll,8)
	}

	private void keyOn_HH() {
//		if (slot_on_flag[SLOT_HH] == 0)
//			slots[(7) << 1].slotOn2(); // MOD(opll,7)
	}
	
	private void keyOn_CYM() {
//		if (slot_on_flag[SLOT_CYM] == 0)
//			slots[((8) << 1) | 1].slotOn2(); // CAR(opll,8)
	}
	
	// Drum key off
	private void keyOff_BD() {
//		keyOff(6);
	}
	
	private void keyOff_SD() {
//		if (slot_on_flag[SLOT_SD] != 0)
//			slots[((7) << 1) | 1].slotOff(); // CAR(opll,7)
	}
	
	private void keyOff_TOM() {
//		if (slot_on_flag[SLOT_TOM] != 0)
//			slots[(8) << 1].slotOff(); // MOD(opll,8)
	}
	
	private void keyOff_HH() {
//		if (slot_on_flag[SLOT_HH] != 0)
//			slots[(7) << 1].slotOff(); // MOD(opll,7)
	}
	
	private void keyOff_CYM() {
//		if (slot_on_flag[SLOT_CYM] != 0)
//			slots[((8) << 1) | 1].slotOff(); // CAR(opll,8)
	}
	
	/**
	 *  Change a voice
	 */
	public void setPatch(int i, int num) {
		RawSoundVRC7 sound = sounds[i];
		sound.patchNum = num;
		sound.modulatorSlot.patch.copyFrom(patches[num * 2]);
		sound.carriorSlot.patch.copyFrom(patches[num * 2 + 1]);
	}
	
	/**
	 * Set sustine parameter
	 */
	private void setSustine(int i, boolean sustine) {
		sounds[i].carriorSlot.sustine = sustine;
	}
	
	/**
	 * Volume : 6bit ( Volume register << 2 )
	 */
	public void setVolume(int i, int volume) {
		sounds[i].carriorSlot.volume = volume;
	}
	
	/**
	 * Set F-Number ( fnum : 9bit ) 
	 */
	private void setFnumber(int i, int fnum) {
		RawSoundVRC7 sound = sounds[i];
		sound.modulatorSlot.fnum = fnum;
		sound.carriorSlot.fnum = fnum;
	}
	
	/**
	 * Set Block data (block : 3bit )
	 */
	private void setBlock(int i, int block) {
		RawSoundVRC7 sound = sounds[i];
		sound.modulatorSlot.block = block;
		sound.carriorSlot.block = block;
	}
	
	private void update_key_status() {
		int ch;

		for (ch = 0; ch < 6; ch++) {
			RawSoundVRC7 sound = sounds[ch];
			sound.modOn = sound.carOn = ((regs[0x20 + ch]) & 0x10) != 0;
		}
	}
	
	/* **********
	 *  初始化  *
	 ********** */
	
	/*
	 * Initializing
	 * 初始化需要将所需要的表和 patches 全部重建
	 */
	
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

		rate = r;
		internal_refresh();
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
	public OPLL(int clk, int rate) {
		initSound();
		
		int i = 0;
		for (; i < sounds.length; i++) {
			RawSoundVRC7 sound = sounds[i];
			slots[(i << 1)] = sound.modulatorSlot;
			slots[(i << 1) | 1] = sound.carriorSlot;
		}
		
		for (i = 0; i < patches.length; i++) {
			patches[i] = new OPLLPatch();
		}
		maketables(clk, rate);

		reset();
		reset_patch (0);

	}
	
	/**
	 * Reset patch datas by system default.
	 */
	public void reset_patch(int type) {
		type = type % OPLL_TONE_NUM;
		int i;

		for (i = 0; i < this.patches.length; i++) {
			this.patches[i].copyFrom(default_patch[type][i]);
		}
	}
	
	/**
	 * Reset whole of OPLL except patch datas.
	 */
	public void reset() {
		int i;

		for (i = 0; i < sounds.length; i++) {
			setPatch(i, 0);
			sounds[i].reset();
		}

		long factor = 1l << 31;
		realstep = (int) (factor / rate);
		if (realstep < 0) {
			System.err.println("opll.realstep < 0");
		}
		opllstep = (int) (factor / (clk / 72));
	}
	
	/**
	 * Force Refresh (When external program changes some parameters).
	 */
	public void forceRefresh() {
		int i;

		int length = slots.length / 2;
		for (i = 0; i < length; i++) {
			setPatch(i, sounds[i].patchNum);
		}

		for (i = 0; i < slots.length; i++) {
			slots[i].forceRefresh();
		}
	}
	
/* ***********************************************************
    I/O Ctrl
*********************************************************** */
	
	/**
	 * 获取 0 号 patch
	 * @return
	 */
	public OPLLPatch getCustomModPatch() {
		return patches[0];
	}
	
	public OPLLPatch getCustomCarPatch() {
		return patches[1];
	}
	
	/**
	 * @param reg
	 *   unsigned
	 * @param data
	 *   unsigned
	 */
	@Deprecated
	public void writeReg(int reg, int data) {
		int i, v, ch;

		data = data & 0xff;
		reg = reg & 0x3f;
		this.regs[reg] = data;

		switch (reg) {
		case 0x00:
			patches[0].AM = (data & 0x80) != 0;
			patches[0].PM = (data & 0x40) != 0;
			patches[0].EG = (data & 0x20) != 0;
			patches[0].KR = (data & 0x10) != 0;
			patches[0].ML = (data) & 15;
			for (i = 0; i < 6; i++) {
				RawSoundVRC7 sound = sounds[i];
				if (sound.patchNum == 0) {
					OPLLSlot s = sound.modulatorSlot;
					s.dphase = dphaseTable[s.fnum][s.block][s.patch.ML];
					s.rks = rksTable[(s.fnum) >> 8][s.block][s.patch.KR ? 1 : 0];
					s.eg_dphase = s.calc_eg_dphase();
				}
			}
			break;

		case 0x01:
			patches[1].AM = (data & 0x80) != 0;
			patches[1].PM = (data & 0x40) != 0;
			patches[1].EG = (data & 0x20) != 0;
			patches[1].KR = (data & 0x10) != 0;
			patches[1].ML = (data) & 15;
			for (i = 0; i < 6; i++) {
				RawSoundVRC7 sound = sounds[i];
				if (sound.patchNum == 0) {
					OPLLSlot s = sound.carriorSlot;
					s.dphase = dphaseTable[s.fnum][s.block][s.patch.ML];
					s.rks = rksTable[(s.fnum) >> 8][s.block][s.patch.KR ? 1 : 0];
					s.eg_dphase = s.calc_eg_dphase();
				}
			}
			break;

		case 0x02:
			patches[0].KL = (data >> 6) & 3;
			patches[0].TL = (data) & 63;
			for (i = 0; i < 6; i++) {
				RawSoundVRC7 sound = sounds[i];
				if (sound.patchNum == 0) {
					OPLLSlot s = sound.modulatorSlot;
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
			for (i = 0; i < 6; i++) {
				RawSoundVRC7 sound = sounds[i];
				if (sound.patchNum == 0) {
					OPLLSlot s = sound.modulatorSlot;
					s.sintbl = waveform[s.patch.WF];
					s = sound.carriorSlot;
					s.sintbl = waveform[s.patch.WF];
				}
			}
			break;

		case 0x04:
			patches[0].AR = (data >> 4) & 15;
			patches[0].DR = (data) & 15;
			for (i = 0; i < 6; i++) {
				RawSoundVRC7 sound = sounds[i];
				if (sound.patchNum == 0) {
					OPLLSlot s = sound.modulatorSlot;
					s.eg_dphase = s.calc_eg_dphase();
				}
			}
			break;

		case 0x05:
			patches[1].AR = (data >> 4) & 15;
			patches[1].DR = (data) & 15;
			for (i = 0; i < 6; i++) {
				RawSoundVRC7 sound = sounds[i];
				if (sound.patchNum == 0) {
					OPLLSlot s = sound.carriorSlot;
					s.eg_dphase = s.calc_eg_dphase();
				}
			}
			break;

		case 0x06:
			patches[0].SL = (data >> 4) & 15;
			patches[0].RR = (data) & 15;
			for (i = 0; i < 6; i++) {
				RawSoundVRC7 sound = sounds[i];
				if (sound.patchNum == 0) {
					OPLLSlot s = sound.modulatorSlot;
					s.eg_dphase = s.calc_eg_dphase();
				}
			}
			break;

		case 0x07:
			patches[1].SL = (data >> 4) & 15;
			patches[1].RR = (data) & 15;
			for (i = 0; i < 6; i++) {
				RawSoundVRC7 sound = sounds[i];
				if (sound.patchNum == 0) {
					OPLLSlot s = sound.carriorSlot;
					s.eg_dphase = s.calc_eg_dphase();
				}
			}
			break;

		case 0x0e:
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
			break;

		case 0x0f:
			break;

		case 0x10:
		case 0x11:
		case 0x12:
		case 0x13:
		case 0x14:
		case 0x15:
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
			s.rks = rksTable[(s.fnum) >> 8][s.block][s.patch.KR ? 1 : 0];
			s.sintbl = waveform[s.patch.WF];
			s.eg_dphase = s.calc_eg_dphase();
			
			s = slots[(ch << 1) | 1];
			s.dphase = dphaseTable[s.fnum][s.block][s.patch.ML];
			if (s.type == 0) {
				s.tll = tllTable[(s.fnum) >> 5][s.block][s.patch.TL][s.patch.KL];
			} else {
				s.tll = tllTable[(s.fnum) >> 5][s.block][s.volume][s.patch.KL];
			}
			s.rks = rksTable[(s.fnum) >> 8][s.block][s.patch.KR ? 1 : 0];
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
			ch = reg - 0x20;
			setFnumber(ch, ((data & 1) << 8) + this.regs[0x10 + ch]);
			setBlock(ch, (data >> 1) & 7);
			setSustine(ch, ((data >> 5) & 1) != 0);
			if ((data & 0x10) != 0)
				keyOn(ch);
			else
				keyOff(ch);
		{
			OPLLSlot s = slots[ch << 1];
			s.dphase = dphaseTable[s.fnum][s.block][s.patch.ML];
			if (s.type == 0) {
				s.tll = tllTable[(s.fnum) >> 5][s.block][s.patch.TL][s.patch.KL];
			} else {
				s.tll = tllTable[(s.fnum) >> 5][s.block][s.volume][s.patch.KL];
			}
			s.rks = rksTable[(s.fnum) >> 8][s.block][s.patch.KR ? 1 : 0];
			s.sintbl = waveform[s.patch.WF];
			s.eg_dphase = s.calc_eg_dphase();
			
			s = slots[(ch << 1) | 1];
			s.dphase = dphaseTable[s.fnum][s.block][s.patch.ML];
			if (s.type == 0) {
				s.tll = tllTable[(s.fnum) >> 5][s.block][s.patch.TL][s.patch.KL];
			} else {
				s.tll = tllTable[(s.fnum) >> 5][s.block][s.volume][s.patch.KL];
			}
			s.rks = rksTable[(s.fnum) >> 8][s.block][s.patch.KR ? 1 : 0];
			s.sintbl = waveform[s.patch.WF];
			s.eg_dphase = s.calc_eg_dphase();
		}
			update_key_status();
			break;

		case 0x30:
		case 0x31:
		case 0x32:
		case 0x33:
		case 0x34:
		case 0x35:
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
			s.rks = rksTable[(s.fnum) >> 8][s.block][s.patch.KR ? 1 : 0];
			s.sintbl = waveform[s.patch.WF];
			s.eg_dphase = s.calc_eg_dphase();

			s = slots[((reg - 0x30) << 1) | 1];
			s.dphase = dphaseTable[s.fnum][s.block][s.patch.ML];
			if (s.type == 0) {
				s.tll = tllTable[(s.fnum) >> 5][s.block][s.patch.TL][s.patch.KL];
			} else {
				s.tll = tllTable[(s.fnum) >> 5][s.block][s.volume][s.patch.KL];
			}
			s.rks = rksTable[(s.fnum) >> 8][s.block][s.patch.KR ? 1 : 0];
			s.sintbl = waveform[s.patch.WF];
			s.eg_dphase = s.calc_eg_dphase();
		}
			break;

		default:
			break;
		}
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
