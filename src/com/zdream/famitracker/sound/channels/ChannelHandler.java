package com.zdream.famitracker.sound.channels;

import com.zdream.famitracker.FamiTrackerDoc;
import com.zdream.famitracker.document.StChanNote;
import com.zdream.famitracker.sound.SoundGen;
import com.zdream.famitracker.sound.emulation.APU;

import static com.zdream.famitracker.FamitrackerTypes.*;

/**
 * <p>轨道音乐渲染的基础类 / 父类
 * <p>Base class for channel renderers
 * @author Zdream
 */
public abstract class ChannelHandler extends SequenceHandler {

	public ChannelHandler(int maxPeriod, int maxVolume) {
		m_iLastInstrument = MAX_INSTRUMENTS;
		m_iMaxPeriod = maxPeriod;
		m_iMaxVolume = maxVolume;
	}

	@Override
	protected int triggerNote(int note) {
		note = Math.min(note, SoundGen.NOTE_COUNT - 1);
		note = Math.max(note, 0);

		// Trigger a note, return note period
		registerKeyState(note); // 空方法

		if (m_pNoteLookupTable == null)
			return note;

		return m_pNoteLookupTable[note];
	}

	@Override
	protected void setVolume(int volume) {
		m_iSeqVolume = limitVolume(volume);
	}

	@Override
	protected void setPeriod(int period) {
		m_iPeriod = limitPeriod(period);
		m_bPeriodUpdated = true;
	}

	@Override
	protected int getPeriod() {
		return m_iPeriod;
	}

	@Override
	protected void setNote(int note) {
		m_iNote = note;
	}

	@Override
	protected int getNote() {
		return m_iNote;
	}

	@Override
	protected void setDutyPeriod(int period) {
		assert(period < 128); // 否则转成负数
		m_iDutyPeriod = (byte) period;
	}

	@Override
	protected boolean isActive() {
		return m_bGate;
	}

	@Override
	protected boolean isReleasing() {
		return m_bRelease;
	}
	
	/**
	 * <p>播放音符, 并且会调用派生类
	 * <p>Plays a note, calls the derived classes
	 * <p>Handle common things before letting the channels play the notes
	 * @param pNoteData
	 * @param effColumns
	 */
	public void playNote(StChanNote pNoteData, int effColumns) {
		assert (pNoteData != null);

		// Handle delay commands
		if (handleDelay(pNoteData, effColumns))
			return;

		// Handle global effects
		m_pSoundGen.evaluateGlobalEffects(pNoteData, effColumns);

		// Let the channel play
		handleNoteData(pNoteData, effColumns);
	}

	/**
	 * <p>初始化
	 * <p>Called from main thread
	 * @param pAPU
	 * @param pVibTable
	 * @param pSoundGen
	 */
	public void initChannel(APU pAPU, int[] pVibTable, SoundGen pSoundGen) {
		m_pAPU = pAPU;
		m_pVibratoTable = pVibTable;
		m_pSoundGen = pSoundGen;

		m_bDelayEnabled = false;

		m_iEffect = 0;

		documentPropertiesChanged(pSoundGen.getDocument());

		resetChannel();
	}
	
	public void arpeggiate(int note) {
		setPeriod(triggerNote(note));
	}

	public void documentPropertiesChanged(FamiTrackerDoc pDoc) {
		m_bNewVibratoMode = (pDoc.getVibratoStyle() == VIBRATO_NEW);
		m_bLinearPitch = pDoc.getLinearPitch();
	}
	
	/**
	 * <p>Run the instrument and effects, all default and common channel processing.
	 * <p>This gets called each frame
	 */
	public void processChannel() {
		updateDelay();
		updateNoteCut();
		updateVolumeSlide();
		updateVibratoTremolo();
		updateEffects();
	}
	
	/**
	 * Update channel registers
	 */
	public abstract void refreshChannel();
	
