package zdream.nsfplayer.sound.vrc7;

import static zdream.nsfplayer.sound.vrc7.VRC7Static.*;

import java.util.Arrays;

public class OPLLSlot {
	
	OPLL parent;
	
	
	OPLLPatch patch;

	/** 0 : modulator 1 : carrier */
	int type;

	/** OUTPUT */
	int feedback;
	/** Output value of slot */
	public int[] output = new int[2];

	// for Phase Generator (PG)
	/** Wavetable, unsigned */
	int[] sintbl;
	/** Phase, unsigned */
	int phase;
	/** Phase increment amount, unsigned */
	int dphase;
	/** output, unsigned */
	int pgout;

	// for Envelope Generator (EG)
	/** F-Number */
	int fnum;
	/** Block */
	int block;
	/** Current volume */
	int volume;
	/** Sustine 1 = ON, 0 = OFF */
	int sustine;
	/** Total Level + Key scale level, unsigned */
	int tll;
	/** Key scale offset (Rks), unsigned */
	int rks;
	/** Current state */
	int eg_mode;
	/** Phase, unsigned */
	int eg_phase;
	/** Phase increment amount, unsigned */
	int eg_dphase;
	/** output, unsigned */
	int egout;
	
	public OPLLSlot(OPLL parent) {
		super();
		this.parent = parent;
	}

	/**
	 * Change a rhythm voice
	 */
	void setPatch(OPLLPatch patch) {
		this.patch = patch;
	}
	
	/**
	 * Slot key off
	 * @param slot
	 */
	void slotOff() {
		if (eg_mode == ATTACK)
			eg_phase = ((parent.AR_ADJUST_TABLE[((eg_phase) >> (EG_DP_BITS - EG_BITS))]) << ((EG_DP_BITS)
					- (EG_BITS)));
		eg_mode = RELEASE;
		eg_dphase = calc_eg_dphase(); // UPDATE_EG(slot);
	}
	
	/**
	 * Slot key on
	 * @param slot
	 */
	void slotOn() {
		eg_mode = ATTACK;
		eg_phase = 0;
		phase = 0;
		eg_dphase = calc_eg_dphase(); // UPDATE_EG(slot);
	}
	
	/**
	 * Slot key on without reseting the phase
	 * @param slot
	 */
	void slotOn2() {
		eg_mode = ATTACK;
		eg_phase = 0;
		eg_dphase = calc_eg_dphase(); // UPDATE_EG(slot);
	}
	
	void setVolume(int volume) {
		this.volume = volume;
	}
	
	/**
	 * PG
	 */
	void calc_phase(int lfo) {
		if (patch.PM != 0)
			phase += ((dphase * lfo) >> PM_AMP_BITS);
		else
			phase += dphase;

		phase &= (DP_WIDTH - 1);

		pgout = (phase) >> (DP_BASE_BITS);
	}
	
