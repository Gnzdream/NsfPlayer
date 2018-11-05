package com.zdream.famitracker.sound.channels;

import static com.zdream.famitracker.sound.emulation.Types.*;

import com.zdream.famitracker.FamiTrackerDoc;

import static com.zdream.famitracker.FamitrackerTypes.*;

import com.zdream.famitracker.document.Sequence;
import com.zdream.famitracker.document.StChanNote;
import com.zdream.famitracker.document.instrument.Instrument;
import com.zdream.famitracker.document.instrument.InstrumentN163;

public class ChannelHandlerN163 extends ChannelHandler {

	public ChannelHandlerN163() {
		super(0xFFFF, 0x0F);
		m_bLoadWave = false;
		m_bResetPhase = false;
		m_iWaveLen = 0;
		m_iWaveIndex = 0;
		m_iWaveCount = 0;
		m_iDutyPeriod = 0;
	}
	
	public static final int N163_PITCH_SLIDE_SHIFT = 2;
	
	private final int getIndex() {
		return m_iChannelID - CHANID_N163_CHAN1;
	}
	
	public void resetChannel() {
		super.resetChannel();

		m_iWaveIndex = 0;
	}

	public void handleNoteData(StChanNote pNoteData, int effColumns) {
		m_iPostEffect = 0;
		m_iPostEffectParam = 0;
		m_bLoadWave = false;
		
		super.handleNoteData(pNoteData, effColumns);

		if (pNoteData.note != NOTE_NONE && pNoteData.note != NOTE_HALT && pNoteData.note != NOTE_RELEASE) {
			if (m_iPostEffect != 0 && (m_iEffect == EF_SLIDE_UP || m_iEffect == EF_SLIDE_DOWN)) {
				setupSlide(m_iPostEffect, m_iPostEffectParam);
				m_iPortaSpeed <<= N163_PITCH_SLIDE_SHIFT;
			}
			else if (m_iEffect == EF_SLIDE_DOWN || m_iEffect == EF_SLIDE_UP)
				m_iEffect = EF_NONE;
		}
	}

	protected void handleCustomEffects(int effNum, int effParam) {
		if (effNum == EF_PORTA_DOWN) {
			m_iPortaSpeed = effParam << N163_PITCH_SLIDE_SHIFT;
			m_iEffect = EF_PORTA_UP;
		}
		else if (effNum == EF_PORTA_UP) {
			m_iPortaSpeed = effParam << N163_PITCH_SLIDE_SHIFT;
			m_iEffect = EF_PORTA_DOWN;
		}
		else if (effNum == EF_PORTAMENTO) {
			m_iPortaSpeed = effParam << N163_PITCH_SLIDE_SHIFT;
			m_iEffect = EF_PORTAMENTO;
		}
		else if (!checkCommonEffects(effNum, effParam)) {
			// Custom effects
			switch (effNum) {
				case EF_DUTY_CYCLE:
					// Duty effect controls wave
					m_iWaveIndex = effParam;
					m_bLoadWave = true;
					break;
				case EF_SLIDE_UP:
				case EF_SLIDE_DOWN:
					m_iPostEffect = effNum;
					m_iPostEffectParam = effParam;
					setupSlide(effNum, effParam);
					m_iPortaSpeed <<= N163_PITCH_SLIDE_SHIFT;
					break;
			}
		}
	}

	protected boolean handleInstrument(int instrument, boolean trigger, boolean newInstrument) {
		FamiTrackerDoc pDocument = m_pSoundGen.getDocument();
		
		Instrument inst = pDocument.getInstrument(instrument);
		if (inst == null || !(inst instanceof InstrumentN163)) {
			return false;
		}
		InstrumentN163 pInstrument = (InstrumentN163) inst;

		for (int i = 0; i < SEQ_COUNT; ++i) {
			final Sequence pSequence = pDocument.getSequence(SNDCHIP_N163, pInstrument.getSeqIndex(i), i);
			if (trigger || !isSequenceEqual(i, pSequence) || pInstrument.getSeqEnable(i) > getSequenceState(i)) {
				if (pInstrument.getSeqEnable(i) == 1)
					setupSequence(i, pSequence);
				else
					clearSequence(i);
			}
		}

		m_iWaveLen = pInstrument.getWaveSize();
		m_iWavePos = /*pInstrument->GetAutoWavePos() ? GetIndex() * 16 :*/ pInstrument.getWavePos();
		m_iWaveCount = pInstrument.getWaveCount();

		if (!m_bLoadWave && newInstrument)
			m_iWaveIndex = 0;

		m_bLoadWave = true;

		return true;
	}

	protected void handleEmptyNote() {}

