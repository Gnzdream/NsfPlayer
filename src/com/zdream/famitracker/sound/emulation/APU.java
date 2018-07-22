package com.zdream.famitracker.sound.emulation;

import static com.zdream.famitracker.sound.emulation.Types.CHANID_DPCM;
import static com.zdream.famitracker.sound.emulation.Types.CHANID_NOISE;
import static com.zdream.famitracker.sound.emulation.Types.CHANID_SQUARE1;
import static com.zdream.famitracker.sound.emulation.Types.CHANID_SQUARE2;
import static com.zdream.famitracker.sound.emulation.Types.CHANID_TRIANGLE;
import static com.zdream.famitracker.sound.emulation.Types.MACHINE_NTSC;
import static com.zdream.famitracker.sound.emulation.Types.MACHINE_PAL;
import static com.zdream.famitracker.sound.emulation.Types.SNDCHIP_MMC5;
import static com.zdream.famitracker.sound.emulation.Types.SNDCHIP_NONE;
import static com.zdream.famitracker.sound.emulation.Types.SNDCHIP_VRC6;

import java.util.ArrayList;
import java.util.Iterator;

import com.zdream.famitracker.sound.IAudioCallback;
import com.zdream.famitracker.sound.SampleMem;
import com.zdream.famitracker.sound.emulation.expansion.External;
import com.zdream.famitracker.sound.emulation.expansion.MMC5;
import com.zdream.famitracker.sound.emulation.expansion.VRC6;

public class APU {
	
	public static final byte[] LENGTH_TABLE = {
			0x0A, (byte) 0xFE, 0x14, 0x02, 0x28, 0x04, 0x50, 0x06,
			(byte) 0xA0, 0x08, 0x3C, 0x0A, 0x0E, 0x0C, 0x1A, 0x0E,
			0x0C, 0x10, 0x18, 0x12, 0x30, 0x14, 0x60, 0x16,
			(byte) 0xC0, 0x18, 0x48, 0x1A, 0x10, 0x1C, 0x20, 0x1E
	};
	
	public static final byte FRAME_RATE_NTSC = 60;
	public static final byte FRAME_RATE_PAL = 50;
	
	public static final int
		BASE_FREQ_NTSC = 1789773,
		BASE_FREQ_PAL = 1662607;
	
	public static final int SEQUENCER_PERIOD = 7458;
	
	static final ArrayList<External> exChips = new ArrayList<>();
	byte[] m_iRegs = new byte[0x20];
	
	public APU(IAudioCallback pCallback, SampleMem pSampleMem) {
		m_pParent = pCallback;
		m_pMixer = new Mixer();
		
		m_pSquare1 = new Square(m_pMixer, CHANID_SQUARE1, SNDCHIP_NONE);
		m_pSquare2 = new Square(m_pMixer, CHANID_SQUARE2, SNDCHIP_NONE);
		m_pTriangle = new Triangle(m_pMixer, CHANID_TRIANGLE);
		m_pNoise = new Noise(m_pMixer, CHANID_NOISE);
		m_pDPCM = new DPCM(m_pMixer, pSampleMem, CHANID_DPCM);
		
		m_pVRC6 = new VRC6(m_pMixer);
		m_pMMC5 = new MMC5(m_pMixer);
		
		// TODO 其它的芯片, 自己后面加, 什么 VCR7 啊, FDS 啊...
	}

	/**
	 * ChannelHandler 调用
	 * @param reg
	 * @param value
	 */
	public void write(int address, byte value) {
		process();

		if (address == 0x4015) {
			write4015(value);
			return;
		}
		else if (address == 0x4017) {
			write4017(value);
			return;
		}

		switch (address & 0x1C) {
			case 0x00: m_pSquare1.write(address & 0x03, value); break;
			case 0x04: m_pSquare2.write(address & 0x03, value); break;
			case 0x08: m_pTriangle.write(address & 0x03, value); break;
			case 0x0C: m_pNoise.write(address & 0x03, value); break;
			case 0x10: m_pDPCM.write(address & 0x03, value); break;
		}

		m_iRegs[address & 0x1F] = value;
	}
	
	/**
	 * Sound Control ($4015)
	 * @param value
	 */
	void write4015(byte value) {
		process();

		m_pSquare1.writeControl(value);
		m_pSquare2.writeControl(value >> 1);
		m_pTriangle.writeControl(value >> 2);
		m_pNoise.writeControl(value >> 3);
		m_pDPCM.writeControl(value >> 4);
	}
	
