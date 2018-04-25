package com.zdream.famitracker.sound.emulation;

import com.zdream.famitracker.test.FamitrackerLogger;

public class Square extends Channel {
	
	private static final byte DUTY_TABLE[][] = {
		{0, 0, 1, 1,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0},
		{0, 0, 1, 1,  1, 1, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0},
		{0, 0, 1, 1,  1, 1, 1, 1,  1, 1, 0, 0,  0, 0, 0, 0},
		{1, 1, 0, 0,  0, 0, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1}
	};

	public Square(Mixer pMixer, byte id, byte chip) {
		super(pMixer, id, chip);
	}
	
	public void reset() {
		m_iEnabled = m_iControlReg = 0;
		m_iCounter = 0;

		m_iSweepCounter = 1;
		m_iSweepPeriod = 1;

		m_iEnvelopeCounter = 1;
		m_iEnvelopeSpeed = 1;

		write(0, 0);
		write(1, 0);
		write(2, 0);
		write(3, 0);

		sweepUpdate(0);

		endFrame();
	}
	
	public void write(int address, int value) {
		value &= 0xFF;
		FamitrackerLogger.instance.logWriteAddress("Square", address, value);
		switch (address) {
		case 0x00:
			m_iDutyLength = value >> 6;
			m_iFixedVolume = value & 0x0F;
			m_iLooping = value & 0x20;
			m_iEnvelopeFix = value & 0x10;
			m_iEnvelopeSpeed = (value & 0x0F) + 1;
			break;
		case 0x01:
			m_iSweepEnabled = value & 0x80;
			m_iSweepPeriod = ((value >> 4) & 0x07) + 1;
			m_iSweepMode = value & 0x08;		
			m_iSweepShift = value & 0x07;
			m_bSweepWritten = true;
			break;
		case 0x02:
			m_iPeriod = value | (m_iPeriod & 0x0700);
			break;
		case 0x03:
			m_iPeriod = ((value & 0x07) << 8) | (m_iPeriod & 0xFF);
			m_iLengthCounter = (short) (APU.LENGTH_TABLE[(value & 0xF8) >> 3] & 0xFF);
			m_iDutyCycle = 0;
			m_iEnvelopeVolume = 0x0F;
			if (m_iControlReg != 0)
				m_iEnabled = 1;
			break;
		}
	}
	
	public void writeControl(int value) {
		m_iControlReg = (byte) (value & 0x01);

		if (m_iControlReg == 0)
			m_iEnabled = 0;
	}
	
	public int readControl() {
		return ((m_iLengthCounter > 0) && (m_iEnabled == 1)) ? 1 : 0;
	}
	
	public void process(int time) {
		if (m_iPeriod == 0) {
			m_iTime += time;
			return;
		}

		boolean valid = (m_iPeriod > 7) && (m_iEnabled != 0) && (m_iLengthCounter > 0) && (m_iSweepResult < 0x800);

		while (time >= m_iCounter) {
			time		-= m_iCounter;
			m_iTime		+= m_iCounter;
			m_iCounter	 = m_iPeriod + 1;
			int volume = m_iEnvelopeFix != 0 ? m_iFixedVolume : m_iEnvelopeVolume;
			mix (valid && DUTY_TABLE[m_iDutyLength][m_iDutyCycle] != 0 ? volume : 0);
			m_iDutyCycle = (m_iDutyCycle + 1) & 0x0F;
		}

		m_iCounter -= time;
		m_iTime += time;
	}

	public void lengthCounterUpdate() {
		if ((m_iLooping == 0) && (m_iLengthCounter > 0)) 
			--m_iLengthCounter;
	}
	
	public void sweepUpdate(int diff) {
		m_iSweepResult = (m_iPeriod >> m_iSweepShift);

		if (m_iSweepMode != 0)
			m_iSweepResult = m_iPeriod - m_iSweepResult - diff;
		else
			m_iSweepResult = m_iPeriod + m_iSweepResult;

		if (--m_iSweepCounter == 0) {
			m_iSweepCounter = m_iSweepPeriod;
			if (m_iSweepEnabled != 0 && (m_iPeriod > 0x07) && (m_iSweepResult < 0x800) && (m_iSweepShift > 0))
				m_iPeriod = m_iSweepResult;
		}

		if (m_bSweepWritten) {
			m_bSweepWritten = false;
			m_iSweepCounter = m_iSweepPeriod;
		}
	}
	
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
	
	
	/**
	 * unsigned
	 */
	int m_iDutyLength, m_iDutyCycle;

	/**
	 * unsigned
	 */
	int m_iLooping, m_iEnvelopeFix, m_iEnvelopeSpeed;

	/**
	 * unsigned
	 */
	int m_iEnvelopeVolume, m_iFixedVolume;
	int m_iEnvelopeCounter;


	/**
	 * unsigned
	 */
	int m_iSweepEnabled, m_iSweepPeriod, m_iSweepMode, m_iSweepShift;
	int m_iSweepCounter, m_iSweepResult;
	boolean m_bSweepWritten;

}
