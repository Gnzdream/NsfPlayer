package zdream.nsfplayer.nsf.device.chip;

import zdream.nsfplayer.nsf.device.AbstractSoundChip;
import zdream.nsfplayer.nsf.device.cpu.IntHolder;
import zdream.nsfplayer.nsf.renderer.NsfRuntime;
import zdream.nsfplayer.sound.AbstractNsfSound;
import zdream.nsfplayer.sound.vrc7.OPLL;

/**
 * VRC7 音频芯片, 管理输出 1 到 6 个 VRC7 轨道的音频
 * 
 * @author Zdream
 * @since v0.2.7
 */
public class NesVRC7 extends AbstractSoundChip {
	
	OPLL opll = new OPLL();
	int address;

	public NesVRC7(NsfRuntime runtime) {
		super(runtime);

		for (int i = 0; i < 0x40; ++i) {
			opll.writeReg(i, 0);
		}
	}

	@Override
	public boolean write(int adr, int val, int id) {
		if (adr == 0x9010) {
			this.address = val;
			return true;
		}
		if (adr == 0x9030) {
			opll.writeReg(this.address, val);
			return true;
		} else
			return false;
	}

	@Override
	public boolean read(int adr, IntHolder val, int id) {
		return false;
	}

	@Override
	public void reset() {
		for (int i = 0; i < 0x40; ++i) {
			opll.writeReg(i, 0);
		}
		
		opll.reset();
		address = 0;
	}

	@Override
	public AbstractNsfSound getSound(byte channelCode) {
		switch (channelCode) {
		case CHANNEL_VRC7_FM1: return opll.getSound(0);
		case CHANNEL_VRC7_FM2: return opll.getSound(1);
		case CHANNEL_VRC7_FM3: return opll.getSound(2);
		case CHANNEL_VRC7_FM4: return opll.getSound(3);
		case CHANNEL_VRC7_FM5: return opll.getSound(4);
		case CHANNEL_VRC7_FM6: return opll.getSound(5);
		}
		
		return null;
	}

	@Override
	public byte[] getAllChannelCodes() {
		return new byte[] {CHANNEL_VRC7_FM1, CHANNEL_VRC7_FM2, CHANNEL_VRC7_FM3,
				CHANNEL_VRC7_FM4, CHANNEL_VRC7_FM5, CHANNEL_VRC7_FM6};
	}

}
