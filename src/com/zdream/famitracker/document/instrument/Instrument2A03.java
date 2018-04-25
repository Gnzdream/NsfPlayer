package com.zdream.famitracker.document.instrument;

import static com.zdream.famitracker.FamitrackerTypes.*;

import java.util.Arrays;

import com.zdream.famitracker.FamiTrackerApp;
import com.zdream.famitracker.FamiTrackerDoc;
import com.zdream.famitracker.components.DocumentFile;
import com.zdream.famitracker.sound.emulation.Types;

public class Instrument2A03 extends Instrument {

	public Instrument2A03() {
		super(INST_2A03);

		for (int i = 0; i < OCTAVE_RANGE; ++i) {
			for (int j = 0; j < 12; ++j) {
				Arrays.fill(m_cSampleDelta[i], (byte) -1);
			}
		}
	}
	
	@Override
	public Instrument2A03 clone() {
		Instrument2A03 pNew = new Instrument2A03();

		for (int i = 0; i < SEQUENCE_COUNT; ++i) {
			pNew.setSeqEnable(i, getSeqEnable(i));
			pNew.setSeqIndex(i, getSeqIndex(i));
		}

		for (int i = 0; i < OCTAVE_RANGE; ++i) {
			for (int j = 0; j < 12; ++j) {
				pNew.setSample(i, j, getSample(i, j));
				pNew.setSamplePitch(i, j, getSamplePitch(i, j));
			}
		}

		pNew.setName(getName());

		return pNew;
	}

	@Override
	public void setup() {
		FamiTrackerDoc pDoc = FamiTrackerApp.getDoc();

		// Select free sequences
		for (int i = 0; i < SEQ_COUNT; ++i) {
			setSeqEnable(i, (byte) 0);
			int Slot = pDoc.getFreeSequence(i);
			if (Slot != -1)
				setSeqIndex(i, Slot);
		}
	}

	@Override
	public void store(DocumentFile pDocFile) {
		pDocFile.writeBlockInt(SEQUENCE_COUNT);

		for (int i = 0; i < SEQUENCE_COUNT; ++i) {
			pDocFile.writeBlockChar(getSeqEnable(i));
			pDocFile.writeBlockChar((byte) getSeqIndex(i));
		}

		for (int i = 0; i < OCTAVE_RANGE; ++i) {
			for (int j = 0; j < 12; ++j) {
				pDocFile.writeBlockChar(getSample(i, j));
				pDocFile.writeBlockChar(getSamplePitch(i, j));
				pDocFile.writeBlockChar(getSampleDeltaValue(i, j));
			}
		}

	}

	@Override
	public boolean load(DocumentFile pDocFile) {
		int version = pDocFile.getBlockVersion();

		int SeqCnt = pDocFile.getBlockInt();
		assert(SeqCnt < (SEQUENCE_COUNT + 1));

		for (int i = 0; i < SeqCnt; ++i) {
			setSeqEnable(i, pDocFile.getBlockChar());
			int Index = pDocFile.getBlockChar();
			assert(Index < MAX_SEQUENCES);
			setSeqIndex(i, Index);
		}

		int Octaves = (version == 1) ? 6 : OCTAVE_RANGE;

		for (int i = 0; i < Octaves; ++i) {
			for (int j = 0; j < 12; ++j) {
				int index = pDocFile.getBlockChar();
				if (index > MAX_DSAMPLES)
					index = 0;
				setSample(i, j, (byte) index);
				setSamplePitch(i, j, pDocFile.getBlockChar());
				if (version > 5) {
					byte Value = pDocFile.getBlockChar();
					if (Value != -1 && Value < 0)
						Value = -1;
					setSampleDeltaValue(i, j, Value);
				}
			}
		}

		return true;
	}

	@Override
	public boolean loadFile() {
		throw new RuntimeException("我不允许你调用");
	}

	@Override
	public boolean canRelease() {
		if (getSeqEnable(0) != 0) {
			int index = getSeqIndex(SEQ_VOLUME);
			return FamiTrackerApp.getDoc().getSequence(Types.SNDCHIP_NONE, index, SEQ_VOLUME).getReleasePoint() != -1;
		}

		return false;
	}
	
	public final byte getSeqEnable(int index) {
		return m_iSeqEnable[index];
	}
	
	public final int getSeqIndex(int index) {
		return m_iSeqIndex[index];
	}
	
	public void setSeqIndex(int index, int value) {
		if (m_iSeqIndex[index] != value)
			InstrumentChanged();
		m_iSeqIndex[index] = value;
	}
	
	public void setSeqEnable(int index, byte value) {
		if (m_iSeqEnable[index] != value)
			InstrumentChanged();
		m_iSeqEnable[index] = value;
	}
	
	// Samples
	public final byte getSample(int octave, int note) {
		return m_cSamples[octave][note];
	}
	
	public final byte getSamplePitch(int octave, int note) {
		return m_cSamplePitch[octave][note];
 	}
	
	public final boolean getSampleLoop(int octave, int note) {
		return (m_cSamplePitch[octave][note] & 0x80) == 0x80;
	}
	
	public final byte getSampleLoopOffset(int octave, int note) {
		return m_cSampleLoopOffset[octave][note];
	}
	
	public final byte getSampleDeltaValue(int octave, int note) {
		return m_cSampleDelta[octave][note];
	}
	
	public void setSample(int octave, int note, byte sample) {
		m_cSamples[octave][note] = sample;
		InstrumentChanged();
	}
	
	public void setSamplePitch(int octave, int note, byte pitch) {
		m_cSamplePitch[octave][note] = pitch;
		InstrumentChanged();
	}
	
	public void setSampleLoop(int octave, int note, boolean loop) {
		m_cSamplePitch[octave][note] = (byte) ((m_cSamplePitch[octave][note] & 0x7F) | (loop ? 0x80 : 0));
		InstrumentChanged();
	}
	
	public void setSampleLoopOffset(int octave, int note, byte offset) {
		m_cSampleLoopOffset[octave][note] = offset;
		InstrumentChanged();
	}
	
	public void setSampleDeltaValue(int octave, int note, byte value) {
		m_cSampleDelta[octave][note] = value;
		InstrumentChanged();
	}

	/**
	 * Returns true if there are assigned samples in this instrument
	 * @return
	 */
	public final boolean assignedSamples() {
		for (int i = 0; i < OCTAVE_RANGE; ++i) {
			for (int j = 0; j < NOTE_RANGE; ++j) {
				if (getSample(i, j) != 0)
					return true;
			}
		}
		return false;
	}

	public static final int SEQUENCE_COUNT = 5;
	public static final int[] SEQUENCE_TYPES = {
			SEQ_VOLUME,
			SEQ_ARPEGGIO,
			SEQ_PITCH,
			SEQ_HIPITCH,
			SEQ_DUTYCYCLE
	};

	private byte[] m_iSeqEnable = new byte[SEQ_COUNT];
	private int[] m_iSeqIndex = new int[SEQ_COUNT];
	
	/**
	 * Samples
	 */
	private byte[][] m_cSamples = new byte[OCTAVE_RANGE][12];
	
	/**
	 * Play pitch/loop
	 */
	private byte[][] m_cSamplePitch = new byte[OCTAVE_RANGE][12];
	
	/**
	 * Loop offset
	 */
	private byte[][] m_cSampleLoopOffset = new byte[OCTAVE_RANGE][12];
	
	/**
	 * Delta setting
	 */
	private byte[][] m_cSampleDelta = new byte[OCTAVE_RANGE][12];

}
