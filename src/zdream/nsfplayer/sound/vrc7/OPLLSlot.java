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
