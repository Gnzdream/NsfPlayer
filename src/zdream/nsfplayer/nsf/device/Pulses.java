package zdream.nsfplayer.nsf.device;

import java.util.Arrays;

import zdream.nsfplayer.sound.PulseSound;
import zdream.nsfplayer.xgm.device.IntHolder;

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

	@Override
	public boolean write(int adr, int val, int id) { // 现在不接收 0x4017 的数据
		if (adr < 0x4000 || adr >= 0x4008 && adr != 0x4015) {
			return false;
		}
		
		if (adr == 0x4015) {
			pulse1.writeControl((val & 1) != 0);
			pulse2.writeControl((val & 2) != 0);
			
			ctrlReg = (byte) val;
			return true;
		}
		
		switch (adr) {
		case 0x4000:
		case 0x4001:
		case 0x4002:
		case 0x4003:
			pulse1.write(adr & 0x3, val & 0xFF);
			break;
			
		case 0x4004:
		case 0x4005:
		case 0x4006:
		case 0x4007:
			pulse2.write(adr & 0x3, val & 0xFF);
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
			val.val = reg[adr & 0x7] & 0xFF;
			return true;
		} else if (adr == 0x4015) {
			val.val = ctrlReg;
			return true;
		} else
			return false;
	}
	
	public PulseSound getSound1() {
		return pulse1;
	}
	
	public PulseSound getSound2() {
		return pulse2;
	}

}
