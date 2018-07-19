package com.zdream.famitracker.sound.emulation;

import static com.zdream.famitracker.sound.emulation.Types.*;

import com.zdream.famitracker.sound.emulation.buffer.BlipBuffer;
import com.zdream.famitracker.sound.emulation.buffer.BlipEQ;
import com.zdream.famitracker.sound.emulation.buffer.BlipSynth;

public class Mixer {
	
	static final double AMP_2A03 = 400.0;

	static final float LEVEL_FALL_OFF_RATE	= 0.6f;
	static final int LEVEL_FALL_OFF_DELAY = 3;
	
	public static final byte
		CHIP_LEVEL_APU1 = 0,
		CHIP_LEVEL_APU2 = 1,
		CHIP_LEVEL_VRC6 = 2,
		CHIP_LEVEL_VRC7 = 3,
		CHIP_LEVEL_MMC5 = 4,
		CHIP_LEVEL_FDS = 5,
		CHIP_LEVEL_N163 = 6,
		CHIP_LEVEL_S5B = 7;

	public Mixer() {
		synth2A03SS = new BlipSynth(12, -500);
		synth2A03TND = new BlipSynth(12, -500);
		synthVRC6 = new BlipSynth(12, -500);
		synthMMC5 = new BlipSynth(12, -130);

		m_fLevelAPU1 = 1.0f;
		m_fLevelAPU2 = 1.0f;
		m_fLevelVRC6 = 1.0f;
		m_fLevelMMC5 = 1.0f;
//		m_fLevelFDS = 1.0f;
//		m_fLevelN163 = 1.0f;
		
		m_fOverallVol = 1.0f;
	}
	
	public void externalSound(byte chip) {
		m_iExternalChip = chip;
		updateSettings(m_iLowCut, m_iHighCut, m_iHighDamp, m_fOverallVol);
	}
	
	/**
	 * Add sound to mixer
	 * @param chanID
	 * @param chip
	 * @param value
	 * @param absValue
	 * @param frameCycles
	 */
	public void addValue(int chanID, int chip, int value, int absValue, int frameCycles) {
		int delta = value - m_iChannels[chanID];
		storeChannelLevel(chanID, absValue);
		m_iChannels[chanID] = value;

		switch (chip) {
			case SNDCHIP_NONE:
				switch (chanID) {
					case CHANID_SQUARE1:
					case CHANID_SQUARE2:
						mixInternal1(frameCycles);
						break;
					case CHANID_TRIANGLE:
					case CHANID_NOISE:
					case CHANID_DPCM:
						mixInternal2(frameCycles);
						break;
				}
				break;
			case SNDCHIP_N163:
				mixN163(value, frameCycles);
				break;
			case SNDCHIP_FDS:
				mixFDS(value, frameCycles);
				break;
			case SNDCHIP_MMC5: {
				mixMMC5(delta, frameCycles);
			} break;
			case SNDCHIP_VRC6:
				mixVRC6(value, frameCycles);
				break;
		}
	}
	
	public void updateSettings(int lowCut,	int highCut, int highDamp, float overallVol) {
		float volume = overallVol * getAttenuation();

		// Blip-buffer filtering
		blipBuffer.bassFreq(lowCut);

		BlipEQ eq = new BlipEQ(-highDamp, highCut, m_iSampleRate, 0);

		synth2A03SS.trebleEq(eq);
		synth2A03TND.trebleEq(eq);
		synthVRC6.trebleEq(eq);
		synthMMC5.trebleEq(eq);
//		SynthS5B.treble_eq(eq);

		// N163 special filtering
		double n163_treble = 24;
		long n163_rolloff = 12000;

		if (highDamp > n163_treble)
			n163_treble = highDamp;

		if (n163_rolloff > highCut)
			n163_rolloff = highCut;

		// TODO N163 暂时不做
//		BlipEQ eq_n163(-n163_treble, n163_rolloff, m_iSampleRate);
//		SynthN163.treble_eq(eq_n163);

		// FDS special filtering TODO FDS 暂时不做
//		BlipEQ fds_eq(-48, 1000, m_iSampleRate);

//		SynthFDS.treble_eq(fds_eq);

		// Volume levels
		synth2A03SS.volume(volume * m_fLevelAPU1);
		synth2A03TND.volume(volume * m_fLevelAPU2);
		synthVRC6.volume(volume * 3.98333f * m_fLevelVRC6);
		
//		SynthFDS.volume(Volume * 1.00f * m_fLevelFDS);
		synthMMC5.volume(volume * 1.18421f * m_fLevelMMC5);
		
		// Not checked
//		SynthN163.volume(Volume * 1.1f * m_fLevelN163);
		//SynthS5B.volume(Volume * 1.0f);

		m_iLowCut = lowCut;
		m_iHighCut = highCut;
		m_iHighDamp = highDamp;
		m_fOverallVol = overallVol;
	}

