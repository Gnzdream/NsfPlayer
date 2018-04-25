package com.zdream.famitracker.document.instrument;

import com.zdream.famitracker.FamitrackerTypes;
import com.zdream.famitracker.components.DocumentFile;
import com.zdream.famitracker.document.Sequence;

public class InstrumentFDS extends Instrument {
	
	private static final byte TEST_WAVE[] = {
		00, 01, 12, 22, 32, 36, 39, 39, 42, 47, 47, 50, 48, 51, 54, 58,
		54, 55, 49, 50, 52, 61, 63, 63, 59, 56, 53, 51, 48, 47, 41, 35,
		35, 35, 41, 47, 48, 51, 53, 56, 59, 63, 63, 61, 52, 50, 49, 55,
		54, 58, 54, 51, 48, 50, 47, 47, 42, 39, 39, 36, 32, 22, 12, 01
	};
	
	public static final int FIXED_FDS_INST_SIZE = 1 + 16 + 4 + 1;

	public InstrumentFDS() {
		super(INST_FDS);

		System.arraycopy(TEST_WAVE, 0, m_iSamples, 0, WAVE_SIZE);

		m_bModulationEnable = true;

		m_pVolume = new Sequence();
		m_pArpeggio = new Sequence();
		m_pPitch = new Sequence();
	}
	
	@Override
	public InstrumentFDS clone() {
		InstrumentFDS pNewInst = new InstrumentFDS();

		// Copy parameters
		System.arraycopy(m_iSamples, 0, pNewInst.m_iSamples, 0, WAVE_SIZE);
		System.arraycopy(m_iModulation, 0, pNewInst.m_iModulation, 0, MOD_SIZE);
		pNewInst.m_iModulationDelay = m_iModulationDelay;
		pNewInst.m_iModulationDepth = m_iModulationDepth;
		pNewInst.m_iModulationSpeed = m_iModulationSpeed;

		// Copy sequences
		pNewInst.m_pVolume.copy(m_pVolume);
		pNewInst.m_pArpeggio.copy(m_pArpeggio);
		pNewInst.m_pPitch.copy(m_pPitch);

		// Copy name
		pNewInst.setName(getName());

		return pNewInst;
	}

	@Override
	public void setup() {}

	@Override
	public boolean load(DocumentFile pDocFile) {
		for (int i = 0; i < WAVE_SIZE; ++i) {
			setSample(i, pDocFile.getBlockChar());
		}

		for (int i = 0; i < MOD_SIZE; ++i) {
			setModulation(i, pDocFile.getBlockChar());
		}

		setModulationSpeed(pDocFile.getBlockInt());
		setModulationDepth(pDocFile.getBlockInt());
		setModulationDelay(pDocFile.getBlockInt());

		// hack to fix earlier saved files (remove this eventually)
		/*
		if (pDocFile.getBlockVersion() > 2) {
			LoadSequence(pDocFile, m_pVolume);
			LoadSequence(pDocFile, m_pArpeggio);
			if (pDocFile.getBlockVersion() > 2)
				LoadSequence(pDocFile, m_pPitch);
		}
		else {
		*/
		
		int a = pDocFile.getBlockInt();
		int b = pDocFile.getBlockInt();
		pDocFile.rollbackPointer(8);

		if (a < 256 && (b & 0xFF) != 0x00) {
		} else {
			loadSequence(pDocFile, m_pVolume);
			loadSequence(pDocFile, m_pArpeggio);
			//
			// Note: Remove this line when files are unable to load 
			// (if a file contains FDS instruments but FDS is disabled)
			// this was a problem in an earlier version.
			//
			if (pDocFile.getBlockVersion() > 2)
				loadSequence(pDocFile, m_pPitch);
		}

//		}

		// Older files was 0-15, new is 0-31
		if (pDocFile.getBlockVersion() <= 3) {
			for (int i = 0; i < m_pVolume.getItemCount(); ++i)
				m_pVolume.setItem(i, (byte) (m_pVolume.getItem(i) * 2));
		}

		return true;
	}

	@Override
	public boolean loadFile() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public final boolean canRelease() {
		if (m_pVolume.getItemCount() > 0) {
			if (m_pVolume.getReleasePoint() != -1)
				return true;
			
		}
		return false;
	}

	public Sequence getArpSeq() {
		return m_pArpeggio;
	}
	
	public final byte getSample(int Index) {
		assert(Index < WAVE_SIZE);
		return m_iSamples[Index];
	}

	public void setSample(int index, byte sample) {
		assert(index < WAVE_SIZE);
		m_iSamples[index] = sample;
		InstrumentChanged();
	}
	
	public final int getModulationSpeed() {
		return m_iModulationSpeed;
	}
	public void setModulationSpeed(int speed) {
		m_iModulationSpeed = speed;
		InstrumentChanged();
	}
	public final int getModulation(int index) {
		return m_iModulation[index];
	}
	public void setModulation(int index, byte value) {
		m_iModulation[index] = value;
		InstrumentChanged();
	}
	public final int getModulationDepth() {
		return m_iModulationDepth;
	}
	public void setModulationDepth(int depth) {
		m_iModulationDepth = depth;
		InstrumentChanged();
	}
	public final int getModulationDelay() {
		return m_iModulationDelay;
	}
	public void setModulationDelay(int delay) {
		m_iModulationDelay = delay;
		InstrumentChanged();
	}
	public final boolean getModulationEnable() {
		return m_bModulationEnable;
	}
	public void setModulationEnable(boolean enable) {
		m_bModulationEnable = enable;
		InstrumentChanged();
	}
//		CSequence* GetVolumeSeq() const;
//		CSequence* GetArpSeq() const;
//		CSequence* GetPitchSeq() const;
//
//	private void storeSequence(DocumentFile pDocFile, Sequence pSeq);
	
	private boolean loadSequence(DocumentFile pDocFile, Sequence pSeq) {
		int seqCount;
		int loopPoint;
		int releasePoint;
		int settings;

		seqCount = pDocFile.getBlockChar() & 0xFF;
		loopPoint = pDocFile.getBlockInt();
		releasePoint = pDocFile.getBlockInt();
		settings = pDocFile.getBlockInt();

		assert(seqCount <= FamitrackerTypes.MAX_SEQUENCE_ITEMS);

		pSeq.clear();
		pSeq.setItemCount(seqCount);
		pSeq.setLoopPoint(loopPoint);
		pSeq.setReleasePoint(releasePoint);
		pSeq.setSetting(settings);

		for (int x = 0; x < seqCount; ++x) {
			byte value = pDocFile.getBlockChar();
			pSeq.setItem(x, value);
		}

		return true;
	}
	
//	private void storeInstSequence(InstrumentFile pDocFile, Sequence pSeq);
//	private boolean loadInstSequence(InstrumentFile pFile, Sequence pSeq);

	public static final int WAVE_SIZE = 64;
	public static final int MOD_SIZE = 32;

	// Instrument data
	private byte[] m_iSamples = new byte[64];
	private byte[] m_iModulation = new byte[32];
	private int m_iModulationSpeed;
	private int m_iModulationDepth;
	private int m_iModulationDelay;
	private boolean m_bModulationEnable;

	private Sequence m_pVolume;
	private Sequence m_pArpeggio;
	private Sequence m_pPitch;

}
