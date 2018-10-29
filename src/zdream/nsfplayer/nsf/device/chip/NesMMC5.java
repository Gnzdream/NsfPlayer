package zdream.nsfplayer.nsf.device.chip;

import java.util.Arrays;

import zdream.nsfplayer.nsf.device.AbstractSoundChip;
import zdream.nsfplayer.nsf.device.cpu.IntHolder;
import zdream.nsfplayer.nsf.renderer.NsfRuntime;
import zdream.nsfplayer.sound.AbstractNsfSound;
import zdream.nsfplayer.sound.PulseSound;

/**
 * MMC5 音频芯片, 管理输出 MMC5 Pulse1, MMC5 Pulse2 轨道的音频
 * (不包含 MMC5 PCM 轨道)
 * 
 * @author Zdream
 * @since v0.2.4
 */
public class NesMMC5 extends AbstractSoundChip {
	
	private final PulseSound pulse1, pulse2;
	
	/**
	 * 记录放置的参数
	 */
	private final byte[] mem = new byte[8];
	private byte mem5015 = 0; // enable

	public NesMMC5(NsfRuntime runtime) {
		super(runtime);
		pulse1 = new PulseSound();
		pulse2 = new PulseSound();
	}

	@Override
	public boolean write(int adr, int val, int id) {
		/*
		 * APU 这里要接收的地址有:
		 * [0x5000, 0x5007], 0x5015
		 */
		switch (adr) {
		case 0x5000: case 0x5001: case 0x5002: case 0x5003: {
			writeToPulse(pulse1, adr & 3, val);
			mem[adr & 7] = (byte) val;
		} break;
		case 0x5004: case 0x5005: case 0x5006: case 0x5007: {
			writeToPulse(pulse2, adr & 3, val);
			mem[adr & 7] = (byte) val;
		} break;
		case 0x5015: {
			// enable
			mem5015 = (byte) val;
			pulse1.setEnable((mem5015 & 1) != 0);
			pulse2.setEnable((mem5015 & 2) != 0);
		} break;

		default:
			return false;
		}
		
//		System.out.println(String.format("[%4X]:%2X, %3d", adr, val, val));
		return true;
	}

	private void writeToPulse(PulseSound pulse, int adr, int value) {
		switch (adr) {
		case 0:
			pulse.dutyLength = (value >> 6);
			pulse.looping = (value & 0x20) != 0;
			pulse.envelopeFix = (value & 0x10) != 0;
			pulse.fixedVolume = (value & 0xF);
			break;
			
		case 1:
			// MMC5 没有 sweep 相关的控制命令, 这里忽略
			break;
			
		case 2: {
			int period = (pulse.period & 0xFF00) + value;
			pulse.period = period;
		} break;
		
		case 3: {
			int period = (pulse.period & 0xFF) + ((value & 7) << 8);
			pulse.period = period;
			pulse.lengthCounter = PulseSound.LENGTH_TABLE[(value & 0xF8) >> 3];
		} break;
		}
	}

	@Override
	public boolean read(int adr, IntHolder val, int id) {
		if (adr >= 0x5000 && adr < 0x5008) {
			val.val = mem[adr & 0x7] & 0xFF;
			return true;
		} else if (adr == 0x5015) {
			val.val = mem5015 & 0xFF;
			return true;
		}
		return false;
	}

	@Override
	public void reset() {
		pulse1.reset();
		pulse2.reset();

		Arrays.fill(mem, (byte) 0);
		mem5015 = 0x7F;
		pulse1.setEnable(true);
		pulse2.setEnable(true);
	}

	@Override
	public AbstractNsfSound getSound(byte channelCode) {
		switch (channelCode) {
		case CHANNEL_MMC5_PULSE1: return pulse1;
		case CHANNEL_MMC5_PULSE2: return pulse2;
		}
		
		return null;
	}

	@Override
	public byte[] getAllChannelCodes() {
		return new byte[] {CHANNEL_MMC5_PULSE1, CHANNEL_MMC5_PULSE2};
	}

}