	/**
	 * Resets all state variables to default
	 */
	public void resetChannel() {
		// Resets the channel states (volume, instrument & duty)
		// Clears channel registers

		// Instrument 
		m_iInstrument		= MAX_INSTRUMENTS;
		m_iLastInstrument	= MAX_INSTRUMENTS;

		// Volume 
		m_iVolume			= VOL_COLUMN_MAX;

		m_iDefaultDuty		= 0;
		m_iSeqVolume		= 0;

		// Period
		m_iPeriod			= 0;
		m_iLastPeriod		= 0xFFFF;
		m_iPeriodPart		= 0;

		// Effect states
		m_iPortaSpeed		= 0;
		m_iPortaTo			= 0;
		m_iArpeggio			= 0;
		m_iArpState			= 0;
		m_iVibratoSpeed		= 0;
		m_iVibratoPhase		= !m_bNewVibratoMode ? 48 : 0;
		m_iTremoloSpeed		= 0;
		m_iTremoloPhase		= 0;
		m_iFinePitch		= 0x80;
		m_iPeriod			= 0;
		m_iVolSlide			= 0;
		m_bDelayEnabled		= false;
		m_iNoteCut			= 0;
		m_iVibratoDepth		= 0;
		m_iTremoloDepth		= 0;

		// States
		m_bRelease			= false;
		m_bGate				= false;

		registerKeyState(-1);

		// Clear channel registers
		clearRegisters();

		clearSequences();
	}

	/**
	 * Installs the note lookup table
	 * @param pNoteLookupTable
	 */
	public void setNoteTable(int[] pNoteLookupTable) {
		m_pNoteLookupTable = pNoteLookupTable;
	}
	public void updateSequencePlayPos() {};
	
	/**
	 * Pitch ranges from -511 to +512
	 * @param pitch
	 */
	public void setPitch(int pitch) {
		m_iPitch = pitch;
		if (m_iPitch == 512)
			m_iPitch = 511;
	}

	public void setChannelID(int id) {
		m_iChannelID = id;
	}
	
	protected abstract void clearRegisters();						// Clear channel registers

	/**
	 * 立即执行 note 里面的操作.
	 * 如果有 Gxx 延迟效果, 则延迟结束的那一帧执行该函数.
	 * @param pNoteData
	 * @param effColumns
	 */
	protected void handleNoteData(StChanNote pNoteData, int effColumns) {
		int lastInstrument = m_iInstrument;
		int instrument = pNoteData.instrument;
		int note = pNoteData.note;

		// Clear the note cut effect
		if (pNoteData.note != NOTE_NONE) {
			m_iNoteCut = 0;
		}

		// Effects
		for (int n = 0; n < effColumns; n++) {
			int effNum = pNoteData.effNumber[n];
			int effParam = pNoteData.effParam[n];
			handleCustomEffects(effNum, effParam);
		}

		// Volume
		if (pNoteData.vol < 0x10) {
			m_iVolume = pNoteData.vol << VOL_COLUMN_SHIFT;
		}

		// Instrument
		if (note == NOTE_HALT || note == NOTE_RELEASE) 
			instrument = MAX_INSTRUMENTS;	// Ignore instrument for release and halt commands

		if (instrument != MAX_INSTRUMENTS)
			m_iInstrument = instrument;

		boolean trigger = (note != NOTE_NONE) && (note != NOTE_HALT) && (note != NOTE_RELEASE);
		boolean newInstrument = (m_iInstrument != lastInstrument) || (m_iInstrument == MAX_INSTRUMENTS);

		if (m_iInstrument == MAX_INSTRUMENTS) {
			// No instrument selected, default to 0
			m_iInstrument = m_pSoundGen.getDefaultInstrument();
		}

		if (newInstrument || trigger) {
			if (!handleInstrument(m_iInstrument, trigger, newInstrument))
				return;
		}

		// 不知道为什么这个有两次, 暂且当成程序错误
		/*if (newInstrument || trigger) {
			if (!handleInstrument(m_iInstrument, trigger, newInstrument))
				return;
		}*/

		// Clear release flag
		if (pNoteData.note != NOTE_RELEASE && pNoteData.note != NOTE_NONE) {
			m_bRelease = false;
		}

		// Note
		switch (pNoteData.note) {
			case NOTE_NONE:
				handleEmptyNote();
				break;
			case NOTE_HALT:
				handleCut();
				break;
			case NOTE_RELEASE:
				handleRelease();
				break;
			default:
				handleNote(pNoteData.note, pNoteData.octave);
				break;
		}
	}

