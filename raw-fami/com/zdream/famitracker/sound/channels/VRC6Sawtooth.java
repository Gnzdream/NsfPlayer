package com.zdream.famitracker.sound.channels;

/**
 * VRC6 Sawtooth
 * @author Zdream
 */
public final class VRC6Sawtooth extends ChannelHandlerVRC6 {

	public VRC6Sawtooth() {
		m_iDefaultDuty = 0;
	}

	@Override
	public void refreshChannel() {
		int period = calculatePeriod();

		byte hiFreq = (byte) (period & 0xFF);
		byte loFreq = (byte) (period >> 8);

		int tremVol = getTremolo();
		int volume = (m_iSeqVolume * (m_iVolume >> VOL_COLUMN_SHIFT)) / 15 - tremVol;

		volume = (volume << 1) | ((m_iDutyPeriod & 1) << 5);

		if (volume < 0)
			volume = 0;
		if (volume > 63)
			volume = 63;

		if (m_iSeqVolume > 0 && m_iVolume > 0 && volume == 0)
			volume = 2;

		if (!m_bGate)
			volume = 0;

		if (!m_bGate || volume == 0) {
			writeExternalRegister(0xB002, (byte) 0x00);
			return;
		}

		writeExternalRegister(0xB000, (byte) volume);
		writeExternalRegister(0xB001, hiFreq);
		writeExternalRegister(0xB002, (byte) (0x80 | loFreq));
	}

	@Override
	protected void clearRegisters() {
		writeExternalRegister(0xB000, (byte) 0);
		writeExternalRegister(0xB001, (byte) 0);
		writeExternalRegister(0xB002, (byte) 0);
	}

}