	/**
	 * EG
	 */
	void calc_envelope(int lfo) {
		int[] SL = new int[16];
		for (int i = 0; i < SL.length; i++) {
			SL[i] = (int) ((3.0 * i / SL_STEP) * (int) (SL_STEP / EG_STEP)) << (EG_DP_BITS - EG_BITS);
		}

		int egout; // unsigned

		switch (this.eg_mode) {
		case ATTACK:
			egout = parent.AR_ADJUST_TABLE[(this.eg_phase) >> (EG_DP_BITS - EG_BITS)];
			this.eg_phase += this.eg_dphase;
			if ((EG_DP_WIDTH & this.eg_phase) != 0 || (this.patch.AR == 15)) {
				egout = 0;
				this.eg_phase = 0;
				this.eg_mode = DECAY;
				this.eg_dphase = this.calc_eg_dphase();
			}
			break;

		case DECAY:
			egout = (this.eg_phase) >> (EG_DP_BITS - EG_BITS);
			this.eg_phase += this.eg_dphase;
			if (this.eg_phase >= SL[this.patch.SL]) {
				if (this.patch.EG != 0) {
					this.eg_phase = SL[this.patch.SL];
					this.eg_mode = SUSHOLD;
					this.eg_dphase = this.calc_eg_dphase();
				} else {
					this.eg_phase = SL[this.patch.SL];
					this.eg_mode = SUSTINE;
					this.eg_dphase = this.calc_eg_dphase();
				}
			}
			break;

		case SUSHOLD:
			egout = (this.eg_phase) >> (EG_DP_BITS - EG_BITS);
			if (this.patch.EG == 0) {
				this.eg_mode = SUSTINE;
				this.eg_dphase = this.calc_eg_dphase();
			}
			break;

		case SUSTINE:
		case RELEASE:
			egout = (this.eg_phase) >> (EG_DP_BITS - EG_BITS);
			this.eg_phase += this.eg_dphase;
			if (egout >= (1 << EG_BITS)) {
				this.eg_mode = FINISH;
				egout = (1 << EG_BITS) - 1;
			}
			break;

		case SETTLE:
			egout = (this.eg_phase) >> (EG_DP_BITS - EG_BITS);
			this.eg_phase += this.eg_dphase;
			if (egout >= (1 << EG_BITS)) {
				this.eg_mode = ATTACK;
				egout = (1 << EG_BITS) - 1;
				this.eg_dphase = this.calc_eg_dphase();
			}
			break;

		case FINISH:
			egout = (1 << EG_BITS) - 1;
			break;

		default:
			egout = (1 << EG_BITS) - 1;
			break;
		}

		if (this.patch.AM != 0) {
			egout = ((egout + this.tll) * (int) (EG_STEP / DB_STEP)) + lfo;
		} else {
			egout = ((egout + this.tll) * (int) (EG_STEP / DB_STEP));
		}

		if (egout >= DB_MUTE)
			egout = DB_MUTE - 1;

		this.egout = egout | 3;
	}
	
	/**
	 * 计算参数
	 * @return
	 */
	int calc_eg_dphase() {
		switch (eg_mode) {
		case ATTACK:
			return parent.dphaseARTable[patch.AR][rks];

		case DECAY:
			return parent.dphaseDRTable[patch.DR][rks];

		case SUSHOLD:
			return 0;

		case SUSTINE:
			return parent.dphaseDRTable[patch.RR][rks];

		case RELEASE:
			if (sustine != 0)
				return parent.dphaseDRTable[5][rks];
			else if (patch.EG != 0)
				return parent.dphaseDRTable[patch.RR][rks];
			else
				return parent.dphaseDRTable[7][rks];

		case SETTLE:
			return parent.dphaseDRTable[15][0];

		case FINISH:
			return 0;

		default:
			return 0;
		}
	}
	
	/**
	 * CARRIOR
	 */
	int calc_slot_car(int fm) {
		if (egout >= (DB_MUTE - 1)) {
			output[0] = 0;
		} else { // #define wave2_8pi(e) ( (e) << ( 2 + PG_BITS - SLOT_AMP_BITS ))
			output[0] = parent.DB2LIN_TABLE[sintbl[(pgout + ((fm) << (2 + PG_BITS - SLOT_AMP_BITS)))
					& (PG_WIDTH - 1)] + egout];
		}

		output[1] = (output[1] + output[0]) >> 1;
		return output[1];
	}
	
	/**
	 * MODULATOR
	 */
	int calc_slot_mod() {
		int fm;

		output[1] = output[0];

		if (egout >= (DB_MUTE - 1)) {
			output[0] = 0;
		} else if (patch.FB != 0) {
			fm = ((feedback) << (1 + PG_BITS - SLOT_AMP_BITS)) >> (7 - patch.FB);
			output[0] = parent.DB2LIN_TABLE[sintbl[(pgout + fm) & (PG_WIDTH - 1)] + egout];
		} else {
			output[0] = parent.DB2LIN_TABLE[sintbl[pgout] + egout];
		}

		feedback = (output[1] + output[0]) >> 1;

		return feedback;

	}
	
	/**
	 * TOM
	 */
	int calc_slot_tom() {
		if (egout >= (DB_MUTE - 1))
			return 0;

		return parent.DB2LIN_TABLE[sintbl[pgout] + egout];
	}
	
