package zdream.nsfplayer.xgm.device.sound.legacy;

import java.util.Arrays;

/**
 * 只在 VRC7 中使用
 * @author Zdream
 */
public class OPLL_env {
	
	public static OPLL_env ins = new OPLL_env(); // instance
	
	static final int OPLL_TONE_NUM = 8;
		
	public static final int
		OPLL_VRC7_RW_TONE = 0,
	    OPLL_VRC7_FT36_TONE = 1,
	    OPLL_VRC7_FT35_TONE = 2,
	    OPLL_VRC7_MO_TONE = 3,
	    OPLL_VRC7_KT2_TONE = 4,
	    OPLL_VRC7_KT1_TONE = 5,
	    OPLL_2413_TONE = 6,
	    OPLL_281B_TONE = 7;
	
	/**
	 * 只在 VRC7 中使用
	 * @author Zdream
	 */
	public class OPLL {
		
		/** unsigned */
		int adr;
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
		int[] pan = new int[16];

		// Register
		/** unsigned */
		public int[] reg = new int[0x40];
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
		public OPLLSlot[] slot = new OPLLSlot[18];

		// Voice Data
		/** 19 x 2 = 38 */
		OPLLPatch[] patch = new OPLLPatch[38];
		/** flag for check patch update */
		int[] patch_update = new int[2];

		/** unsigned */
		int mask;

		{
			for (int i = 0; i < slot.length; i++) {
				slot[i] = new OPLLSlot();
			}
			for (int i = 0; i < patch.length; i++) {
				patch[i] = new OPLLPatch();
			}
		}
	}
	
	public class OPLLSlot {
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
		
		@Override
		public String toString() {
			return "[patch=" + patch + ", type=" + type + ", feedback=" + feedback
					+ ", output=" + Arrays.toString(output) + ", sintbl=" + Arrays.toString(sintbl) + ", phase=" + phase
					+ ", dphase=" + dphase + ", pgout=" + pgout + ", fnum=" + fnum + ", block=" + block + ", volume="
					+ volume + ", sustine=" + sustine + ", tll=" + tll + ", rks=" + rks + ", eg_mode=" + eg_mode
					+ ", eg_phase=" + eg_phase + ", eg_dphase=" + eg_dphase + ", egout=" + egout + "]";
		}
	}
	
	public final class OPLLPatch {
		/** unsigned */
		int TL, FB, EG, ML, AR, DR, SL, RR, KR, KL, AM, PM, WF;
		
		public OPLLPatch clone() {
			OPLLPatch o = new OPLLPatch();
			o.TL = TL;
			o.FB = FB;
			o.EG = EG;
			o.ML = ML;
			o.AR = AR;
			o.DR = DR;
			o.SL = SL;
			o.RR = RR;
			o.KR = KR;
			o.KL = KL;
			o.AM = AM;
			o.PM = PM;
			o.WF = WF;
			return o;
		}
		
		@Override
		public String toString() {
			StringBuilder b = new StringBuilder();
			b.append("TL:").append(TL).append(',');
			b.append("FB:").append(FB).append(',');
			b.append("EG:").append(EG).append(',');
			b.append("ML:").append(ML).append(',');
			b.append("AR:").append(AR).append(',');
			b.append("DR:").append(DR).append(',');
			b.append("SL:").append(SL).append(',');
			b.append("RR:").append(RR).append(',');
			b.append("KR:").append(KR).append(',');
			b.append("KL:").append(KL).append(',');
			b.append("AM:").append(AM).append(',');
			b.append("PM:").append(PM).append(',');
			b.append("WF:").append(WF);
			return b.toString();
		}
	}
	
