package zdream.nsfplayer.nsf.device.chip;

import zdream.nsfplayer.nsf.device.AbstractSoundChip;
import zdream.nsfplayer.nsf.renderer.NsfRuntime;
import zdream.nsfplayer.sound.AbstractNsfSound;
import zdream.nsfplayer.sound.DPCMSound;
import zdream.nsfplayer.sound.NoiseSound;
import zdream.nsfplayer.sound.TriangleSound;
import zdream.nsfplayer.xgm.device.IntHolder;

/**
 * 一种 2A03 音频设备, 管理输出 Triangle, Noise 和 DPCM 轨道的音频
 * 
 * @author Zdream
 * @since v0.2.4
 */
public class NesDMC extends AbstractSoundChip {
	
	private TriangleSound triangle;
	private NoiseSound noise;
	private DPCMSound dpcm;

	public NesDMC(NsfRuntime runtime) {
		super(runtime);
		triangle = new TriangleSound();
		noise = new NoiseSound();
		dpcm = new DPCMSound();
	}

	@Override
	public boolean write(int adr, int val, int id) {
		/*
		 * APU 这里要接收的地址有:
		 * [0x4000, 0x4007], 0x4015, 0x4017
		 */
		switch (adr) {
		case 0x4008: case 0x4009: case 0x400A: case 0x400B: {
			// triangle
		} break;
		case 0x400C: case 0x400D: case 0x400E: case 0x400F: {
			// noise
		} break;
		case 0x4010: case 0x4011: case 0x4012: case 0x4013: {
			// dpcm
		} break;
		case 0x4015: {
			// enable
		} break;

		default:
			return false;
		}
		
		if (adr != 0x4015)
			System.out.println(String.format("[%4X]:%2X, %3d", adr, val, val));
		return true;
	}

	@Override
	public boolean read(int adr, IntHolder val, int id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void reset() {
		triangle.reset();
		noise.reset();
		dpcm.reset();
		
		// TODO Auto-generated method stub
	}

	@Override
	public AbstractNsfSound getSound(byte code) {
		switch (code) {
		case CHANNEL_2A03_TRIANGLE: return triangle;
		case CHANNEL_2A03_NOISE: return noise;
		case CHANNEL_2A03_DPCM: return dpcm;
		}
		
		return null;
	}
	
	@Override
	public byte[] getAllChannelCodes() {
		return new byte[] {CHANNEL_2A03_TRIANGLE, CHANNEL_2A03_NOISE, CHANNEL_2A03_DPCM};
	}

}
