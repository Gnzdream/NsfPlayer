package zdream.nsfplayer.nsf.device.chip;

import zdream.nsfplayer.nsf.device.AbstractSoundChip;
import zdream.nsfplayer.nsf.device.cpu.IntHolder;
import zdream.nsfplayer.nsf.renderer.NsfRuntime;
import zdream.nsfplayer.sound.AbstractNsfSound;
import zdream.nsfplayer.sound.SoundFDS;

/**
 * FDS 音频芯片, 管理输出 FDS 轨道的音频
 * 
 * @author Zdream
 * @since v0.2.4
 */
public class NesFDS extends AbstractSoundChip {
	
	private final SoundFDS fds;
	
	/**
	 * FDS 启用标志, $4023
	 */
	private boolean masterIo;

	public NesFDS(NsfRuntime runtime) {
		super(runtime);
		fds = new SoundFDS();
	}

	@Override
	public boolean write(int adr, int val, int id) {
		if (adr == 0x4023) {
			masterIo = ((val & 2) != 0);
			return true;
		}

		if (!masterIo)
			return false;
		
		if (adr < 0x4040 || adr > 0x408A)
			return false;
		if (adr < 0x4080) { // $4040-407F wave table write
			if (fds.wavWrite)
				fds.wave[adr - 0x4040] = (byte) (val & 0x3F);
			return true;
		}

		switch (adr) {
		case 0x4080: // $4080 volume envelope
			fds.wavEnvDisable = ((val & 0x80) != 0);
			fds.wavEnvMode = ((val & 0x40) != 0);
			fds.resetWavCounter();
			fds.wavEnvSpeed = val & 0x3F;
//			if (fds.wavenv_disable)
//				fds.wavenv_out = fds.wavenv_speed;
			return true;
		case 0x4081: // $4081 ---
			return false;
		case 0x4082: // $4082 wave frequency low
			fds.wavFreq = (fds.wavFreq & 0xF00) | val;
			return true;
		case 0x4083: // $4083 wave frequency high / enables
			fds.wavFreq = (fds.wavFreq & 0x0FF) | ((val & 0x0F) << 8);
			fds.wavHalt = ((val & 0x80) != 0);
			fds.envHalt = ((val & 0x40) != 0);
//			if (wavHalt)
//				phase[TWAV] = 0;
//			if (envHalt) {
//				env_timer[EMOD] = 0;
//				env_timer[EVOL] = 0;
//			}
			
			return true;
		case 0x4084: // $4084 mod envelope
			fds.modEnvDisable = ((val & 0x80) != 0);
			fds.modEnvMode = ((val & 0x40) != 0);
			fds.resetModCounter();
			fds.modEnvSpeed = val & 0x3F;
//			if (env_disable[EMOD])
//				env_out[EMOD] = env_speed[EMOD];
			return true;
		case 0x4085: // $4085 mod position
			fds.modPos = val & 0x7F;
			// not hardware accurate., but prevents detune due to cycle inaccuracies
			// (notably in Bio Miracle Bokutte Upa)
//			if (option[OPT_4085_RESET] != 0)
//				phase[TMOD] = mod_write_pos << 16;
			return true;
		case 0x4086: // $4086 mod frequency low
			fds.modFreq = (fds.modFreq & 0xF00) | val;
			return true;
		case 0x4087: // $4087 mod frequency high / enable
			fds.modFreq = (fds.modFreq & 0x0FF) | ((val & 0x0F) << 8);
			fds.modHalt = ((val & 0x80) != 0);
//			if (fds.mod_halt)
//				phase[TMOD] = phase[TMOD] & 0x3F0000; // reset accumulator phase
			return true;
		case 0x4088: // $4088 mod table write
			fds.writeMods(val);
			return true;
		case 0x4089: // $4089 wave write enable, master volume
			fds.wavWrite = ((val & 0x80) != 0);
			fds.masterVolume = val & 0x03;
			return true;
		case 0x408A: // $408A envelope speed
			fds.masterEnvSpeed = val;
			// haven't tested whether this register resets phase on hardware,
			// but this ensures my inplementation won't spam envelope clocks
			// if this value suddenly goes low.
			fds.resetCounter();
			return true;
		default:
			return false;
		}
	}

	@Override
	public boolean read(int adr, IntHolder val, int id) {
		if (adr >= 0x4040 && adr <= 0x407F) {
			// 原 NsfPlayer 工程
			// TODO: if wav_write is not enabled, the
			// read address may not be reliable? need
			// to test this on hardware.
			val.val = fds.wave[adr - 0x4040];
			return true;
		}

		if (adr == 0x4090) { // $4090 read volume envelope
			val.val = fds.getWavEnvOut() | 0x40;
			return true;
		}

		if (adr == 0x4092) { // $4092 read mod envelope
			val.val = fds.getModEnvOut() | 0x40;
			return true;
		}

		return false;
	}

	@Override
	public void reset() {
		fds.reset();
		masterIo = true;
	}

	@Override
	public AbstractNsfSound getSound(byte channelCode) {
		switch (channelCode) {
		case CHANNEL_FDS: return fds;
		}
		
		return null;
	}

	@Override
	public byte[] getAllChannelCodes() {
		return new byte[] {CHANNEL_FDS};
	}

}