	/**
	 * 原本存放的值都是 unsigned byte 的
	 */
	public static final short[][] default_inst = {
		{ // patch set
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x03, 0x21, 0x05, 0x06, 0xB8, 0x82, 0x42, 0x27, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x13, 0x41, 0x13, 0x0D, 0xD8, 0xD6, 0x23, 0x12, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x31, 0x11, 0x08, 0x08, 0xFA, 0x9A, 0x22, 0x02,	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			
			0x31, 0x61, 0x18, 0x07, 0x78, 0x64, 0x30, 0x27,	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x22, 0x21, 0x1E, 0x06, 0xF0, 0x76, 0x08, 0x28,	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x02, 0x01, 0x06, 0x00, 0xF0, 0xF2, 0x03, 0xF5,	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x21, 0x61, 0x1D, 0x07, 0x82, 0x81, 0x16, 0x07,	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			
			0x23, 0x21, 0x1A, 0x17, 0xCF, 0x72, 0x25, 0x17,	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x15, 0x11, 0x25, 0x00, 0x4F, 0x71, 0x00, 0x11,	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x85, 0x01, 0x12, 0x0F, 0x99, 0xA2, 0x40, 0x02,	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x07, 0xC1, 0x69, 0x07, 0xF3, 0xF5, 0xA7, 0x12,	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			
			0x71, 0x23, 0x0D, 0x06, 0x66, 0x75, 0x23, 0x16,	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x01, 0x02, 0xD3, 0x05, 0xA3, 0x92, 0xF7, 0x52,	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x61, 0x63, 0x0C, 0x00, 0x94, 0xAF, 0x34, 0x06,	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x21, 0x62, 0x0D, 0x00, 0xB1, 0xA0, 0x54, 0x17,	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
		},{ // patch set used in FamiTracker 0.3.6
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x03, 0x21, 0x04, 0x06, 0x8D, 0xF2, 0x42, 0x17,	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x13, 0x41, 0x05, 0x0E, 0x99, 0x96, 0x63, 0x12,	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x31, 0x11, 0x10, 0x0A, 0xF0, 0x9C, 0x32, 0x02,	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			
			0x21, 0x61, 0x1D, 0x07, 0x9F, 0x64, 0x20, 0x27,	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x22, 0x21, 0x1E, 0x06, 0xF0, 0x76, 0x08, 0x28,	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x02, 0x01, 0x06, 0x00, 0xF0, 0xF2, 0x03, 0x95,	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x21, 0x61, 0x1C, 0x07, 0x82, 0x81, 0x16, 0x07,	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			
			0x23, 0x21, 0x1A, 0x17, 0xEF, 0x82, 0x25, 0x15,	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x25, 0x11, 0x1F, 0x00, 0x86, 0x41, 0x20, 0x11,	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x85, 0x01, 0x1F, 0x0F, 0xE4, 0xA2, 0x11, 0x12,	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x07, 0xC1, 0x2B, 0x45, 0xB4, 0xF1, 0x24, 0xF4,	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			
			0x61, 0x23, 0x11, 0x06, 0x96, 0x96, 0x13, 0x16,	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x01, 0x02, 0xD3, 0x05, 0x82, 0xA2, 0x31, 0x51,	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x61, 0x22, 0x0D, 0x02, 0xC3, 0x7F, 0x24, 0x05,	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x21, 0x62, 0x0E, 0x00, 0xA1, 0xA0, 0x44, 0x17,	0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
		},{ // patch set used in FamiTracker 0.3.5 and prior
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x33, 0x01, 0x09, 0x0e, 0x94, 0x90, 0x40, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x13, 0x41, 0x0f, 0x0d, 0xce, 0xd3, 0x43, 0x13, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x01, 0x12, 0x1b, 0x06, 0xff, 0xd2, 0x00, 0x32, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			
			0x61, 0x61, 0x1b, 0x07, 0xaf, 0x63, 0x20, 0x28, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x22, 0x21, 0x1e, 0x06, 0xf0, 0x76, 0x08, 0x28, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x66, 0x21, 0x15, 0x00, 0x93, 0x94, 0x20, 0xf8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x21, 0x61, 0x1c, 0x07, 0x82, 0x81, 0x10, 0x17, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			
			0x23, 0x21, 0x20, 0x1f, 0xc0, 0x71, 0x07, 0x47, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x25, 0x31, 0x26, 0x05, 0x64, 0x41, 0x18, 0xf8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x17, 0x21, 0x28, 0x07, 0xff, 0x83, 0x02, 0xf8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x97, 0x81, 0x25, 0x07, 0xcf, 0xc8, 0x02, 0x14, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			
			0x21, 0x21, 0x54, 0x0f, 0x80, 0x7f, 0x07, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x01, 0x01, 0x56, 0x03, 0xd3, 0xb2, 0x43, 0x58, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x31, 0x21, 0x0c, 0x03, 0x82, 0xc0, 0x40, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x21, 0x01, 0x0c, 0x03, 0xd4, 0xd3, 0x40, 0x84, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			
			0x04, 0x21, 0x28, 0x00, 0xdf, 0xf8, 0xff, 0xf8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x23, 0x22, 0x00, 0x00, 0xa8, 0xf8, 0xf8, 0xf8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x25, 0x18, 0x00, 0x00, 0xf8, 0xa9, 0xf8, 0x55, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
		},{ // VRC7 TONES
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x33, 0x01, 0x09, 0x0e, 0x94, 0x90, 0x40, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x13, 0x41, 0x0f, 0x0d, 0xce, 0xd3, 0x43, 0x13, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x01, 0x12, 0x1b, 0x06, 0xff, 0xd2, 0x00, 0x32, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			
			0x61, 0x61, 0x1b, 0x07, 0xaf, 0x63, 0x20, 0x28, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x22, 0x21, 0x1e, 0x06, 0xf0, 0x76, 0x08, 0x28, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x66, 0x21, 0x15, 0x00, 0x93, 0x94, 0x20, 0xf8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x21, 0x61, 0x1c, 0x07, 0x82, 0x81, 0x10, 0x17, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			
			0x23, 0x21, 0x20, 0x1f, 0xc0, 0x71, 0x07, 0x47, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x25, 0x31, 0x26, 0x05, 0x64, 0x41, 0x18, 0xf8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x17, 0x21, 0x28, 0x07, 0xff, 0x83, 0x02, 0xf8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x97, 0x81, 0x25, 0x07, 0xcf, 0xc8, 0x02, 0x14, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			
			0x21, 0x21, 0x54, 0x0f, 0x80, 0x7f, 0x07, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x01, 0x01, 0x56, 0x03, 0xd3, 0xb2, 0x43, 0x58, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x31, 0x21, 0x0c, 0x03, 0x82, 0xc0, 0x40, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x21, 0x01, 0x0c, 0x03, 0xd4, 0xd3, 0x40, 0x84, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			
			0x07, 0x21, 0x14, 0x00, 0xee, 0xf8, 0xff, 0xf8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x01, 0x31, 0x00, 0x00, 0xf8, 0xf7, 0xf8, 0xf7, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x25, 0x11, 0x00, 0x00, 0xf8, 0xfa, 0xf8, 0x55, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
		},{ // patch set 2 by kevtris
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x31, 0x22, 0x23, 0x07, 0xF0, 0xF0, 0xE8, 0xF7, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x03, 0x31, 0x68, 0x05, 0xF2, 0x74, 0x79, 0x9C, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x01, 0x51, 0x72, 0x04, 0xF1, 0xD3, 0x9D, 0x8B, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			
			0x22, 0x61, 0x1B, 0x05, 0xC0, 0xA1, 0xF8, 0xE8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x22, 0x61, 0x2C, 0x03, 0xD2, 0xA1, 0xA7, 0xE8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x31, 0x22, 0xFA, 0x01, 0xF1, 0xF1, 0xF4, 0xEE, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x21, 0x61, 0x28, 0x06, 0xF1, 0xF1, 0xCE, 0x9B, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			
			0x27, 0x61, 0x60, 0x00, 0xF0, 0xF0, 0xFF, 0xFD, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x60, 0x21, 0x2B, 0x06, 0x85, 0xF1, 0x79, 0x9D, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x31, 0xA1, 0xFF, 0x0A, 0x53, 0x62, 0x5E, 0xAF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x03, 0xA1, 0x70, 0x0F, 0xD4, 0xA3, 0x94, 0xBE, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			
			0x2B, 0x61, 0xE4, 0x07, 0xF6, 0x93, 0xBD, 0xAC, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x21, 0x63, 0xED, 0x07, 0x77, 0xF1, 0xC7, 0xE8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x21, 0x61, 0x2A, 0x03, 0xF3, 0xE2, 0xB6, 0xD9, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x21, 0x63, 0x37, 0x03, 0xF3, 0xE2, 0xB6, 0xD9, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
		},{ // patch set 1 by kevtris
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x05, 0x03, 0x10, 0x06, 0x74, 0xA1, 0x13, 0xF4, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x05, 0x01, 0x16, 0x00, 0xF9, 0xA2, 0x15, 0xF5, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x01, 0x41, 0x11, 0x00, 0xA0, 0xA0, 0x83, 0x95, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			
			0x01, 0x41, 0x17, 0x00, 0x60, 0xF0, 0x83, 0x95, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x24, 0x41, 0x1F, 0x00, 0x50, 0xB0, 0x94, 0x94, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x05, 0x01, 0x0B, 0x04, 0x65, 0xA0, 0x54, 0x95, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x11, 0x41, 0x0E, 0x04, 0x70, 0xC7, 0x13, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			
			0x02, 0x44, 0x16, 0x06, 0xE0, 0xE0, 0x31, 0x35, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x48, 0x22, 0x22, 0x07, 0x50, 0xA1, 0xA5, 0xF4, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x05, 0xA1, 0x18, 0x00, 0xA2, 0xA2, 0xF5, 0xF5, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x07, 0x81, 0x2B, 0x05, 0xA5, 0xA5, 0x03, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			
			0x01, 0x41, 0x08, 0x08, 0xA0, 0xA0, 0x83, 0x95, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x21, 0x61, 0x12, 0x00, 0x93, 0x92, 0x74, 0x75, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x21, 0x62, 0x21, 0x00, 0x84, 0x85, 0x34, 0x15, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x21, 0x62, 0x0E, 0x00, 0xA1, 0xA0, 0x34, 0x15, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
		},{ // YM2413 tone
			0x49, 0x4c, 0x4c, 0x32, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x61, 0x61, 0x1e, 0x17, 0xf0, 0x7f, 0x00, 0x17, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x13, 0x41, 0x16, 0x0e, 0xfd, 0xf4, 0x23, 0x23, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x03, 0x01, 0x9a, 0x04, 0xf3, 0xf3, 0x13, 0xf3, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			
			0x11, 0x61, 0x0e, 0x07, 0xfa, 0x64, 0x70, 0x17, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x22, 0x21, 0x1e, 0x06, 0xf0, 0x76, 0x00, 0x28, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x21, 0x22, 0x16, 0x05, 0xf0, 0x71, 0x00, 0x18, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x21, 0x61, 0x1d, 0x07, 0x82, 0x80, 0x17, 0x17, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			
			0x23, 0x21, 0x2d, 0x16, 0x90, 0x90, 0x00, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x21, 0x21, 0x1b, 0x06, 0x64, 0x65, 0x10, 0x17, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x21, 0x21, 0x0b, 0x1a, 0x85, 0xa0, 0x70, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x23, 0x01, 0x83, 0x10, 0xff, 0xb4, 0x10, 0xf4, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			
			0x97, 0xc1, 0x20, 0x07, 0xff, 0xf4, 0x22, 0x22, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x61, 0x00, 0x0c, 0x05, 0xc2, 0xf6, 0x40, 0x44, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x01, 0x01, 0x56, 0x03, 0x94, 0xc2, 0x03, 0x12, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x21, 0x01, 0x89, 0x03, 0xf1, 0xe4, 0xf0, 0x23, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			
			0x07, 0x21, 0x14, 0x00, 0xee, 0xf8, 0xff, 0xf8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x01, 0x31, 0x00, 0x00, 0xf8, 0xf7, 0xf8, 0xf7, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x25, 0x11, 0x00, 0x00, 0xf8, 0xfa, 0xf8, 0x55, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
		},{ // YMF281B tone
			0x49, 0x4c, 0x4c, 0x32, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x62, 0x21, 0x1a, 0x07, 0xf0, 0x6f, 0x00, 0x16, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x00, 0x10, 0x44, 0x02, 0xf6, 0xf4, 0x54, 0x23, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x03, 0x01, 0x97, 0x04, 0xf3, 0xf3, 0x13, 0xf3, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			
			0x01, 0x61, 0x0a, 0x0f, 0xfa, 0x64, 0x70, 0x17, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x22, 0x21, 0x1e, 0x06, 0xf0, 0x76, 0x00, 0x28, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x00, 0x61, 0x8a, 0x0e, 0xc0, 0x61, 0x00, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x21, 0x61, 0x1b, 0x07, 0x84, 0x80, 0x17, 0x17, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			
			0x37, 0x32, 0xc9, 0x01, 0x66, 0x64, 0x40, 0x28, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x01, 0x21, 0x06, 0x03, 0xa5, 0x71, 0x51, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x06, 0x11, 0x5e, 0x07, 0xf3, 0xf2, 0xf6, 0x11, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x00, 0x20, 0x18, 0x06, 0xf5, 0xf3, 0x20, 0x26, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			
			0x97, 0x41, 0x20, 0x07, 0xff, 0xf4, 0x22, 0x22, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x65, 0x61, 0x15, 0x00, 0xf7, 0xf3, 0x16, 0xf4, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x01, 0x31, 0x0e, 0x07, 0xfa, 0xf3, 0xff, 0xff, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x48, 0x61, 0x09, 0x07, 0xf1, 0x94, 0xf0, 0xf5, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			
			0x07, 0x21, 0x14, 0x00, 0xee, 0xf8, 0xff, 0xf8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x01, 0x31, 0x00, 0x00, 0xf8, 0xf7, 0xf8, 0xf7, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x25, 0x11, 0x00, 0x00, 0xf8, 0xfa, 0xf8, 0x55, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
		}
	};
	
