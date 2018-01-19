package com.zdream.nsfplayer.nsf.device;

import com.zdream.nsfplayer.sound.PulseSound;
import com.zdream.nsfplayer.xgm.device.IntHolder;

public class Pulses implements IDevice {
	
	/**
	 * 矩形波发声器
	 */
	PulseSound pulse1, pulse2;
	
	/**
	 * 两个发声器是否可用
	 */
	boolean enable1, enable2;
	
	public Pulses() {
		pulse1 = new PulseSound();
		pulse2 = new PulseSound();
	}

	@Override
	public void reset() {
		pulse1.reset();
		pulse2.reset();
	}

	@Override
	public boolean write(int adr, int val, int id) {
		if (adr < 0x4000 || adr >= 0x4008 && adr != 0x4015) {
			return false;
		}
		
		if (adr == 0x4015) {
			enable1 = (val & 1) != 0;
			enable2 = (val & 2) != 0;
			
			if (!enable1) {
				pulse1.length_counter = 0;
			}
			if (!enable2) {
				pulse2.length_counter = 0;
			}
			
			return true;
		}
		
		switch (adr) {
		case 0x4000:
			pulse1.volume = val & 15; // 0000xxxx
			pulse1.envelope_disable = ((val >> 4) & 1) != 0; // 000x0000
			pulse1.envelope_loop = ((val >> 5) & 1) != 0; // 00x00000
			pulse1.envelope_div_period = (val & 15);
			pulse1.duty = (val >> 6) & 3; // xx000000
			break;
			
		case 0x4001:
			pulse1.sweep_enable = ((val >> 7) & 1) != 0;
			pulse1.sweep_div_period = (((val >> 4) & 7));
			pulse1.sweep_mode = ((val >> 3) & 1) != 0;
			pulse1.sweep_amount = val & 7;
			pulse1.sweep_write = true;
			pulse1.sweepUpdate(true);
			break;
			
		case 0x4002:
			
			break;
			
		case 0x4003:
			
			break;
		case 0x4004:
			pulse2.volume = val & 15; // 0000xxxx
			pulse2.envelope_disable = ((val >> 4) & 1) != 0; // 000x0000
			pulse2.envelope_loop = ((val >> 5) & 1) != 0; // 00x00000
			pulse2.envelope_div_period = (val & 15);
			pulse2.duty = (val >> 6) & 3; // xx000000
			break;
			
		case 0x4005:
			pulse2.sweep_enable = ((val >> 7) & 1) != 0;
			pulse2.sweep_div_period = (((val >> 4) & 7));
			pulse2.sweep_mode = ((val >> 3) & 1) != 0;
			pulse2.sweep_amount = val & 7;
			pulse2.sweep_write = true;
			pulse2.sweepUpdate(false);
			break;
			
		case 0x4006:
			
			break;
			
		case 0x4007:
			
			break;
			
		default:
			break;
		}
		
		
		
		
		return true;
	}

	@Override
	public boolean read(int adr, IntHolder val, int id) {
		// TODO Auto-generated method stub
		return false;
	}

}