	// Pure virtual functions for handling notes
	protected abstract void handleCustomEffects(int effNum, int effParam);
	protected abstract boolean handleInstrument(int instrument, boolean trigger, boolean newInstrument);
	protected abstract void handleEmptyNote();
	protected abstract void handleCut();
	protected abstract void handleRelease();
	protected abstract void handleNote(int note, int octave);

	protected void setupSlide(int type, int effParam) {
		m_iPortaSpeed = ((effParam & 0xF0) >> 3) + 1;
		
		assert(type < 128); // 由于后面要进行强制转换
		m_iEffect = (byte) type;

		if (type == EF_SLIDE_UP)
			m_iNote = m_iNote + (effParam & 0xF);
		else
			m_iNote = m_iNote - (effParam & 0xF);

		m_iPortaTo = triggerNote(m_iNote);
	}

	/**
	 * 计算当前的音高. 实际计算的是波长.
	 * <br>const 方法
	 * @return
	 */
	protected int calculatePeriod() {
		return limitPeriod(getPeriod() - getVibrato() + getFinePitch() + getPitch());
	}
	
	protected final int calculateVolume() {
		// Volume calculation
		int volume = m_iVolume >> VOL_COLUMN_SHIFT; // [0, 15]

		volume = (m_iSeqVolume * volume) / 15 - getTremolo();
		volume = Math.max(volume, 0);
		volume = Math.min(volume, m_iMaxVolume);

		// 实际有声音的轨道（即总音量大于零, seq 音量大于零）, 至少保留 1 点音量
		if (m_iSeqVolume > 0 && m_iVolume > 0 && volume == 0)
			volume = 1;

		if (!m_bGate)
			volume = 0;

		return volume;
	}

	// 
	// Internal non-virtual functions
	//
	/**
	 * <p>Cut currently playing note
	 * <p>Called on note cut commands
	 * 
	 * <p>立刻结束现在播放的 note.
	 */
	protected void cutNote() {
		registerKeyState(-1);

		m_bGate = false;
		m_iPeriod = 0;
		m_iPortaTo = 0;
	}
	
	/**
	 * <p>Release currently playing note
	 * <p>Called on note release commands
	 */
	protected void releaseNote() {
		registerKeyState(-1);

		m_bRelease = true;
	}

	/**
	 * 限定波长的范围, 让波长最终落到 [0, {@link #m_iMaxPeriod}] 之间的数值中
	 * @param period
	 *   原始波长
	 * @return
	 *   修改后的波长. 在范围内的波长不做变动, 而在范围外的波长调整到 0 或 {@link #m_iMaxPeriod}
	 */
	protected final int limitPeriod(int period) {
		period = Math.min(period, m_iMaxPeriod);
		period = Math.max(period, 0);
		return period;
	}
	/**
	 * 限定音量的范围, 让波长最终落到 [0, 15] 之间的数值中
	 * @param period
	 *   原始音量（粗值）
	 * @return
	 *   修改后的音量（粗值）. 在范围内的音量不做变动, 而在范围外的音量调整到 0 或 15
	 */
	protected final int limitVolume(int volume) {
		volume = Math.min(volume, 15);
		volume = Math.max(volume, 0);
		return volume;
	}

	/**
	 * <p>可能为向外报告该 note 播放状态的改变.
	 * <p>注: 该方法现阶段没有实际功能
	 * </p>
	 * @param note
	 */
	protected void registerKeyState(int note) {
		m_pSoundGen.registerKeyState(m_iChannelID, note); // 这个是个空方法
	}

	/**
	 * Run the note and handle portamento
	 * @param octave
	 * @param note
	 * @return
	 */
	protected int runNote(int octave, int note) {
		int newNote = octave * 12 + note - 1;
		int nesFreq = triggerNote(newNote);

		if (m_iPortaSpeed > 0 && m_iEffect == EF_PORTAMENTO) {
			if (m_iPeriod == 0)
				m_iPeriod = nesFreq;
			m_iPortaTo = nesFreq;
		} else
			m_iPeriod = nesFreq;

		m_bGate = true;

		return newNote;
	}
	
