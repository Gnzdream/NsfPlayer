package com.zdream.famitracker.document.instrument;

import static com.zdream.famitracker.FamitrackerTypes.*;

import com.zdream.famitracker.FamiTrackerApp;
import com.zdream.famitracker.FamiTrackerDoc;
import com.zdream.famitracker.components.DocumentFile;
import com.zdream.famitracker.sound.emulation.Types;

public class InstrumentVRC6 extends Instrument {
	
	public InstrumentVRC6() {
		super(INST_VRC6);
	}
	
	@Override
	public InstrumentVRC6 clone() {
		InstrumentVRC6 pNew = new InstrumentVRC6();
		
		System.arraycopy(m_iSeqEnable, 0, pNew.m_iSeqEnable, 0, SEQ_COUNT);
		System.arraycopy(m_iSeqIndex, 0, pNew.m_iSeqIndex, 0, SEQ_COUNT);

		pNew.setName(getName());

		return pNew;
	}

	@Override
	public void setup() {
		FamiTrackerDoc pDoc = FamiTrackerApp.getDoc();

		for (int i = 0; i < SEQ_COUNT; ++i) {
			setSeqEnable(i, (byte) 0);
			int index = pDoc.getFreeSequenceVRC6(i);
			if (index != -1)
				setSeqIndex(i, index);
		}
	}

	@Override
	public boolean load(DocumentFile pDocFile) {
		int SeqCnt = pDocFile.getBlockInt();

		assert(SeqCnt < (SEQUENCE_COUNT + 1));

		SeqCnt = SEQUENCE_COUNT;//SEQ_COUNT;

		for (int i = 0; i < SeqCnt; i++) {
			setSeqEnable(i, pDocFile.getBlockChar());
			int index = pDocFile.getBlockChar();
			assert(index < MAX_SEQUENCES);
			setSeqIndex(i, index);
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
			return FamiTrackerApp.getDoc().getSequence(Types.SNDCHIP_VRC6, index, SEQ_VOLUME).getReleasePoint() != -1;
		}

		return false;
	}
	
	public final byte getSeqEnable(int index) {
		return m_iSeqEnable[index];
	}
	public final int getSeqIndex(int index) {
		return m_iSeqIndex[index];
	}
	public void setSeqEnable(int index, byte value) {
		if (m_iSeqEnable[index] != value)
			InstrumentChanged();		
		m_iSeqEnable[index] = value;
	}
	public void setSeqIndex(int index, int value) {
		if (m_iSeqIndex[index] != value)
			InstrumentChanged();
		m_iSeqIndex[index] = value;
	}

	public static final int SEQUENCE_COUNT = 5;
	public static final int SEQUENCE_TYPES[] = {
			SEQ_VOLUME, SEQ_ARPEGGIO, SEQ_PITCH, SEQ_HIPITCH, SEQ_DUTYCYCLE
	};

	private byte[] m_iSeqEnable = new byte[SEQ_COUNT];
	private int[] m_iSeqIndex = new int[SEQ_COUNT];

}
