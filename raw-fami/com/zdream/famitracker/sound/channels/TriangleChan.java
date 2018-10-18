package com.zdream.famitracker.sound.channels;

/**
 * 2A03 Triangle
 * @author Zdream
 */
public final class TriangleChan extends ChannelHandler2A03 {

	public TriangleChan() {}
	
	@Override
	public void refreshChannel() {
		int freq = calculatePeriod();

		byte hiFreq = (byte) (freq & 0xFF);
		byte loFreq = (byte) (freq >> 8);
		
		if (m_iSeqVolume > 0 && m_iVolume > 0 && m_bGate) {
			writeRegister(0x4008, (byte) 0x81);
			writeRegister(0x400A, hiFreq);
			writeRegister(0x400B, loFreq);
		} else
			writeRegister(0x4008, (byte) 0);
	}
	
	@Override
	protected void clearRegisters() {
		writeRegister(0x4008, (byte) 0);
		writeRegister(0x4009, (byte) 0);
		writeRegister(0x400A, (byte) 0);
		writeRegister(0x400B, (byte) 0);
	}

}