	/** Size of Sintable ( 8 -- 18 can be used. 9 recommended.) */
	public static final int
			PG_BITS = 9,
			PG_WIDTH = (1<<PG_BITS);

	/** Phase increment counter */
	public static final int
			DP_BITS = 18,
			DP_WIDTH = (1<<DP_BITS),
			DP_BASE_BITS = (DP_BITS - PG_BITS);

	/** Dynamic range (Accuracy of sin table) */
	public static final int
			DB_BITS = 8,
			DB_MUTE = (1<<DB_BITS);
	/** Dynamic range (Accuracy of sin table) */
	public static final double DB_STEP = (48.0/(1<<DB_BITS));

	/** Dynamic range of envelope */
	public static final double EG_STEP = 0.375;
	/** Dynamic range of envelope */
	public static final int
			EG_BITS = 7,
			EG_MUTE = (1<<EG_BITS);

	/** Dynamic range of total level */
	public static final double TL_STEP = 0.75;
	/** Dynamic range of total level */
	public static final int
			TL_BITS = 6,
			TL_MUTE = (1<<TL_BITS);

	/** Dynamic range of sustine level */
	public static final double SL_STEP = 3.0;
	/** Dynamic range of sustine level */
	public static final int
			SL_BITS = 4,
			SL_MUTE = (1<<SL_BITS);

	/** Bits for liner value */
	public static final int DB2LIN_AMP_BITS = 8, SLOT_AMP_BITS = 8;

	/** Bits for envelope phase incremental counter */
	public static final int EG_DP_BITS = 22, EG_DP_WIDTH = (1<<EG_DP_BITS);

	/** Bits for Pitch and Amp modulator */
	public static final int
			PM_PG_BITS = 8, PM_PG_WIDTH = (1 << PM_PG_BITS),
			PM_DP_BITS = 16, PM_DP_WIDTH = (1 << PM_DP_BITS),
			AM_PG_BITS = 8, AM_PG_WIDTH = (1 << AM_PG_BITS),
			AM_DP_BITS = 16, AM_DP_WIDTH = (1 << AM_DP_BITS);

	/** PM table is calcurated by PM_AMP * pow(2,PM_DEPTH*sin(x)/1200) */
	public static final int PM_AMP_BITS = 8, PM_AMP = (1 << PM_AMP_BITS);

	/** PM speed(Hz) and depth(cent) */
	public static final double PM_SPEED = 6.4, PM_DEPTH = 13.75;

	/** AM speed(Hz) and depth(dB) */
	public static final double AM_SPEED = 3.6413, AM_DEPTH = 4.875;
	
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

	// Definition of envelope mode
	public static final int
			READY = 0,
			ATTACK = 1,
			DECAY = 2,
			SUSHOLD = 3,
			SUSTINE = 4,
			RELEASE = 5,
			SETTLE = 6,
			FINISH = 7; 

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
	
	private void makeDefaultPatch() {
		for (int i = 0; i < OPLL_env.OPLL_TONE_NUM; i++) {
			for (int j = 0; j < 19; j++) {
				dump2patch(default_inst[i], j * 16,
						default_patch[i][j * 2], default_patch[i][j * 2 + 1]);
			}
		}
	}

