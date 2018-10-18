package com.zdream.famitracker.sound.emulation.expansion;

import com.zdream.famitracker.sound.emulation.Mixer;

public abstract class ExChannel {

	public ExChannel(Mixer pMixer, byte chip, byte id) {
		m_pMixer = pMixer;
		m_iChip = chip;
		m_iChanId = id;
	}
	
	public void endFrame() {
		m_iTime = 0;
	}
	
	protected void mix(int Value) {
		int delta = Value - m_iLastValue;
		if (delta != 0)
			m_pMixer.addValue(m_iChanId, m_iChip, delta, Value, m_iTime);
		m_iLastValue = Value;
	}
	
	protected Mixer m_pMixer;

	/**
	 * Cycle counter, resets every new frame
	 */
	protected int m_iTime;
	protected byte m_iChip;
	protected byte m_iChanId;
	
	/**
	 * Last value sent to mixer
	 */
	protected int m_iLastValue;

}
