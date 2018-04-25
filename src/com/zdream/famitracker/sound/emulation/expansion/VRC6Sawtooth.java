package com.zdream.famitracker.sound.emulation.expansion;

import com.zdream.famitracker.sound.emulation.Mixer;

import static com.zdream.famitracker.sound.emulation.Types.*;

public class VRC6Sawtooth extends ExChannel {

	public VRC6Sawtooth(Mixer pMixer, byte id) {
		super(pMixer, SNDCHIP_VRC6, id);
		reset();
	}
	
	public void reset() {
		m_iPhaseAccumulator = m_iPhaseInput = m_iEnabled = m_iResetReg = 0;
		m_iPeriod = 0;
		m_iPeriodLow = m_iPeriodHigh = 0;
		m_iCounter = 0;
	}
	
	public void write(int address, int value) {
		switch (address) {
		case 0x00:
			m_iPhaseInput = (value & 0x3F);
			break;
		case 0x01:
			m_iPeriodLow = value;
			m_iPeriod = m_iPeriodLow + (m_iPeriodHigh << 8);
			break;
		case 0x02:
			m_iEnabled = (value & 0x80);
			m_iPeriodHigh = (value & 0x0F);
			m_iPeriod = m_iPeriodLow + (m_iPeriodHigh << 8);
			break;
		}
	}
	
	public void process(int time) {
		if (m_iEnabled == 0 || m_iPeriod == 0) {
			m_iTime += time;
			return;
		}

		while (time >= m_iCounter) {
			time 	  -= m_iCounter;
			m_iTime	  += m_iCounter;
			m_iCounter = m_iPeriod + 1;

			if ((m_iResetReg & 1) != 0)
				m_iPhaseAccumulator = (m_iPhaseAccumulator + m_iPhaseInput) & 0xFF;

			m_iResetReg++;

			if (m_iResetReg == 14) {
				m_iPhaseAccumulator = 0;
				m_iResetReg = 0;
			}

			// The 5 highest bits of accumulator are sent to the mixer
			mix(m_iPhaseAccumulator >> 3);
		}

		m_iCounter -= time;
		m_iTime += time;
	}

	int m_iPhaseAccumulator, 
		m_iPhaseInput, 
		m_iEnabled, 
		m_iResetReg;
	int m_iPeriod;
	int m_iPeriodLow, 
		m_iPeriodHigh;
	int m_iCounter;

}