	// unused
	public void setPatch (OPLL opll, short[] dump) {
		OPLLPatch[] patch = { new OPLLPatch(), new OPLLPatch() };
		int i;

		for (i = 0; i < 19; i++) {
			dump2patch(dump, i * 16, patch[0], patch[1]);
			opll.patch[i * 2] = patch[0].clone();
			opll.patch[i * 2 + 1] = patch[1].clone();
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
    Calc Parameters
*********************************************************** */
	
	private int calc_eg_dphase(OPLLSlot slot) {
		switch (slot.eg_mode) {
		case ATTACK:
			return dphaseARTable[slot.patch.AR][slot.rks];

		case DECAY:
			return dphaseDRTable[slot.patch.DR][slot.rks];

		case SUSHOLD:
			return 0;

		case SUSTINE:
			return dphaseDRTable[slot.patch.RR][slot.rks];

		case RELEASE:
			if (slot.sustine != 0)
				return dphaseDRTable[5][slot.rks];
			else if (slot.patch.EG != 0)
				return dphaseDRTable[slot.patch.RR][slot.rks];
			else
				return dphaseDRTable[7][slot.rks];

		case SETTLE:
			return dphaseDRTable[15][0];

		case FINISH:
			return 0;

		default:
			return 0;
		}
	}
	
/* ***********************************************************
    OPLL internal interfaces
*********************************************************** */

	public static final int
			SLOT_BD1 = 12,
			SLOT_BD2 = 13,
			SLOT_HH = 14,
			SLOT_SD = 15,
			SLOT_TOM = 16,
			SLOT_CYM = 17;
	
	/**
	 * Slot key on
	 * @param slot
	 */
	private void slotOn(OPLLSlot slot) {
		slot.eg_mode = ATTACK;
		slot.eg_phase = 0;
		slot.phase = 0;
		slot.eg_dphase = calc_eg_dphase(slot); // UPDATE_EG(slot);
	}
	
	/**
	 * Slot key on without reseting the phase
	 * @param slot
	 */
	private void slotOn2(OPLLSlot slot) {
		slot.eg_mode = ATTACK;
		slot.eg_phase = 0;
		slot.eg_dphase = calc_eg_dphase(slot); // UPDATE_EG(slot);
	}
	
	/**
	 * Slot key off
	 * @param slot
	 */
	private void slotOff(OPLLSlot slot) {
		if (slot.eg_mode == ATTACK)
			slot.eg_phase = ((AR_ADJUST_TABLE[((slot.eg_phase) >> (EG_DP_BITS - EG_BITS))]) << ((EG_DP_BITS)
					- (EG_BITS)));
		slot.eg_mode = RELEASE;
		slot.eg_dphase = calc_eg_dphase(slot); // UPDATE_EG(slot);
	}
	
	/**
	 * Channel key on
	 */
	private void keyOn (OPLL opll, int i) {
		if (opll.slot_on_flag[i * 2] == 0) {
			slotOn((opll).slot[(i) << 1]); // MOD(opll,i)
		}

		if ((opll.slot_on_flag[i * 2 + 1]) == 0) {
			slotOn(((opll).slot[((i) << 1) | 1])); // CAR(opll,i)
		}
		opll.key_status[i] = 1;
	}
	
	/**
	 * Channel key off
	 */
	private void keyOff(OPLL opll, int i) {
		if (opll.slot_on_flag[i * 2 + 1] != 0)
			slotOff(((opll).slot[((i) << 1) | 1])); // CAR(opll,i)
		opll.key_status[i] = 0;
	}
	
	private void keyOn_BD(OPLL opll) {
		keyOn(opll, 6);
	}
	
	private void keyOn_SD(OPLL opll) {
		if (opll.slot_on_flag[SLOT_SD] == 0)
			slotOn(((opll).slot[((7) << 1) | 1])); // CAR(opll,7)
	}

	private void keyOn_TOM(OPLL opll) {
		if (opll.slot_on_flag[SLOT_TOM] == 0)
			slotOn((opll).slot[(8) << 1]); // MOD(opll,8)
	}

	private void keyOn_HH(OPLL opll) {
		if (opll.slot_on_flag[SLOT_HH] == 0)
			slotOn2((opll).slot[(7) << 1]); // MOD(opll,7)
	}
	
	private void keyOn_CYM(OPLL opll) {
		if (opll.slot_on_flag[SLOT_CYM] == 0)
			slotOn2(((opll).slot[((8) << 1) | 1])); // CAR(opll,8)
	}
	
	// Drum key off
	private void keyOff_BD(OPLL opll) {
		keyOff(opll, 6);
	}
	
	private void keyOff_SD(OPLL opll) {
		if (opll.slot_on_flag[SLOT_SD] != 0)
			slotOff(((opll).slot[((7) << 1) | 1])); // CAR(opll,7)
	}
	
	private void keyOff_TOM(OPLL opll) {
		if (opll.slot_on_flag[SLOT_TOM] != 0)
			slotOff((opll).slot[(8) << 1]); // MOD(opll,8)
	}
	
	private void keyOff_HH (OPLL opll) {
		if (opll.slot_on_flag[SLOT_HH] != 0)
			slotOff((opll).slot[(7) << 1]); // MOD(opll,7)
	}
	
	private void keyOff_CYM(OPLL opll) {
		if (opll.slot_on_flag[SLOT_CYM] != 0)
			slotOff(((opll).slot[((8) << 1) | 1])); // CAR(opll,8)
	}
	
	/**
	 *  Change a voice
	 */
	private void setPatch(OPLL opll, int i, int num) {
		opll.patch_number[i] = num;
		opll.slot[i << 1].patch = opll.patch[num * 2]; // MOD(opll,i)
		opll.slot[((i) << 1) | 1].patch = opll.patch[num * 2 + 1]; // CAR(opll,i)
	}
	
	/**
	 * Change a rhythm voice
	 */
	private void setSlotPatch(OPLLSlot slot, OPLLPatch patch) {
		slot.patch = patch;
	}
	
	/**
	 * Set sustine parameter
	 */
	private void setSustine(OPLL opll, int c, int sustine) {
		opll.slot[(c << 1) | 1].sustine = sustine; // CAR(opll,c)
		if (opll.slot[c << 1].type != 0) // MOD(opll,c)
			opll.slot[c << 1].sustine = sustine; // MOD(opll,c)
	}
	
	/**
	 * Volume : 6bit ( Volume register << 2 )
	 */
	private void setVolume(OPLL opll, int c, int volume) {
		opll.slot[(c << 1) | 1].volume = volume; // CAR(opll,c)
	}
	
	private void setSlotVolume(OPLLSlot slot, int volume) {
		slot.volume = volume;
	}
	
	/**
	 * Set F-Number ( fnum : 9bit ) 
	 */
	private void setFnumber(OPLL opll, int c, int fnum) {
		opll.slot[(c << 1) | 1].fnum = fnum; // CAR(opll,c)
		opll.slot[c << 1].fnum = fnum; // MOD(opll,c)
	}
	
	/**
	 * Set Block data (block : 3bit )
	 */
	private void setBlock(OPLL opll, int c, int block) {
		opll.slot[(c << 1) | 1].block = block; // CAR(opll,c)
		opll.slot[c << 1].block = block; // MOD(opll,c)
	}
	
	/**
	 * Change Rhythm Mode
	 */
	private void update_rhythm_mode(OPLL opll) {
		if ((opll.patch_number[6] & 0x10) != 0) {
			if ((opll.slot_on_flag[SLOT_BD2] | (opll.reg[0x0e] & 32)) == 0) {
				opll.slot[SLOT_BD1].eg_mode = FINISH;
				opll.slot[SLOT_BD2].eg_mode = FINISH;
				setPatch(opll, 6, opll.reg[0x36] >> 4);
			}
		} else if ((opll.reg[0x0e] & 32) != 0) {
			opll.patch_number[6] = 16;
			opll.slot[SLOT_BD1].eg_mode = FINISH;
			opll.slot[SLOT_BD2].eg_mode = FINISH;
			setSlotPatch(opll.slot[SLOT_BD1], opll.patch[16 * 2 + 0]);
			setSlotPatch(opll.slot[SLOT_BD2], opll.patch[16 * 2 + 1]);
		}

		if ((opll.patch_number[7] & 0x10) != 0) {
			if (!((opll.slot_on_flag[SLOT_HH] != 0 && opll.slot_on_flag[SLOT_SD] != 0) || (opll.reg[0x0e] & 32) != 0)) {
				opll.slot[SLOT_HH].type = 0;
				opll.slot[SLOT_HH].eg_mode = FINISH;
				opll.slot[SLOT_SD].eg_mode = FINISH;
				setPatch(opll, 7, opll.reg[0x37] >> 4);
			}
		} else if ((opll.reg[0x0e] & 32) != 0) {
			opll.patch_number[7] = 17;
			opll.slot[SLOT_HH].type = 1;
			opll.slot[SLOT_HH].eg_mode = FINISH;
			opll.slot[SLOT_SD].eg_mode = FINISH;
			setSlotPatch(opll.slot[SLOT_HH], opll.patch[17 * 2 + 0]);
			setSlotPatch(opll.slot[SLOT_SD], opll.patch[17 * 2 + 1]);
		}

		if ((opll.patch_number[8] & 0x10) != 0) {
			if (!((opll.slot_on_flag[SLOT_CYM] != 0 && opll.slot_on_flag[SLOT_TOM] != 0)
					|| (opll.reg[0x0e] & 32) != 0)) {
				opll.slot[SLOT_TOM].type = 0;
				opll.slot[SLOT_TOM].eg_mode = FINISH;
				opll.slot[SLOT_CYM].eg_mode = FINISH;
				setPatch(opll, 8, opll.reg[0x38] >> 4);
			}
		} else if ((opll.reg[0x0e] & 32) != 0) {
			opll.patch_number[8] = 18;
			opll.slot[SLOT_TOM].type = 1;
			opll.slot[SLOT_TOM].eg_mode = FINISH;
			opll.slot[SLOT_CYM].eg_mode = FINISH;
			setSlotPatch(opll.slot[SLOT_TOM], opll.patch[18 * 2 + 0]);
			setSlotPatch(opll.slot[SLOT_CYM], opll.patch[18 * 2 + 1]);
		}
	}
	
	private void update_key_status(OPLL opll) {
		int ch;

		for (ch = 0; ch < 9; ch++)
			opll.slot_on_flag[ch * 2] = opll.slot_on_flag[ch * 2 + 1] = (opll.reg[0x20 + ch]) & 0x10;

		if ((opll.reg[0x0e] & 32) != 0) {
			opll.slot_on_flag[SLOT_BD1] |= (opll.reg[0x0e] & 0x10);
			opll.slot_on_flag[SLOT_BD2] |= (opll.reg[0x0e] & 0x10);
			opll.slot_on_flag[SLOT_SD] |= (opll.reg[0x0e] & 0x08);
			opll.slot_on_flag[SLOT_HH] |= (opll.reg[0x0e] & 0x01);
			opll.slot_on_flag[SLOT_TOM] |= (opll.reg[0x0e] & 0x04);
			opll.slot_on_flag[SLOT_CYM] |= (opll.reg[0x0e] & 0x02);
		}
	}
	
	private void copyPatch(OPLL opll, int num, OPLLPatch patch) {
		opll.patch[num] = patch.clone();
	}
	
/* ***********************************************************
    Initializing
*********************************************************** */
	
	private void slotReset(OPLLSlot slot, int type) {
		slot.type = type;
		slot.sintbl = waveform[0];
		slot.phase = 0;
		slot.dphase = 0;
		slot.output[0] = 0;
		slot.output[1] = 0;
		slot.feedback = 0;
		slot.eg_mode = FINISH;
		slot.eg_phase = EG_DP_WIDTH;
		slot.eg_dphase = 0;
		slot.rks = 0;
		slot.tll = 0;
		slot.sustine = 0;
		slot.fnum = 0;
		slot.block = 0;
		slot.volume = 0;
		slot.pgout = 0;
		slot.egout = 0;
		slot.patch = null_patch;
	}
	
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
	 * 
	 * @param c
	 *   unsigned
	 * @param r
	 *   unsigned
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
	
	/**
	 * 
	 * @param clk
	 *   unsigned
	 * @param rate
	 *   unsigned
	 * @return
	 */
	public OPLL createOPLL(int clk, int rate) {
		OPLL opll;
		int i;

		maketables(clk, rate);

		opll = new OPLL();

		for (i = 0; i < 19 * 2; i++) {
			opll.patch[i] = null_patch.clone();
		}

		opll.mask = 0;

		reset (opll);
		reset_patch (opll, 0);

		return opll;
	}
	
	/**
	 * Reset patch datas by system default.
	 * 初始化时调用
	 */
	public void reset_patch(OPLL opll, int type) {
		int i;

		for (i = 0; i < 19 * 2; i++) {
			copyPatch(opll, i, default_patch[type % OPLL_TONE_NUM][i]);
		}
	}
	
	/**
	 * Reset whole of OPLL except patch datas.
	 */
	public void reset(OPLL opll) {
		int i;

		if (opll == null)
			return;

		opll.adr = 0;
		opll.out = 0;

		opll.pm_phase = 0;
		opll.am_phase = 0;

		opll.noise_seed = 0xffff;
		opll.mask = 0;

		for (i = 0; i < 18; i++)
			slotReset(opll.slot[i], i % 2);

		for (i = 0; i < 9; i++) {
			opll.key_status[i] = 0;
			setPatch(opll, i, 0);
		}

		for (i = 0; i < 0x40; i++)
			writeReg(opll, i, 0);

		long factor = 1l << 31;
		opll.realstep = (int) (factor / rate);
		if (opll.realstep < 0) {
			System.out.println("opll.realstep < 0");
		}
		opll.opllstep = (int) (factor / (clk / 72));
		opll.oplltime = 0;
		for (i = 0; i < 14; i++)
			opll.pan[i] = 2;
		opll.sprev[0] = opll.sprev[1] = 0;
		opll.snext[0] = opll.snext[1] = 0;
	}
	
	/**
	 * Force Refresh (When external program changes some parameters).
	 * (未调用)
	 */
	public void forceRefresh(OPLL opll) {
		int i;

		if (opll == null)
			return;

		for (i = 0; i < 9; i++)
			setPatch(opll, i, opll.patch_number[i]);

		for (i = 0; i < 18; i++) {
			OPLLSlot s = opll.slot[i];
			s.dphase = dphaseTable[s.fnum][s.block][s.patch.ML];
			s.rks = rksTable[(s.fnum) >> 8][s.block][s.patch.KR];

			if (s.type == 0) {
				s.tll = tllTable[(s.fnum) >> 5][s.block][s.patch.TL][s.patch.KL];
			} else {
				s.tll = tllTable[(s.fnum) >> 5][s.block][s.volume][s.patch.KL];
			}
			s.sintbl = waveform[s.patch.WF];
			s.eg_dphase = calc_eg_dphase(s);
		}
	}
	
	public void setRate(OPLL opll, int r) {
		if (opll.quality != 0)
			rate = 49716;
		else
			rate = r;
		internal_refresh();
		rate = r;
	}
	
	public void setQuality(OPLL opll, int q) {
		opll.quality = q;
		setRate(opll, rate);
	}
	
/* ***********************************************************
    Generate wave data
*********************************************************** */
	
	/**
	 * Update AM, PM unit
	 * @param opll
	 */
	private void update_ampm(OPLL opll) {
		opll.pm_phase = (opll.pm_phase + pm_dphase) & (PM_DP_WIDTH - 1);
		opll.am_phase = (opll.am_phase + am_dphase) & (AM_DP_WIDTH - 1);
		opll.lfo_am = amtable[(opll.am_phase) >> (AM_DP_BITS - AM_PG_BITS)];
		opll.lfo_pm = pmtable[(opll.pm_phase) >> (PM_DP_BITS - PM_PG_BITS)];
	}
	
	/**
	 * PG
	 */
	private void calc_phase(OPLLSlot slot, int lfo) {
		if (slot.patch.PM != 0)
			slot.phase += ((slot.dphase * lfo) >> PM_AMP_BITS);
		else
			slot.phase += slot.dphase;

		slot.phase &= (DP_WIDTH - 1);

		slot.pgout = (slot.phase) >> (DP_BASE_BITS);
	}
	
	/**
	 * Update Noise unit
	 */
	private void update_noise (OPLL opll) {
		if ((opll.noise_seed & 1) != 0)
			opll.noise_seed ^= 0x8003020;
		opll.noise_seed >>= 1;
	}
	
	/**
	 * EG
	 */
	private void calc_envelope(OPLLSlot slot, int lfo) {
		int[] SL = new int[16];
		for (int i = 0; i < SL.length; i++) {
			SL[i] = (int) ((3.0 * i / SL_STEP) * (int) (SL_STEP / EG_STEP)) << (EG_DP_BITS - EG_BITS);
		}

		int egout; // unsigned

		switch (slot.eg_mode) {
		case ATTACK:
			egout = AR_ADJUST_TABLE[(slot.eg_phase) >> (EG_DP_BITS - EG_BITS)];
			slot.eg_phase += slot.eg_dphase;
			if ((EG_DP_WIDTH & slot.eg_phase) != 0 || (slot.patch.AR == 15)) {
				egout = 0;
				slot.eg_phase = 0;
				slot.eg_mode = DECAY;
				slot.eg_dphase = calc_eg_dphase(slot);
			}
			break;

		case DECAY:
			egout = (slot.eg_phase) >> (EG_DP_BITS - EG_BITS);
			slot.eg_phase += slot.eg_dphase;
			if (slot.eg_phase >= SL[slot.patch.SL]) {
				if (slot.patch.EG != 0) {
					slot.eg_phase = SL[slot.patch.SL];
					slot.eg_mode = SUSHOLD;
					slot.eg_dphase = calc_eg_dphase(slot);
				} else {
					slot.eg_phase = SL[slot.patch.SL];
					slot.eg_mode = SUSTINE;
					slot.eg_dphase = calc_eg_dphase(slot);
				}
			}
			break;

		case SUSHOLD:
			egout = (slot.eg_phase) >> (EG_DP_BITS - EG_BITS);
			if (slot.patch.EG == 0) {
				slot.eg_mode = SUSTINE;
				slot.eg_dphase = calc_eg_dphase(slot);
			}
			break;

		case SUSTINE:
		case RELEASE:
			egout = (slot.eg_phase) >> (EG_DP_BITS - EG_BITS);
			slot.eg_phase += slot.eg_dphase;
			if (egout >= (1 << EG_BITS)) {
				slot.eg_mode = FINISH;
				egout = (1 << EG_BITS) - 1;
			}
			break;

		case SETTLE:
			egout = (slot.eg_phase) >> (EG_DP_BITS - EG_BITS);
			slot.eg_phase += slot.eg_dphase;
			if (egout >= (1 << EG_BITS)) {
				slot.eg_mode = ATTACK;
				egout = (1 << EG_BITS) - 1;
				slot.eg_dphase = calc_eg_dphase(slot);
			}
			break;

		case FINISH:
			egout = (1 << EG_BITS) - 1;
			break;

		default:
			egout = (1 << EG_BITS) - 1;
			break;
		}

		if (slot.patch.AM != 0) {
			egout = ((egout + slot.tll) * (int) (EG_STEP / DB_STEP)) + lfo;
		} else {
			egout = ((egout + slot.tll) * (int) (EG_STEP / DB_STEP));
		}

		if (egout >= DB_MUTE)
			egout = DB_MUTE - 1;

		slot.egout = egout | 3;
	}
	
	/**
	 * CARRIOR
	 */
	private int calc_slot_car(OPLLSlot slot, int fm) {
		if (slot.egout >= (DB_MUTE - 1)) {
			slot.output[0] = 0;
		} else { // #define wave2_8pi(e) ( (e) << ( 2 + PG_BITS - SLOT_AMP_BITS ))
			slot.output[0] = DB2LIN_TABLE[slot.sintbl[(slot.pgout + ((fm) << (2 + PG_BITS - SLOT_AMP_BITS)))
					& (PG_WIDTH - 1)] + slot.egout];
		}

		slot.output[1] = (slot.output[1] + slot.output[0]) >> 1;
		return slot.output[1];
	}
	
	/**
	 * MODULATOR
	 */
	private int calc_slot_mod(OPLLSlot slot) {
		int fm;

		slot.output[1] = slot.output[0];

		if (slot.egout >= (DB_MUTE - 1)) {
			slot.output[0] = 0;
		} else if (slot.patch.FB != 0) {
			fm = ((slot.feedback) << (1 + PG_BITS - SLOT_AMP_BITS)) >> (7 - slot.patch.FB);
			slot.output[0] = DB2LIN_TABLE[slot.sintbl[(slot.pgout + fm) & (PG_WIDTH - 1)] + slot.egout];
		} else {
			slot.output[0] = DB2LIN_TABLE[slot.sintbl[slot.pgout] + slot.egout];
		}

		slot.feedback = (slot.output[1] + slot.output[0]) >> 1;

		return slot.feedback;

	}
	
	/**
	 * TOM
	 */
	private int calc_slot_tom(OPLLSlot slot) {
		if (slot.egout >= (DB_MUTE - 1))
			return 0;

		return DB2LIN_TABLE[slot.sintbl[slot.pgout] + slot.egout];
	}
	
	/**
	 * SNARE
	 * @param noise
	 *   unsigned
	 */
	private int calc_slot_snare(OPLLSlot slot, int noise) {
		if (slot.egout >= (DB_MUTE - 1))
			return 0;

		if (((slot.pgout >> 7) & 1) != 0)
			return DB2LIN_TABLE[(noise != 0 ? 0 : (int) ((15.0) / DB_STEP)) + slot.egout];
		else
			return DB2LIN_TABLE[(noise != 0 ? DB_MUTE + DB_MUTE : DB_MUTE + DB_MUTE + (int) (15.0 / DB_STEP))
					+ slot.egout];
	}
	
	/**
	 * TOP-CYM
	 * @param pgout_hh
	 *   unsigned
	 */
	private int calc_slot_cym(OPLLSlot slot, int pgout_hh) {
		int dbout;

		if (slot.egout >= (DB_MUTE - 1))
			return 0;
		else if
		/* the same as fmopl.c */
		((((pgout_hh >> (PG_BITS - 8)) & 1) ^ ((pgout_hh >> (PG_BITS - 1)) & 1) | ((pgout_hh >> (PG_BITS - 7)) & 1) ^
		/* different from fmopl.c */
				(((slot.pgout >> (PG_BITS - 7)) & 1) & (((slot.pgout >> (PG_BITS - 5)) & 1) == 0 ? 1 : 0))) != 0)
			dbout = (int) (DB_MUTE + DB_MUTE + (3.0) / DB_STEP);
		else
			dbout = (int) ((3.0) / DB_STEP);

		return DB2LIN_TABLE[dbout + slot.egout];
	}
	
	/**
	 * HI-HAT
	 * @param noise
	 *   unsigned
	 */
	private int calc_slot_hat(OPLLSlot slot, int pgout_cym, int noise) {
		int dbout;

		if (slot.egout >= (DB_MUTE - 1))
			return 0;
		else if ((
		/* the same as fmopl.c */
		((((slot.pgout >> (PG_BITS - 8)) & 1) ^ ((slot.pgout >> (PG_BITS - 1)) & 1))
				| ((slot.pgout >> (PG_BITS - 7)) & 1)) ^
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

		return DB2LIN_TABLE[dbout + slot.egout];
	}
	
	private int calc1(OPLL opll) {
		int inst = 0, perc = 0, out = 0;
		int i;

		update_ampm(opll);
		update_noise(opll);

		for (i = 0; i < 18; i++) {
			calc_phase(opll.slot[i], opll.lfo_pm);
			calc_envelope(opll.slot[i], opll.lfo_am);
		}

		for (i = 0; i < 6; i++)
			if ((opll.mask & (1 << i)) == 0 && (opll.slot[(i << 1) | 1].eg_mode != FINISH))
				inst += calc_slot_car(opll.slot[(i << 1) | 1], calc_slot_mod((opll.slot[i << 1])));

		/* CH6 */
		if (opll.patch_number[6] <= 15) {
			if ((opll.mask & (1 << 6)) == 0 && (opll.slot[(6 << 1) | 1].eg_mode != FINISH))
				inst += calc_slot_car(opll.slot[(6 << 1) | 1], calc_slot_mod(opll.slot[6 << 1]));
		} else {
			if ((opll.mask & 1 << 13) == 0 && (opll.slot[(6 << 1) | 1].eg_mode != FINISH))
				perc += calc_slot_car(opll.slot[(6 << 1) | 1], calc_slot_mod(opll.slot[6 << 1]));
		}

		/* CH7 */
		if (opll.patch_number[7] <= 15) {
			if ((opll.mask & (1 << 7)) == 0 && (opll.slot[(7 << 1) | 1].eg_mode != FINISH))
				inst += calc_slot_car(opll.slot[(7 << 1) | 1], calc_slot_mod(opll.slot[7 << 1]));
		} else {
			if ((opll.mask & 1 << 9) == 0 && (opll.slot[7 << 1].eg_mode != FINISH))
				perc += calc_slot_hat(opll.slot[7 << 1], opll.slot[(8 << 1) | 1].pgout, opll.noise_seed & 1);
			if ((opll.mask & 1 << 12) == 0 && (opll.slot[(7 << 1) | 1].eg_mode != FINISH))
				perc -= calc_slot_snare(opll.slot[(7 << 1) | 1], opll.noise_seed & 1);
		}

		/* CH8 */
		if (opll.patch_number[8] <= 15) {
			if ((opll.mask & (1 << 8)) == 0 && (opll.slot[(8 << 1) | 1].eg_mode != FINISH))
				inst += calc_slot_car(opll.slot[(8 << 1) | 1], calc_slot_mod(opll.slot[8 << 1]));
		} else {
			if ((opll.mask & 1 << 11) == 0 && (opll.slot[8 << 1].eg_mode != FINISH))
				perc += calc_slot_tom(opll.slot[i << 1]);
			if ((opll.mask & 1 << 10) == 0 && (opll.slot[(8 << 1) | 1].eg_mode != FINISH))
				perc -= calc_slot_cym(opll.slot[(8 << 1) | 1], opll.slot[7 << 1].pgout);
		}

		out = inst + (perc << 1);
		return out << 3;
	}
	
	/**
	 * NesVRC7.tick(int clocks) 调用
	 * @param opll
	 * @return
	 */
	public int calc0(OPLL opll) {
		if (opll.quality == 0)
			return calc1(opll);

		while (opll.realstep > opll.oplltime) {
			opll.oplltime += opll.opllstep;
			opll.prev = opll.next;
			opll.next = calc1(opll);
		}

		opll.oplltime -= opll.realstep;
		opll.out = (int) (((double) opll.next * (opll.opllstep - opll.oplltime) + (double) opll.prev * opll.oplltime)
				/ opll.opllstep);

		return (int) opll.out;
	}
	
	/**
	 * @param mask
	 *   unsigned
	 * @return
	 *   unsigned
	 */
	public int setMask(OPLL opll, int m) {
		int ret;

		if (opll != null) {
			ret = opll.mask;
			opll.mask = m;
			return ret;
		} else
			return 0;
	}
	
	/**
	 * (未调用)
	 * @param mask
	 *   unsigned
	 * @return
	 *   unsigned
	 */
	public int toggleMask(OPLL opll, int mask) {
		int ret;

		if (opll != null) {
			ret = opll.mask;
			opll.mask ^= mask;
			return ret;
		} else
			return 0;
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
	public void writeReg(OPLL opll, int reg, int data) {
		int i, v, ch;

		data = data & 0xff;
		reg = reg & 0x3f;
		opll.reg[reg] = data;

		switch (reg) {
		case 0x00:
			opll.patch[0].AM = (data >> 7) & 1;
			opll.patch[0].PM = (data >> 6) & 1;
			opll.patch[0].EG = (data >> 5) & 1;
			opll.patch[0].KR = (data >> 4) & 1;
			opll.patch[0].ML = (data) & 15;
			for (i = 0; i < 9; i++) {
				if (opll.patch_number[i] == 0) {
					OPLLSlot s = opll.slot[i << 1];
					s.dphase = dphaseTable[s.fnum][s.block][s.patch.ML];
					s.rks = rksTable[(s.fnum) >> 8][s.block][s.patch.KR];
					s.eg_dphase = calc_eg_dphase(s);
				}
			}
			break;

		case 0x01:
			opll.patch[1].AM = (data >> 7) & 1;
			opll.patch[1].PM = (data >> 6) & 1;
			opll.patch[1].EG = (data >> 5) & 1;
			opll.patch[1].KR = (data >> 4) & 1;
			opll.patch[1].ML = (data) & 15;
			for (i = 0; i < 9; i++) {
				if (opll.patch_number[i] == 0) {
					OPLLSlot s = opll.slot[(i << 1) | 1];
					s.dphase = dphaseTable[s.fnum][s.block][s.patch.ML];
					s.rks = rksTable[(s.fnum) >> 8][s.block][s.patch.KR];
					s.eg_dphase = calc_eg_dphase(s);
				}
			}
			break;

		case 0x02:
			opll.patch[0].KL = (data >> 6) & 3;
			opll.patch[0].TL = (data) & 63;
			for (i = 0; i < 9; i++) {
				if (opll.patch_number[i] == 0) {
					OPLLSlot s = opll.slot[i << 1];
					if (s.type == 0) {
						s.tll = tllTable[(s.fnum) >> 5][s.block][s.patch.TL][s.patch.KL];
					} else {
						s.tll = tllTable[(s.fnum) >> 5][s.block][s.volume][s.patch.KL];
					}
				}
			}
			break;

		case 0x03:
			opll.patch[1].KL = (data >> 6) & 3;
			opll.patch[1].WF = (data >> 4) & 1;
			opll.patch[0].WF = (data >> 3) & 1;
			opll.patch[0].FB = (data) & 7;
			for (i = 0; i < 9; i++) {
				if (opll.patch_number[i] == 0) {
					OPLLSlot s = opll.slot[i << 1];
					s.sintbl = waveform[s.patch.WF];
					s = opll.slot[(i << 1) | 1];
					s.sintbl = waveform[s.patch.WF];
				}
			}
			break;

		case 0x04:
			opll.patch[0].AR = (data >> 4) & 15;
			opll.patch[0].DR = (data) & 15;
			for (i = 0; i < 9; i++) {
				if (opll.patch_number[i] == 0) {
					OPLLSlot s = opll.slot[i << 1];
					s.eg_dphase = calc_eg_dphase(s);
				}
			}
			break;

		case 0x05:
			opll.patch[1].AR = (data >> 4) & 15;
			opll.patch[1].DR = (data) & 15;
			for (i = 0; i < 9; i++) {
				if (opll.patch_number[i] == 0) {
					OPLLSlot s = opll.slot[(i << 1) | 1];
					s.eg_dphase = calc_eg_dphase(s);
				}
			}
			break;

		case 0x06:
			opll.patch[0].SL = (data >> 4) & 15;
			opll.patch[0].RR = (data) & 15;
			for (i = 0; i < 9; i++) {
				if (opll.patch_number[i] == 0) {
					OPLLSlot s = opll.slot[i << 1];
					s.eg_dphase = calc_eg_dphase(s);
				}
			}
			break;

		case 0x07:
			opll.patch[1].SL = (data >> 4) & 15;
			opll.patch[1].RR = (data) & 15;
			for (i = 0; i < 9; i++) {
				if (opll.patch_number[i] == 0) {
					OPLLSlot s = opll.slot[(i << 1) | 1];
					s.eg_dphase = calc_eg_dphase(s);
				}
			}
			break;

		case 0x0e:
			update_rhythm_mode(opll);
			if ((data & 32) != 0) {
				if ((data & 0x10) != 0)
					keyOn_BD(opll);
				else
					keyOff_BD(opll);
				if ((data & 0x8) != 0)
					keyOn_SD(opll);
				else
					keyOff_SD(opll);
				if ((data & 0x4) != 0)
					keyOn_TOM(opll);
				else
					keyOff_TOM(opll);
				if ((data & 0x2) != 0)
					keyOn_CYM(opll);
				else
					keyOff_CYM(opll);
				if ((data & 0x1) != 0)
					keyOn_HH(opll);
				else
					keyOff_HH(opll);
			}
			update_key_status(opll);

			for (int j = 0; j < 6; j++) {
				OPLLSlot s;
				switch (j) {
				case 0:
					s = opll.slot[6 << 1];
					break;
				case 1:
					s = opll.slot[(6 << 1) | 1];
					break;
				case 2:
					s = opll.slot[7 << 1];
					break;
				case 3:
					s = opll.slot[(7 << 1) | 1];
					break;
				case 4:
					s = opll.slot[8 << 1];
					break;
				case 5:
					s = opll.slot[(8 << 1) | 1];
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
				s.eg_dphase = calc_eg_dphase(s);
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
			setFnumber(opll, ch, data + ((opll.reg[0x20 + ch] & 1) << 8));

		{
			OPLLSlot s = opll.slot[ch << 1];
			s.dphase = dphaseTable[s.fnum][s.block][s.patch.ML];
			if (s.type == 0) {
				s.tll = tllTable[(s.fnum) >> 5][s.block][s.patch.TL][s.patch.KL];
			} else {
				s.tll = tllTable[(s.fnum) >> 5][s.block][s.volume][s.patch.KL];
			}
			s.rks = rksTable[(s.fnum) >> 8][s.block][s.patch.KR];
			s.sintbl = waveform[s.patch.WF];
			s.eg_dphase = calc_eg_dphase(s);
			
			s = opll.slot[(ch << 1) | 1];
			s.dphase = dphaseTable[s.fnum][s.block][s.patch.ML];
			if (s.type == 0) {
				s.tll = tllTable[(s.fnum) >> 5][s.block][s.patch.TL][s.patch.KL];
			} else {
				s.tll = tllTable[(s.fnum) >> 5][s.block][s.volume][s.patch.KL];
			}
			s.rks = rksTable[(s.fnum) >> 8][s.block][s.patch.KR];
			s.sintbl = waveform[s.patch.WF];
			s.eg_dphase = calc_eg_dphase(s);
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
			setFnumber(opll, ch, ((data & 1) << 8) + opll.reg[0x10 + ch]);
			setBlock(opll, ch, (data >> 1) & 7);
			setSustine(opll, ch, (data >> 5) & 1);
			if ((data & 0x10) != 0)
				keyOn(opll, ch);
			else
				keyOff(opll, ch); {
			OPLLSlot s = opll.slot[ch << 1];
			s.dphase = dphaseTable[s.fnum][s.block][s.patch.ML];
			if (s.type == 0) {
				s.tll = tllTable[(s.fnum) >> 5][s.block][s.patch.TL][s.patch.KL];
			} else {
				s.tll = tllTable[(s.fnum) >> 5][s.block][s.volume][s.patch.KL];
			}
			s.rks = rksTable[(s.fnum) >> 8][s.block][s.patch.KR];
			s.sintbl = waveform[s.patch.WF];
			s.eg_dphase = calc_eg_dphase(s);
			
			s = opll.slot[(ch << 1) | 1];
			s.dphase = dphaseTable[s.fnum][s.block][s.patch.ML];
			if (s.type == 0) {
				s.tll = tllTable[(s.fnum) >> 5][s.block][s.patch.TL][s.patch.KL];
			} else {
				s.tll = tllTable[(s.fnum) >> 5][s.block][s.volume][s.patch.KL];
			}
			s.rks = rksTable[(s.fnum) >> 8][s.block][s.patch.KR];
			s.sintbl = waveform[s.patch.WF];
			s.eg_dphase = calc_eg_dphase(s);
		}
			update_key_status(opll);
			update_rhythm_mode(opll);
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
			if ((opll.reg[0x0e] & 32) != 0 && (reg >= 0x36)) {
				switch (reg) {
				case 0x37:
					setSlotVolume(opll.slot[7 << 1], i << 2);
					break;
				case 0x38:
					setSlotVolume(opll.slot[8 << 1], i << 2);
					break;
				default:
					break;
				}
			} else {
				setPatch(opll, reg - 0x30, i);
			}
			setVolume(opll, reg - 0x30, v << 2);

		{
			OPLLSlot s = opll.slot[(reg - 0x30) << 1];
			s.dphase = dphaseTable[s.fnum][s.block][s.patch.ML];
			if (s.type == 0) {
				s.tll = tllTable[(s.fnum) >> 5][s.block][s.patch.TL][s.patch.KL];
			} else {
				s.tll = tllTable[(s.fnum) >> 5][s.block][s.volume][s.patch.KL];
			}
			s.rks = rksTable[(s.fnum) >> 8][s.block][s.patch.KR];
			s.sintbl = waveform[s.patch.WF];
			s.eg_dphase = calc_eg_dphase(s);

			s = opll.slot[((reg - 0x30) << 1) | 1];
			s.dphase = dphaseTable[s.fnum][s.block][s.patch.ML];
			if (s.type == 0) {
				s.tll = tllTable[(s.fnum) >> 5][s.block][s.patch.TL][s.patch.KL];
			} else {
				s.tll = tllTable[(s.fnum) >> 5][s.block][s.volume][s.patch.KL];
			}
			s.rks = rksTable[(s.fnum) >> 8][s.block][s.patch.KR];
			s.sintbl = waveform[s.patch.WF];
			s.eg_dphase = calc_eg_dphase(s);
		}
			break;

		default:
			break;
		}
	}
	
	/**
	 * @param adr
	 *   unsigned
	 * @param val
	 *   unsigned
	 */
	public void writeIO(OPLL opll, int adr, int val) {
		if ((adr & 1) != 0) {
			writeReg(opll, opll.adr, val);
		}
		else
			opll.adr = val;
	}
	
	/**
	 * STEREO MODE (OPT)
	 * (未调用)
	 * @param ch
	 *   unsigned
	 * @param pan
	 *   unsigned
	 */
	public void set_pan(OPLL opll, int ch, int pan) {
		opll.pan[ch & 15] = pan & 3;
	}
	
	/**
	 * static calc_stereo
	 * (未调用)
	 * @param out
	 *   len : 2
	 */
	public void calc_stereo0(OPLL opll, int[] out) {
		int b[] = { 0, 0, 0, 0 }; /* Ignore, Right, Left, Center */
		int r[] = { 0, 0, 0, 0 }; /* Ignore, Right, Left, Center */
		int i;

		update_ampm(opll);
		update_noise(opll);

		for (i = 0; i < 18; i++) {
			calc_phase(opll.slot[i], opll.lfo_pm);
			calc_envelope(opll.slot[i], opll.lfo_am);
		}

		for (i = 0; i < 6; i++)
			if ((opll.mask & (1 << i)) == 0 && (opll.slot[(i << 1) | 1].eg_mode != FINISH))
				b[opll.pan[i]] += calc_slot_car(opll.slot[(i << 1) | 1], calc_slot_mod(opll.slot[i << 1]));

		if (opll.patch_number[6] <= 15) {
			if ((opll.mask & (1 << 6)) == 0 && (opll.slot[(6 << 1) | 1].eg_mode != FINISH))
				b[opll.pan[6]] += calc_slot_car(opll.slot[(6 << 1) | 1], calc_slot_mod(opll.slot[6 << 1]));
		} else {
			if ((opll.mask & (1 << 13)) == 0 && (opll.slot[(6 << 1) | 1].eg_mode != FINISH))
				r[opll.pan[9]] += calc_slot_car(opll.slot[(6 << 1) | 1], calc_slot_mod(opll.slot[6 << 1]));
		}

		if (opll.patch_number[7] <= 15) {
			if ((opll.mask & (1 << 7)) == 0 && (opll.slot[(7 << 1) | 1].eg_mode != FINISH))
				b[opll.pan[7]] += calc_slot_car(opll.slot[(7 << 1) | 1], calc_slot_mod(opll.slot[7 << 1]));
		} else {
			if ((opll.mask & (1 << 9)) == 0 && (opll.slot[7 << 1].eg_mode != FINISH))
				r[opll.pan[10]] += calc_slot_hat(opll.slot[7 << 1], opll.slot[(8 << 1) | 1].pgout, opll.noise_seed & 1);
			if ((opll.mask & (1 << 12)) == 0 && (opll.slot[(7 << 1) | 1].eg_mode != FINISH))
				r[opll.pan[11]] -= calc_slot_snare(opll.slot[(7 << 1) | 1], opll.noise_seed & 1);
		}

		if (opll.patch_number[8] <= 15) {
			if ((opll.mask & (1 << 8)) == 0 && (opll.slot[(8 << 1) | 1].eg_mode != FINISH))
				b[opll.pan[8]] += calc_slot_car(opll.slot[(8 << 1) | 1], calc_slot_mod(opll.slot[8 << 1]));
		} else {
			if ((opll.mask & (1 << 11)) == 0 && (opll.slot[8 << 1].eg_mode != FINISH))
				r[opll.pan[12]] += calc_slot_tom(opll.slot[8 << 1]);
			if ((opll.mask & (1 << 10)) == 0 && (opll.slot[(8 << 1) | 1].eg_mode != FINISH))
				r[opll.pan[13]] -= calc_slot_cym(opll.slot[(8 << 1) | 1], opll.slot[7 << 1].pgout);
		}

		out[1] = (b[1] + b[3] + ((r[1] + r[3]) << 1)) << 3;
		out[0] = (b[2] + b[3] + ((r[2] + r[3]) << 1)) << 3;
	}
	
	/**
	 * OPLL_calc_stereo
	 * (未调用)
	 * @param out
	 *   len : 2
	 */
	public void calc_stereo1(OPLL opll, int[] out) {
		if (opll.quality == 0) {
			calc_stereo0(opll, out);
			return;
		}

		while (opll.realstep > opll.oplltime) {
			opll.oplltime += opll.opllstep;
			opll.sprev[0] = opll.snext[0];
			opll.sprev[1] = opll.snext[1];
			calc_stereo0(opll, opll.snext);
		}

		opll.oplltime -= opll.realstep;
		out[0] = (int) (((double) opll.snext[0] * (opll.opllstep - opll.oplltime)
				+ (double) opll.sprev[0] * opll.oplltime) / opll.opllstep);
		out[1] = (int) (((double) opll.snext[1] * (opll.opllstep - opll.oplltime)
				+ (double) opll.sprev[1] * opll.oplltime) / opll.opllstep);
	}

}
