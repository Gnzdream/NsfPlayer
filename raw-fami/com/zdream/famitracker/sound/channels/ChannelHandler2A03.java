package com.zdream.famitracker.sound.channels;

import static com.zdream.famitracker.FamitrackerTypes.EF_DUTY_CYCLE;
import static com.zdream.famitracker.FamitrackerTypes.EF_NONE;
import static com.zdream.famitracker.FamitrackerTypes.EF_SLIDE_DOWN;
import static com.zdream.famitracker.FamitrackerTypes.EF_SLIDE_UP;
import static com.zdream.famitracker.FamitrackerTypes.EF_SWEEPDOWN;
import static com.zdream.famitracker.FamitrackerTypes.EF_SWEEPUP;
import static com.zdream.famitracker.FamitrackerTypes.EF_VOLUME;
import static com.zdream.famitracker.FamitrackerTypes.NOTE_HALT;
import static com.zdream.famitracker.FamitrackerTypes.NOTE_NONE;
import static com.zdream.famitracker.FamitrackerTypes.NOTE_RELEASE;
import static com.zdream.famitracker.sound.emulation.Types.CHANID_DPCM;
import static com.zdream.famitracker.sound.emulation.Types.SNDCHIP_NONE;

import com.zdream.famitracker.FamiTrackerDoc;
import com.zdream.famitracker.document.Sequence;
import com.zdream.famitracker.document.StChanNote;
import com.zdream.famitracker.document.instrument.Instrument;
import com.zdream.famitracker.document.instrument.Instrument2A03;

public abstract class ChannelHandler2A03 extends ChannelHandler {

	public ChannelHandler2A03() {
		super(0x7FF, 0x0F);
	}

	@Override
	public void processChannel() {
		// Default effects
		super.processChannel();
		
		// Skip when DPCM
		if (m_iChannelID == CHANID_DPCM)
			return;

		// Sequences
		for (int i = 0; i < Instrument2A03.SEQUENCE_COUNT; ++i)
			runSequence(i);
	}

	@Override
	protected void handleNoteData(StChanNote pNoteData, int effColumns) {
		m_iPostEffect = 0;
		m_iPostEffectParam = 0;
		m_iSweep = 0;
		m_bSweeping = false;
		m_iInitVolume = 0x0F;
		m_bManualVolume = false;

		super.handleNoteData(pNoteData, effColumns);

		if (pNoteData.note != NOTE_NONE && pNoteData.note != NOTE_HALT && pNoteData.note != NOTE_RELEASE) {
			if (m_iPostEffect != 0 && (m_iEffect == EF_SLIDE_UP || m_iEffect == EF_SLIDE_DOWN))
				setupSlide(m_iPostEffect, m_iPostEffectParam);
			else if (m_iEffect == EF_SLIDE_DOWN || m_iEffect == EF_SLIDE_UP)
				m_iEffect = EF_NONE;
		}
	}
	
	@Override
	protected void handleCustomEffects(int effNum, int effParam) {
		if (!checkCommonEffects((byte) effNum, (byte) effParam)) {
			// Custom effects
			switch (effNum) {
				case EF_VOLUME:
					// Kill this eventually
					m_iInitVolume = effParam;
					m_bManualVolume = true;
					break;
				case EF_SWEEPUP:
					m_iSweep = 0x80 | (effParam & 0x77);
					m_iLastPeriod = 0xFFFF;
					m_bSweeping = true;
					break;
				case EF_SWEEPDOWN:
					m_iSweep = 0x80 | (effParam & 0x77);
					m_iLastPeriod = 0xFFFF;
					m_bSweeping = true;
					break;
				case EF_DUTY_CYCLE:
					assert(effParam < 128);
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
		
		Instrument pinst = pDocument.getInstrument(instrument);
		Instrument2A03 pInstrument = null;
		if (pinst != null && pinst instanceof Instrument2A03) {
			pInstrument = (Instrument2A03) pinst;
		}

		if (pInstrument == null)
			return false;

		for (int i = 0; i < Instrument2A03.SEQUENCE_COUNT; ++i) {
			final Sequence pSequence = pDocument.getSequence(SNDCHIP_NONE, pInstrument.getSeqIndex(i), i);
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
		if (m_bManualVolume)
			m_iSeqVolume = m_iInitVolume;
		
		if (m_bSweeping) {
			assert(m_iSweep < 128);
			m_cSweep = (byte) m_iSweep;
		}
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
	
	protected void handleNote(int note, int octave) {
		m_iNote			= runNote(octave, note);
		m_iDutyPeriod	= m_iDefaultDuty;
		m_iSeqVolume	= m_iInitVolume;

		m_iArpState = 0;

		if (!m_bSweeping && (m_cSweep != 0 || m_iSweep != 0)) {
			m_iSweep = 0;
			m_cSweep = 0;
			m_iLastPeriod = 0xFFFF;
		} else if (m_bSweeping) {
			m_cSweep = m_iSweep;
			m_iLastPeriod = 0xFFFF;
		}
	}

	/**
	 * Sweep, used by pulse channels
	 */
	protected int m_cSweep;

	/**
	 * Flag for Exx
	 */
	protected boolean m_bManualVolume;
	
	/**
	 * Initial volume
	 */
	protected int m_iInitVolume;
	
	/**
	 * Flag for HW sweep
	 */
	protected boolean m_bSweeping;
	protected int m_iSweep;
	protected int m_iPostEffect;
	protected int m_iPostEffectParam;

}
