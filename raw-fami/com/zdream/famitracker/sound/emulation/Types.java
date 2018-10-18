package com.zdream.famitracker.sound.emulation;

public class Types {
	
	public static final byte SNDCHIP_NONE = 0;
	/**
	 * Konami VRCVI
	 */
	public static final byte SNDCHIP_VRC6  = 1;
	/**
	 * Konami VRCVII
	 */
	public static final byte SNDCHIP_VRC7  = 2;
	/**
	 * Famicom Disk Sound
	 */
	public static final byte SNDCHIP_FDS	  = 4;
	/**
	 * Nintendo MMC5
	 */
	public static final byte SNDCHIP_MMC5  = 8;
	/**
	 * Namco N-106
	 */
	public static final byte SNDCHIP_N163  = 16;
	/**
	 * Sunsoft 5B
	 */
	public static final byte SNDCHIP_S5B	  = 32;

	public static final byte
		CHANID_SQUARE1 = 0,
		CHANID_SQUARE2 = 1,
		CHANID_TRIANGLE = 2,
		CHANID_NOISE = 3,
		CHANID_DPCM = 4,

		CHANID_VRC6_PULSE1 = 5,
		CHANID_VRC6_PULSE2 = 6,
		CHANID_VRC6_SAWTOOTH = 7,

		CHANID_MMC5_SQUARE1 = 8,
		CHANID_MMC5_SQUARE2 = 9,
		CHANID_MMC5_VOICE = 10,

		CHANID_N163_CHAN1 = 11,
		CHANID_N163_CHAN2 = 12,
		CHANID_N163_CHAN3 = 13,
		CHANID_N163_CHAN4 = 14,
		CHANID_N163_CHAN5 = 15,
		CHANID_N163_CHAN6 = 16,
		CHANID_N163_CHAN7 = 17,
		CHANID_N163_CHAN8 = 18,

		CHANID_FDS = 19,

		CHANID_VRC7_CH1 = 20,
		CHANID_VRC7_CH2 = 21,
		CHANID_VRC7_CH3 = 22,
		CHANID_VRC7_CH4 = 23,
		CHANID_VRC7_CH5 = 24,
		CHANID_VRC7_CH6 = 25,

		CHANID_S5B_CH1 = 26,
		CHANID_S5B_CH2 = 27,
		CHANID_S5B_CH3 = 28,
		/* Total number of channels */
		CHANNELS = 29;

	public static final byte
		MACHINE_NTSC = 0, 
		MACHINE_PAL = 1;

}