	protected final int getPitch() {
		// 这个判断里面的内容, 在只有 2A03 和 VRC6 的情况下, 确定不会调用.
		if (m_iPitch != 0 && m_iNote != 0 && m_pNoteLookupTable != null) {
			// Interpolate pitch
			int lowNote  = Math.max(m_iNote - PITCH_WHEEL_RANGE, 0);
			int highNote = Math.min(m_iNote + PITCH_WHEEL_RANGE, 95);
			int freq	 = m_pNoteLookupTable[m_iNote];
			int lower	 = m_pNoteLookupTable[lowNote];
			int higher	 = m_pNoteLookupTable[highNote];
			int pitch	 = (m_iPitch < 0) ? (freq - lower) : (higher - freq);
			return (pitch * m_iPitch) / 511;
		}

		return 0;
	}

	/**
	 * Handle common effects for all channels
	 * @param effCmd
	 * @param effParam
	 * @return
	 */
	protected boolean checkCommonEffects(byte effCmd, byte effParam) {
		switch (effCmd) {
			case EF_PORTAMENTO:
				m_iPortaSpeed = effParam & 0xFF;
				m_iEffect = EF_PORTAMENTO;
				if (effParam == 0)
					m_iPortaTo = 0;
				break;
			case EF_VIBRATO:
				m_iVibratoDepth = (effParam & 0x0F) << 4;
				m_iVibratoSpeed = effParam >> 4;
				if (effParam == 0)
					m_iVibratoPhase = !m_bNewVibratoMode ? 48 : 0;
				break;
			case EF_TREMOLO:
				m_iTremoloDepth = (effParam & 0x0F) << 4;
				m_iTremoloSpeed = effParam >> 4;
				if (effParam == 0)
					m_iTremoloPhase = 0;
				break;
			case EF_ARPEGGIO:
				m_iArpeggio = effParam;
				m_iEffect = EF_ARPEGGIO;
				break;
			case EF_PITCH:
				m_iFinePitch = effParam & 0xFF;
				break;
			case EF_PORTA_DOWN:
				m_iPortaSpeed = effParam & 0xFF;
				m_iEffect = EF_PORTA_DOWN;
				break;
			case EF_PORTA_UP:
				m_iPortaSpeed = effParam & 0xFF;
				m_iEffect = EF_PORTA_UP;
				break;
			case EF_VOLUME_SLIDE:
				m_iVolSlide = effParam;
				break;
			case EF_NOTE_CUT:
				m_iNoteCut = (byte) (effParam + 1);
				break;
			default:
				return false;
		}
		
		return true;
	}
	
	/**
	 * <p>Note 的延迟操作, 效果号码是 Gxx
	 * <p>Handle note delay, Gxx
	 * @param pNoteData
	 * @param effColumns
	 * @return
	 */
	private boolean handleDelay(StChanNote pNoteData, int effColumns) {
		// 如果 m_bDelayEnabled = true 说明上一个有效的键 (note) 有延迟效果,
		// 但是还没等延迟触发, 新的键已经到来了, 这时, 上一个延迟触发的数据将立即执行
		if (m_bDelayEnabled) {
			m_bDelayEnabled = false;
			handleNoteData(m_cnDelayed, m_iDelayEffColumns);
		}
		
		// Check delay
		for (int i = 0; i < effColumns; ++i) {
			if (pNoteData.effNumber[i] == EF_DELAY && pNoteData.effParam[i] > 0) {
				m_bDelayEnabled = true;
				m_cDelayCounter = (byte) pNoteData.effParam[i];
				m_iDelayEffColumns = effColumns;
				m_cnDelayed.copyFrom(pNoteData);

				// Only one delay/row is allowed. Remove global effects
				for (int j = 0; j < effColumns; ++j) {
					switch (m_cnDelayed.effNumber[j]) {
						case EF_DELAY:
							m_cnDelayed.effNumber[j] = EF_NONE;
							m_cnDelayed.effParam[j] = 0;
							break;
						case EF_JUMP:
							m_pSoundGen.setJumpPattern(m_cnDelayed.effParam[j]);
							m_cnDelayed.effNumber[j] = EF_NONE;
							m_cnDelayed.effParam[j] = 0;
							break;
						case EF_SKIP:
							m_pSoundGen.setSkipRow(m_cnDelayed.effParam[j]);
							m_cnDelayed.effNumber[j] = EF_NONE;
							m_cnDelayed.effParam[j] = 0;
							break;
					}
				}
				return true;
			}
		}

		return false;
	}

