package com.zdream.nsfplayer.nsf.device;

import java.util.Arrays;

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
	
	/**
	 * 缓存写入的数据, 0x4000 到 0x4008
	 */
	byte[] reg = new byte[8];
	
	/**
	 * 缓存写入的控制位置, 0x4015
	 */
	byte ctrlReg;
	
	/*
	 * 下面的数据与 0x4017 位置相关
	 */
	boolean frame_irq_enable;
	boolean frame_irq;
	int frame_sequence_count;
	int frame_sequence_steps;
	int frame_sequence_step;
	
	
	public Pulses() {
		pulse1 = new PulseSound();
		pulse2 = new PulseSound();
		
		this.reset();
	}

	@Override
	public void reset() {
		pulse1.reset();
		pulse2.reset();
		
		Arrays.fill(reg, (byte) 0);
		
		// 0x4015 位置写入 0x0f
		ctrlReg = 0x0f;
		enable1 = true;
		enable2 = true;
	}
	
	private static final int[] LENGTH_TABLE = { // len : 32
			0x0A, 0xFE,
			0x14, 0x02,
			0x28, 0x04,
			0x50, 0x06,
			0xA0, 0x08,
			0x3C, 0x0A,
			0x0E, 0x0C,
			0x1A, 0x0E,
			0x0C, 0x10,
			0x18, 0x12,
			0x30, 0x14,
			0x60, 0x16,
			0xC0, 0x18,
			0x48, 0x1A,
			0x10, 0x1C,
			0x20, 0x1E
		};

	@Override
	public boolean write(int adr, int val, int id) {
		if (adr < 0x4000 || adr >= 0x4008 && adr != 0x4015 && adr != 0x4017) {
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
			
			ctrlReg = (byte) val;
			return true;
		}
		
		if (adr == 0x4017) {
			frame_irq_enable = ((val & 0x40) == 0x40);
			frame_irq = (frame_irq_enable ? frame_irq : false);
			frame_sequence_count = 0;
			if ((val & 0x80) != 0) {
				frame_sequence_steps = 5;
				frame_sequence_step = 0;
				frameSequence();
				++frame_sequence_step;
			} else {
				frame_sequence_steps = 4;
				frame_sequence_step = 1;
			}
			return true;
		}
		
		switch (adr) {
		case 0x4000:
			pulse1.volume = val & 15; // 0000xxxx
			pulse1.envelope_disable = ((val >> 4) & 1) != 0; // 000x0000
			pulse1.envelope_loop = ((val >> 5) & 1) != 0; // 00x00000
			pulse1.envelope_div_period = pulse1.volume;
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
			pulse1.period = val | (pulse1.period & 0x700);
			pulse1.sweepUpdate(true);
			if (pulse1.scounter > pulse1.period)
				pulse1.scounter = pulse1.period;
			break;
			
		case 0x4003:
			pulse1.period = (pulse1.period & 0xFF) | ((val & 0x7) << 8);
			pulse1.envelope_write = true;
			if (enable1) {
				pulse1.length_counter = LENGTH_TABLE[(val >> 3) & 0x1f];
			}
			pulse1.sweepUpdate(true);
			if (pulse1.scounter > pulse1.period)
				pulse1.scounter = pulse1.period;
			break;
			
		case 0x4004:
			pulse2.volume = val & 15; // 0000xxxx
			pulse2.envelope_disable = ((val >> 4) & 1) != 0; // 000x0000
			pulse2.envelope_loop = ((val >> 5) & 1) != 0; // 00x00000
			pulse2.envelope_div_period = pulse2.volume;
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
			pulse2.period = val | (pulse2.period & 0x700);
			pulse2.sweepUpdate(false);
			if (pulse2.scounter > pulse2.period)
				pulse2.scounter = pulse2.period;
			break;
			
		case 0x4007:
			pulse2.period = (pulse2.period & 0xFF) | ((val & 0x7) << 8);
			pulse2.envelope_write = true;
			if (enable2) {
				pulse2.length_counter = LENGTH_TABLE[(val >> 3) & 0x1f];
			}
			pulse2.sweepUpdate(false);
			if (pulse2.scounter > pulse2.period)
				pulse2.scounter = pulse2.period;
			break;
			
		default:
			break;
		}
		
		reg[adr - 0x4000] = (byte) val;
		
		return true;
	}

	@Override
	public boolean read(int adr, IntHolder val, int id) {
		if (0x4000 <= adr && adr < 0x4008) {
			val.val |= reg[adr & 0x7] & 0xFF;
			return true;
		} else if (adr == 0x4015) {
			val.val |= (pulse2.length_counter != 0 ? 2 : 0) | (pulse1.length_counter != 0 ? 1 : 0);
			return true;
		} else
			return false;
	}
	
	private void frameSequence() {
		int s = frame_sequence_step;
		if (s > 3) return; // no operation in step 4

		// 240hz clock
		for (int i = 0; i < 2; i++) {
			PulseSound p = (i == 0) ? pulse1 : pulse2;
			
			boolean divider = false;
			if (p.envelope_write) {
				p.envelope_write = false;
				p.envelope_counter = 15;
				p.envelope_div = 0;
			} else {
				++p.envelope_div;
				if (p.envelope_div > p.envelope_div_period) {
					divider = true;
					p.envelope_div = 0;
				}
			}
			if (divider) {
				if (p.envelope_loop && p.envelope_counter == 0)
					p.envelope_counter = 15;
				else if (p.envelope_counter > 0)
					--p.envelope_counter;
			}
		}

		// 120hz clock
		if ((s & 1) == 0) {
			for (int i = 0; i < 2; ++i) {
				PulseSound p = (i == 0) ? pulse1 : pulse2;
				
				if (!p.envelope_loop && (p.length_counter > 0))
					--p.length_counter;

				if (p.sweep_enable) {

					--p.sweep_div;
					if (p.sweep_div <= 0) {
						p.sweepUpdate(i == 0); // calculate new sweep target

						if (p.period >= 8 && p.sweep_freq < 0x800 && p.sweep_amount > 0) { // update frequency if appropriate
							p.sweep_freq = p.sweep_freq < 0 ? 0 : p.sweep_freq;
							if (p.scounter > p.period)
								p.scounter = p.period;
						}
						p.sweep_div = p.sweep_div_period + 1;
					}

					if (p.sweep_write) {
						p.sweep_div = p.sweep_div_period + 1;
						p.sweep_write = false;
					}
				}
			}
		}
	}
	
	public void tick(int clocks) {
		pulse1.tick(clocks);
		pulse2.tick(clocks);
	}

}
