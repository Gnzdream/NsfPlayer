package com.zdream.famitracker.document;

import java.util.Arrays;

import com.zdream.famitracker.FamiTrackerDoc;

import static com.zdream.famitracker.FamitrackerTypes.*;

/**
 * <p>该类存放了整个模块的 Note.<br>
 * CPatternData holds all notes in the patterns.
 * <p>Famitracker: struct stChanNote, at PatternData.h
 * @author Zdream
 */
public class PatternData {
	
	public PatternData(int patternLength, int speed, int tempo) {
		this.m_iPatternLength = patternLength;
		this.m_iFrameCount = 1;
		this.m_iSongSpeed = speed;
		this.m_iSongTempo = tempo;
		this.m_iRowHighlight1 = FamiTrackerDoc.DEFAULT_FIRST_HIGHLIGHT;
		this.m_iRowHighlight2 = FamiTrackerDoc.DEFAULT_SECOND_HIGHLIGHT;
	}

	public final int getNote(int channel, int pattern, int row) { 
		StChanNote pNote = getPatternData0(channel, pattern, row);
		return pNote == null ? 0 : pNote.note; 
	};

	public final int getOctave(int channel, int pattern, int row) { 
		StChanNote pNote = getPatternData0(channel, pattern, row);
		return pNote == null ? 0 : pNote.octave; 
	};

	public final int getInstrument(int channel, int pattern, int row) { 
		StChanNote pNote = getPatternData0(channel, pattern, row);
		return pNote == null ? 0 : pNote.instrument; 
	};

	public final int getVolume(int channel, int pattern, int row) { 
		StChanNote pNote = getPatternData0(channel, pattern, row);
		return pNote == null ? 0 : pNote.vol;
	};

	public final int getEffect(int channel, int pattern, int row, int column) { 
		StChanNote pNote = getPatternData0(channel, pattern, row);
		return pNote == null ? 0 : pNote.effNumber[column];
	};

	public final int getEffectParam(int channel, int pattern, int row, int column) { 
		StChanNote pNote = getPatternData0(channel, pattern, row);
		return pNote == null ? 0 : pNote.effParam[column]; 
	};

	public final boolean isCellFree(int channel, int pattern, int row) {
		StChanNote pNote = getPatternData0(channel, pattern, row);

		if (pNote == null)
			return true;

		boolean isFree = pNote.note == NOTE_NONE && 
			pNote.effNumber[0] == 0 && pNote.effNumber[1] == 0 && 
			pNote.effNumber[2] == 0 && pNote.effNumber[3] == 0 && 
			pNote.vol == MAX_VOLUME && pNote.instrument == MAX_INSTRUMENTS;

		return isFree;
	}
	
	public final boolean isPatternEmpty(int channel, int pattern) {
		// Unallocated pattern means empty
		if (m_pPatternData[channel][pattern] == null)
			return true;

		// Check if allocated pattern is empty
		for (int i = 0; i < m_iPatternLength; ++i) {
			if (!isCellFree(channel, pattern, i))
				return false;
		}

		return true;
	}
	
	public final boolean isPatternInUse(int channel, int pattern) {
		// Check if pattern is addressed in frame list
		for (int i = 0; i < m_iFrameCount; ++i) {
			if (m_iFrameList[i][channel] == pattern)
				return true;
		}

		return false;
	}

	public final int getEffectColumnCount(int channel) { 
		return m_iEffectColumns[channel]; 
	};

	public void setEffectColumnCount(int channel, int count) { 
		m_iEffectColumns[channel] = count; 
	};

	public void clearEverything() {
		// Release all patterns and clear frame list

		// Frame list
		for (int i = 0; i < m_iFrameList.length; i++) {
			int[] a = m_iFrameList[i];
			Arrays.fill(a, 0);
		}
		
		m_iFrameCount = 1;
		
		// Patterns, deallocate everything
		for (int i = 0; i < MAX_CHANNELS; ++i) {
			for (int j = 0; j < MAX_PATTERN; ++j) {
				clearPattern(i, j);
			}
		}
	}
	
