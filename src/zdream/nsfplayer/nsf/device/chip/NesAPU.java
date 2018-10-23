package zdream.nsfplayer.nsf.device.chip;

import zdream.nsfplayer.nsf.device.AbstractSoundChip;
import zdream.nsfplayer.nsf.renderer.NsfRuntime;
import zdream.nsfplayer.sound.AbstractNsfSound;
import zdream.nsfplayer.sound.PulseSound;
import zdream.nsfplayer.xgm.device.IntHolder;

/**
 * APU 音频设备, 管理输出 Pulse1 和 Pulse2 轨道的音频
 * 
 * @author Zdream
 * @since v0.2.4
 */
public class NesAPU extends AbstractSoundChip {
	
	private PulseSound pulse1, pulse2;
	
	public NesAPU(NsfRuntime runtime) {
		super(runtime);
		pulse1 = new PulseSound();
		pulse2 = new PulseSound();
	}

	@Override
	public boolean write(int adr, int val, int id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean read(int adr, IntHolder val, int id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void reset() {
		pulse1.reset();
		pulse2.reset();

		// TODO
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
