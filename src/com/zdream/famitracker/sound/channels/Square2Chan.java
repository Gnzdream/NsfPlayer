package com.zdream.famitracker.sound.channels;

import com.zdream.famitracker.test.FamitrackerLogger;

/**
 * 2A03 Square 2
 * @author Zdream
 */
public final class Square2Chan extends ChannelHandler2A03 {

	public Square2Chan() {
		m_iDefaultDuty = 0;
	}
	
	@Override
	public void refreshChannel() {
		int period = calculatePeriod();
		int volume = calculateVolume();
		byte dutyCycle = (byte) (m_iDutyPeriod & 0x03);

		int hiFreq = (period & 0xFF);
		int loFreq = (period >> 8);
		int lastLoFreq	= (m_iLastPeriod >> 8) & 0xFF;

		if (!m_bGate || volume == 0) {
			writeRegister(0x4004, (byte) 0x30);
			m_iLastPeriod = 0xFFFF;
			return;
		}

		writeRegister(0x4004, (byte) ((dutyCycle << 6) | 0x30 | volume));

		if (m_cSweep != 0) {
			if ((m_cSweep & 0x80) != 0) {
				writeRegister(0x4005, (byte) m_cSweep);
				m_cSweep &= 0x7F;
				writeRegister(0x4017, (byte) 0x80);	// Clear sweep unit
				writeRegister(0x4017, (byte) 0x00);
				FamitrackerLogger.instance.logValue("2P 4017 sweep");
				writeRegister(0x4006, (byte) hiFreq);
				writeRegister(0x4007, (byte) loFreq);
				m_iLastPeriod = 0xFFFF;
			}
		} else {
			writeRegister(0x4005, (byte) 0x08);
//			writeRegister(0x4017, (byte) 0x80);	// Manually execute one APU frame sequence to kill the sweep unit
//			writeRegister(0x4017, (byte) 0x00);
			writeRegister(0x4006, (byte) hiFreq);
			
			if (loFreq != lastLoFreq)
				writeRegister(0x4007, (byte) loFreq);
		}

		m_iLastPeriod = period;
	}
	
	@Override
	protected void clearRegisters() {
		writeRegister(0x4004, (byte) 0x30);
		writeRegister(0x4005, (byte) 0x08);
		writeRegister(0x4006, (byte) 0x00);
		writeRegister(0x4007, (byte) 0x00);
		m_iLastPeriod = 0xFFFF;
	}

}
