package com.zdream.famitracker.document.instrument;

import static com.zdream.famitracker.FamitrackerTypes.*;

import com.zdream.famitracker.FamiTrackerApp;
import com.zdream.famitracker.FamiTrackerDoc;
import com.zdream.famitracker.components.DocumentFile;
import com.zdream.famitracker.sound.emulation.Types;

public class InstrumentN163 extends Instrument {
	
	public static final byte TRIANGLE_WAVE[] = {
		0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 
		15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0
	};
	
	private static final int DEFAULT_WAVE_SIZE = 32;

	public InstrumentN163() {
		super(INST_N163);
		
		for (int i = 0; i < MAX_WAVE_COUNT; ++i) {
			for (int j = 0; j < DEFAULT_WAVE_SIZE; ++j) {
				m_iSamples[i][j] = (i == 0) ? TRIANGLE_WAVE[j] : 0;
			}
		}

		m_iWaveSize = DEFAULT_WAVE_SIZE;
		m_iWavePos = 0;
		m_iWaveCount = 1;
	}
	
	@Override
	public InstrumentN163 clone() {
		InstrumentN163 pNew = new InstrumentN163();

		for (int i = 0; i < SEQUENCE_COUNT; ++i) {
			pNew.setSeqEnable(i, getSeqEnable(i));
			pNew.setSeqIndex(i, getSeqIndex(i));
		}

		pNew.setWaveSize(getWaveSize());
		pNew.setWavePos(getWavePos());
//		pNew.setAutoWavePos(getAutoWavePos());
		pNew.setWaveCount(getWaveCount());

		for (int i = 0; i < MAX_WAVE_COUNT; ++i) {
			for (int j = 0; j < MAX_WAVE_SIZE; ++j) {
				pNew.setSample(i, j, getSample(i, j));
			}
		}

		pNew.setName(getName());

		return pNew;
	}

	@Override
	public void setup() {
		FamiTrackerDoc pDoc = FamiTrackerApp.getDoc();

		for (int i = 0; i < SEQ_COUNT; ++i) {
			setSeqEnable(i, 0);
			int Index = pDoc.getFreeSequenceN163(i);
			if (Index != -1)
				setSeqIndex(i, Index);
		}

	}

	@Override
	public boolean load(DocumentFile pDocFile) {
		int seqCnt = pDocFile.getBlockInt();

		assert(seqCnt < (SEQUENCE_COUNT + 1));

		seqCnt = SEQUENCE_COUNT;

		for (int i = 0; i < seqCnt; ++i) {
			setSeqEnable(i, pDocFile.getBlockChar());
			int index = pDocFile.getBlockChar();
			assert(index < MAX_SEQUENCES);
			setSeqIndex(i, index);
		}

		m_iWaveSize = pDocFile.getBlockInt();
		assert(m_iWaveSize >= 0 && m_iWaveSize <= MAX_WAVE_SIZE);
		m_iWavePos = pDocFile.getBlockInt();
		assert(m_iWavePos >= 0 && m_iWavePos < 128);

		m_iWaveCount = pDocFile.getBlockInt();
		assert(m_iWaveCount >= 1 && m_iWaveCount <= MAX_WAVE_COUNT);
		
		for (int i = 0; i < m_iWaveCount; ++i) {
			for (int j = 0; j < m_iWaveSize; ++j) {
				byte waveSample = pDocFile.getBlockChar();
				assert(waveSample < 16);
				m_iSamples[i][j] = waveSample;
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
			return FamiTrackerApp.getDoc().getSequence(Types.SNDCHIP_N163, index, SEQ_VOLUME).getReleasePoint() != -1;
		}

		return false;
	}
	
	public final int getSeqEnable(int index) {
		assert(index < SEQ_COUNT);
		return m_iSeqEnable[index];
	}
	public final int getSeqIndex(int index) {
		assert(index < SEQ_COUNT);
		return m_iSeqIndex[index];
	}
	public void setSeqEnable(int index, int value) {
		assert(index < SEQ_COUNT);
		if (m_iSeqEnable[index] != value)
			InstrumentChanged();
		m_iSeqEnable[index] = value;
	}
	public void setSeqIndex(int index, int value) {
		assert(index < SEQ_COUNT);
		if (m_iSeqIndex[index] != value)
			InstrumentChanged();
		m_iSeqIndex[index] = value;
	}
	public final int getWaveSize() {
		return m_iWaveSize;
	}
	public void setWaveSize(int size) {
		m_iWaveSize = size;
		InstrumentChanged();
	}
	public final int getWavePos() {
		return m_iWavePos;
	}
	public void setWavePos(int pos) {
		m_iWavePos = pos;
		InstrumentChanged();
	}
	public final int getSample(int wave, int pos) {
		assert(wave < MAX_WAVE_COUNT);
		assert(pos < MAX_WAVE_SIZE);

		return m_iSamples[wave][pos];
	}
	public void setSample(int wave, int pos, int sample) {
		assert(wave < MAX_WAVE_COUNT);
		assert(pos < MAX_WAVE_SIZE);

		m_iSamples[wave][pos] = sample;
		InstrumentChanged();
	}
		/*
		void	SetAutoWavePos(bool Enable);
		bool	GetAutoWavePos() const;
		*/
	public void setWaveCount(int count) {
		assert(count <= MAX_WAVE_COUNT);
		m_iWaveCount = count;
		InstrumentChanged();
	}
		
	public int getWaveCount() {
		return m_iWaveCount;
	}

//	public int		storeWave(CChunk *pChunk) const;
		
	public boolean isWaveEqual(InstrumentN163 pInstrument) {
		int count = getWaveCount();
		int size = getWaveSize();

		if (pInstrument.getWaveCount() != count)
			return false;

		if (pInstrument.getWaveSize() != size)
			return false;

		for (int i = 0; i < count; ++i) {
			for (int j = 0; j < size; ++j) {
				if (getSample(i, j) != pInstrument.getSample(i, j))
					return false;
			}
		}

		return true;
	}

	public static final int SEQUENCE_COUNT = 5;
	public static final int SEQUENCE_TYPES[] = {
			SEQ_VOLUME, SEQ_ARPEGGIO, SEQ_PITCH, SEQ_HIPITCH, SEQ_DUTYCYCLE
	};

	/**
	 * Wave size (32 samples), 后面会扩展为 128
	 */
	public static final int MAX_WAVE_SIZE = 32;
	/**
	 * Number of waves
	 */
	public static final int MAX_WAVE_COUNT = 16;

	private int[] m_iSeqEnable = new int[SEQ_COUNT];
	private int[] m_iSeqIndex = new int[SEQ_COUNT];
	private int[][] m_iSamples = new int[MAX_WAVE_COUNT][MAX_WAVE_SIZE];
	private int m_iWaveSize;
	private int m_iWavePos;
	private int m_iWaveCount;

}
