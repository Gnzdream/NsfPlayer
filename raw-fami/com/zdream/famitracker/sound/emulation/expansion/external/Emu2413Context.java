package com.zdream.famitracker.sound.emulation.expansion.external;

public class Emu2413Context {

	public static final int
		OPLL_2413_TONE = 0,
		OPLL_VRC7_TONE = 1,
		OPLL_281B_TONE = 2;

	/* voice data */
	class OPLL_PATCH {
		int TL, FB, EG, ML, AR, DR, SL, RR, KR, KL, AM, PM, WF;
	}

	/* slot */
	class OPLL_SLOT {

		OPLL_PATCH patch;

		/**
		 * 0: modulator<br>
		 * 1: carrier
		 */
		int type;

		/* OUTPUT */
		int feedback;
		/**
		 * Output value of slot
		 */
		int[] output = new int[2];

		/* for Phase Generator (PG) */
		/**
		 * Wavetable, 这个原来是 unsigned 的
		 */
		short[] sintbl;

		/**
		 * Phase, unsigned
		 */
		int phase;

		/**
		 * Phase increment amount, unsigned
		 */
		int dphase;

		/**
		 * output, unsigned
		 */
		int pgout;

		/* for Envelope Generator (EG) */

		/**
		 * F-Number
		 */
		int fnum;

		/**
		 * Block
		 */
		int block;

		/**
		 * Current volume
		 */
		int volume;

		/**
		 * Sustine 1 = ON, 0 = OFF
		 */
		int sustine;

		/**
		 * Total Level + Key scale level, unsigned
		 */
		int tll;

		/**
		 * Key scale offset (Rks), unsigned
		 */
		int rks;

		/**
		 * Current state
		 */
		int eg_mode;

		/**
		 * Phase, unsigned
		 */
		int eg_phase;

		/**
		 * Phase increment amount, unsigned
		 */
		int eg_dphase;

		/**
		 * output, unsigned
		 */
		int egout;
	}

	/* Mask */
/*	#define OPLL_MASK_CH(x) (1<<(x))
	#define OPLL_MASK_HH (1<<(9))
	#define OPLL_MASK_CYM (1<<(10))
	#define OPLL_MASK_TOM (1<<(11))
	#define OPLL_MASK_SD (1<<(12))
	#define OPLL_MASK_BD (1<<(13))
	#define OPLL_MASK_RHYTHM ( OPLL_MASK_HH | OPLL_MASK_CYM | OPLL_MASK_TOM | OPLL_MASK_SD | OPLL_MASK_BD )*/
	
	
	/* Input clock */
	int clk = 844451141;
	/* Sampling rate */
	int rate = 3354932;

	/* WaveTable for each envelope amp */
	int[][] waveform = {new int[512], new int[512]};
	int[] fullsintable = waveform[0];
	int[] halfsintable = waveform[1];

	/* LFO Table */
	int[] pmtable = new int[256];
	int[] amtable = new int[256];

	/* Phase delta for LFO */
	int pm_dphase;
	int am_dphase;

	/* dB to Liner table */
	int[] DB2LIN_TABLE = new int[1024];

	/* Liner to Log curve conversion table (for Attack rate). */
	int[] AR_ADJUST_TABLE = new int[128];

	/* Empty voice data */
	OPLL_PATCH null_patch = new OPLL_PATCH(); // 其中的参数全为 0

	/* Basic voice Data */
	OPLL_PATCH[][] default_patch = new OPLL_PATCH[1][38]; // TODO 这里后面没有动

	/* Definition of envelope mode 
	enum OPLL_EG_STATE 
	{ READY, ATTACK, DECAY, SUSHOLD, SUSTINE, RELEASE, SETTLE, FINISH };

	 Phase incr table for Attack 
	static uint32 dphaseARTable[16][16];
	 Phase incr table for Decay and Release 
	static uint32 dphaseDRTable[16][16];

	 KSL + TL Table 
	static uint32 tllTable[16][8][1 << TL_BITS][4];
	static int32 rksTable[2][8][2];

	 Phase incr table for PG 
	static uint32 dphaseTable[512][8][16];

	// Added by jsr
	int32 opll_volumes[10];*/
	
	{
		
	}
}