	/**
	 * The $4017 Control port
	 * @param value
	 */
	void write4017(byte value) {
		process();

		// Reset counter
		m_iFrameSequence = 0;

		// Mode 1
		if ((value & 0x80) != 0) {
			m_iFrameMode = 1;
			// Immediately run all units		
			clock_240Hz();
			clock_120Hz();
			clock_60Hz();
		}
		// Mode 0
		else
			m_iFrameMode = 0;

		// IRQs are not generated when playing NSFs
	}
	
	/**
	 * 240Hz Frame counter (1/4 frame)
	 */
	private final void clock_240Hz() {
		m_pSquare1.envelopeUpdate();
		m_pSquare2.envelopeUpdate();
		m_pNoise.envelopeUpdate();
		m_pTriangle.linearCounterUpdate();
	}

	/**
	 * 120Hz Frame counter (1/2 frame)
	 */
	private final void clock_120Hz() {
		m_pSquare1.sweepUpdate(1);
		m_pSquare2.sweepUpdate(0);

		m_pSquare1.lengthCounterUpdate();
		m_pSquare2.lengthCounterUpdate();
		m_pTriangle.lengthCounterUpdate();
		m_pNoise.lengthCounterUpdate();
	}

	/**
	 * 60Hz Frame counter (1/1 frame)
	 */
	private final void clock_60Hz() {
		// No IRQs are generated for NSFs
	}

	/**
	 * <p>Data was written to an external sound chip
	 * <p>(this doesn't really belong in the APU but are here for convenience)
	 * <p>ChannelHandler 调用
	 * @param reg
	 * @param value
	 */
	public void externalWrite(int reg, byte value) {
		process();
		
		for (Iterator<External> it = exChips.iterator(); it.hasNext();) {
			External external = it.next();
			int v = value & 0xFF;
			external.write(reg, v);
		}

		logExternalWrite(reg, value);
	}
	
	void logExternalWrite(int Address, byte Value) {
//		if (Address >= 0x9000 && Address <= 0x9003)
//			m_iRegsVRC6[Address - 0x9000] = Value;
//		else if (Address >= 0xA000 && Address <= 0xA003)
//			m_iRegsVRC6[Address - 0xA000 + 3] = Value;
//		else if (Address >= 0xB000 && Address <= 0xB003)
//			m_iRegsVRC6[Address - 0xB000 + 6] = Value;
//		else if (Address >= 0x4080 && Address <= 0x408F)
//			m_iRegsFDS[Address - 0x4080] = Value;
	}
	
	
	private Mixer m_pMixer;
	private IAudioCallback m_pParent;
	
	public int getVol(byte channel) {
		// TODO Auto-generated method stub
		return 0;
	}

	// Internal channels
	Square m_pSquare1;
	Square m_pSquare2;
	Triangle m_pTriangle;
	Noise m_pNoise;
	DPCM m_pDPCM;

	// Expansion chips
	VRC6 m_pVRC6;
	MMC5 m_pMMC5;
	
//	CMMC5		*m_pMMC5;
//	CFDS		*m_pFDS;
//	CN163		*m_pN163;
//	CVRC7		*m_pVRC7;
//	CS5B		*m_pS5B;

	/**
	 * <p>外接音源, 它是一个位开关.
	 * <p>External sound chip, if used
	 */
	byte m_iExternalSoundChip;

	/*uint32		m_iFramePeriod;						// Cycles per frame */
	
	/**
	 * <p>从每一个 Frame 的开始, 一共模拟了多少周期
	 * <p>Cycles emulated from start of frame
	 */
	int m_iFrameCycles;
	/**
	 * <p>Frame 序列使用的时钟（周期） 不知道有没有翻译正确
	 * <p>Clock for frame sequencer
	 */
	int m_iSequencerClock;
	/**
	 * Frame sequence
	 */
	int m_iFrameSequence;
	/**
	 * 4 or 5-steps frame sequence
	 */
	int m_iFrameMode;

	/**
	 * 仍然不清楚它的作用
	 */
	int m_iFrameCycleCount;
	/**
	 * Numbers of cycles/audio frame
	 */
	int m_iFrameClock;
	
	/**
	 * <p>记录 APU 在调用 {@link #process()} 这个方法时, 要运行的时间数.<br>
	 * 现阶段由 {@link #addTime(int)} 修改
	 * <p>Number of cycles to process
	 */
	int m_iCyclesToRun;

	/**
	 * Size of buffer, in samples
	 */
	int m_iSoundBufferSamples;
	/**
	 * If stereo is enabled
	 */
	boolean m_bStereoEnabled;

