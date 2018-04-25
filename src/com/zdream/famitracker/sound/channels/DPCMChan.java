package com.zdream.famitracker.sound.channels;

import static com.zdream.famitracker.FamitrackerTypes.*;

import com.zdream.famitracker.FamiTrackerApp;
import com.zdream.famitracker.FamiTrackerDoc;
import com.zdream.famitracker.document.DSample;
import com.zdream.famitracker.document.StChanNote;
import com.zdream.famitracker.document.instrument.Instrument;
import com.zdream.famitracker.document.instrument.Instrument2A03;
import com.zdream.famitracker.sound.SampleMem;

public final class DPCMChan extends ChannelHandler2A03 {
	
	public DPCMChan(SampleMem pSampleMem) {
		m_pSampleMem = pSampleMem;
	}

	@Override
	public void refreshChannel() {
		if (m_cDAC != 255) {
			writeRegister(0x4011, (byte) m_cDAC);
			m_cDAC = 255;
		}

		if (m_iRetrigger != 0) {
			m_iRetriggerCntr--;
			if (m_iRetriggerCntr == 0) {
				m_iRetriggerCntr = m_iRetrigger;
				m_bEnabled = true;
				m_bTrigger = true;
			}
		}

		if (m_bRelease) {
			// Release command
			writeRegister(0x4015, (byte) 0x0F);
			m_bEnabled = false;
			m_bRelease = false;
		}
		
		if (!m_bEnabled)
			return;

		if (!m_bGate) {
			// Cut sample
			writeRegister(0x4015, (byte) 0x0F);

			if (!FamiTrackerApp.getInstance().getSettings().general.bNoDPCMReset || FamiTrackerApp.getInstance().isPlaying()) {
				writeRegister(0x4011, (byte) 0);	// regain full volume for TN
			}

			m_bEnabled = false;		// don't write to this channel anymore
		}
		else if (m_bTrigger) {
			// Start playing the sample
			writeRegister(0x4010, (byte) ((m_iPeriod & 0x0F) | m_iLoop));
			writeRegister(0x4012, (byte) m_iOffset);							// load address, start at $C000
			writeRegister(0x4013, (byte) m_iSampleLength);						// length
			writeRegister(0x4015, (byte) 0x0F);
			writeRegister(0x4015, (byte) 0x1F);								// fire sample

			// Loop offset
			if (m_iLoopOffset > 0) {
				writeRegister(0x4012, (byte) m_iLoopOffset);
				writeRegister(0x4013, (byte) m_iLoopLength);
			}

			m_bTrigger = false;
		}
	}
	
	@Override
	protected void handleNoteData(StChanNote pNoteData, int effColumns) {
		m_iCustomPitch = -1;
		m_iRetrigger = 0;

		if (pNoteData.note != NOTE_NONE) {
			m_iNoteCut = 0;
		}
		
		super.handleNoteData(pNoteData, effColumns);
	}
	
	protected void handleCustomEffects(int effNum, int effParam) {
		switch (effNum) {
		case EF_DAC:
			m_cDAC = effParam & 0x7F;
			break;
		case EF_SAMPLE_OFFSET:
			m_iOffset = effParam;
			break;
		case EF_DPCM_PITCH:
			m_iCustomPitch = effParam;
			break;
		case EF_RETRIGGER:
//				if (NoteData->EffParam[i] > 0) {
				m_iRetrigger = effParam + 1;
				if (m_iRetriggerCntr == 0)
					m_iRetriggerCntr = m_iRetrigger;
//				}
//				m_iEnableRetrigger = 1;
			break;
		case EF_NOTE_CUT:
			m_iNoteCut = (byte) (effParam + 1);
			break;
		}
	}
	
	@Override
	protected boolean handleInstrument(int instrument, boolean trigger, boolean newInstrument) {
		return true;
	}

	@Override
	protected void handleEmptyNote() {}

	@Override
	protected void handleRelease() {
		m_bRelease = true;
	}

	@Override
	protected void handleNote(int note, int octave) {
		FamiTrackerDoc pDocument = m_pSoundGen.getDocument();
		Instrument2A03 pInstrument = (Instrument2A03) pDocument.getInstrument(m_iInstrument);

		if (pInstrument == null)
			return;

		if (pInstrument.getType() != Instrument.INST_2A03)
			return;

		int sampleIndex = pInstrument.getSample(octave, note - 1);

		if (sampleIndex > 0) {

			int pitch = pInstrument.getSamplePitch(octave, note - 1);
			m_iLoop = (pitch & 0x80) >> 1;

			if (m_iCustomPitch != -1)
				pitch = m_iCustomPitch;
		
			m_iLoopOffset = pInstrument.getSampleLoopOffset(octave, note - 1);

			final DSample pDSample = pDocument.getSample1(sampleIndex - 1);
			int sampleSize = pDSample.getSize();

			if (sampleSize > 0) {
				m_pSampleMem.setMem(pDSample.getData());
				m_iPeriod = pitch & 0x0F;
				m_iSampleLength = (sampleSize >> 4) - (m_iOffset << 2);
				m_iLoopLength = sampleSize - m_iLoopOffset;
				m_bEnabled = true;
				m_bTrigger = true;
				m_bGate = true;

				// Initial delta counter value
				byte delta = pInstrument.getSampleDeltaValue(octave, note - 1);
				
				if (delta != 255 && m_cDAC == 255)
					m_cDAC = delta;

				m_iRetriggerCntr = m_iRetrigger;
			}
		}

		registerKeyState((note - 1) + (octave * 12));
	}
	
	@Override
	protected void clearRegisters() {
		writeRegister(0x4015, (byte) 0x0F);

		writeRegister(0x4010, (byte) 0);	
		writeRegister(0x4011, (byte) 0);
		writeRegister(0x4012, (byte) 0);
		writeRegister(0x4013, (byte) 0);

		m_iOffset = 0;
		m_cDAC = 255;
	}
	
	// DPCM variables
	private SampleMem m_pSampleMem;

	/**
	 * 范围 0 - 255
	 */
	private int m_cDAC = 255;
	private int m_iLoop;
	private int m_iOffset;
	private int m_iSampleLength;
	private int m_iLoopOffset;
	private int m_iLoopLength;
	private int m_iRetrigger;
	private int m_iRetriggerCntr;
	private int m_iCustomPitch;
	private boolean m_bTrigger;
	private boolean m_bEnabled;
}
