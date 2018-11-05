package com.zdream.famitracker.sound.channels;

import com.zdream.famitracker.document.Sequence;
import com.zdream.famitracker.document.StChanNote;
import com.zdream.famitracker.document.instrument.Instrument;
import com.zdream.famitracker.document.instrument.InstrumentFDS;

import static com.zdream.famitracker.FamitrackerTypes.*;
import java.util.Arrays;

import com.zdream.famitracker.FamiTrackerDoc;

public class ChannelHandlerFDS extends ChannelHandler {
	
	public ChannelHandlerFDS() {
		super(0xFFF, 32);
		
		clearSequences();
		
		m_bResetMod = false;
	}
	
	public void processChannel() {
		// Default effects
		super.processChannel();	

		// Sequences
		if (getSequenceState(SEQ_VOLUME) != SEQ_STATE_DISABLED)
			runSequence(SEQ_VOLUME);

		if (getSequenceState(SEQ_ARPEGGIO) != SEQ_STATE_DISABLED)
			runSequence(SEQ_ARPEGGIO);

		if (getSequenceState(SEQ_PITCH) != SEQ_STATE_DISABLED)
			runSequence(SEQ_PITCH);
	}
	
	public void refreshChannel() {
		checkWaveUpdate();	

		int frequency = calculatePeriod();
		
		// unsigned
		int loFreq = frequency & 0xFF;
		// unsigned
		int hiFreq = (frequency >> 8) & 0x0F;

		// unsigned
		int modFreqLo = m_iModulationSpeed & 0xFF;
		// unsigned
		int modFreqHi = (m_iModulationSpeed >> 8) & 0x0F;

		// unsigned
		int volume = calculateVolume();

		if (!m_bGate)
			volume = 0;

		// Write frequency
		writeExternalRegister(0x4082, (byte) loFreq);
		writeExternalRegister(0x4083, (byte) hiFreq);

		// Write volume, disable envelope
		writeExternalRegister(0x4080, (byte) (0x80 | volume));

		if (m_bResetMod)
			writeExternalRegister(0x4085, (byte) 0);

		m_bResetMod = false;

		// Update modulation unit
		if (m_iModulationDelay == 0) {
			// Modulation frequency
			writeExternalRegister(0x4086, (byte) modFreqLo);
			writeExternalRegister(0x4087, (byte) modFreqHi);

			// Sweep depth, disable sweep envelope
			writeExternalRegister(0x4084, (byte) (0x80 | m_iModulationDepth)); 
		}
		else {
			// Delayed modulation
			writeExternalRegister(0x4087, (byte) 0x80);
			m_iModulationDelay--;
		}
	}
	
	protected void handleNoteData(StChanNote pNoteData, int effColumns) {
		m_iPostEffect = 0;
		m_iPostEffectParam = 0;

		m_iEffModDepth = -1;
		m_iEffModSpeedHi = -1;
		m_iEffModSpeedLo = -1;

		super.handleNoteData(pNoteData, effColumns);

		if (pNoteData.note != NOTE_NONE && pNoteData.note != NOTE_HALT && pNoteData.note != NOTE_RELEASE) {
			if (m_iPostEffect != 0 && (m_iEffect == EF_SLIDE_UP || m_iEffect == EF_SLIDE_DOWN))
				setupSlide(m_iPostEffect, m_iPostEffectParam);
			else if (m_iEffect == EF_SLIDE_DOWN || m_iEffect == EF_SLIDE_UP)
				m_iEffect = EF_NONE;
		}

		if (m_iEffModDepth != -1)
			m_iModulationDepth = m_iEffModDepth;

		if (m_iEffModSpeedHi != -1)
			m_iModulationSpeed = (m_iModulationSpeed & 0xFF) | (m_iEffModSpeedHi << 8);

		if (m_iEffModSpeedLo != -1)
			m_iModulationSpeed = (m_iModulationSpeed & 0xF00) | m_iEffModSpeedLo;
	}
	
	protected void handleCustomEffects(int effNum, int effParam) {
		if (effNum == EF_PORTA_DOWN) {
			m_iPortaSpeed = effParam;
			m_iEffect = EF_PORTA_UP;
		}
		else if (effNum == EF_PORTA_UP) {
			m_iPortaSpeed = effParam;
			m_iEffect = EF_PORTA_DOWN;
		}
		else if (!checkCommonEffects(effNum, effParam)) {
			// Custom effects
			switch (effNum) {
				case EF_SLIDE_UP:
				case EF_SLIDE_DOWN:
					m_iPostEffect = effNum;
					m_iPostEffectParam = effParam;
					setupSlide(effNum, effParam);
					break;
				case EF_FDS_MOD_DEPTH:
					m_iEffModDepth = effParam & 0x3F;
					break;
				case EF_FDS_MOD_SPEED_HI:
					m_iEffModSpeedHi = effParam & 0x0F;
					break;
				case EF_FDS_MOD_SPEED_LO:
					m_iEffModSpeedLo = effParam;
					break;
			}
		}
	}
	