	/**
	 * To convert samples to bytes
	 */
	int m_iSampleSizeShift;
	/**
	 * Size of buffer, in samples
	 */
	int m_iSoundBufferSize;
	/**
	 * Fill pos in buffer
	 */
	int m_iBufferPointer;
	/**
	 * <p>Sound transfer buffer
	 */
	byte[] m_pSoundBuffer;

	/* uint8		m_iRegs[0x20];
	uint8		m_iRegsVRC6[0x10];
	uint8		m_iRegsFDS[0x10];

	float		m_fLevelVRC7;
	float		m_fLevelS5B;*/
	
	/**
	 * Reset APU
	 */
	public void reset() {
		m_iCyclesToRun = 0;
		m_iFrameCycles = 0;
		m_iSequencerClock = SEQUENCER_PERIOD;
		m_iFrameSequence = 0;
		m_iFrameMode = 0;
		m_iFrameClock = m_iFrameCycleCount;
		
		m_pMixer.clearBuffer();

		m_pSquare1.reset();
		m_pSquare2.reset();
		m_pTriangle.reset();
		m_pNoise.reset();
		m_pDPCM.reset();
		
		for (Iterator<External> it = exChips.iterator(); it.hasNext();) {
			External external = it.next();
			external.reset();
		}
	}
	
	/**
	 * <p>The main APU emulation
	 * <p>The amount of cycles that will be emulated is added by {@link #addTime()}
	 */
	public void process() {
		int time;
		while (m_iCyclesToRun > 0) {

			time = m_iCyclesToRun;
			time = Math.min(time, m_iSequencerClock);
			time = Math.min(time, m_iFrameClock);
			
			// Run internal channels
			runAPU1(time);
			runAPU2(time);

			for (Iterator<External> it = exChips.iterator(); it.hasNext();) {
				External ex = it.next();
				ex.process(time);
			}

			m_iFrameCycles += time;
			m_iSequencerClock -= time;
			m_iFrameClock -= time;
			m_iCyclesToRun -= time;

			if (m_iSequencerClock == 0)
				clockSequence();

			if (m_iFrameClock == 0)
				endFrame();
		}
	}

	/**
	 * APU pin 1
	 * @param time
	 */
	private void runAPU1(int time) {
		while (time > 0) {
			int period = Math.min(m_pSquare1.getPeriod(), m_pSquare2.getPeriod());
			period = Math.min(Math.max(period, 7), time);
			m_pSquare1.process(period);
			m_pSquare2.process(period);
			time -= period;
		}
	}

	/**
	 * APU pin 2
	 * @param time
	 */
	private void runAPU2(int time) {
		while (time > 0) {
			int period = Math.min(m_pTriangle.getPeriod(), m_pNoise.getPeriod());
			period = Math.min(period, m_pDPCM.getPeriod());
			period = Math.min(Math.max(period, 7), time);
			m_pTriangle.process(period);
			m_pNoise.process(period);
			m_pDPCM.process(period);
			time -= period;
		}
	}

	private void clockSequence() {
		m_iSequencerClock += SEQUENCER_PERIOD;

		if (m_iFrameMode == 0) {
			m_iFrameSequence = (m_iFrameSequence + 1) % 4;
			switch (m_iFrameSequence) {
				case 0: clock_240Hz(); break;
				case 1: clock_240Hz(); clock_120Hz(); break;
				case 2: clock_240Hz(); break;
				case 3: clock_240Hz(); clock_120Hz(); clock_60Hz(); break;
			}
		} else {
			m_iFrameSequence = (m_iFrameSequence + 1) % 5;
			switch (m_iFrameSequence) {
				case 0: clock_240Hz(); clock_120Hz(); break;
				case 1: clock_240Hz(); break;
				case 2: clock_240Hz(); clock_120Hz(); break;
				case 3: clock_240Hz(); break;
				case 4: break;
			}
		}
	}

	private void endFrame() {
		m_pSquare1.endFrame();
		m_pSquare2.endFrame();
		m_pTriangle.endFrame();
		m_pNoise.endFrame();
		m_pDPCM.endFrame();
		
		for (Iterator<External> it = exChips.iterator(); it.hasNext();) {
			External external = it.next();
			external.endFrame();
		}

		int SamplesAvail = m_pMixer.finishBuffer(m_iFrameCycles);
		int ReadSamples	= m_pMixer.readBuffer(SamplesAvail, m_pSoundBuffer, m_bStereoEnabled);
		m_pParent.flushBuffer(m_pSoundBuffer, 0, ReadSamples);
		
		m_iFrameClock= m_iFrameCycleCount;
		m_iFrameCycles = 0;
	}

