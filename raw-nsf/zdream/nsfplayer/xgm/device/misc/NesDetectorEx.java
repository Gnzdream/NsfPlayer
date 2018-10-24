package zdream.nsfplayer.xgm.device.misc;

import zdream.nsfplayer.nsf.device.cpu.IntHolder;

public class NesDetectorEx extends NesDetector {
	
	/**
	 * 音频轨道
	 */
	public static final int
			SQR_0 = 0, // 矩形波 1
			SQR_1 = 1, // 矩形波 2
			TRI = 2, // 三角波
			NOIZ = 3, // 噪音
			DPCM = 4,
		    N106_0 = 5,
		    N106_1 = 6,
		    N106_2 = 7,
		    N106_3 = 8,
		    N106_4 = 9,
		    N106_5 = 10,
		    N106_6 = 11,
		    N106_7 = 12,
		    MAX_CH = 13;
	
	static final int maskAPU[] = new int[] {
			0xff, 0xff, 0xff, 0xff,
			0xff, 0xff, 0xff, 0xff,
			0xff, 0x00, 0xff, 0xff,
			0x3f, 0x00, 0x8f, 0xf8 };
	
	protected BasicDetector[] m_LD = new BasicDetector[MAX_CH];
	protected boolean[] m_looped = new boolean[MAX_CH];
	protected int m_n106_addr; // unsigned
	protected int m_loop_start, m_loop_end;
	
	public NesDetectorEx() {
		int[] bufsize_table = new int[]{
				15, 15, 15, 15, 15, // SQR0, SQR1, TRI, NOIZ, DPCM
				14, 14, 14, 14, // N106[0-3]
				14, 14, 14, 14 // N106[4-7]
		};
		for (int i = 0; i < MAX_CH; i++)
			m_LD[i] = new BasicDetector(bufsize_table[i]);
	}

	@Override
	public void reset() {
		for (int i = 0; i < MAX_CH; i++) {
			m_LD[i].reset();
			m_looped[i] = false;
		}
	}
	
	@Override
	public boolean isLooped(int timeInMs, int matchSecond, int matchInterval) {
		boolean all_empty = true, all_looped = true;
		for (int i = 0; i < MAX_CH; i++) {
			if (!m_looped[i]) {
				m_looped[i] = m_LD[i].isLooped(timeInMs, matchSecond, matchInterval);
				if (m_looped[i]) {
					m_loop_start = m_LD[i].getLoopStart();
					m_loop_end = m_LD[i].getLoopEnd();
				}
			}
			all_looped &= m_looped[i] | m_LD[i].isEmpty();
			all_empty &= m_LD[i].isEmpty();
		}

		return !all_empty & all_looped;
	}

	@Override
	public boolean write(int adr, int val, int id) {
		if (0x4000 <= adr && adr < 0x4004)
			m_LD[SQR_0].write(adr, val & maskAPU[adr - 0x4000], 0);
		else if (0x4004 <= adr && adr < 0x4008)
			m_LD[SQR_1].write(adr, val & maskAPU[adr - 0x4000], 0);
		else if (0x4008 <= adr && adr < 0x400C)
			m_LD[TRI].write(adr, val & maskAPU[adr - 0x4000], 0);
		else if (0x400C <= adr && adr < 0x4010)
			m_LD[NOIZ].write(adr, val & maskAPU[adr - 0x4000], 0);
		else if (adr == 0x4012 || adr == 0x4013)
			m_LD[DPCM].write(adr, val, 0);
		else if (0xF800 == adr)
			m_n106_addr = val;
		else if (0x4800 == adr) {
			if (0x40 <= m_n106_addr) {
				m_LD[N106_0 + ((m_n106_addr >> 3) & 7)].write(m_n106_addr, val, 0);

			}
			if ((m_n106_addr & 0x80) != 0)
				m_n106_addr++;
		}
		return false;
	}

	@Override
	public boolean read(int addr, IntHolder val, int id) {
		return false;
	}

	@Override
	public void setOption(int id, int value) {}

	@Override
	public final int getLoopStart() {
		return m_loop_start;
	}

	@Override
	public final int getLoopEnd() {
		return m_loop_end;
	}

	@Override
	public final boolean isEmpty() {
		boolean ret = true;
		for (int i = 0; i < MAX_CH; i++)
			ret &= m_LD[i].isEmpty();
		return ret;
	}

}
