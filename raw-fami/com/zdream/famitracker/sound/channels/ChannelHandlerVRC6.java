package com.zdream.famitracker.sound.channels;

import static com.zdream.famitracker.FamitrackerTypes.*;
import static com.zdream.famitracker.sound.emulation.Types.*;

import com.zdream.famitracker.FamiTrackerDoc;
import com.zdream.famitracker.document.Sequence;
import com.zdream.famitracker.document.StChanNote;
import com.zdream.famitracker.document.instrument.Instrument;
import com.zdream.famitracker.document.instrument.InstrumentVRC6;

/**
 * Derived channels, VRC6
 * @author Zdream
 */
public abstract class ChannelHandlerVRC6 extends ChannelHandler {

	public ChannelHandlerVRC6() {
		super(0xFFF, 0x0F);
	}

	@Override
	protected void handleCustomEffects(int effNum, int effParam) {
		if (!checkCommonEffects((byte) effNum, (byte) effParam)) {
			switch (effNum) {
				case EF_DUTY_CYCLE:
					m_iDefaultDuty = m_iDutyPeriod = (byte) effParam;
					break;
				case EF_SLIDE_UP:
				case EF_SLIDE_DOWN:
					m_iPostEffect = effNum;
					m_iPostEffectParam = effParam;
					setupSlide(effNum, effParam);
					break;
			}
		}
	}

	@Override
	protected boolean handleInstrument(int instrument, boolean trigger, boolean newInstrument) {
		FamiTrackerDoc pDocument = m_pSoundGen.getDocument();
		
		Instrument inst = pDocument.getInstrument(instrument);
		if (inst == null || !(inst instanceof InstrumentVRC6)) {
			return false;
		}
		
		InstrumentVRC6 pInstrument = (InstrumentVRC6) inst;

		// Setup instrument
		for (int i = 0; i < InstrumentVRC6.SEQUENCE_COUNT; ++i) {
			final Sequence pSequence = pDocument.getSequence(SNDCHIP_VRC6, pInstrument.getSeqIndex(i), i);
			if (trigger || !isSequenceEqual(i, pSequence) || pInstrument.getSeqEnable(i) > getSequenceState(i)) {
				if (pInstrument.getSeqEnable(i) == 1)
					setupSequence(i, pSequence);
				else
					clearSequence(i);
			}
		}

		return true;
	}

	@Override
	protected void handleEmptyNote() {
		// do nothing
	}

	@Override
	protected void handleCut() {
		cutNote();
	}

	@Override
	protected void handleRelease() {
		if (!m_bRelease) {
			releaseNote();
			releaseSequences();
		}
	}

	@Override
	protected void handleNote(int note, int octave) {
		m_iNote = runNote(octave, note);
		m_iSeqVolume = 0x0F;
		m_iDutyPeriod = m_iDefaultDuty;
	}
	
	@Override
	protected void handleNoteData(StChanNote pNoteData, int effColumns) {
		m_iPostEffect = 0;
		m_iPostEffectParam = 0;
		
		super.handleNoteData(pNoteData, effColumns);
		
		if (pNoteData.note != NOTE_NONE && pNoteData.note != NOTE_HALT && pNoteData.note != NOTE_RELEASE) {
			if (m_iPostEffect != 0 && (m_iEffect == EF_SLIDE_UP || m_iEffect == EF_SLIDE_DOWN))
				setupSlide(m_iPostEffect, m_iPostEffectParam);
			else if (m_iEffect == EF_SLIDE_DOWN || m_iEffect == EF_SLIDE_UP)
				m_iEffect = EF_NONE;
		}
	}
	
	@Override
	public void processChannel() {
		// Default effects
		super.processChannel();

		// Sequences
		for (int i = 0; i < InstrumentVRC6.SEQUENCE_COUNT; ++i)
			runSequence(i);
	}
	
	protected int m_iPostEffect;
	protected int m_iPostEffectParam;

}
