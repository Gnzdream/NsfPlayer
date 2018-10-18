package com.zdream.famitracker.sound.channels;

/**
 * VRC6 Square 1
 * @author Zdream
 */
public final class VRC6Square1 extends ChannelHandlerVRC6 {

	public VRC6Square1() {
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
			writeExternalRegister(0x9002, (byte) 0x00);
			return;
		}

		writeExternalRegister(0x9000, (byte) (dutyCycle | volume));
		writeExternalRegister(0x9001, hiFreq);
		writeExternalRegister(0x9002, (byte) (0x80 | loFreq));
	}

	@Override
	protected void clearRegisters() {
		writeExternalRegister(0x9000, (byte) 0);
		writeExternalRegister(0x9001, (byte) 0);
		writeExternalRegister(0x9002, (byte) 0);
	}

}