	/**
	 * Vibrato offset (4xx)
	 * 颤音, 影响音高
	 * @return
	 */
	protected int getVibrato() {
		int vibFreq = 0;

		if ((m_iVibratoPhase & 0xF0) == 0x00)
			vibFreq = m_pVibratoTable[m_iVibratoDepth + m_iVibratoPhase];
		else if ((m_iVibratoPhase & 0xF0) == 0x10)
			vibFreq = m_pVibratoTable[m_iVibratoDepth + 15 - (m_iVibratoPhase - 16)];
		else if ((m_iVibratoPhase & 0xF0) == 0x20)
			vibFreq = -m_pVibratoTable[m_iVibratoDepth + (m_iVibratoPhase - 32)];
		else if ((m_iVibratoPhase & 0xF0) == 0x30)
			vibFreq = -m_pVibratoTable[m_iVibratoDepth + 15 - (m_iVibratoPhase - 48)];

		if (!m_bNewVibratoMode) {
			vibFreq += m_pVibratoTable[m_iVibratoDepth + 15] + 1;
			vibFreq >>= 1;
		}

		if (m_bLinearPitch)
			vibFreq = (getPeriod() * vibFreq) / 128;

		return vibFreq;
	}
	
	/**
	 * Tremolo offset (7xx)
	 * 颤音, 影响音量
	 * @return
	 */
	protected int getTremolo() {
		int tremVol = 0;
		int phase = m_iTremoloPhase >> 1;

		if ((phase & 0xF0) == 0x00)
			tremVol = m_pVibratoTable[m_iTremoloDepth + phase];
		else if ((phase & 0xF0) == 0x10)
			tremVol = m_pVibratoTable[m_iTremoloDepth + 15 - (phase - 16)];

		return (tremVol >> 1);
	}
	
	/**
	 * Fine pitch setting (Pxx)
	 * 设置音高 (Pxx)
	 * @return
	 */
	protected int getFinePitch() {
		return (0x80 - m_iFinePitch);
	}

	/**
	 * 用于让 APU 添加更多周期. 现没有被调用.
	 * @param count
	 */
	protected void addCycles(int count) {
		m_pSoundGen.addCycles(count);
	}

	protected void periodAdd(final int step) {
		if (m_bLinearPitch)
			linearAdd(step);
		else
			setPeriod(getPeriod() + step);
	}
	
	protected void periodRemove(final int step) {
		if (m_bLinearPitch)
			linearRemove(step);
		else
			setPeriod(getPeriod() - step);
	}

	protected void linearAdd(final int step) {
		m_iPeriod = (m_iPeriod << 5) | m_iPeriodPart;
		int value = (m_iPeriod * step) / 512;
		if (value == 0)
			value = 1;
		m_iPeriod += value;
		m_iPeriodPart = m_iPeriod & 0x1F;
		m_iPeriod >>= 5;
	}
	
	protected void linearRemove(final int step) {
		m_iPeriod = (m_iPeriod << 5) | m_iPeriodPart;
		int value = (m_iPeriod * step) / 512;
		if (value == 0)
			value = 1;
		m_iPeriod -= value;
		m_iPeriodPart = m_iPeriod & 0x1F;
		m_iPeriod >>= 5;
	}

	protected void writeRegister(int reg, byte value) {
		m_pAPU.write(reg, value);
		// m_pSoundGen.writeRegister(reg, value); 里面是 empty
	}
		
	protected void writeExternalRegister(int reg, byte value) {
		m_pAPU.externalWrite(reg, value);
		// m_pSoundGen.writeExternalRegister(reg, value); 里面是 empty
	}

	// CSequenceHandler virtual methods

