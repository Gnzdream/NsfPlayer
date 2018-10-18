package com.zdream.famitracker.sound.emulation;

import com.zdream.famitracker.sound.SampleMem;

import static com.zdream.famitracker.sound.emulation.Types.*;

public class DPCM extends Channel {
	
	public static final short[] DMC_PERIODS_NTSC = {
			428, 380, 340, 320, 286, 254, 226, 214, 190, 160, 142, 128, 106, 84, 72, 54
	};
	public static final short[] DMC_PERIODS_PAL = {
			398, 354, 316, 298, 276, 236, 210, 198, 176, 148, 132, 118, 98, 78, 66, 50
	};

	public short[] PERIOD_TABLE = DMC_PERIODS_NTSC;

	public DPCM(Mixer pMixer, SampleMem pSampleMem, byte id) {
		super(pMixer, id, SNDCHIP_NONE);
		this.m_pSampleMem = pSampleMem;
	}
	
	public void reset() {
		m_iCounter = m_iPeriod = DMC_PERIODS_NTSC[0];

		m_iBitDivider = m_iShiftReg = 0;
		m_iDMA_LoadReg = 0;
		m_iDMA_LengthReg = 0;
		m_iDMA_Address = 0;
		m_iDMA_BytesRemaining = 0;
		
		m_bTriggeredIRQ = m_bSampleFilled = false;

		// loaded with 0 on power-up.
		m_iDeltaCounter = 0;

		endFrame();
	}
	
	public void write(int address, int value) {
		value &= 0xFF;
		switch (address) {
		case 0x00:
			m_iPlayMode = value & 0xC0;
			m_iPeriod = PERIOD_TABLE[value & 0x0F];
			if ((value & 0x80) == 0x00) 
				m_bTriggeredIRQ = false;
			break;
		case 0x01:
			m_iDeltaCounter = value & 0x7F;
			mix(m_iDeltaCounter);
			break;
		case 0x02:
			m_iDMA_LoadReg = value;
			break;
		case 0x03:
			m_iDMA_LengthReg = value;
			break;
		}
	}
	
	/**
	 * APU.write4015 调用
	 * @param value
	 */
	public void writeControl(int value) {
		if ((value & 1) == 1) {
			if (m_iDMA_BytesRemaining == 0)
				reload();
		} else {
			m_iDMA_BytesRemaining = 0;
		}

		m_bTriggeredIRQ = false;
	}
	
	@SuppressWarnings("unused")
	private final int readControl() {
		return (m_iDMA_BytesRemaining > 0) ? 1 : 0;
	}
	
	@SuppressWarnings("unused")
	private final int didIRQ() {
		return m_bTriggeredIRQ ? 1 : 0;
	}
	
	public void process(int time) {
		while (time >= m_iCounter) {
			time	  -= m_iCounter;
			m_iTime	  += m_iCounter;
			m_iCounter = m_iPeriod;

			// DMA reader
			// Check if a new byte should be fetched
			if (!m_bSampleFilled && (m_iDMA_BytesRemaining > 0)) {

				m_iSampleBuffer = m_pSampleMem.read(m_iDMA_Address | 0x8000)
					& 0xFF; // 转成非负整数
				m_iDMA_Address = (m_iDMA_Address + 1) & 0xFFFF; // 转成非负整数
				--m_iDMA_BytesRemaining;
				m_bSampleFilled = true;

				if (m_iDMA_BytesRemaining == 0) {
					switch (m_iPlayMode) {
						case 0x00:	// Stop
							break;
						case 0x40:	// Loop
						case 0xC0:
							reload();
							break;
						case 0x80:	// Stop and do IRQ (not when an NSF is playing)
							m_bTriggeredIRQ = true;
							break;
					}
				}
			}

			// Output unit
			if (m_iBitDivider == 0) {
				// Begin new output cycle
				m_iBitDivider = 8;
				if (m_bSampleFilled) {
					m_iShiftReg		= m_iSampleBuffer;
					m_bSampleFilled = false;
					m_bSilenceFlag	= false;
				}
				else {
					m_bSilenceFlag = true;
				}
			}

			if (!m_bSilenceFlag) {
				if ((m_iShiftReg & 1) == 1) {
					if (m_iDeltaCounter < 126)
						m_iDeltaCounter += 2;
				}
				else {
					if (m_iDeltaCounter > 1)
						m_iDeltaCounter -= 2;
				}
			}

			m_iShiftReg >>= 1;
			--m_iBitDivider;

			mix(m_iDeltaCounter);
		}

		m_iCounter -= time;
		m_iTime += time;
	}
	
	private void reload() {
		m_iDMA_Address = (m_iDMA_LoadReg << 6) | 0x4000;
	    m_iDMA_BytesRemaining = (m_iDMA_LengthReg << 4) + 1;
	}

	/**
	 * 未调用
	 * @return
	 */
	public final int getSamplePos() {
		return (m_iDMA_Address - (m_iDMA_LoadReg << 6 | 0x4000)) >> 6;
	};

	/**
	 * 未调用
	 * @return
	 */
	public final int getDeltaCounter() {
		return m_iDeltaCounter;
	};

	/**
	 * 未调用
	 * @return
	 */
	public boolean isPlaying() {
		return (m_iDMA_BytesRemaining > 0);
	};

	/*
	 * 下面的 9 类数据全是 unsigned
	 */

	/**
	 * 当前读取的 byte 中, 剩余还未读取的位数. [0, 8]
	 */
	private int m_iBitDivider;
	private int m_iShiftReg;
	
	/**
	 * 指示是循环播放还是只播放一次
	 */
	private int m_iPlayMode;
	
	/**
	 * 很可能与音量有关.
	 * 看起来是音像的位置
	 */
	private int m_iDeltaCounter;
	/**
	 * 当前取的 byte 的数值.
	 * 因为在解析时, 需要一位一位地解析, 因此用它来缓存.
	 */
	private int m_iSampleBuffer;

	/**
	 * 起始读取位 offset, 单位 64 bytes
	 */
	private int m_iDMA_LoadReg;
	
	/**
	 * 采样长度, 单位 16 bytes
	 */
	private int m_iDMA_LengthReg;
	/**
	 * 虚拟的读取位置. 用于储存现在读取的位置在 m_pSampleMem 的哪个地址号码上
	 */
	private int m_iDMA_Address;
	/**
	 * 剩余的 byte 个数
	 */
	private int m_iDMA_BytesRemaining;

	/**
	 * 没有实际作用
	 */
	private boolean m_bTriggeredIRQ;
	private boolean m_bSampleFilled;
	private boolean m_bSilenceFlag;

	// Needed by FamiTracker 
	private SampleMem m_pSampleMem;

}