	public void allocateBuffer(int size, int sampleRate, byte nrChannels) {
		m_iSampleRate = sampleRate;
		blipBuffer.setSampleRate(sampleRate, (size * 1000 * 2) / sampleRate);
	}
	
	public void setClockRate(int rate) {
		blipBuffer.clockRate(rate);
	}
	
	public void clearBuffer() {
		blipBuffer.clear(true);

		m_dSumSS = 0;
		m_dSumTND = 0;
	}
	
	public int finishBuffer(int t) {
		blipBuffer.endFrame(t);

		// Get channel levels for VRC7 TODO VRC6 以外暂时不考虑
//		for (int i = 0; i < 6; ++i)
//			StoreChannelLevel(CHANID_VRC7_CH1 + i, OPLL_getchanvol(i));
//
//		// Get channel levels for Sunsoft
//		for (int i = 0; i < 3; ++i)
//			StoreChannelLevel(CHANID_S5B_CH1 + i, PSG_getchanvol(i));

		for (int i = 0; i < CHANNELS; ++i) {
			if (m_iChanLevelFallOff[i] > 0)
				m_iChanLevelFallOff[i]--;
			else {
				if (m_fChannelLevels[i] > 0) {
					m_fChannelLevels[i] -= LEVEL_FALL_OFF_RATE;
					if (m_fChannelLevels[i] < 0)
						m_fChannelLevels[i] = 0;
				}
			}
		}

		// Return number of samples available
		return blipBuffer.samplesAvail();
	}
	
	public final int samplesAvail() {
		return blipBuffer.samplesAvail();
	}
	
	public void mixSamples(short[] pBuffer, int offset, int count) {
		// For VRC7
		blipBuffer.mixSamples(pBuffer, offset, count);
	}
	
	public final int getMixSampleCount(int t) {
		return blipBuffer.countSamples(t);
	}

//	public void addSample(int ChanID, int Value);
	
	public int readBuffer(int size, byte[] buffer, boolean stereo) {
		return blipBuffer.readSamples(buffer, 0, size, false);
	}

	public final int getChanOutput(byte chan) {
		return (int) m_fChannelLevels[chan];
	}
	
	public void setChipLevel(int chip, float level) {
		switch (chip) {
		case CHIP_LEVEL_APU1:
			m_fLevelAPU1 = level;
			break;
		case CHIP_LEVEL_APU2:
			m_fLevelAPU2 = level;
			break;
		case CHIP_LEVEL_VRC6:
			m_fLevelVRC6 = level;
			break;
		case CHIP_LEVEL_MMC5:
			m_fLevelMMC5 = level;
			break;
//		case CHIP_LEVEL_FDS:
//			m_fLevelFDS = level;
//			break;
//		case CHIP_LEVEL_N163:
//			m_fLevelN163 = level;
//			break;
		}
	}
	
	public final int resampleDuration(int time) {
		return blipBuffer.resampledDuration(time);
	}
	
	public void setNamcoVolume(float fVol) {
		// TODO N163 暂时不做
		// float fVolume = fVol * m_fOverallVol * getAttenuation();
		// synthN163.volume(fVolume * 1.1f * m_fLevelN163);
	}

	private double calcPin1(double val1, double val2) {
		// Mix the output of APU audio pin 1: square
		// 一般而言, val1 和 val2 都不会小于 0
		
		double sum = val1 + val2;

		if (sum > 0)
			return 95.88 / ((8128.0 / sum) + 100.0);
		
		return 0;
	}
	
	private double calcPin2(double val1, double val2, double val3) {
		// Mix the output of APU audio pin 2: triangle, noise and DPCM
		// 一般而言, val* 都不会小于 0
		
		if ((val1 + val2 + val3) > 0)
			return 159.79 / ((1.0 / ((val1 / 8227.0) + (val2 / 12241.0) + (val3 / 22638.0))) + 100.0);

		return 0;
	}

	private void mixInternal1(int time) {
		double sum = calcPin1(m_iChannels[CHANID_SQUARE1], m_iChannels[CHANID_SQUARE2]);
		
		double delta = (sum - m_dSumSS) * AMP_2A03;
		synth2A03SS.offset(time, (int)delta, blipBuffer);
		m_dSumSS = sum;
	}
	
	private void mixInternal2(int time) {
		double sum = calcPin2(m_iChannels[CHANID_TRIANGLE], m_iChannels[CHANID_NOISE], m_iChannels[CHANID_DPCM]);
//		double sum = calcPin2(m_iChannels[CHANID_TRIANGLE], 0, m_iChannels[CHANID_DPCM]);
		
		double delta = (sum - m_dSumTND) * AMP_2A03;
		synth2A03TND.offset(time, (int)delta, blipBuffer);
		m_dSumTND = sum;
	}
	
