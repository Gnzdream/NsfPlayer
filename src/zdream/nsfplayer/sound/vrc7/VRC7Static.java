package zdream.nsfplayer.sound.vrc7;

public class VRC7Static {
	
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

	public static final int
			SLOT_BD1 = 12,
			SLOT_BD2 = 13,
			SLOT_HH = 14,
			SLOT_SD = 15,
			SLOT_TOM = 16,
			SLOT_CYM = 17;
	
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

}
