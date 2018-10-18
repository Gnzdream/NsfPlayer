package com.zdream.famitracker.document;

import java.util.Arrays;

import static com.zdream.famitracker.FamitrackerTypes.*;

/**
 * <p>该类用于存放乐器序列
 * <p>This class is used to store instrument sequences
 * @author Zdream
 */
public class Sequence implements ISequence {
	
	/**
	 * 设置项
	 */
	public static final int
			ARP_SETTING_ABSOLUTE = 0,
			ARP_SETTING_FIXED = 1,
			ARP_SETTING_RELATIVE = 2;
	
	/**
	 * Sunsoft modes
	 */
	public static final int
			S5B_MODE_SQUARE = 64,
			S5B_MODE_NOISE = 128;
	
	public Sequence() {
		clear();
	}

	public	void clear() {
		m_iItemCount = 0;
		m_iLoopPoint = -1;
		m_iReleasePoint = -1;
		m_iSetting = 0;

		Arrays.fill(this.m_cValues, (byte) 0);

		m_iPlaying = -1;
	}
	
	public final int getReleasePoint() {
		return m_iReleasePoint;
	}
	
	public final int getSetting() {
		return m_iSetting;
	}
	
	public	void setItem(int index, byte value) {
		assert(index <= MAX_SEQUENCE_ITEMS);
		m_cValues[index] = value;
	}
	
	public	void setItemCount(int count) {
		assert(count <= MAX_SEQUENCE_ITEMS);
		
		m_iItemCount = count;

		if (m_iLoopPoint > m_iItemCount)
			m_iLoopPoint = -1;
		if (m_iReleasePoint > m_iItemCount)
			m_iReleasePoint = -1;
	}
	
	public	void setLoopPoint(int point) {
		m_iLoopPoint = point;
		// Loop point cannot be beyond release point (at the moment)
		if (m_iLoopPoint >= m_iReleasePoint && m_iReleasePoint != -1)
			m_iLoopPoint = -1;
	}
	
	public	void setReleasePoint(int point) {
		m_iReleasePoint = point;
		// Loop point cannot be beyond release point (at the moment)
		if (m_iLoopPoint >= m_iReleasePoint && m_iReleasePoint != -1)
			m_iLoopPoint = -1;
	}
	
	public	void setSetting(int setting) {
		m_iSetting = setting;
	}
	
	public	void copy(final Sequence pSeq) {
		// Copy all values from pSeq
		m_iItemCount = pSeq.m_iItemCount;
		m_iLoopPoint = pSeq.m_iLoopPoint;
		m_iReleasePoint = pSeq.m_iReleasePoint;
		m_iSetting = pSeq.m_iSetting;

		System.arraycopy(pSeq.m_cValues, 0, m_cValues, 0, MAX_SEQUENCE_ITEMS);
	}

	@Override
	public final int getItem(int index) {
		assert(index <= MAX_SEQUENCE_ITEMS);
		return m_cValues[index];
	}

	@Override
	public final int getItemCount() {
		return m_iItemCount;
	}

	@Override
	public final int getLoopPoint() {
		return m_iLoopPoint;
	}

	// Sequence data
	private int m_iItemCount;
	private int m_iLoopPoint;
	private int m_iReleasePoint;
	private int m_iSetting;
	private byte[] m_cValues = new byte[MAX_SEQUENCE_ITEMS];
	
	@SuppressWarnings("unused")
	private int m_iPlaying;

}
