package zdream.nsfplayer.nsf.device.chip;

import zdream.nsfplayer.nsf.device.AbstractSoundChip;
import zdream.nsfplayer.nsf.device.cpu.IntHolder;
import zdream.nsfplayer.nsf.renderer.NsfRuntime;
import zdream.nsfplayer.sound.AbstractNsfSound;
import zdream.nsfplayer.sound.SoundS5B;

/**
 * S5B 音频芯片, 管理输出 1 到 3 号共 3 个 Sunsoft 5B 轨道的音频
 * 
 * @author Zdream
 * @since v0.2.8
 */
public class NesS5B extends AbstractSoundChip {
	
	private final SoundS5B s1, s2, s3;
	int address;

	public NesS5B(NsfRuntime runtime) {
		super(runtime);
		s1 = new SoundS5B();
		s2 = new SoundS5B();
		s3 = new SoundS5B();
	}

	@Override
	public boolean write(int adr, int val, int id) {
		if (adr == 0xC000) {
			this.address = val & 15;
			return true;
		}
		if (adr == 0xE000) {
			writeToSound(this.address, val);
			return true;
		} else
			return false;
	}
	
	public void writeToSound(int address, int value) {
		value &= 0xFF;
		
		switch (address) {
		case 0:
			s1.freq = (s1.freq & 0xFF00) | value;
			break;
		case 1:
			s1.freq = (s1.freq & 0x00FF) | (value << 8);
			break;
		case 2:
			s2.freq = (s2.freq & 0xFF00) | value;
			break;
		case 3:
			s2.freq = (s2.freq & 0x00FF) | (value << 8);
			break;
		case 4:
			s3.freq = (s3.freq & 0xFF00) | value;
			break;
		case 5:
			s3.freq = (s3.freq & 0x00FF) | (value << 8);
			break;

		case 6: {
			int reg = value & 0x1F;
			int noiseFreq = (reg == 0) ? 1 : reg << 1;
			s1.noiseFreq = noiseFreq;
			s2.noiseFreq = noiseFreq;
			s3.noiseFreq = noiseFreq;
		} break;

		case 7: {
			s1.waveEnable = (value & 1) != 0;
			s2.waveEnable = (value & 2) != 0;
			s3.waveEnable = (value & 4) != 0;
			s1.noiseEnable = (value & 8) != 0;
			s2.noiseEnable = (value & 16) != 0;
			s3.noiseEnable = (value & 32) != 0;
		} break;

		case 8:
			s1.volume = value << 1;
			break;

		case 9:
			s2.volume = value << 1;
			break;

		case 10:
			s3.volume = value << 1;
			break;

		case 11: {
			int envSpeed = (s1.envelopeSpeed & 0xFF00) | value;
			s1.envelopeSpeed = envSpeed;
			s2.envelopeSpeed = envSpeed;
			s3.envelopeSpeed = envSpeed;
		} break;

		case 12: {
			int envSpeed = (s1.envelopeSpeed & 0xFF) | (value << 8);
			s1.envelopeSpeed = envSpeed;
			s2.envelopeSpeed = envSpeed;
			s3.envelopeSpeed = envSpeed;
		} break;

		case 13: {
			s1.envelopeContinue = s2.envelopeContinue = s3.envelopeContinue = (value & 8) != 0;
			s1.envelopeAttack = s2.envelopeAttack = s3.envelopeAttack = (value & 4) != 0;
			s1.envelopeAlternate = s2.envelopeAlternate = s3.envelopeAlternate = (value & 2) != 0;
			s1.envelopeHold = s2.envelopeHold = s3.envelopeHold = (value & 1) != 0;
			
			s1.envelopeReset();
			s2.envelopeReset();
			s3.envelopeReset();
		} break;

		default:
			break;
		}
	}

	@Override
	public boolean read(int adr, IntHolder val, int id) {
		return false;
	}

	@Override
	public void reset() {
		s1.reset();
		s2.reset();
		s3.reset();
	}

	@Override
	public AbstractNsfSound getSound(byte channelCode) {
		switch (channelCode) {
		case CHANNEL_S5B_SQUARE1: return s1;
		case CHANNEL_S5B_SQUARE2: return s2;
		case CHANNEL_S5B_SQUARE3: return s3;
		}
		
		return null;
	}

	@Override
	public byte[] getAllChannelCodes() {
		return new byte[] {CHANNEL_S5B_SQUARE1, CHANNEL_S5B_SQUARE2, CHANNEL_S5B_SQUARE3};
	}

}
