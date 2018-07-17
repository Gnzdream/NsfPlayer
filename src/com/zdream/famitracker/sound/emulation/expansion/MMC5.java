package com.zdream.famitracker.sound.emulation.expansion;

import static com.zdream.famitracker.sound.emulation.Types.*;

import com.zdream.famitracker.sound.emulation.Mixer;
import com.zdream.famitracker.sound.emulation.Square;

public class MMC5 extends External {

	public MMC5(Mixer pMixer) {
		super(pMixer);
		
		this.m_pEXRAM = new byte[0x400];
		m_pSquare1 = new Square(pMixer, CHANID_MMC5_SQUARE1, SNDCHIP_MMC5);
		m_pSquare2 = new Square(pMixer, CHANID_MMC5_SQUARE2, SNDCHIP_MMC5);
		m_iMulLow = 0;
		m_iMulHigh = 0;
	}

	@Override
	public void reset() {
		m_pSquare1.reset();
		m_pSquare2.reset();

		m_pSquare1.write(0x01, 0x08);
		m_pSquare2.write(0x01, 0x08);
	}

	@Override
	public void write(int address, int value) {
		if (address >= 0x5C00 && address <= 0x5FF5) {
			m_pEXRAM[address & 0x3FF] = (byte) value;
			return;
		}

		switch (address) {
			// Channel 1
			case 0x5000:
				m_pSquare1.write(0, value);
				break;
			case 0x5002:
				m_pSquare1.write(2, value);
				break;
			case 0x5003:
				m_pSquare1.write(3, value);
				break;
			// Channel 2
			case 0x5004:
				m_pSquare2.write(0, value);
				break;
			case 0x5006:
				m_pSquare2.write(2, value);
				break;
			case 0x5007:
				m_pSquare2.write(3, value);
				break;
			// Channel 3... (doesn't exist)
			// Control
			case 0x5015:
				m_pSquare1.writeControl(value & 1);
				m_pSquare2.writeControl((value >> 1) & 1);
				break;
			// Hardware multiplier
			case 0x5205:
				m_iMulLow = value;
				break;
			case 0x5206:
				m_iMulHigh = value;
				break;
		}
	}

	@Override
	public int read(int address) {
		if (address >= 0x5C00 && address <= 0x5FF5) {
			return m_pEXRAM[address & 0x3FF] & 0xFF;
		}
		
		switch (address) {
			case 0x5205:
				return (m_iMulLow * m_iMulHigh) & 0xFF;
			case 0x5206:
				return (m_iMulLow * m_iMulHigh) >> 8;
		}

		return 0;
	}

	@Override
	public boolean isMapped(int address) {
		if (address >= 0x5C00 && address <= 0x5FF5) {
			return true;
		}
		
		switch (address) {
			case 0x5205:
			case 0x5206:
				return true;
		}
		return false;
	}

	@Override
	public void endFrame() {
		m_pSquare1.endFrame();
		m_pSquare2.endFrame();
	}

	@Override
	public void process(int time) {
		m_pSquare1.process(time);
		m_pSquare2.process(time);
	}
	
	public void lengthCounterUpdate() {
		m_pSquare1.lengthCounterUpdate();
		m_pSquare2.lengthCounterUpdate();
	}
	
	public void envelopeUpdate() {
		m_pSquare1.envelopeUpdate();
		m_pSquare2.envelopeUpdate();
	}
	
	private Square	m_pSquare1;
	private Square	m_pSquare2;
	
	/**
	 * uint8 *
	 * <p>注意取数据时要转成非负数
	 */
	private byte[] m_pEXRAM;
	
	/**
	 * uint8
	 */
	private int m_iMulLow;
	
	/**
	 * uint8
	 */
	private int m_iMulHigh;

}