	public void clearPattern(int channel, int pattern) {
		// Deletes a specified pattern in a channel
		if (m_pPatternData[channel][pattern] == null) {
			m_pPatternData[channel][pattern] = null;
		}
	}

	public StChanNote getPatternData(int channel, int pattern, int row) {
		// Allocate pattern if accessed for the first time
		if (m_pPatternData[channel][pattern] == null)
			allocatePattern(channel, pattern);

		return m_pPatternData[channel][pattern][row];
	}

	public final int getPatternLength() {
		return m_iPatternLength;
	};

	/**
	 * @return {@link #m_iFrameCount}
	 */
	public final int getFrameCount() {
		return m_iFrameCount;
	};

	public final int getSongSpeed() {
		return m_iSongSpeed;
	};

	public final int getSongTempo() {
		return m_iSongTempo;
	};

	public void setPatternLength(int length) {
		m_iPatternLength = length;
	};

	public void setFrameCount(int count) {
		m_iFrameCount = count;
	};

	public void setSongSpeed(int speed) {
		m_iSongSpeed = speed;
	};

	public void setSongTempo(int tempo) {
		m_iSongTempo = tempo;
	};

	public final int getFramePattern(int frame, int channel) {
		return m_iFrameList[frame][channel];
	}
	
	public void setFramePattern(int frame, int channel, int pattern) {
		m_iFrameList[frame][channel] = pattern;
	}

	public void setHighlight(int first, int second) {
		m_iRowHighlight1 = first;
		m_iRowHighlight2 = second;
	}
	
	public final int getFirstRowHighlight() {
		return m_iRowHighlight1;
	}
	
	public final int getSecondRowHighlight() {
		return m_iRowHighlight2;
	}

	private final StChanNote getPatternData0(int channel, int pattern, int row) {
		// Private method, may return NULL
		if (m_pPatternData[channel][pattern] == null)
			allocatePattern(channel, pattern);

		return m_pPatternData[channel][pattern][row];
	}
	
	private void allocatePattern(int channel, int pattern) {
		// Allocate memory
		StChanNote[] ss =
		m_pPatternData[channel][pattern] = new StChanNote[MAX_PATTERN_LENGTH];
		
		for (int i = 0; i < MAX_PATTERN_LENGTH; i++) {
			ss[i] = new StChanNote();
		}

		// Clear memory
		for (int i = 0; i < MAX_PATTERN_LENGTH; ++i) {
			StChanNote pNote = m_pPatternData[channel][pattern][i];
			pNote.note = 0;
			pNote.octave = 0;
			pNote.instrument = MAX_INSTRUMENTS;
			pNote.vol = MAX_VOLUME;

			for (int n = 0; n < MAX_EFFECT_COLUMNS; ++n) {
				pNote.effNumber[n] = 0;
				pNote.effParam[n] = 0;
			}
		}
	}

	// Pattern data
	// ?

	// Track parameters
		
	/**
	 * Amount of rows in one pattern
	 */
	private int m_iPatternLength;
	
	/**
	 * Number of frames
	 */
	private int m_iFrameCount;
	
	/**
	 * Song speed
	 */
	private int m_iSongSpeed;
	
	/**
	 * Song tempo
	 */
	private int m_iSongTempo;

	// 其它选项
	/**
	 * Row highlight settings
	 */
	private int m_iRowHighlight1, m_iRowHighlight2;

	/**
	 * Number of visible effect columns for each channel
	 */
	private int[] m_iEffectColumns = new int[MAX_CHANNELS];

	/**
	 * List of the patterns assigned to frames
	 */
	private int[][] m_iFrameList = new int[MAX_FRAMES][MAX_CHANNELS];		

	/**
	 * All accesses to m_pPatternData must go through GetPatternData()
	 */
	private StChanNote[][][] m_pPatternData = new StChanNote[MAX_CHANNELS][MAX_PATTERN][];
}