	protected void handleCut() {
		cutNote();
		m_iNote = 0;
		m_bRelease = false;
	}

	protected void handleRelease() {
		if (!m_bRelease) {
			releaseNote();
			releaseSequences();
		}
	}

	protected void handleNote(int note, int octave) {
		// New note
		m_iNote	= runNote(octave, note);
		m_iSeqVolume = 0x0F;
		m_bRelease = false;

//		m_bResetPhase = true;
	}

	public void processChannel() {
		FamiTrackerDoc pDocument = m_pSoundGen.getDocument();

		// Default effects
		super.processChannel();

		boolean bUpdateWave = getSequenceState(SEQ_DUTYCYCLE) != SEQ_STATE_DISABLED;

		m_iChannels = pDocument.getNamcoChannels() - 1;

		// Sequences
		for (int i = 0; i < InstrumentN163.SEQUENCE_COUNT; ++i)
			runSequence(i);

		if (bUpdateWave) {
			m_iWaveIndex = m_iDutyPeriod;
			m_bLoadWave = true;
		}
	}

	public void refreshChannel() {
		checkWaveUpdate();

		int Channel = 7 - getIndex();		// Channel #
		int WaveSize = 256 - (m_iWaveLen >> 2);
		int Frequency = limitPeriod(getPeriod() - ((getVibrato() + getFinePitch() + getPitch()) << 4)) << 2;

		// Compensate for shorter waves
//		Frequency >>= 5 - int(log(double(m_iWaveLen)) / log(2.0));

		int Volume = calculateVolume();
		int ChannelAddrBase = 0x40 + Channel * 8;

		if (!m_bGate)
			Volume = 0;

		if (m_bLoadWave && m_bGate) {
			m_bLoadWave = false;
			loadWave();
		}

		// Update channel
		writeData(ChannelAddrBase + 0, Frequency & 0xFF);
		writeData(ChannelAddrBase + 2, (Frequency >> 8) & 0xFF);
		writeData(ChannelAddrBase + 4, (WaveSize << 2) | ((Frequency >> 16) & 0x03));
		writeData(ChannelAddrBase + 6, m_iWavePos);
		writeData(ChannelAddrBase + 7, (m_iChannels << 4) | Volume);

		if (m_bResetPhase) {
			m_bResetPhase = false;
			writeData(ChannelAddrBase + 1, 0);
			writeData(ChannelAddrBase + 3, 0);
			writeData(ChannelAddrBase + 5, 0);
		}
	}

	protected void clearRegisters() {
		int Channel = getIndex();
		int ChannelAddrBase = 0x40 + Channel * 8;

		writeReg(ChannelAddrBase + 7, (m_iChannels << 4) | 0);

		m_bLoadWave = false;
		m_iDutyPeriod = 0;
	}

	private void writeReg(int reg, int value) {
		writeExternalRegister(0xF800, (byte) reg);
		writeExternalRegister(0x4800, (byte) value);
	}

	private void setAddress(int addr, boolean autoInc) {
		writeExternalRegister(0xF800, (byte) ((autoInc ? 0x80 : 0) | addr));
	}

	private void writeData(int data) {
		writeExternalRegister(0x4800, (byte) data);
	}

	private void writeData(int addr, int data) {
		setAddress(addr, false);
		writeData(data);
	}

	private void loadWave() {
		FamiTrackerDoc pDocument = m_pSoundGen.getDocument();

		if (m_iInstrument == MAX_INSTRUMENTS)
			return;

		// Fill the wave RAM
		Instrument inst = pDocument.getInstrument(m_iInstrument);
		if (inst == null || !(inst instanceof InstrumentN163)) {
			return;
		}
		InstrumentN163 pInstrument = (InstrumentN163) inst;

		// Start of wave in memory
		// int channel = getIndex(); // unused
		int startAddr = m_iWavePos >> 1;

		setAddress(startAddr, true);

		if (m_iWaveIndex >= m_iWaveCount)
			m_iWaveIndex = m_iWaveCount - 1;

		for (int i = 0; i < m_iWaveLen; i += 2) {
			writeData((pInstrument.getSample(m_iWaveIndex, i + 1) << 4) | pInstrument.getSample(m_iWaveIndex, i));
		}
	}

	private void checkWaveUpdate() {
		// Check wave changes
//		if (theApp.GetSoundGenerator()->HasWaveChanged())
//			m_bLoadWave = true;
	}
	
	private boolean m_bLoadWave;
	private int m_iChannels;
	private int m_iWaveLen;
	private int m_iWavePos;
	private int m_iWaveIndex;
	private int m_iWaveCount;
	protected int m_iPostEffect;
	protected int m_iPostEffectParam;

	protected boolean m_bResetPhase;

}
