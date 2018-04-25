package com.zdream.famitracker.sound.emulation.expansion;

import com.zdream.famitracker.sound.emulation.Mixer;

import static com.zdream.famitracker.sound.emulation.Types.*;

public class VRC6Pulse extends ExChannel {

	public VRC6Pulse(Mixer pMixer, byte id) {
		super(pMixer, SNDCHIP_VRC6, id);
		reset();
	}
	
	public void reset() {
		m_iDutyCycle = m_iVolume = m_iGate = m_iEnabled = 0;
		m_iPeriod = m_iPeriodLow = m_iPeriodHigh = 0;
		m_iCounter = 0;
		m_iDutyCycleCounter = 0;
	}
	
	public void write(int address, int value) {
		switch (address) {
		case 0x00:
			m_iGate = value & 0x80;
			m_iDutyCycle = ((value & 0x70) >> 4) + 1;
			m_iVolume = value & 0x0F;
			if (m_iGate != 0)
				mix(m_iVolume);
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
		
		// check
		if (m_iPeriod < 0) {
			System.out.println();
		}

		while (time >= m_iCounter) {
			time      -= m_iCounter;
			m_iTime	  += m_iCounter;
			m_iCounter = m_iPeriod + 1;
		
			m_iDutyCycleCounter = (m_iDutyCycleCounter + 1) & 0x0F;
			mix((m_iGate != 0 || m_iDutyCycleCounter >= m_iDutyCycle) ? m_iVolume : 0);
		}

		m_iCounter -= time;
		m_iTime += time;
	}

	int m_iDutyCycle, 
		m_iVolume, 
		m_iGate, 
		m_iEnabled;
	int m_iPeriod;
	int m_iPeriodLow, 
		m_iPeriodHigh;
	int m_iCounter;
	int m_iDutyCycleCounter;

}