	protected boolean handleInstrument(int instrument, boolean trigger, boolean newInstrument) {
		FamiTrackerDoc pDocument = m_pSoundGen.getDocument();

		Instrument inst = pDocument.getInstrument(instrument);
		if (inst == null || !(inst instanceof InstrumentFDS)) {
			return false;
		}
		
		InstrumentFDS pInstrument = (InstrumentFDS) inst;
		
		if (trigger || newInstrument) {
			fillWaveRAM(pInstrument);
			fillModulationTable(pInstrument);
		}

		if (trigger) {
			Sequence pVolSeq = pInstrument.getVolumeSeq();
			Sequence pArpSeq = pInstrument.getArpSeq();
			Sequence pPitchSeq = pInstrument.getPitchSeq();
			
			if (pVolSeq.getItemCount() > 0) {
				setupSequence(SEQ_VOLUME, pVolSeq);
			} else {
				clearSequence(SEQ_VOLUME);
			}
			
			if (pArpSeq.getItemCount() > 0) {
				setupSequence(SEQ_ARPEGGIO, pArpSeq);
			} else {
				clearSequence(SEQ_ARPEGGIO);
			}
			
			if (pPitchSeq.getItemCount() > 0) {
				setupSequence(SEQ_PITCH, pPitchSeq);
			} else {
				clearSequence(SEQ_PITCH);
			}

//			if (pInstrument->GetModulationEnable()) {
				m_iModulationSpeed = pInstrument.getModulationSpeed();
				m_iModulationDepth = pInstrument.getModulationDepth();
				m_iModulationDelay = pInstrument.getModulationDelay();
//			}
		}

		return true;
	}
	
	protected void handleEmptyNote() {}
	
	protected void handleCut() {
		cutNote();
	}
	
	protected void handleRelease() {
		if (!m_bRelease) {
			releaseNote();
			releaseSequences();
		}
	}
	
	protected void handleNote(int note, int octave) {
		// Trigger a new note
		m_iNote	= runNote(octave, note);
		m_bResetMod = true;
		m_iLastInstrument = m_iInstrument;

		m_iSeqVolume = 0x1F;
	}
	
	protected void clearRegisters() {
		// Clear gain
		writeExternalRegister(0x4090, (byte) 0x00);

		// Clear volume
		writeExternalRegister(0x4080, (byte) 0x80);

		// Silence channel
		writeExternalRegister(0x4083, (byte) 0x80);

		// Default speed
		writeExternalRegister(0x408A, (byte) 0xFF);

		// Disable modulation
		writeExternalRegister(0x4087, (byte) 0x80);

		m_iSeqVolume = 0x20;

		Arrays.fill(m_iModTable, (byte) 0);
		Arrays.fill(m_iWaveTable, (byte) 0);
	}

		// FDS functions
	protected void fillWaveRAM(InstrumentFDS pInstrument) {
		boolean bNew = false;

		for (int i = 0; i < 64; ++i) {
			if (m_iWaveTable[i] != pInstrument.getSample(i)) {
				bNew = true;
				break;
			}
		}

		if (bNew) {
			for (int i = 0; i < 64; ++i)
				m_iWaveTable[i] = pInstrument.getSample(i);

			// Fills the 64 byte waveform table
			// Enable write for waveform RAM
			writeExternalRegister(0x4089, (byte) 0x80);

			// This is the time the loop takes in NSF code
			addCycles(1088);

			// Wave ram
			for (int i = 0; i < 0x40; ++i)
				writeExternalRegister(0x4040 + i, pInstrument.getSample(i));

			// Disable write for waveform RAM, master volume = full
			writeExternalRegister(0x4089, (byte) 0x00);
		}
	}
	
	/**
	 * Fills the 32 byte modulation table
	 * @param pInstrument
	 */
	protected void fillModulationTable(InstrumentFDS pInstrument) {
		boolean bNew = true;

		for (int i = 0; i < 32; ++i) {
			int mod = m_iModTable[i] & 0xFF;
			if (mod != pInstrument.getModulation(i)) {
				bNew = true;
				break;
			}
		}

		if (bNew) {
			// Copy table
			for (int i = 0; i < 32; ++i)
				m_iModTable[i] = (byte) pInstrument.getModulation(i);

			// Disable modulation
			writeExternalRegister(0x4087, (byte) 0x80);
			// Reset modulation table pointer, set bias to zero
			writeExternalRegister(0x4085, (byte) 0x00);
			// Fill the table
			for (int i = 0; i < 32; ++i)
				writeExternalRegister(0x4088, m_iModTable[i]);
		}
	}
	
	/**
	 * 由于不会修改乐器, 下面的代码均不会被执行
	 */
	private void checkWaveUpdate() {
		// Check wave changes
		// FamiTrackerDoc pDocument = m_pSoundGen.getDocument();
		
		// 在该 Java 工程中, 下面的 bWaveChanged 恒等于 false
		// boolean bWaveChanged = theApp.GetSoundGenerator()->hasWaveChanged();

		/*if (m_iInstrument != MAX_INSTRUMENTS && bWaveChanged) {
			CInstrumentContainer<CInstrumentFDS> instContainer(pDocument, m_iInstrument);
			CInstrumentFDS *pInstrument = instContainer();
			if (pInstrument != NULL) {
				// Realtime update
				m_iModulationSpeed = pInstrument->GetModulationSpeed();
				m_iModulationDepth = pInstrument->GetModulationDepth();
				FillWaveRAM(pInstrument);
				FillModulationTable(pInstrument);
			}
		}*/
	}
	
	// FDS control variables
	protected int m_iModulationSpeed;
	protected int m_iModulationDepth;
	protected int m_iModulationDelay;
	
	// FDS sequences 原工程自己注释掉的
	//CSequence *m_pVolumeSeq;
	//CSequence *m_pArpeggioSeq;
	//CSequence *m_pPitchSeq;
	
	// Modulation table
	protected byte[] m_iModTable = new byte[32];
	protected byte[] m_iWaveTable = new byte[64];
	
	// Modulation
	protected boolean m_bResetMod;
	
	protected int m_iPostEffect;
	protected int m_iPostEffectParam;

	protected int m_iEffModDepth;
	protected int m_iEffModSpeedHi;
	protected int m_iEffModSpeedLo;

}
