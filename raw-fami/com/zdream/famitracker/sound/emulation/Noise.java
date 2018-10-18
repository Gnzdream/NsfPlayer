package com.zdream.famitracker.sound.emulation;

import static com.zdream.famitracker.sound.emulation.Types.*;

import com.zdream.famitracker.test.FamitrackerLogger;

public class Noise extends Channel {
	
	public static final short[] NOISE_PERIODS_NTSC = {
		4, 8, 16, 32, 64, 96, 128, 160, 202, 254, 380, 508, 762, 1016, 2034, 4068
	};

	public static final short[] NOISE_PERIODS_PAL = {
		4, 8, 14, 30, 60, 88, 118, 148, 188, 236, 354, 472, 708,  944, 1890, 3778
	};
	
	public short[] PERIOD_TABLE = NOISE_PERIODS_NTSC;

	public Noise(Mixer pMixer, byte id) {
		super(pMixer, id, SNDCHIP_NONE);
	}
	
	public void reset() {
		m_iEnabled = m_iControlReg = 0;
		m_iCounter = m_iLengthCounter = 0;
		
		m_iShiftReg = 1;

		m_iEnvelopeCounter = m_iEnvelopeSpeed = 1;

		write(0, 0);
		write(1, 0);
		write(2, 0);
		write(3, 0);

		endFrame();
	}
	
	public void write(int address, int value) {
		value &= 0xFF;
		FamitrackerLogger.instance.logWriteAddress("Noise", address, value);
		switch (address) {
		case 0x00:
			m_iLooping = value & 0x20;
			m_iEnvelopeFix = value & 0x10;
			m_iFixedVolume = value & 0x0F;
			m_iEnvelopeSpeed = (value & 0x0F) + 1;
			break;
		case 0x01:
			break;
		case 0x02:
			m_iPeriod = PERIOD_TABLE[value & 0x0F];
			m_iSampleRate = (value & 0x80) != 0 ? 8 : 13;
			break;
		case 0x03:
			m_iLengthCounter = (short) (APU.LENGTH_TABLE[(value >> 3) & 0x1F] & 0xFF);
			m_iEnvelopeVolume = 0x0F;
			if (m_iControlReg != 0)
				m_iEnabled = 1;
			break;
		}
	}
	
	/**
	 * APU.write4015() 调用
	 */
	public void writeControl(int value) {
		m_iControlReg = (byte) (value & 1);

		if (m_iControlReg == 0) 
			m_iEnabled = 0;
	}
	
	/**
	 * 暂无调用
	 */
	public int readControl() {
		return ((m_iLengthCounter > 0) && (m_iEnabled == 1)) ? 1 : 0;
	}
	
	public void process(int time) {
		boolean valid = m_iEnabled != 0 && (m_iLengthCounter > 0);

		while (time >= m_iCounter) {
			time	  -= m_iCounter;
			m_iTime	  += m_iCounter;
			m_iCounter = m_iPeriod;
			int Volume = m_iEnvelopeFix != 0 ? m_iFixedVolume : m_iEnvelopeVolume;
			mix(valid && (m_iShiftReg & 1) != 0 ? Volume : 0);
			m_iShiftReg = (((m_iShiftReg << 14) ^ (m_iShiftReg << m_iSampleRate)) & 0x4000) | (m_iShiftReg >> 1);
		}

		m_iCounter -= time;
		m_iTime += time;
	}
	
	/**
	 * APU.clock_120Hz() 调用
	 */
	public void lengthCounterUpdate() {
		if ((m_iLooping == 0) && (m_iLengthCounter > 0)) 
			--m_iLengthCounter;
	}
	
	/**
	 * APU.clock_240Hz() 调用
	 */
	public void envelopeUpdate() {
		if (--m_iEnvelopeCounter == 0) {
			m_iEnvelopeCounter = m_iEnvelopeSpeed;
			if (m_iEnvelopeFix == 0) {
				if (m_iLooping != 0)
					m_iEnvelopeVolume = (m_iEnvelopeVolume - 1) & 0x0F;
				else if (m_iEnvelopeVolume > 0)
					m_iEnvelopeVolume--;
			}
		}
	}

	int m_iLooping, m_iEnvelopeFix, m_iEnvelopeSpeed;
	int m_iEnvelopeVolume, m_iFixedVolume;
	int m_iEnvelopeCounter;
	
	int m_iSampleRate;
	int m_iShiftReg;

}
