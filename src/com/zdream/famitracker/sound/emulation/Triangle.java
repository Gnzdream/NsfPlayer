package com.zdream.famitracker.sound.emulation;

import static com.zdream.famitracker.sound.emulation.Types.*;

import com.zdream.famitracker.test.FamitrackerLogger;

public class Triangle extends Channel {
	
	private static final byte[] TRIANGLE_WAVE = {
		0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 
		0x0F, 0x0E, 0x0D, 0x0C, 0x0B, 0x0A, 0x09, 0x08, 0x07, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01, 0x00
	};

	public Triangle(Mixer pMixer, byte id) {
		super(pMixer, id, SNDCHIP_NONE);
	}
	
	public void reset() {
		m_iEnabled = m_iControlReg = 0;
		m_iCounter = m_iLengthCounter = 0;

		write(0, 0);
		write(1, 0);
		write(2, 0);
		write(3, 0);

		endFrame();
	}
	
	public void write(int address, int value) {
		value &= 0xFF;
		FamitrackerLogger.instance.logWriteAddress("Triangle", address, value);
		switch (address) {
		case 0x00:
			m_iLinearLoad = value & 0x7F;
			m_iLoop = value & 0x80;
			break;
		case 0x01:
			break;
		case 0x02:
			m_iPeriod = value | (m_iPeriod & 0x0700);
			break;
		case 0x03:
			m_iPeriod = ((value & 0x07) << 8) | (m_iPeriod & 0xFF);
			m_iLengthCounter = (short) (APU.LENGTH_TABLE[(value & 0xF8) >> 3] & 0xFF);
			m_iHalt = 1;
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
	 * 没有使用
	 */
	public int readControl() {
		return ((m_iLengthCounter > 0) && (m_iEnabled == 1)) ? 1 : 0;
	}
	
	/**
	 * <p>Triangle skips if a wavelength less than 2 is used
	 * It takes to much CPU and it wouldn't be possible to hear anyway
	 * @param time
	 */
	public void process(int time) {
		if (m_iLinearCounter == 0 || m_iLengthCounter == 0 || m_iEnabled == 0) {
			m_iTime += time;
			return;
		} else if (m_iPeriod <= 1) {
			// Frequency is too high to be audible
			m_iTime += time;
			// m_iStepGen = 7; 原程序有, 我把它注释了
			mix(TRIANGLE_WAVE[m_iStepGen]);
			return;
		}

		while (time >= m_iCounter) {
			time	  -= m_iCounter;
			m_iTime   += m_iCounter;
			m_iCounter = m_iPeriod + 1;
			mix(TRIANGLE_WAVE[m_iStepGen]);
			m_iStepGen = (m_iStepGen + 1) & 0x1F;
		}
		// System.out.println(TRIANGLE_WAVE[m_iStepGen]);
		
		m_iCounter -= time;
		m_iTime += time;
	}

	/**
	 * APU.clock_120Hz() 调用
	 */
	public void lengthCounterUpdate() {
		if ((m_iLoop == 0) && (m_iLengthCounter > 0)) 
			m_iLengthCounter--;
	}
	
	/**
	 * 1.  If the halt flag is set, the linear counter is reloaded with the counter reload value, 
	 *     otherwise if the linear counter is non-zero, it is decremented.
	 * 2.  If the control flag is clear, the halt flag is cleared. 
	 * 
	 * APU.clock_240Hz() 调用
	 */
	public void linearCounterUpdate() {
		if (m_iHalt == 1)
			m_iLinearCounter = m_iLinearLoad;
		else
			if (m_iLinearCounter > 0)
				m_iLinearCounter--;

		if (m_iLoop == 0)
			m_iHalt = 0;
	}

	private int m_iLoop, m_iLinearLoad, m_iHalt;
	private int m_iLinearCounter;
	private int m_iStepGen;

}
