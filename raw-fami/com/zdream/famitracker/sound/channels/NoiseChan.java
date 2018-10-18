package com.zdream.famitracker.sound.channels;

import static com.zdream.famitracker.FamitrackerTypes.*;

/**
 * 2A03 Noise
 * @author Zdream
 */
public final class NoiseChan extends ChannelHandler2A03 {
	
	public NoiseChan() {
		m_iDefaultDuty = 0;
	}
	
	@Override
	public void refreshChannel() {
		int period = calculatePeriod();
		int volume = calculateVolume();
		byte noiseMode = (byte) ((m_iDutyPeriod & 0x01) << 7);

		if (!m_bGate || volume == 0) {
			writeRegister(0x400C, (byte) 0x30);
			return;
		}

		period = period & 0x0F;

		period ^= 0x0F;

		writeRegister(0x400C, (byte) (0x30 | volume));
		writeRegister(0x400D, (byte) 0x00);
		writeRegister(0x400E, (byte) (noiseMode | period));
		writeRegister(0x400F, (byte) 0x00);
	}
	
	protected void clearRegisters() {
		writeRegister(0x400C, (byte) 0x30);
		writeRegister(0x400D, (byte) 0);
		writeRegister(0x400E, (byte) 0);
		writeRegister(0x400F, (byte) 0);
	}

	@Override
	protected void handleNote(int note, int octave) {
		int newNote = ((octave) * 12 + (note) - 1);
		int nesFreq = triggerNote(newNote);

		nesFreq = (nesFreq & 0x0F) | 0x10;

//		NewNote &= 0x0F;

		if (m_iPortaSpeed > 0 && m_iEffect == EF_PORTAMENTO) {
			if (m_iPeriod == 0)
				m_iPeriod = nesFreq;
			m_iPortaTo = nesFreq;
		} else
			m_iPeriod = nesFreq;

		m_bGate = true;

		m_iNote			= newNote;
		m_iDutyPeriod	= m_iDefaultDuty;
		m_iSeqVolume	= m_iInitVolume;
	}

	@Override
	protected void setupSlide(int type, int effParam) {
		super.setupSlide(type, effParam);
		
		if (m_iEffect == EF_SLIDE_DOWN)
			m_iEffect = EF_SLIDE_UP;
		else
			m_iEffect = EF_SLIDE_DOWN;
	}

	@Override
	protected int triggerNote(int note) {
		registerKeyState(note);
		
		return note | 0x10;
	}

}
