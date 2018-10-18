package com.zdream.famitracker.sound;

import com.zdream.famitracker.FamiTrackerDoc;
import com.zdream.famitracker.document.StChanNote;

import static com.zdream.famitracker.sound.emulation.Types.*;
import static com.zdream.famitracker.document.instrument.Instrument.*;

public class TrackerChannel {
	
	public static final int
		NOTE_PRIO_0 = 0,
		NOTE_PRIO_1 = 1,
		NOTE_PRIO_2 = 2;
	
	public TrackerChannel(String name, final byte chip, final byte id) {
		m_pChannelName = name;
		m_iChip = chip;
		m_iChannelID = id;
	}
	
	public final int getColumnCount() {
		return m_iColumnCount;
	}
	
	public void setColumnCount(int count) {
		m_iColumnCount = count;
	}

	public StChanNote getNote() {
		m_bNewNote = false;
		m_iNotePriority = NOTE_PRIO_0;
		
		return m_Note;
	}
	
	public void setNote(StChanNote note, int priority) {
		if (priority >= m_iNotePriority) {
			m_Note = note;
			m_bNewNote = true;
			m_iNotePriority = priority;
		}
	}
	
	/**
	 * Getter {@link #m_bNewNote}
	 * @return
	 */
	public final boolean newNoteData() {
		return m_bNewNote;
	}
	public void reset() {
		m_bNewNote = false;
		m_iVolumeMeter = 0;
		m_iNotePriority = NOTE_PRIO_0;
	}

	public void setVolumeMeter(int value) {
		m_iVolumeMeter = value;
	}
	public final int getVolumeMeter() {
		return m_iVolumeMeter;
	}

	/**
	 * 暂时确定, 在 2A03 和 VRC6 情况下, 没有调用该方法的地方.
	 * @param pitch
	 */
	public void setPitch(int pitch) {
		m_iPitch = pitch;
	}
	public int getPitch() {
		return m_iPitch;
	}

	public final boolean isInstrumentCompatible(int instrument, FamiTrackerDoc pDoc) {
		int InstType = pDoc.getInstrumentType(instrument);

		switch (m_iChip) {
			case SNDCHIP_NONE:
			case SNDCHIP_MMC5:
				return InstType == INST_2A03;
			case SNDCHIP_N163:
				return InstType == INST_N163;
			case SNDCHIP_S5B:
				return InstType == INST_S5B;
			case SNDCHIP_VRC6:
				return InstType == INST_VRC6;
			case SNDCHIP_VRC7:
				return InstType == INST_VRC7;
			case SNDCHIP_FDS:
				return InstType == INST_FDS;
		}

		return false;
	}

	public final String m_pChannelName;
	public final byte m_iChip;
	public final byte m_iChannelID;
	int m_iColumnCount;

	StChanNote m_Note;
	boolean m_bNewNote;
	int m_iNotePriority;

	int m_iVolumeMeter;
	int m_iPitch;
	
}
