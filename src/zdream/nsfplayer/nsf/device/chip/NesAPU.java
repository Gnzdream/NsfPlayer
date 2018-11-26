package zdream.nsfplayer.nsf.device.chip;

import java.util.Arrays;

import zdream.nsfplayer.core.ERegion;
import zdream.nsfplayer.nsf.device.AbstractSoundChip;
import zdream.nsfplayer.nsf.device.cpu.IntHolder;
import zdream.nsfplayer.nsf.renderer.NsfRuntime;
import zdream.nsfplayer.sound.AbstractNsfSound;
import zdream.nsfplayer.sound.EnvelopeSoundNoise;
import zdream.nsfplayer.sound.PulseSound;
import zdream.nsfplayer.sound.SweepSoundPulse;

/**
 * APU 音频设备, 管理输出 Pulse1 和 Pulse2 轨道的音频
 * 
 * @author Zdream
 * @since v0.2.4
 */
public class NesAPU extends AbstractSoundChip {
	
	private SweepSoundPulse pulse1, pulse2;
	
	/**
	 * 记录放置的参数
	 */
	private byte[] mem = new byte[8];
	private byte mem4015, mem4017 = 0;
	
	public NesAPU(NsfRuntime runtime) {
		super(runtime);
		pulse1 = new SweepSoundPulse(true);
		pulse2 = new SweepSoundPulse(false);
	}

	@Override
	public boolean write(int adr, int val, int id) {
		/*
		 * APU 这里要接收的地址有:
		 * [0x4000, 0x4007], 0x4015, 0x4017
		 */
		switch (adr) {
		case 0x4000: case 0x4001: case 0x4002: case 0x4003: {
			writeToPulse(adr & 3, val, pulse1);
			mem[adr & 3] = (byte) val;
		} break;
		case 0x4004: case 0x4005: case 0x4006: case 0x4007: {
			writeToPulse(adr & 3, val, pulse2);
			mem[(adr & 3) + 4] = (byte) val;
		} break;
		case 0x4015: {
			// enable
			mem4015 = (byte) val;
			handleEnable();
		} break;
		case 0x4017: {
			// 未知
			mem4017 = (byte) val;
		} break;

		default:
			return false;
		}
		
		return true;
	}
	
	public void writeToPulse(int adr, int value, SweepSoundPulse pulse) {
		switch (adr) {
		case 0:
			pulse.dutyLength = (value >> 6);
			pulse.envelopeLoop = (value & 0x20) != 0;
			pulse.envelopeFix = (value & 0x10) != 0;
			pulse.fixedVolume = (value & 0xF);
			break;
			
		case 1:
			pulse.sweepEnabled = (value >> 7) != 0;
			pulse.sweepPeriod = (value & 0x70) >> 4;
			pulse.sweepMode = (value & 8) != 0;
			pulse.sweepShift = value & 7;
			pulse.onSweepUpdated();
			break;
			
		case 2: {
			int period = (pulse.period & 0xFF00) + value;
			pulse.period = period;
			pulse.onSweepUpdated();
		} break;
		
		case 3: {
			int period = (pulse.period & 0xFF) + ((value & 7) << 8);
			pulse.period = period;
			pulse.lengthCounter = PulseSound.LENGTH_TABLE[(value & 0xF8) >> 3];
			pulse.onEnvelopeUpdated();
			pulse.onSweepUpdated();
		} break;
		}
	}
	
	/**
	 * <p>这里主要处理发声器的 enable 开关
	 * </p>
	 */
	private void handleEnable() {
		pulse1.setEnable((mem4015 & 1) != 0);
		pulse2.setEnable((mem4015 & 2) != 0);

		if (!pulse1.isEnable()) {
			pulse1.lengthCounter = 0;
		}
		if (!pulse2.isEnable()) {
			pulse2.lengthCounter = 0;
		}
	}

	@Override
	public boolean read(int adr, IntHolder val, int id) {
		if (adr >= 0x4000 && adr < 0x4008) {
			val.val = mem[adr & 0x7] & 0xFF;
			return true;
		} else if (adr == 0x4015) {
			int m = pulse1.isEnable() ? 1 : 0;
			m |= pulse2.isEnable() ? 1 : 0;
			val.val = m;
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
		pulse1.setSequenceStep(
				getRuntime().manager.getRegion() == ERegion.PAL ?
						EnvelopeSoundNoise.SEQUENCE_STEP_PAL : EnvelopeSoundNoise.SEQUENCE_STEP_NTSC);
		pulse2.setSequenceStep(
				getRuntime().manager.getRegion() == ERegion.PAL ?
						EnvelopeSoundNoise.SEQUENCE_STEP_PAL : EnvelopeSoundNoise.SEQUENCE_STEP_NTSC);

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
