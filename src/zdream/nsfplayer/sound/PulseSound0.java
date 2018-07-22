package zdream.nsfplayer.sound;

/**
 * 矩形波发声器（旧）
 * @author Zdream
 * @date 2017-12-06
 */
public class PulseSound0 extends AbstractNsfSound0 {
	
	static final boolean[][] DUTY_TABLE = {
			{ false, false,  true,  true, false, false, false, false, false, false, false, false, false, false, false, false },
			{ false, false,  true,  true,  true,  true, false, false, false, false, false, false, false, false, false, false },
			{ false, false,  true,  true,  true,  true,  true,  true,  true,  true, false, false, false, false, false, false },
			{  true,  true, false, false, false, false,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true }
	};
	
	private static final int[] LENGTH_TABLE = { // len : 32
			0x0A, 0xFE,
			0x14, 0x02,
			0x28, 0x04,
			0x50, 0x06,
			0xA0, 0x08,
			0x3C, 0x0A,
			0x0E, 0x0C,
			0x1A, 0x0E,
			0x0C, 0x10,
			0x18, 0x12,
			0x30, 0x14,
			0x60, 0x16,
			0xC0, 0x18,
			0x48, 0x1A,
			0x10, 0x1C,
			0x20, 0x1E
		};
	
	public void reset() {
		m_iEnabled = 0;
		m_iControlReg = false;
		m_iCounter = 0;

		m_iSweepCounter = 1;
		m_iSweepPeriod = 1;

		m_iEnvelopeCounter = 1;
		m_iEnvelopeSpeed = 1;

		write(0, 0);
		write(1, 0);
		write(2, 0);
		write(3, 0);

		sweepUpdate(0);

		endFrame();
	}
	
	public void write(int address, int value) {
		switch (address) {
		case 0x00:
			m_iDutyLength = value >> 6;
			m_iFixedVolume = value & 0x0F;
			m_iLooping = value & 0x20;
			m_iEnvelopeFix = value & 0x10;
			m_iEnvelopeSpeed = (value & 0x0F) + 1;
			break;
		case 0x01:
			m_iSweepEnabled = value & 0x80;
			m_iSweepPeriod = ((value >> 4) & 0x07) + 1;
			m_iSweepMode = value & 0x08;		
			m_iSweepShift = value & 0x07;
			m_bSweepWritten = true;
			break;
		case 0x02:
			period = value | (period & 0x0700);
			break;
		case 0x03:
			period = ((value & 0x07) << 8) | (period & 0xFF);
			m_iLengthCounter = (short) (LENGTH_TABLE[(value & 0xF8) >> 3] & 0xFF);
			m_iDutyCycle = 0;
			m_iEnvelopeVolume = 0x0F;
			if (m_iControlReg)
				m_iEnabled = 1;
			break;
		}
	}
	
	public void writeControl(boolean value) {
		m_iControlReg = value;

		if (!m_iControlReg)
			m_iEnabled = 0;
	}
	
	public int readControl() {
		return ((m_iLengthCounter > 0) && (m_iEnabled == 1)) ? 1 : 0;
	}
	
	public void process(int time) {
		if (period == 0) {
			mclock += time;
			return;
		}

		boolean valid = (period > 7) && (m_iEnabled != 0) && (m_iLengthCounter > 0) && (m_iSweepResult < 0x800);

		while (time >= m_iCounter) {
			time		-= m_iCounter;
			mclock		+= m_iCounter;
			m_iCounter	 = period + 1;
			int volume = m_iEnvelopeFix != 0 ? m_iFixedVolume : m_iEnvelopeVolume;
			mix (valid && DUTY_TABLE[m_iDutyLength][m_iDutyCycle] ? volume : 0);
			m_iDutyCycle = (m_iDutyCycle + 1) & 0x0F;
		}

		m_iCounter -= time;
		mclock += time;
	}

	public void lengthCounterUpdate() {
		if ((m_iLooping == 0) && (m_iLengthCounter > 0)) 
			--m_iLengthCounter;
	}
	
	public void sweepUpdate(int diff) {
		m_iSweepResult = (period >> m_iSweepShift);

		if (m_iSweepMode != 0)
			m_iSweepResult = period - m_iSweepResult - diff;
		else
			m_iSweepResult = period + m_iSweepResult;

		if (--m_iSweepCounter == 0) {
			m_iSweepCounter = m_iSweepPeriod;
			if (m_iSweepEnabled != 0 && (period > 0x07) && (m_iSweepResult < 0x800) && (m_iSweepShift > 0))
				period = m_iSweepResult;
		}

		if (m_bSweepWritten) {
			m_bSweepWritten = false;
			m_iSweepCounter = m_iSweepPeriod;
		}
	}
	
	public void envelopeUpdate() {
		if (--m_iEnvelopeCounter == 0) {
			m_iEnvelopeCounter = m_iEnvelopeSpeed;
			if (m_iEnvelopeFix == 0) {
				if (m_iLooping != 0)
					m_iEnvelopeVolume = (m_iEnvelopeVolume - 1) & 0x0F;
				else if (m_iEnvelopeVolume > 0)
					m_iEnvelopeVolume--;
			}
		}
	}
	
	/**
	 * unsigned
	 */
	int m_iDutyLength, m_iDutyCycle;

	/**
	 * unsigned
	 */
	int m_iLooping, m_iEnvelopeFix, m_iEnvelopeSpeed;

	/**
	 * unsigned
	 */
	int m_iEnvelopeVolume, m_iFixedVolume;
	int m_iEnvelopeCounter;

	/**
	 * unsigned
	 */
	int m_iSweepEnabled, m_iSweepPeriod, m_iSweepMode, m_iSweepShift;
	int m_iSweepCounter, m_iSweepResult;
	boolean m_bSweepWritten;
	
	@Override
	public String name() {
		return "Pulse";
	}
	
}
