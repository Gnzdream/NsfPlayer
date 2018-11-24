package zdream.nsfplayer.nsf.device.chip;

import java.util.Arrays;

import zdream.nsfplayer.core.ERegion;
import zdream.nsfplayer.ftm.format.FtmDPCMSample;
import zdream.nsfplayer.nsf.device.AbstractSoundChip;
import zdream.nsfplayer.nsf.device.cpu.IntHolder;
import zdream.nsfplayer.nsf.renderer.NsfRuntime;
import zdream.nsfplayer.sound.AbstractNsfSound;
import zdream.nsfplayer.sound.DPCMSound;
import zdream.nsfplayer.sound.EnvelopeSoundNoise;
import zdream.nsfplayer.sound.PulseSound;
import zdream.nsfplayer.sound.TriangleSound;

/**
 * 一种 2A03 音频设备, 管理输出 Triangle, Noise 和 DPCM 轨道的音频
 * 
 * @author Zdream
 * @since v0.2.4
 */
public class NesDMC extends AbstractSoundChip {
	
	private TriangleSound triangle;
	private EnvelopeSoundNoise noise;
	private DPCMSound dpcm;
	
	/**
	 * 记录放置的参数
	 */
	private byte[] mem = new byte[12];
	private byte mem4015 = 0;

	public NesDMC(NsfRuntime runtime) {
		super(runtime);
		triangle = new TriangleSound();
		noise = new EnvelopeSoundNoise();
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
			writeToTriangle(adr & 3, val);
			mem[adr & 3] = (byte) val;
		} break;
		case 0x400C: case 0x400D: case 0x400E: case 0x400F: {
			// noise
			writeToNoise(adr & 3, val);
			mem[(adr & 3) + 4] = (byte) val;
		} break;
		case 0x4010: case 0x4011: case 0x4012: case 0x4013: {
			// dpcm
			writeToDPCM(adr & 3, val);
			mem[(adr & 3) + 8] = (byte) val;
		} break;
		case 0x4015: {
			// enable
			mem4015 = (byte) val;
			handleEnable();
		} break;

		default:
			return false;
		}
		
		return true;
	}
	
	private void writeToTriangle(int adr, int value) {
		switch (adr) {
		case 0:
			triangle.looping = (value >> 7) != 0;
			triangle.linearLoad = (value & 0x7F);
			break;
			
		// 忽略 1 号位
			
		case 2: {
			int period = (triangle.period & 0xFF00) + value;
			triangle.period = period;
		} break;
			
		case 3: {
			int period = (triangle.period & 0xFF) + ((value & 7) << 8);
			triangle.period = period;
			triangle.lengthCounter = PulseSound.LENGTH_TABLE[(value & 0xF8) >> 3];
		} break;
		
		}
	}
	
	private void writeToNoise(int adr, int value) {
		switch (adr) {
		case 0:
			noise.envelopeLoop = (value & 0x20) != 0;
			noise.envelopeDisable = (value & 0x10) != 0;
			noise.fixedVolume = (value & 0xF);
			break;
			
		// 忽略 1 号位
			
		case 2: {
			noise.periodIndex = (value & 0xF);
			noise.dutySampleRate = ((value & 0x80) != 0) ?
					EnvelopeSoundNoise.DUTY_SAMPLE_RATE1 : EnvelopeSoundNoise.DUTY_SAMPLE_RATE0;
		} break;
			
		case 3: {
			noise.lengthCounter = PulseSound.LENGTH_TABLE[(value & 0xF8) >> 3];
			noise.onEnvelopeUpdated();
		} break;
		
		}
	}
	
	private void writeToDPCM(int adr, int value) {
		switch (adr) {
		case 0:
			dpcm.loop = (value & 0x40) != 0;
			dpcm.periodIndex = (value & 0xF);
			break;
			
		case 1:
			dpcm.deltaCounter = (value & 0x7F);
			break;
			
		case 2: {
			dpcm.offsetAddress = value * 64;
		} break;
			
		case 3: {
			dpcm.length = value * 16;
		} break;
		
		}
	}
	
	/**
	 * <p>这里主要处理 DPCM 读取采样的问题
	 * <p>以及其它发声器的 enable 开关
	 * </p>
	 */
	private void handleEnable() {
		if ((mem4015 & 16) == 0) {
			dpcm.setEnable(false);
			dpcm.sample = null; // not active, 禁用
		} else {
			dpcm.setEnable(true);
			if (dpcm.sample == null) {
				// 需要准备读取
				FtmDPCMSample sample = new FtmDPCMSample();
				int address = (0xC000 | (dpcm.offsetAddress));
				if (dpcm.length == 0) {
					return;
				}
				
				int length = (dpcm.length + 1);
				sample.data = new byte[length];
				
				getRuntime().manager.stack.read(sample.data, 0, length, address);
				dpcm.sample = sample;
				dpcm.offsetAddress = 0; // TODO 这个地方我用来强制重置
				dpcm.reload(); // 在 reset 阶段不应该调用这个
			}
		}
		
		triangle.setEnable((mem4015 & 4) != 0);
		noise.setEnable((mem4015 & 8) != 0);

		if (!triangle.isEnable()) {
			triangle.lengthCounter = 0;
		}
		if (!noise.isEnable()) {
			noise.lengthCounter = 0;
		}
	}

	@Override
	public boolean read(int adr, IntHolder val, int id) {
		if (adr >= 0x4008 && adr < 0x4014) {
			val.val = mem[adr - 0x4008] & 0xFF;
			return true;
		} else if (adr == 0x4015) {
			val.val = mem4015 & 0xFF;
			return true;
		}
		return false;
	}

	@Override
	public void reset() {
		triangle.reset();
		noise.reset();
		noise.setSequenceStep(
				getRuntime().manager.getRegion() == ERegion.PAL ?
						EnvelopeSoundNoise.SEQUENCE_STEP_PAL : EnvelopeSoundNoise.SEQUENCE_STEP_NTSC);
		dpcm.reset();
		
		Arrays.fill(mem, (byte) 0);
		mem4015 = 0x7F;
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
