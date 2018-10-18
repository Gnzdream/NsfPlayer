package com.zdream.famitracker.sound.channels;

/**
 * MMC5 Pulse 2
 * @author Zdream
 */
public final class MMC5Square2Chan extends ChannelHandlerMMC5 {

	public MMC5Square2Chan() {}

	@Override
	public void refreshChannel() {
		int period = calculatePeriod();
		int volume = calculateVolume();
		int dutyCycle = (m_iDutyPeriod & 0x03); // [0, 3]

		int hiFreq = (period & 0xFF);
		int loFreq = (period >> 8);
		int lastLoFreq = (m_iLastPeriod >> 8);

		m_iLastPeriod = period;

		writeExternalRegister(0x5015, (byte) 0x03);

		writeExternalRegister(0x5004, (byte) ((dutyCycle << 6) | 0x30 | volume));
		writeExternalRegister(0x5006, (byte) hiFreq);

		if (loFreq != lastLoFreq)
			writeExternalRegister(0x5007, (byte) loFreq);
	}

	@Override
	protected void clearRegisters() {
		writeExternalRegister(0x5004, (byte) 0);
		writeExternalRegister(0x5006, (byte) 0);
		writeExternalRegister(0x5007, (byte) 0);
	}

}
