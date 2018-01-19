package com.zdream.nsfplayer.nsf.xgm;

import static com.zdream.nsfplayer.nsf.xgm.NsfTypes.*;

public enum ENsfChannel {
	
	UNKNOWED,
	
	PULSE1(CHIP_INTERNAL),
	PULSE2(CHIP_INTERNAL),
	TRIANGLE(CHIP_INTERNAL),
	NOISE(CHIP_INTERNAL),
	DCPM(CHIP_INTERNAL),
	
	VRC6_PULSE1(CHIP_VRC6),
	VRC6_PULSE2(CHIP_VRC6),
	VRC6_SAWTOOTH(CHIP_VRC6);
	
	/**
	 * <p>这个轨道从属于哪个芯片的
	 * @see NsfTypes#CHIP_INTERNAL
	 */
	final public byte chip;
	
	private ENsfChannel() {
		this(CHIP_UNKNOWED);
		System.out.print(".");
	}
	
	private ENsfChannel(byte chip) {
		this.chip = chip;
	}
	
}