	private void updateNoteCut() {
		// Note cut ()
		if (m_iNoteCut > 0) {
			m_iNoteCut--;
			if (m_iNoteCut == 0) {
				handleCut();
			}
		}
	}
	private void updateDelay() {
		// Delay (Gxx)
		if (m_bDelayEnabled) {
			if (m_cDelayCounter == 0) {
				m_bDelayEnabled = false;
				playNote(m_cnDelayed, m_iDelayEffColumns);
			}
			else
				m_cDelayCounter--;
		}
	}
	
	private void updateVolumeSlide() {
		// Volume slide (Axx)
		m_iVolume -= (m_iVolSlide & 0x0F);
		if (m_iVolume < 0)
			m_iVolume = 0;

		m_iVolume += (m_iVolSlide & 0xF0) >> 4;
		if (m_iVolume > VOL_COLUMN_MAX)
			m_iVolume = VOL_COLUMN_MAX;
	}
	
	private void updateVibratoTremolo() {
		// Vibrato and tremolo
		m_iVibratoPhase = (m_iVibratoPhase + m_iVibratoSpeed) & 63;
		m_iTremoloPhase = (m_iTremoloPhase + m_iTremoloSpeed) & 63;
	}
	
	private void updateEffects() {
		// Handle other effects
		switch (m_iEffect) {
			case EF_ARPEGGIO:
				if (m_iArpeggio != 0) {
					switch (m_iArpState) {
						case 0:
							setPeriod(triggerNote(m_iNote));
							break;
						case 1:
							setPeriod(triggerNote(m_iNote + (m_iArpeggio >> 4)));
							if ((m_iArpeggio & 0x0F) == 0)
								++m_iArpState;
							break;
						case 2:
							setPeriod(triggerNote(m_iNote + (m_iArpeggio & 0x0F)));
							break;
					}
					m_iArpState = (byte) ((m_iArpState + 1) % 3);
				}
				break;
			case EF_PORTAMENTO:
				// Automatic portamento
				if (m_iPortaSpeed > 0 && m_iPortaTo > 0) {
					if (m_iPeriod > m_iPortaTo) {
						periodRemove(m_iPortaSpeed);
						if (m_iPeriod < m_iPortaTo)
							setPeriod(m_iPortaTo);
					}
					else if (m_iPeriod < m_iPortaTo) {
						periodAdd(m_iPortaSpeed);
						if (m_iPeriod > m_iPortaTo)
							setPeriod(m_iPortaTo);
					}
				}
				break;
			case EF_SLIDE_UP:
				if (m_iPortaSpeed > 0) {
					periodRemove(m_iPortaSpeed);
					if (m_iPeriod < m_iPortaTo) {
						setPeriod(m_iPortaTo);
						m_iPortaTo = 0;
						m_iEffect = EF_NONE;
					}
				}
				break;
			case EF_SLIDE_DOWN:
				if (m_iPortaSpeed > 0) {
					periodAdd(m_iPortaSpeed);
					if (m_iPeriod > m_iPortaTo) {
						setPeriod(m_iPortaTo);
						m_iPortaTo = 0;
						m_iEffect = EF_NONE;
					}
				}
				break;
			case EF_PORTA_DOWN:
				if (getPeriod() > 0)
					periodAdd(m_iPortaSpeed);
				break;
			case EF_PORTA_UP:
				if (getPeriod() > 0)
					periodRemove(m_iPortaSpeed);
				break;
		}
	}

	/**
	 * Range for the pitch wheel command (in semitones)
	 */
	private static final int PITCH_WHEEL_RANGE = 6;

	public static final int VOL_COLUMN_SHIFT = 3;
	public static final int VOL_COLUMN_MAX = 0x7F;

	/**
	 * Channel ID
	 */
	protected int m_iChannelID;

	/**
	 * Note released flag
	 */
	protected boolean m_bRelease;
	
	/**
	 * Note gate flag
	 */
	protected boolean m_bGate;

	/**
	 * Instrument
	 */
	protected int m_iInstrument;
	
	/**
	 * Previous instrument
	 */
	protected int m_iLastInstrument;

	/**
	 * Active note
	 */
	protected int m_iNote;
	
