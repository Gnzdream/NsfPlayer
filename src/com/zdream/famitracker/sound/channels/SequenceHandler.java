package com.zdream.famitracker.sound.channels;

import static com.zdream.famitracker.FamitrackerTypes.*;

import com.zdream.famitracker.FamiTrackerApp;
import com.zdream.famitracker.document.Sequence;

/**
 * Sequence handler class
 * @author Zdream
 */
public abstract class SequenceHandler {
	
	public static final int
			SEQ_STATE_DISABLED = 0,
			SEQ_STATE_RUNNING = 1,
			SEQ_STATE_END = 2,
			SEQ_STATE_HALT = 3;

	protected SequenceHandler() {
		clearSequences();
	}
	
	protected abstract int triggerNote(int note);
	protected abstract void setVolume(int volume);
	
	/**
	 * 设置波长
	 * @param period
	 *   波长（可能是相对值）
	 */
	protected abstract void setPeriod(int period);
	/**
	 * @return
	 *   获取波长
	 */
	protected abstract int getPeriod();
	protected abstract void setNote(int note);
	protected abstract int getNote();
	protected abstract void setDutyPeriod(int period);
	protected abstract boolean isActive();
	protected abstract boolean isReleasing();

	// Sequence functions
	protected void setupSequence(int index, final Sequence pSequence) {
		m_iSeqState[index] = SEQ_STATE_RUNNING;
		m_iSeqPointer[index] = 0;
		m_pSequence[index] = pSequence;
	}
	protected void clearSequence(int index) {
		m_iSeqState[index] = SEQ_STATE_DISABLED;
		m_iSeqPointer[index] = 0;
		m_pSequence[index] = null;
	}
	protected void runSequence(int index) {
		final Sequence pSequence = m_pSequence[index];

		if (pSequence == null || pSequence.getItemCount() == 0 || !isActive())
			return;

		switch (m_iSeqState[index]) {
			case SEQ_STATE_RUNNING:
				updateSequenceRunning(index, pSequence);
				break;
			case SEQ_STATE_END:
				updateSequenceEnd(index, pSequence);
				break;
			case SEQ_STATE_DISABLED:
			case SEQ_STATE_HALT:
				// Do nothing
				break;
		}
	}
	protected void clearSequences() {
		for (int i = 0; i < SEQ_COUNT; ++i) {
			m_iSeqState[i] = SEQ_STATE_DISABLED;
			m_iSeqPointer[i] = 0;
			m_pSequence[i] = null;
		}
	}
	protected void releaseSequences() {
		for (int i = 0; i < SEQ_COUNT; ++i) {
			if (m_iSeqState[i] == SEQ_STATE_RUNNING || m_iSeqState[i] == SEQ_STATE_END) {
				releaseSequence(i, m_pSequence[i]);
			}
		}
	}
	/**
	 * const 方法
	 * @param index
	 * @param pSequence
	 * @return
	 */
	protected final boolean isSequenceEqual(int index, final Sequence pSequence) {
		return pSequence == m_pSequence[index];
	}
	/**
	 * const 方法
	 * @param index
	 * @return
	 */
	protected final int getSequenceState(int index) {
		return m_iSeqState[index];
	}

	private void updateSequenceRunning(int index, final Sequence pSequence) {
		int value = pSequence.getItem(m_iSeqPointer[index]);

		switch (index) {
			// Volume modifier
			case SEQ_VOLUME:
				setVolume(value);
				break;
			// Arpeggiator
			case SEQ_ARPEGGIO:
				switch (pSequence.getSetting()) {
					case Sequence.ARP_SETTING_ABSOLUTE:
						setPeriod(triggerNote(getNote() + value));
						break;
					case Sequence.ARP_SETTING_FIXED:
						setPeriod(triggerNote(value));
						break;
					case Sequence.ARP_SETTING_RELATIVE:
						setNote(getNote() + value);
						setPeriod(triggerNote(getNote()));
						break;
				}
				break;
			// Pitch
			case SEQ_PITCH:
				setPeriod(getPeriod() + value);
				break;
			// Hi-pitch
			case SEQ_HIPITCH:
				setPeriod(getPeriod() + (value << 4));
				break;
			// Duty cycling
			case SEQ_DUTYCYCLE:
				setDutyPeriod(value);
				break;
		}

		++m_iSeqPointer[index];

		int release = pSequence.getReleasePoint();
		int items = pSequence.getItemCount();
		int loop = pSequence.getLoopPoint();

		if (m_iSeqPointer[index] == (release + 1) || m_iSeqPointer[index] >= items) {
			// End point reached
			if (loop != -1 && !(isReleasing() && release != -1)) {
				m_iSeqPointer[index] = loop;
			} else {
				if (m_iSeqPointer[index] >= items) {
					// End of sequence 
					m_iSeqState[index] = SEQ_STATE_END;
				} else if (!isReleasing()) {
					// Waiting for release
					--m_iSeqPointer[index];
				}
			}
		}

		FamiTrackerApp.getInstance().getSoundGenerator().setSequencePlayPos(pSequence, m_iSeqPointer[index]);
	}
	private void updateSequenceEnd(int index, final Sequence pSequence) {
		switch (index) {
		case SEQ_ARPEGGIO:
			if (pSequence.getSetting() == Sequence.ARP_SETTING_FIXED) {
				setPeriod(triggerNote(getNote()));
			}
			break;
		}
	
		m_iSeqState[index] = SEQ_STATE_HALT;
	
		FamiTrackerApp.getInstance().getSoundGenerator().setSequencePlayPos(pSequence, -1);
	}
	private void releaseSequence(int index, final Sequence pSeq) {
		int releasePoint = pSeq.getReleasePoint();

		if (releasePoint != -1) {
			m_iSeqPointer[index] = releasePoint;
			m_iSeqState[index] = SEQ_STATE_RUNNING;
		}
	}

	// Sequence variables
	private Sequence[] m_pSequence = new Sequence[SEQ_COUNT];
	private int[] m_iSeqState = new int[SEQ_COUNT];
	private int[] m_iSeqPointer = new int[SEQ_COUNT];

}