	/**
	 * SNARE
	 * @param noise
	 *   unsigned
	 */
	int calc_slot_snare(int noise) {
		if (egout >= (DB_MUTE - 1))
			return 0;

		if (((pgout >> 7) & 1) != 0)
			return parent.DB2LIN_TABLE[(noise != 0 ? 0 : (int) ((15.0) / DB_STEP)) + egout];
		else
			return parent.DB2LIN_TABLE[(noise != 0 ? DB_MUTE + DB_MUTE : DB_MUTE + DB_MUTE + (int) (15.0 / DB_STEP))
					+ egout];
	}
	
	/**
	 * TOP-CYM
	 * @param pgout_hh
	 *   unsigned
	 */
	int calc_slot_cym(int pgout_hh) {
		int dbout;

		if (egout >= (DB_MUTE - 1))
			return 0;
		else if
		/* the same as fmopl.c */
		((((pgout_hh >> (PG_BITS - 8)) & 1) ^ ((pgout_hh >> (PG_BITS - 1)) & 1) | ((pgout_hh >> (PG_BITS - 7)) & 1) ^
		/* different from fmopl.c */
				(((pgout >> (PG_BITS - 7)) & 1) & (((pgout >> (PG_BITS - 5)) & 1) == 0 ? 1 : 0))) != 0)
			dbout = (int) (DB_MUTE + DB_MUTE + (3.0) / DB_STEP);
		else
			dbout = (int) ((3.0) / DB_STEP);

		return parent.DB2LIN_TABLE[dbout + egout];
	}
	
	/**
	 * HI-HAT
	 * @param noise
	 *   unsigned
	 */
	int calc_slot_hat(int pgout_cym, int noise) {
		int dbout;

		if (egout >= (DB_MUTE - 1))
			return 0;
		else if ((
		/* the same as fmopl.c */
		((((pgout >> (PG_BITS - 8)) & 1) ^ ((pgout >> (PG_BITS - 1)) & 1))
				| ((pgout >> (PG_BITS - 7)) & 1)) ^
		/* different from fmopl.c */
				(((pgout_cym >> (PG_BITS - 7)) & 1) & (((pgout_cym >> (PG_BITS - 5)) & 1) == 1 ? 0 : 1))) != 0) {
			if (noise != 0)
				dbout = (int) (DB_MUTE + DB_MUTE + (12.0) / DB_STEP);
			else
				dbout = (int) (DB_MUTE + DB_MUTE + (24.0) / DB_STEP);
		} else {
			if (noise != 0)
				dbout = (int) ((12.0) / DB_STEP);
			else
				dbout = (int) ((24.0) / DB_STEP);
		}

		return parent.DB2LIN_TABLE[dbout + egout];
	}
	
	/**
	 * 初始化
	 * @param type
	 */
	void reset(int type) {
		this.type = type;
		sintbl = parent.waveform[0];
		phase = 0;
		dphase = 0;
		output[0] = 0;
		output[1] = 0;
		feedback = 0;
		eg_mode = FINISH;
		eg_phase = EG_DP_WIDTH;
		eg_dphase = 0;
		rks = 0;
		tll = 0;
		sustine = 0;
		fnum = 0;
		block = 0;
		volume = 0;
		pgout = 0;
		egout = 0;
		patch = parent.null_patch;
	}

	@Override
	public String toString() {
		return "[patch=" + patch + ", type=" + type + ", feedback=" + feedback
				+ ", output=" + Arrays.toString(output) + ", sintbl=" + Arrays.toString(sintbl) + ", phase=" + phase
				+ ", dphase=" + dphase + ", pgout=" + pgout + ", fnum=" + fnum + ", block=" + block + ", volume="
				+ volume + ", sustine=" + sustine + ", tll=" + tll + ", rks=" + rks + ", eg_mode=" + eg_mode
				+ ", eg_phase=" + eg_phase + ", eg_dphase=" + eg_dphase + ", egout=" + egout + "]";
	}
}