	private void mixN163(int value, int time) {
//		SynthN163.offset(Time, Value, &BlipBuffer);
	}
	
	private void mixFDS(int value, int time) {
//		SynthFDS.offset(Time, Value, &BlipBuffer);
	}
	
	private void mixVRC6(int value, int time) {
		synthVRC6.offset(time, value, blipBuffer);
	}
	
	private void mixMMC5(int value, int time) {
		synthMMC5.offset(time, value, blipBuffer);	
	}
	
	@SuppressWarnings("unused")
	private void mixS5B(int value, int time) {
//		SynthS5B.offset(Time, Value, &BlipBuffer);
	}

	private void storeChannelLevel(int channel, int value) {
		int absVol = Math.abs(value);

		// Adjust channel levels for some channels
		if (channel == CHANID_VRC6_SAWTOOTH)
			absVol = (absVol * 3) / 4;

		if (channel == CHANID_DPCM)
			absVol /= 8;

		if (channel == CHANID_FDS)
			absVol = absVol / 38;

		if (channel >= CHANID_N163_CHAN1 && channel <= CHANID_N163_CHAN8) {
			absVol /= 15;
			channel = (7 - (channel - CHANID_N163_CHAN1)) + CHANID_N163_CHAN1;
		}

		if (channel >= CHANID_VRC7_CH1 && channel <= CHANID_VRC7_CH6) {
			absVol = (int)(Math.log(absVol) * 3.0f);
		}

		if (channel >= CHANID_S5B_CH1 && channel <= CHANID_S5B_CH3) {
			absVol = (int)(Math.log(absVol) * 2.8f);
		}

		if (absVol >= m_fChannelLevels[channel]) {
			m_fChannelLevels[channel] = absVol;
			m_iChanLevelFallOff[channel] = LEVEL_FALL_OFF_DELAY;
		}
	}
	
	/*private void clearChannelLevels() {
		Arrays.fill(m_fChannelLevels, 0);
		Arrays.fill(m_iChanLevelFallOff, 0);
	}*/

	/**
	 * <p>轨道变多之后, 总音量会降低, 但是各轨道音量比值不会发生变化.
	 * <p>TODO 暂时不做 VCR6, MMC5 以外的部分
	 */
	final float getAttenuation()  {
		final float ATTENUATION_VRC6 = 0.80f;
//		const float ATTENUATION_VRC7 = 0.64f;
//		const float ATTENUATION_N163 = 0.70f;
		final float ATTENUATION_MMC5 = 0.83f;
//		const float ATTENUATION_FDS  = 0.90f;

		float attenuation = 1.0f;

		// Increase headroom if some expansion chips are enabled

//		if ((m_iExternalChip & SNDCHIP_VRC7) != 0)
//			Attenuation *= ATTENUATION_VRC7;
//
//		if (m_iExternalChip & SNDCHIP_N163)
//			Attenuation *= ATTENUATION_N163;

		if ((m_iExternalChip & SNDCHIP_VRC6) != 0)
			attenuation *= ATTENUATION_VRC6;

		if ((m_iExternalChip & SNDCHIP_MMC5) != 0)
			attenuation *= ATTENUATION_MMC5;
//
//		if (m_iExternalChip & SNDCHIP_FDS)
//			Attenuation *= ATTENUATION_FDS;

		return attenuation;
	}

	// Blip buffer synths
	private BlipSynth synth2A03SS;
	private BlipSynth synth2A03TND;
	private BlipSynth synthVRC6;
	private BlipSynth synthMMC5; //	Blip_Synth<blip_good_quality, -130> SynthMMC5;	
//	Blip_Synth<blip_good_quality, -1600>	SynthN163;
//	Blip_Synth<blip_good_quality, -3500>	SynthFDS;
//	Blip_Synth<blip_good_quality, -2000>	SynthS5B;
	
	// Blip buffer object
	private BlipBuffer blipBuffer = new BlipBuffer();

	private double m_dSumSS;
	private double m_dSumTND;

	private int[] m_iChannels = new int[CHANNELS];
	
	/**
	 * unsigned
	 */
	private byte m_iExternalChip;
	/**
	 * unsigned
	 */
	private int m_iSampleRate;

	private float[] m_fChannelLevels = new float[CHANNELS];
	/**
	 * unsigned
	 */
	private int[] m_iChanLevelFallOff = new int[CHANNELS];

	private int m_iLowCut;
	private int m_iHighCut;
	private int m_iHighDamp;
	private float m_fOverallVol;

	private float m_fLevelAPU1;
	private float m_fLevelAPU2;
	private float m_fLevelVRC6;
	private float m_fLevelMMC5;
//	private float m_fLevelFDS;
//	private float m_fLevelN163;

}
