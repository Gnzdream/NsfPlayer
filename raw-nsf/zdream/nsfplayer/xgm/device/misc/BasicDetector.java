package zdream.nsfplayer.xgm.device.misc;

import zdream.nsfplayer.nsf.device.cpu.IntHolder;

public class BasicDetector implements ILoopDetector {
	
	protected int m_bufmask;
	
	/**
	 * <p>用于记录最近在内存中写入的数据以及位置
	 * </p>
	 */
	private int[] m_stream_buf, m_time_buf;
	private int m_bidx;
	
	protected int m_blast;
	protected int m_wspeed;
	protected int m_current_time;
	protected int m_loop_start;
	protected int m_loop_end;
	/**
	 * 表示整个 Nes 内存在重置之后, 是否写入过数据.
	 */
	private boolean m_empty;
	
	public BasicDetector(int bufbits) {
		int m_bufsize = 1 << bufbits;
		m_bufmask = m_bufsize - 1;
		m_stream_buf = new int[m_bufsize];
		m_time_buf = new int[m_bufsize];
	}

	@Override
	public void reset() {
		int i;

		int m_bufsize = m_stream_buf.length;
		for (i = 0; i < m_bufsize; i++) {
			m_stream_buf[i] = -i;
			m_time_buf[i] = 0;
		}

		m_current_time = 0;
		m_wspeed = 0;

		m_bidx = 0;
		m_blast = 0;
		m_loop_start = -1;
		m_loop_end = -1;
		m_empty = true;
	}

	@Override
	public boolean write(int addr, int value, int id) {
		m_empty = false;
		m_time_buf[m_bidx] = m_current_time;
		m_stream_buf[m_bidx] = ((addr & 0xffff) << 8) | (value & 0xff);
		m_bidx = (m_bidx + 1) & m_bufmask;
		return false;
	}

	@Override
	public boolean read(int addr, IntHolder val, int id) {
		return false;
	}

	@Override
	public void setOption(int id, int value) {}

	@Override
	public boolean isLooped(int timeInMs, int matchSecond, int matchInterval) {
		int i, j;
		int match_size, match_length;
		int m_bufsize = this.m_stream_buf.length;

		if (timeInMs - m_current_time < matchInterval)
			return false;

		m_current_time = timeInMs;

		if (m_bidx <= m_blast)
			return false;
		if (m_wspeed != 0)
			m_wspeed = (m_wspeed + m_bidx - m_blast) / 2;
		else
			m_wspeed = m_bidx - m_blast; // 第一次
		m_blast = m_bidx;

		match_size = m_wspeed * matchSecond / matchInterval;
		match_length = m_bufsize - match_size;

		if (match_length < 0)
			return false;

		for (i = 0; i < match_length; i++) {
			for (j = 0; j < match_size; j++) {
				if (m_stream_buf[(m_bidx + j + match_length) & m_bufmask] != m_stream_buf[(m_bidx + i + j) & m_bufmask])
					break;
			}
			if (j == match_size) {
				m_loop_start = m_time_buf[(m_bidx + i) & m_bufmask];
				m_loop_end = m_time_buf[(m_bidx + match_length) & m_bufmask];
				return true;
			}
		}
		return false;
	}

	@Override
	public int getLoopStart() {
		return m_loop_start;
	}

	@Override
	public int getLoopEnd() {
		return m_loop_end;
	}

	@Override
	public boolean isEmpty() {
		return m_empty;
	}

}
