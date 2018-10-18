package com.zdream.famitracker.sound.channels;

/**
 * MMC5 Pulse 1
 * @author Zdream
 */
public final class MMC5Square1Chan extends ChannelHandlerMMC5 {

	public MMC5Square1Chan() {}

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

		writeExternalRegister(0x5000, (byte) ((dutyCycle << 6) | 0x30 | volume));
		writeExternalRegister(0x5002, (byte) hiFreq);

		if (loFreq != lastLoFreq)
			writeExternalRegister(0x5003, (byte) loFreq);
	}

	@Override
	protected void clearRegisters() {
		writeExternalRegister(0x5000, (byte) 0);
		writeExternalRegister(0x5002, (byte) 0);
		writeExternalRegister(0x5003, (byte) 0);
	}

}
