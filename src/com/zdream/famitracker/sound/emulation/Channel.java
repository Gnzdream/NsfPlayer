package com.zdream.famitracker.sound.emulation;

/**
 * <p>这个类用于派生音频通道
 * <p>This class is used to derive the audio channels
 * @author Zdream
 */
public abstract class Channel {

	public Channel(Mixer pMixer, byte id, byte chip) {
		m_pMixer = pMixer;
		m_iChanId = id;
		m_iChip = chip;
	}
	
	/**
	 * Begin a new audio frame
	 */
	public void endFrame() {
		m_iTime = 0;
	}

	public final int getPeriod() {
		return m_iPeriod;
	}

	protected void mix(int value) {
		if (m_iLastValue != value) {
			m_pMixer.addValue(m_iChanId, m_iChip, value, value, m_iTime);
			m_iLastValue = value;
		}
	};

	protected Mixer m_pMixer; // The mixer

	/**
	 * Cycle counter, resets every new frame, unsigned
	 */
	protected int m_iTime;
	
	/**
	 * Last value sent to mixer
	 */
	protected int m_iLastValue;
	
	/**
	 * This channels unique ID, unsigned, 这个值在 {@link Types} 中
	 */
	protected byte m_iChanId;
	
	/**
	 * Chip, unsigned, 这个值在 {@link Types} 中
	 */
	protected byte m_iChip;

	// Variables used by channels
	/**
	 * unsigned 4015 的值
	 */
	protected byte m_iControlReg;
	/**
	 * unsigned
	 */
	protected byte m_iEnabled;
	/**
	 * unsigned
	 */
	protected int m_iPeriod;
	/**
	 * unsigned
	 */
	protected short m_iLengthCounter;
	/**
	 * unsigned
	 */
	protected int m_iCounter;

}
