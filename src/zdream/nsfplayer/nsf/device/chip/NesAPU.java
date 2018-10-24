package zdream.nsfplayer.nsf.device.chip;

import java.util.Arrays;

import zdream.nsfplayer.nsf.device.AbstractSoundChip;
import zdream.nsfplayer.nsf.device.cpu.IntHolder;
import zdream.nsfplayer.nsf.renderer.NsfRuntime;
import zdream.nsfplayer.sound.AbstractNsfSound;
import zdream.nsfplayer.sound.PulseSound;

/**
 * APU 音频设备, 管理输出 Pulse1 和 Pulse2 轨道的音频
 * 
 * @author Zdream
 * @since v0.2.4
 */
public class NesAPU extends AbstractSoundChip {
	
	private PulseSound pulse1, pulse2;
	
	/**
	 * 记录放置的参数
	 */
	private byte[] mem = new byte[8];
	private byte mem4015, mem4017 = 0;
	
	public NesAPU(NsfRuntime runtime) {
		super(runtime);
		pulse1 = new PulseSound();
		pulse2 = new PulseSound();
	}

	@Override
	public boolean write(int adr, int val, int id) {
		/*
		 * APU 这里要接收的地址有:
		 * [0x4000, 0x4007], 0x4015, 0x4017
		 */
		switch (adr) {
		case 0x4000: case 0x4001: case 0x4002: case 0x4003: {
			writeToPulse1(adr & 3, val);
			mem[adr & 3] = (byte) val;
		} break;
		case 0x4004: case 0x4005: case 0x4006: case 0x4007: {
			writeToPulse2(adr & 3, val);
			mem[(adr & 3) + 4] = (byte) val;
		} break;
		case 0x4015: {
			// enable
			mem4015 = (byte) val;
		} break;
		case 0x4017: {
			// 未知
			mem4017 = (byte) val;
		} break;

		default:
			return false;
		}
		
//		System.out.println(String.format("[%4X]:%2X, %3d", adr, val, val));
		return true;
	}
	
	public void writeToPulse1(int adr, int value) {
		switch (adr) {
		case 0:
			pulse1.dutyLength = (value >> 6);
			pulse1.looping = (value & 0x20) != 0;
			pulse1.envelopeFix = (value & 0x10) != 0;
			pulse1.fixedVolume = (value & 0xF);
			break;
			
		case 1:
			pulse1.sweepEnabled = (value >> 7) != 0;
			pulse1.sweepPeriod = (value & 0x70) >> 4;
			pulse1.sweepMode = (value & 8) != 0;
			pulse1.sweepShift = value & 7;
			break;
			
		case 2: {
			int period = (pulse1.period & 0xFF00) + value;
			pulse1.period = period;
		} break;
		
		case 3: {
			int period = (pulse1.period & 0xFF) + ((value & 7) << 8);
			pulse1.period = period;
			pulse1.lengthCounter = PulseSound.LENGTH_TABLE[(value & 0xF8) >> 3];
		} break;
		}
	}
	
	public void writeToPulse2(int adr, int value) {
		switch (adr) {
		case 0:
			pulse2.dutyLength = (value >> 6);
			pulse2.looping = (value & 0x20) != 0;
			pulse2.envelopeFix = (value & 0x10) != 0;
			pulse2.fixedVolume = (value & 0xF);
			break;
			
		case 1:
			pulse2.sweepEnabled = (value >> 7) != 0;
			pulse2.sweepPeriod = (value & 0x70) >> 4;
			pulse2.sweepMode = (value & 8) != 0;
			pulse2.sweepShift = value & 7;
			break;
			
		case 2: {
			int period = (pulse2.period & 0xFF00) + value;
			pulse2.period = period;
		} break;
		
		case 3: {
			int period = (pulse2.period & 0xFF) + ((value & 7) << 8);
			pulse2.period = period;
			pulse2.lengthCounter = PulseSound.LENGTH_TABLE[(value & 0xF8) >> 3];
		} break;
		}
	}

	@Override
	public boolean read(int adr, IntHolder val, int id) {
		if (adr >= 0x4000 && adr < 0x4008) {
			val.val = mem[adr & 0x7] & 0xFF;
			return true;
		} else if (adr == 0x4015) {
			val.val = mem4015 & 0xFF;
			return true;
		} else if (adr == 0x4017) {
			val.val = mem4017 & 0xFF;
			return true;
		}
		return false;
	}

	@Override
	public void reset() {
		pulse1.reset();
		pulse2.reset();

		Arrays.fill(mem, (byte) 0);
		mem4015 = 0x7F;
		mem4017 = 0;
	}

	@Override
	public AbstractNsfSound getSound(byte code) {
		switch (code) {
		case CHANNEL_2A03_PULSE1: return pulse1;
		case CHANNEL_2A03_PULSE2: return pulse2;
		}
		
		return null;
	}
	
	@Override
	public byte[] getAllChannelCodes() {
		return new byte[] {CHANNEL_2A03_PULSE1, CHANNEL_2A03_PULSE2};
	}

}
