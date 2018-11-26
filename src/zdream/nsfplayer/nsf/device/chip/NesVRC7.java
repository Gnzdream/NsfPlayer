package zdream.nsfplayer.nsf.device.chip;

import zdream.nsfplayer.nsf.device.AbstractSoundChip;
import zdream.nsfplayer.nsf.device.cpu.IntHolder;
import zdream.nsfplayer.nsf.renderer.NsfRuntime;
import zdream.nsfplayer.sound.AbstractNsfSound;
import zdream.nsfplayer.sound.vrc7.OPLL;
import zdream.nsfplayer.sound.vrc7.OPLLPatch;
import zdream.nsfplayer.sound.vrc7.SoundVRC7;

/**
 * VRC7 音频芯片, 管理输出 1 到 6 号共 6 个 VRC7 轨道的音频
 * 
 * @author Zdream
 * @since v0.2.7
 */
public class NesVRC7 extends AbstractSoundChip {
	
	OPLL opll = new OPLL();
	int address;
	public final short[] regs = new short[0x40];

	public NesVRC7(NsfRuntime runtime) {
		super(runtime);

		reset();
	}

	@Override
	public boolean write(int adr, int val, int id) {
		if (adr == 0x9010) {
			this.address = val;
			return true;
		}
		if (adr == 0x9030) {
			writeReg(val & 0xff);
			return true;
		} else
			return false;
	}
	
	private void writeReg(int data) {
		int reg = this.address & 0x3f;
		this.regs[reg] = (short) data;
		
		switch (reg) {
		case 0x00: {
			OPLLPatch p = opll.getCustomModPatch();
			p.AM = (data & 0x80) != 0;
			p.PM = (data & 0x40) != 0;
			p.EG = (data & 0x20) != 0;
			p.KR = (data & 0x10) != 0;
			p.ML = (data) & 15;
			for (int i = 0; i < 6; i++) {
				SoundVRC7 sound = opll.getSound(i);
				if (sound.useCustomPatch()) {
					sound.rebuildModDphase();
					sound.recalcModDphase();
				}
			}
		} break;

		case 0x01: {
			OPLLPatch p = opll.getCustomCarPatch();
			p.AM = (data & 0x80) != 0;
			p.PM = (data & 0x40) != 0;
			p.EG = (data & 0x20) != 0;
			p.KR = (data & 0x10) != 0;
			p.ML = (data) & 15;
			for (int i = 0; i < 6; i++) {
				SoundVRC7 sound = opll.getSound(i);
				if (sound.useCustomPatch()) {
					sound.rebuildCarDphase();
					sound.recalcCarDphase();
				}
			}
		} break;

		case 0x02: {
			OPLLPatch p = opll.getCustomModPatch();
			p.KL = (data >> 6) & 3;
			p.TL = (data) & 63;
			for (int i = 0; i < 6; i++) {
				SoundVRC7 sound = opll.getSound(i);
				if (sound.useCustomPatch()) {
					sound.rebuildModTll();
				}
			}
		} break;

		case 0x03: {
			OPLLPatch pmod = opll.getCustomModPatch(), pcar = opll.getCustomCarPatch();
			pcar.KL = (data >> 6) & 3;
			pcar.WF = (data >> 4) & 1;
			pmod.WF = (data >> 3) & 1;
			pmod.FB = (data) & 7;
			for (int i = 0; i < 6; i++) {
				SoundVRC7 sound = opll.getSound(i);
				if (sound.useCustomPatch()) {
					sound.rebuildModSintbl();
					sound.rebuildCarSintbl();
				}
			}
		} break;

		case 0x04: {
			OPLLPatch p = opll.getCustomModPatch();
			p.AR = (data >> 4) & 15;
			p.DR = (data) & 15;
			for (int i = 0; i < 6; i++) {
				SoundVRC7 sound = opll.getSound(i);
				if (sound.useCustomPatch()) {
					sound.recalcModDphase();
				}
			}
		} break;

		case 0x05: {
			OPLLPatch p = opll.getCustomCarPatch();
			p.AR = (data >> 4) & 15;
			p.DR = (data) & 15;
			for (int i = 0; i < 6; i++) {
				SoundVRC7 sound = opll.getSound(i);
				if (sound.useCustomPatch()) {
					sound.recalcCarDphase();
				}
			}
		} break;

		case 0x06: {
			OPLLPatch p = opll.getCustomModPatch();
			p.SL = (data >> 4) & 15;
			p.RR = (data) & 15;
			for (int i = 0; i < 6; i++) {
				SoundVRC7 sound = opll.getSound(i);
				if (sound.useCustomPatch()) {
					sound.recalcModDphase();
				}
			}
		} break;

		case 0x07: {
			OPLLPatch p = opll.getCustomCarPatch();
			p.SL = (data >> 4) & 15;
			p.RR = (data) & 15;
			for (int i = 0; i < 6; i++) {
				SoundVRC7 sound = opll.getSound(i);
				if (sound.useCustomPatch()) {
					sound.recalcCarDphase();
				}
			}
		} break;

		case 0x10:
		case 0x11:
		case 0x12:
		case 0x13:
		case 0x14:
		case 0x15:
		{
			int num = reg - 0x10;
			SoundVRC7 sound = opll.getSound(num);
			
			int fnum = data + ((this.regs[0x20 + num] & 1) << 8);
			sound.modulatorSlot.fnum = fnum;
			sound.carriorSlot.fnum = fnum;
			
			sound.rebuildAll();
		} break;

		case 0x20:
		case 0x21:
		case 0x22:
		case 0x23:
		case 0x24:
		case 0x25:
		{
			int num = reg - 0x20;
			SoundVRC7 sound = opll.getSound(num);
			
			int fnum = ((data & 1) << 8) + this.regs[0x10 + num];
			int block = (data >> 1) & 7;
			boolean sustine = (data & 0x20) != 0;
			
			sound.modulatorSlot.fnum = fnum;
			sound.carriorSlot.fnum = fnum;
			sound.modulatorSlot.block = block;
			sound.carriorSlot.block = block;
			sound.carriorSlot.sustine = sustine;
			
			if ((data & 0x10) != 0)
				sound.keyOn();
			else
				sound.keyOff();

			sound.rebuildAll();
		} break;

		case 0x30:
		case 0x31:
		case 0x32:
		case 0x33:
		case 0x34:
		case 0x35:
		{
			int num = reg - 0x30;
			SoundVRC7 sound = opll.getSound(num);
			
			int i = (data >> 4) & 15, v = data & 15;
			sound.setPatch(i);
			sound.carriorSlot.volume = v << 2;

			sound.rebuildAll();
			
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
		for (int i = 0; i < 0x40; i++) {
			this.address = 0;
			writeReg(0);
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
