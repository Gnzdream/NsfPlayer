package zdream.nsfplayer.nsf.device.chip;

import zdream.nsfplayer.nsf.device.AbstractSoundChip;
import zdream.nsfplayer.nsf.device.cpu.IntHolder;
import zdream.nsfplayer.nsf.renderer.NsfRuntime;
import zdream.nsfplayer.sound.AbstractNsfSound;
import zdream.nsfplayer.sound.SoundVRC6Pulse;
import zdream.nsfplayer.sound.SoundVRC6Sawtooth;

/**
 * VRC6 音频芯片, 管理输出 VRC6 Pulse1, VRC6 Pulse2 和 VRC6 Sawtooth 轨道的音频
 * 
 * @author Zdream
 * @since v0.2.4
 */
public class NesVRC6 extends AbstractSoundChip {
	
	private SoundVRC6Pulse pulse1, pulse2;
	private SoundVRC6Sawtooth sawtooth;

	public NesVRC6(NsfRuntime runtime) {
		super(runtime);
		pulse1 = new SoundVRC6Pulse();
		pulse2 = new SoundVRC6Pulse();
		sawtooth = new SoundVRC6Sawtooth();
	}

	@Override
	public boolean write(int adr, int val, int id) {
		switch (adr) {
		case 0x9000: case 0x9001: case 0x9002: {
			int address = adr & 3;
			writeToPulse1(address, val);
		} break;
		case 0xA000: case 0xA001: case 0xA002: {
			int address = adr & 3;
			writeToPulse2(address, val);
		} break;
		case 0xB000: case 0xB001: case 0xB002: {
			int address = adr & 3;
			writeToSawtooth(address, val);
		} break;

		default:
			return false;
		}

		return true;
	}
	
	private void writeToPulse1(int address, int value) {
		switch (address) {
		case 0:
			pulse1.gate = (value & 0x80) != 0;
			pulse1.duty = (value >> 4) & 7;
			pulse1.volume = (value & 0xF);
			break;
			
		case 1: {
			int period = (pulse1.period & 0xFF00) + value;
			pulse1.period = period;
		} break;
		
		case 2: {
			int period = (pulse1.period & 0xFF) + ((value & 15) << 8);
			pulse1.period = period;
			pulse1.setEnable((value & 0x80) != 0);
		} break;
		}
	}
	
	private void writeToPulse2(int address, int value) {
		switch (address) {
		case 0:
			pulse2.gate = (value >> 7) != 0;
			pulse2.duty = (value >> 4) & 7;
			pulse2.volume = (value & 0xF);
			break;
			
		case 1: {
			int period = (pulse2.period & 0xFF00) + value;
			pulse2.period = period;
		} break;
		
		case 2: {
			int period = (pulse2.period & 0xFF) + ((value & 15) << 8);
			pulse2.period = period;
			pulse2.setEnable((value & 0x80) != 0);
		} break;
		}
	}
	
	private void writeToSawtooth(int address, int value) {
		switch (address) {
		case 0:
			sawtooth.volume = value & 0x3F;
			break;
			
		case 1: {
			int period = (sawtooth.period & 0xFF00) + value;
			sawtooth.period = period;
		} break;
		
		case 2: {
			int period = (sawtooth.period & 0xFF) + ((value & 15) << 8);
			sawtooth.period = period;
			sawtooth.setEnable((value & 0x80) != 0);
		} break;
		}
	}

	@Override
	public boolean read(int adr, IntHolder val, int id) {
		// 扩展的芯片一致返回 false, 不输出
		return false;
	}

	@Override
	public void reset() {
		pulse1.reset();
		pulse2.reset();
		sawtooth.reset();
	}

	@Override
	public AbstractNsfSound getSound(byte code) {
		switch (code) {
		case CHANNEL_VRC6_PULSE1: return pulse1;
		case CHANNEL_VRC6_PULSE2: return pulse2;
		case CHANNEL_VRC6_SAWTOOTH: return sawtooth;
		}
		
		return null;
	}

	@Override
	public byte[] getAllChannelCodes() {
		return new byte[] {CHANNEL_VRC6_PULSE1, CHANNEL_VRC6_PULSE2, CHANNEL_VRC6_SAWTOOTH};
	}
}