	/**
	 * 就让你这个 APU 跑这么多时间. TODO 没完成
	 * @param cycles
	 *   周期数
	 */
	public void addTime(int cycles) {
		if (cycles < 0)
			return;
		m_iCyclesToRun += cycles;
	}

	public void setupSound(int sampleRate, int nrChannels, byte machine) {
		int BaseFreq = (machine == MACHINE_NTSC) ? BASE_FREQ_NTSC : BASE_FREQ_PAL;
		int FrameRate = (machine == MACHINE_NTSC) ? FRAME_RATE_NTSC : FRAME_RATE_PAL;

		// Samples / frame. Allocate for PAL, since it's more
		m_iSoundBufferSamples = sampleRate / FRAME_RATE_PAL;
		
		m_bStereoEnabled = (nrChannels == 2);	
		m_iSoundBufferSize = m_iSoundBufferSamples * nrChannels;		// Total amount of samples to allocate
		m_iSampleSizeShift = (nrChannels == 2) ? 1 : 0;
		m_iBufferPointer = 0;

		m_pMixer.allocateBuffer(m_iSoundBufferSamples, sampleRate, (byte) nrChannels);

		m_pMixer.setClockRate(BaseFreq);

		m_pSoundBuffer = new byte[m_iSoundBufferSize << 2]; // 双声道, 16 位 (每个音频的采样占 2 byte)

		changeMachine(machine);

		// TODO VRC7 generates samples on it's own
//		m_pVRC7->SetSampleSpeed(SampleRate, BaseFreq, FrameRate);

		// TODO Same for sunsoft
//		m_pS5B->SetSampleSpeed(SampleRate, BaseFreq, FrameRate);

		// Numbers of cycles/audio frame
		m_iFrameCycleCount = BaseFreq / FrameRate;
	}

	public void setChipLevel(byte chip, float level) {
		float fLevel = (float) Math.pow(10, level / 20.0f); // Convert dB to linear

		switch (chip) {
			case Mixer.CHIP_LEVEL_VRC7:
//				m_fLevelVRC7 = fLevel;
				break;
			case Mixer.CHIP_LEVEL_S5B:
//				m_fLevelS5B = fLevel;
				break;
			default:
				m_pMixer.setChipLevel(chip, fLevel);
		}
	}
	
	public void changeMachine(byte machine) {
		switch (machine) {
		case MACHINE_NTSC:
			m_pNoise.PERIOD_TABLE = Noise.NOISE_PERIODS_NTSC;
			m_pDPCM.PERIOD_TABLE = DPCM.DMC_PERIODS_NTSC;			
			m_pMixer.setClockRate(BASE_FREQ_NTSC);
			break;
		case MACHINE_PAL:
			m_pNoise.PERIOD_TABLE = Noise.NOISE_PERIODS_PAL;
			m_pDPCM.PERIOD_TABLE = DPCM.DMC_PERIODS_PAL;			
			m_pMixer.setClockRate(BASE_FREQ_PAL);
			break;
		}
	}

	public final void setupMixer(int lowCut, int highCut, int highDamp, int volume) {
		m_pMixer.updateSettings(lowCut, highCut, highDamp, (volume) / 100.0f);
//		m_pVRC7.setVolume((float(volume) / 100.0f) * m_fLevelVRC7); TODO 现阶段忽略 VRC7
	}

	public boolean DPCMPlaying() {
		System.out.println("调用了还没有完成的方法: ");
		System.out.println(Thread.currentThread().getStackTrace()[1]);
		return false;
	}
	
	public void setExternalSound(byte chip) {
		m_iExternalSoundChip = chip;
		m_pMixer.externalSound(chip);
		
		exChips.clear();
		
		if ((chip & SNDCHIP_VRC6) != 0)
			exChips.add(m_pVRC6);
		// TODO 忽略除 VRC6 以外的其它音源芯片
//		if ((chip & SNDCHIP_VRC7) != 0)
//			exChips.add(m_pVRC7);
//		if ((chip & SNDCHIP_FDS) != 0)
//			exChips.add(m_pFDS);
		if ((chip & SNDCHIP_MMC5) != 0)
			exChips.add(m_pMMC5);
//		if ((chip & SNDCHIP_N163) != 0)
//			exChips.add(m_pN163);
//		if ((chip & SNDCHIP_S5B) != 0)
//			exChips.add(m_pS5B);

		reset();
		
	}

}