	/**
	 * Channel period/frequency
	 */
	protected int m_iPeriod;
	
	/**
	 * Previous period
	 */
	protected int m_iLastPeriod;
	
	/**
	 * Sequence volume
	 */
	protected int m_iSeqVolume;
	
	/**
	 * Volume, 音量（细值）
	 * <br>据判断, 该值是粗值的 8 倍, 值域为 [0, 127]
	 */
	protected int m_iVolume;
	
	protected byte m_iDutyPeriod;

	/**
	 * <p>Used by linear slides
	 * <p>该值在 m_bLinearPitch 启用时有效.
	 * 它用于记录波长值的更小粒度单位数值, 即波长值的小数点位.
	 * 值域为 [0, 31], 逢 32 进 1, 进位表现在 m_iPeriod 波长值上.
	 * 也就是说, m_iPeriodPart 每计算达到 32, 则 m_iPeriod 加一.
	 * <p>在 m_bLinearPitch 启用时, 在原波长变化时, 并不是直接添加的,
	 * 而是利用方法 {@link #linearAdd(int)} 和 {@link #linearRemove(int)} 方法
	 * 更新 m_iPeriodPart 和 m_iPeriod.
	 * </p>
	 */
	protected int m_iPeriodPart;

	protected boolean m_bNewVibratoMode;
	
	/**
	 * <p>是否采用线性波长计算方式. 一般为 false.
	 * <p>当 m_bLinearPitch = true 时, m_iPeriodPart 将被启用.
	 * </p>
	 * @see #m_iPeriodPart
	 */
	protected boolean m_bLinearPitch;

	/**
	 * Flag for detecting new period value
	 */
	protected boolean m_bPeriodUpdated;
	
	/**
	 * Flag for detecting new volume value (currently unused)
	 */
	protected boolean m_bVolumeUpdate;

	// Delay effect variables
	/**
	 * Gxx 延迟的效果是否正在触发
	 */
	protected boolean m_bDelayEnabled;
	/**
	 * Gxx 延迟的计数器.
	 * 检测到 Gxx 存在后, 记录延迟的时间, 然后每一帧减一
	 */
	protected byte m_cDelayCounter;
	/**
	 * Gxx 延迟键的有效的效果列数
	 */
	protected int m_iDelayEffColumns;
	/**
	 * Gxx 延迟的键
	 */
	protected StChanNote m_cnDelayed = new StChanNote();

	// Vibrato & tremolo
	protected int m_iVibratoDepth;
	protected int m_iVibratoSpeed;
	/**
	 * 音高颤音的相位
	 */
	protected int m_iVibratoPhase;

	protected int m_iTremoloDepth;
	protected int m_iTremoloSpeed;
	/**
	 * 音量颤音的相位
	 */
	protected int m_iTremoloPhase;

	/**
	 * arpeggio & portamento
	 */
	protected byte m_iEffect;
	protected byte m_iArpeggio;
	protected byte m_iArpState;
	protected int m_iPortaTo;
	protected int m_iPortaSpeed;

	/**
	 * Note cut effect
	 */
	protected byte m_iNoteCut;

	/**
	 * Fine pitch effect
	 */
	protected int m_iFinePitch;

	/**
	 * Duty effect
	 */
	protected byte m_iDefaultDuty;

	/**
	 * Volume slide effect
	 */
	protected byte m_iVolSlide;

	// Misc 
	protected APU m_pAPU;
	protected SoundGen m_pSoundGen;

	/**
	 * Note->period table, 原先是个指针
	 * <br>这里记录的数组是, 每个音符（含音阶）, 在波形图中的波长（可能是相对值）.
	 * <br>它在 {@link SoundGen#loadMachineSettings()} 中创建. 
	 */
	protected int[] m_pNoteLookupTable;
	
	/**
	 * Vibrato table
	 */
	protected int[] m_pVibratoTable;

	/**
	 * Used by the pitch wheel
	 */
	protected int m_iPitch;

	/**
	 * Period register limit
	 */
	private int m_iMaxPeriod;
	
	/**
	 * Max channel volume
	 */
	private int m_iMaxVolume;

}
