package com.zdream.famitracker.sound.channels;

/**
 * VRC6 Square 2
 * @author Zdream
 */
public final class VRC6Square2 extends ChannelHandlerVRC6 {

	public VRC6Square2() {
		m_iDefaultDuty = 0;
	}

	@Override
	public void refreshChannel() {
		int period = calculatePeriod();
		int volume = calculateVolume();
		byte dutyCycle = (byte) (m_iDutyPeriod << 4);

		byte hiFreq = (byte) (period & 0xFF);
		byte loFreq = (byte) (period >> 8);

		if (!m_bGate || volume == 0) {
			writeExternalRegister(0xA002, (byte) 0x00);
			return;
		}

		writeExternalRegister(0xA000, (byte) (dutyCycle | volume));
		writeExternalRegister(0xA001, hiFreq);
		writeExternalRegister(0xA002, (byte) (0x80 | loFreq));
	}

	@Override
	protected void clearRegisters() {
		writeExternalRegister(0xA000, (byte) 0);
		writeExternalRegister(0xA001, (byte) 0);
		writeExternalRegister(0xA002, (byte) 0);
	}

}
