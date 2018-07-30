package com.zdream.famitracker.sound;

import com.zdream.famitracker.FamiTrackerDoc;
import com.zdream.famitracker.components.Settings;
import com.zdream.famitracker.document.DSample;
import com.zdream.famitracker.document.Sequence;
import com.zdream.famitracker.document.StChanNote;
import com.zdream.famitracker.sound.channels.ChannelHandler;
import com.zdream.famitracker.sound.channels.DPCMChan;
import com.zdream.famitracker.sound.channels.MMC5Square1Chan;
import com.zdream.famitracker.sound.channels.MMC5Square2Chan;
import com.zdream.famitracker.sound.channels.NoiseChan;
import com.zdream.famitracker.sound.channels.Square1Chan;
import com.zdream.famitracker.sound.channels.Square2Chan;
import com.zdream.famitracker.sound.channels.TriangleChan;
import com.zdream.famitracker.sound.channels.VRC6Sawtooth;
import com.zdream.famitracker.sound.channels.VRC6Square1;
import com.zdream.famitracker.sound.channels.VRC6Square2;
import com.zdream.famitracker.sound.emulation.APU;
import com.zdream.famitracker.sound.emulation.Mixer;
import com.zdream.famitracker.test.BytesPlayer;
import com.zdream.famitracker.test.FamitrackerLogger;

import static com.zdream.famitracker.FamitrackerTypes.*;
import static com.zdream.famitracker.sound.emulation.Types.*;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.zdream.famitracker.FamiTrackerApp;

import static com.zdream.famitracker.FamiTrackerDoc.*;

public class SoundGen implements IAudioCallback {
	
	static Logger log = Logger.getLogger("com.zdream.famitracker.sound.SoundGen");

	/**
	 * 96 available notes
	 */
	public static final int NOTE_COUNT = 96;
	
	public static final int 
		VIBRATO_LENGTH = 256,
		TREMOLO_LENGTH = 256;
	
	public static final double[] NEW_VIBRATO_DEPTH = {
			1.0, 1.5, 2.5, 4.0, 5.0, 7.0, 10.0, 12.0, 14.0, 17.0, 22.0, 30.0, 44.0, 64.0, 96.0, 128.0
	};
	public static final double[] OLD_VIBRATO_DEPTH = {
			1.0, 1.0, 2.0, 3.0, 4.0, 7.0, 8.0, 15.0, 16.0, 31.0, 32.0, 63.0, 64.0, 127.0, 128.0, 255.0
	};
	
	/**
	 * <p>MODE_PLAY: Play from top of pattern
	 * <p>MODE_PLAY_START: Play from start of song
	 * <p>MODE_PLAY_REPEAT: Play and repeat
	 * <p>MODE_PLAY_CURSOR: Play from cursor
	 * <p>MODE_PLAY_FRAME: Play frame
	 */
	public static final byte
		MODE_PLAY = 0,
		MODE_PLAY_START = 1,
		MODE_PLAY_REPEAT = 2,
		MODE_PLAY_CURSOR = 3,
		MODE_PLAY_FRAME = 4;
	
	/**
	 * 音频文档
	 */
	FamiTrackerDoc m_pDocument;
	/**
	 * 各个轨道
	 */
	ChannelHandler[] m_pChannels = new ChannelHandler[CHANNELS];
	TrackerChannel[] m_pTrackerChannels = new TrackerChannel[CHANNELS];
	/**
	 * 其它部分
	 */
//	DSound m_pDSound;
//	DSoundChannel m_pDSoundChannel;
	APU m_pAPU;
	SampleMem m_pSampleMem;
	boolean m_bRunning;
	DSample m_pPreviewSample;
	
	int m_iTempo, m_iSpeed;
	
	
	/**
	 * <p>它是一个阈值, 和 effect: Fxx (变速) 有关.
	 * 当 Fxx 效果要实现时, 系统将判断它的参数 xx 是否大于 m_iSpeedSplitPoint;
	 * <p>如果 xx 大于 m_iSpeedSplitPoint 则判定 Fxx 修改的是 {@link #m_iTempo},
	 * 反之修改的是 {@link #m_iSpeed}
	 */
	int m_iSpeedSplitPoint;
	
	int m_iPlayTicks;
	boolean m_bPlaying;
	
	
	/**
	 * 一般而言, 当它为 true 时, 就是音乐停止, 等待播放的时候.
	 */
	boolean m_bHaltRequest;
	
	/**
	 * <p>用来记录 {@link #runFrame()} 这个方法每次调用, 音乐需要往下走几行.<br>
	 * 这时 {@link #m_bUpdateRow} 一定为 true.
	 */
	int m_iStepRows;
	
	/**
	 * {@link #runFrame()} 这个方法调用了, 但是内容仍不明确
	 */
	int m_iTempoAccum;
	
	/**
	 * {@link #runFrame()} 这个方法调用了, 但是内容仍不明确
	 */
	@SuppressWarnings("unused")
	private int m_iTempoFrames;
	
	/**
	 * 效果 jump (Bxx) 就是跳到另一个 pattern 的开头进行播放. 这里进行暂存数据
	 */
	int m_iJumpToPattern;
	/**
	 * 效果 skip (Dxx) 就是跳到下一个 pattern 的第 m_iSkipToRow 行开始播放. 这里进行暂存数据
	 */
	int m_iSkipToRow;
	
	// Play control
	byte m_iPlayMode;
	
	int[] m_iVibratoTable = new int[VIBRATO_LENGTH];
	
	/**
	 * NTSC/PAL
	 */
	byte m_iMachineType;
	
	// Tracker player variables
	/**
	 * Size of samples, in bits
	 */
	int m_iSampleSize;
	/**
	 * Buffer size in samples
	 */
	int m_iBufSizeSamples;
	/**
	 * Buffer size in bytes
	 */
	int m_iBufSizeBytes;
	/**
	 * This will point in samples
	 */
	int m_iBufferPtr;

	byte[] m_pAccumBuffer;
	short[] m_iGraphBuffer;
	/**
	 * Keep track of underruns to inform user
	 */
	int m_iAudioUnderruns;
	
	boolean m_bBufferTimeout;
	boolean m_bBufferUnderrun;
	boolean m_bAudioClipping;
	int m_iClipCounter;
	
	/**
	 * <p>Famitracker 有选项允许用户循环播放一个 frame. 这个选项就是开关.
	 * <p>当该参数为 true 时, 播放器将重复播放同一个 frame, 而不会播放其它 frame.
	 */
	boolean m_bPlayLooping;
	
	/**
	 * <p>这个程序非常麻烦的一点是, 有两个单位都交叫 frame;
	 * 一个是 Pattern 同等级的单位, 一个是音乐的帧的概念, 而这里使用的是后者;
	 * <p>你会看到, 它每执行一次 {@link #playFrame()}, 这个参数会加一.
	 */
	int m_iFrameCounter;
	
	// ****** Player state
	
	/**
	 * Queued frame
	 */
	int m_iQueuedFrame;
	/**
	 * 正在播放的曲目号
	 */
	int m_iPlayTrack;
	/**
	 * <p>现在正在播放的 frame 号码. frame 与 pattern 虽然不同, 但也可以相同.
	 * <p>Current frame to play
	 */
	int m_iPlayFrame;
	/**
	 * Current row to play
	 */
	int m_iPlayRow;
	/**
	 * Row/frame has changed
	 */
	boolean m_bDirty;
	/**
	 * Total number of frames played since start
	 */
	int m_iFramesPlayed;
	/**
	 * Total number of rows played since start
	 */
	int m_iRowsPlayed;
	/**
	 * true for each frame played
	 */
	boolean[] m_bFramePlayed = new boolean[MAX_FRAMES];
	
	// ****** Rendering
	/**
	 * 这两个参数是根据 {@link #m_iTempo} 和 {@link #m_iSpeed} 计算出来的
	 */
	int m_iTempoDecrement, m_iTempoRemainder;
	/**
	 * 判断最后一帧的值, 和 {@link #m_iPlayTicks} 比对
	 */
	int m_iRenderEndParam;
	boolean m_bRequestRenderStop;
	
	boolean m_bRendering;
	
	public final byte
			SONG_TIME_LIMIT = 0, 
			SONG_LOOP_LIMIT = 1;
	
	/**
	 * {@link #SONG_TIME_LIMIT} and {@link #SONG_LOOP_LIMIT}
	 */
	byte m_iRenderEndWhen;
	
	int m_iDelayedStart;
	int m_iDelayedEnd;
	int m_iRenderTrack;
	int m_iRenderRowCount;
	
	/**
	 * <p>记录了渲染的行数.
	 * <p>我跟踪了原来的 C++ 程序, 发现这个参数可以不从 0 开始,<br>
	 * 而且无论你换音乐、暂停、重播, 这个值都不会重置;<br>
	 * 换句话说, 这个值将不断递增.
	 */
	int m_iRenderRow = Integer.MIN_VALUE;
	
	/**
	 * <p>这个数据为 true 表示, {@link #runFrame()} 这个方法中检测发现, 音乐需要向下换行.<br>
	 * 这时 {@link #m_iStepRows} 一定大于零.
	 */
	boolean m_bUpdateRow;
	
	// ****** 其它
	
	/**
	 * <p>（NTSC）一秒 60 帧, 每帧要运行的时钟周期数: 29829. 这个值产生后就不会变化
	 * <p>Number of cycles/APU update
	 * </p>
	 */
	private int m_iUpdateCycles;
	/**
	 * Cycles consumed by the update registers functions
	 */
	int m_iConsumedCycles;
	
	/**
	 * <p>模块的帧速率
	 * <p>Module frame rate
	 */
	int m_iFrameRate;
	
	
	int[] m_pNoteLookupTable; // NTSC or PAL
	int[] m_iNoteLookupTableNTSC = new int[96]; // For 2A03
	int[] m_iNoteLookupTablePAL = new int[96]; // For 2A07
	int[] m_iNoteLookupTableSaw = new int[96]; // For VRC6 sawtooth
//	int[] m_iNoteLookupTableFDS[96];			// For FDS
//	int[] m_iNoteLookupTableN163[96];			// For N163
//	int[] m_iNoteLookupTableS5B[96];			// For sunsoft
//	int[] m_iVibratoTable[VIBRATO_LENGTH];

	/**
	 * @return {@link #m_bPlaying}
	 */
	public boolean isPlaying() {
		return m_bPlaying;
	}

	public FamiTrackerDoc getDocument() {
		return m_pDocument;
	}
	
	public SoundGen() {
		m_pSampleMem = new SampleMem();
		m_pAPU = new APU(this, m_pSampleMem);
		
		m_iQueuedFrame = -1;
		
		// TODO 这里将所有的轨道全部创建, 而实际上只需要建立需要的轨道就可以了
		createChannels();
	}

	/**
	 * 这个方法只会在初始化时调用一次
	 */
	void createChannels() {
		
		// 2A03 | 2A07
		assignChannel(new TrackerChannel("Pulse 1", SNDCHIP_NONE, CHANID_SQUARE1), new Square1Chan());
		assignChannel(new TrackerChannel("Pulse 2", SNDCHIP_NONE, CHANID_SQUARE2), new Square2Chan());
		assignChannel(new TrackerChannel("Triangle", SNDCHIP_NONE, CHANID_TRIANGLE), new TriangleChan());
		assignChannel(new TrackerChannel("Noise", SNDCHIP_NONE, CHANID_NOISE), new NoiseChan());
		assignChannel(new TrackerChannel("DPCM", SNDCHIP_NONE, CHANID_DPCM), new DPCMChan(m_pSampleMem));
		
		// Konami VRC6
		assignChannel(new TrackerChannel("Pulse 1", SNDCHIP_VRC6, CHANID_VRC6_PULSE1), new VRC6Square1());
		assignChannel(new TrackerChannel("Pulse 2", SNDCHIP_VRC6, CHANID_VRC6_PULSE2), new VRC6Square2());
		assignChannel(new TrackerChannel("Sawtooth", SNDCHIP_VRC6, CHANID_VRC6_SAWTOOTH), new VRC6Sawtooth());
		
		// Nintendo MMC5
		assignChannel(new TrackerChannel("Pulse 1", SNDCHIP_MMC5, CHANID_MMC5_SQUARE1), new MMC5Square1Chan());
		assignChannel(new TrackerChannel("Pulse 2", SNDCHIP_MMC5, CHANID_MMC5_SQUARE2), new MMC5Square2Chan());
		
		// TODO 其它芯片的暂时不管
		
	}
	
	/**
	 * 原方法: InitInstance
	 */
	void init() {
		assert(m_pDocument != null);

//		m_pDSound = new DSound();

		// Set running flag
		m_bRunning = true;

		// Generate default vibrato table
		generateVibratoTable(VIBRATO_NEW);
		
		// m_iExpansionChip
		setUpChip();

		resetAudioDevice();

		resetAPU();

		// Default tempo & speed
		m_iSpeed = DEFAULT_SPEED;
		m_iTempo = DEFAULT_TEMPO_NTSC;

		// 这个参数用来线程同步的, 因此这里并不需要
//		m_iDelayedStart = 0;
		m_iFrameCounter = 0;
		
		// 下面的方法在原来是不在这里执行的, 不过我把它们移过来了
		resetState();
		resetTempo();
		
		documentPropertiesChanged();
	}
	
	private void generateVibratoTable(int type) {
		for (int i = 0; i < 16; ++i) {	// depth 
			for (int j = 0; j < 16; ++j) {	// phase
				int value = 0;
				if (type == VIBRATO_NEW) {
					double angle = (j / 16.0) * (Math.PI / 2.0);
					value = (int) (Math.sin(angle) * NEW_VIBRATO_DEPTH[i]);
				} else {
					value = (int) (j * OLD_VIBRATO_DEPTH[i] / 16.0 + 1);
				}

				m_iVibratoTable[i * 16 + j] = value;
			}
		}
	}
	
	/**
	 * <p>Setup sound
	 */
	void resetAudioDevice() {
		Settings pSettings = FamiTrackerApp.getInstance().getSettings();

		int SampleSize = pSettings.sound.iSampleSize;
		int SampleRate = pSettings.sound.iSampleRate;
		int BufferLen = pSettings.sound.iBufferLength;
//		int Device = pSettings.sound.iDevice;

		m_iSampleSize = SampleSize;
		m_iAudioUnderruns = 0;
		m_iBufferPtr = 0;

		// Close the old sound channel
		closeAudioDevice();

//		if (Device >= m_pDSound.getDeviceCount()) {
//			// Invalid device detected, reset to 0
//			Device = 0;
//			pSettings.sound.iDevice = 0;
//		}
//
//		// Reinitialize direct sound
//		m_pDSound.setupDevice(Device);
//
//		int iBlocks = 2;	// default = 2
//
//		// Create more blocks if a bigger buffer than 100ms is used to reduce lag
//		if (BufferLen > 100)
//			iBlocks += (BufferLen / 66);
//
//		// Create channel
//		m_pDSoundChannel = m_pDSound.openChannel(SampleRate, SampleSize, 1, BufferLen, iBlocks);
//
//		// Channel failed
//		if (m_pDSoundChannel == null) {
//			throw new NullPointerException("m_pDSoundChannel = null");
//		}

		// Create a buffer
		m_iBufSizeBytes = 1000; // 这个数据在这里被强行指定
		m_iBufSizeSamples = m_iBufSizeBytes / (SampleSize / 8);

		// Temp. audio buffer
		m_pAccumBuffer = new byte[m_iBufSizeBytes];

		// Sample graph buffer
		m_iGraphBuffer = new short[m_iBufSizeSamples];

		// Sample graph rate
		// MFC 相关, 直接删掉

		m_pAPU.setupSound(SampleRate, 1, (m_iMachineType == NTSC) ? MACHINE_NTSC : MACHINE_PAL);

		m_pAPU.setChipLevel(Mixer.CHIP_LEVEL_APU1, (float) (pSettings.chipLevels.iLevelAPU1 / 10.0f));
		m_pAPU.setChipLevel(Mixer.CHIP_LEVEL_APU2, (float) (pSettings.chipLevels.iLevelAPU2 / 10.0f));
		m_pAPU.setChipLevel(Mixer.CHIP_LEVEL_VRC6, (float) (pSettings.chipLevels.iLevelVRC6 / 10.0f));
		m_pAPU.setChipLevel(Mixer.CHIP_LEVEL_VRC7, (float) (pSettings.chipLevels.iLevelVRC7 / 10.0f));
		m_pAPU.setChipLevel(Mixer.CHIP_LEVEL_MMC5, (float) (pSettings.chipLevels.iLevelMMC5 / 10.0f));
		m_pAPU.setChipLevel(Mixer.CHIP_LEVEL_FDS, (float) (pSettings.chipLevels.iLevelFDS / 10.0f));
		m_pAPU.setChipLevel(Mixer.CHIP_LEVEL_N163, (float) (pSettings.chipLevels.iLevelN163 / 10.0f));
		m_pAPU.setChipLevel(Mixer.CHIP_LEVEL_S5B, (float) (pSettings.chipLevels.iLevelS5B / 10.0f));
		
		// Update blip-buffer filtering 
		m_pAPU.setupMixer(pSettings.sound.iBassFilter, pSettings.sound.iTrebleFilter,  pSettings.sound.iTrebleDamping, pSettings.sound.iMixVolume);

		m_bAudioClipping = false;
		m_bBufferUnderrun = false;
		m_bBufferTimeout = false;
		m_iClipCounter = 0;

		log.log(Level.INFO,
				String.format("SoundGen: Created sound channel with params: %d Hz, %d bits, %d ms\n",
						SampleRate, SampleSize, BufferLen));
	}
	
	void closeAudioDevice() {
		// Kill DirectSound
//		if (m_pDSoundChannel != null) {
//			m_pDSoundChannel.stop();
//			m_pDSound.closeChannel(m_pDSoundChannel);
//			m_pDSoundChannel = null;
//		}
	}
	
	void resetAPU() {
		// Reset the APU
		m_pAPU.reset();

		// Enable all channels
		m_pAPU.write(0x4015, (byte) 0x0F);
		m_pAPU.write(0x4017, (byte) 0x00);

		// MMC5
		m_pAPU.externalWrite(0x5015, (byte) 0x03);

		m_pSampleMem.clear();
	}

	void assignChannel(TrackerChannel pTrackerChannel, ChannelHandler pRenderer) {
		byte id = pTrackerChannel.m_iChannelID;

		pRenderer.setChannelID(id);

		m_pTrackerChannels[id] = pTrackerChannel;
		m_pChannels[id] = pRenderer;
	}

	/**
	 * <p>分配音乐文档.
	 * <p>这个方法只会在初始化的时候调用一次, 后面不会再调用.
	 * @param doc
	 */
	public void assignDocument(FamiTrackerDoc doc) throws NullPointerException {
		assert(doc != null);
		
		m_pDocument = doc;
		
		// Setup all channels
		for (int i = 0; i < CHANNELS; ++i) {
			if (m_pChannels[i] != null)
				m_pChannels[i].initChannel(m_pAPU, m_iVibratoTable, this);
		}
	}
	
	public void documentPropertiesChanged() {
		assert(m_pDocument != null);

		for (int i = 0; i < CHANNELS; ++i) {
			if (m_pChannels[i] != null)
				m_pChannels[i].documentPropertiesChanged(m_pDocument);
		}
		
		m_iSpeedSplitPoint = m_pDocument.getSpeedSplitPoint();
	}

	public void registerChannels(byte chip, FamiTrackerDoc pDoc) {
		System.out.println("registerChannels");
		
		// This method will add channels to the document object, depending on the expansion chip used.
		// Called from the document object (from the main thread)
		
		// Clear all registered channels
		pDoc.resetChannels();

		// Register the channels in the document
		// Expansion & internal channels
		/*for (int i = 0; i < CHANNELS; ++i) {
			if (m_pTrackerChannels[i] != null && ((m_pTrackerChannels[i].getChip() & chip) || (i < 5))) {
				pDoc.registerChannel(m_pTrackerChannels[i], i, m_pTrackerChannels[i].getChip());
			}
		}*/
	}

	public void setSequencePlayPos(Sequence pSequence, int i) {
		// TODO Auto-generated method stub
		
	}

	public void evaluateGlobalEffects(StChanNote pNoteData, int effColumns) {
		// Handle global effects (effects that affects all channels)
		
		for (int i = 0; i < effColumns; ++i) {

			int effNum = pNoteData.effNumber[i];
			int effParam = pNoteData.effParam[i];

			switch (effNum) {
				// Fxx: Sets speed to xx
				case EF_SPEED:
					if (effParam == 0)
						++effParam;
					if (effParam >= m_iSpeedSplitPoint)
						m_iTempo = effParam;
					else
						m_iSpeed = effParam;
					setupSpeed();
					break;

				// Bxx: Jump to pattern xx
				case EF_JUMP:
					setJumpPattern(effParam);
					break;

				// Dxx: Skip to next track and start at row xx
				case EF_SKIP:
					setSkipRow(effParam);
					break;

				// Cxx: Halt playback
				case EF_HALT:
					m_bHaltRequest = true;
					if (m_bRendering) {
						// Unconditional stop
						++m_iFramesPlayed;
						m_bRequestRenderStop = true;
					}
					break;
			}
		}
		
	}

	/**
	 * ChannelHandler 调用
	 * @return
	 */
	public int getDefaultInstrument() {
		FamitrackerLogger.instance.logToDo("调用了还没有完成的方法");
		return 0;
	}

	/**
	 * ChannelHandler 调用
	 * @param i
	 */
	public void addCycles(int count) {
		// Add APU cycles
		m_iConsumedCycles += count;
		m_pAPU.addTime(count);
	}

	/**
	 * ChannelHandler 调用. 现在已经是空方法
	 * @param i
	 */
	public void registerKeyState(int m_iChannelID, int note) {
		// TODO 发出消息
		// m_pTrackerView->PostMessage(WM_USER_NOTE_EVENT, Channel, Note);
	}
	
	/**
	 * 检查所有跳帧 (效果 Bxx, Dxx), 以及 Pattern 播放完这类需要切换 Pattern,<br>
	 * 或者更通常的, 当播放器检查一行播放完后, 移到下一行进行播放的情形.
	 */
	void checkControl() {
		// This function takes care of jumping and skipping
//		assert(m_pTrackerView != null); // MFC 相关

		if (isPlaying()) {
			// If looping, halt when a jump or skip command are encountered
			if (m_bPlayLooping) {
				if (m_iJumpToPattern != -1 || m_iSkipToRow != -1)
					m_iPlayRow = 0;
				else {
					while ((m_iStepRows--) != 0)
						playerStepRow();
				}
			} else {
				// Jump
				if (m_iJumpToPattern != -1)
					playerJumpTo(m_iJumpToPattern);
				// Skip
				else if (m_iSkipToRow != -1)
					playerSkipTo(m_iSkipToRow);
				// or just move on
				else {
					while ((m_iStepRows--) != 0)
						playerStepRow();
				}
			}

			m_iJumpToPattern = -1;
			m_iSkipToRow = -1;
		}
		
		// 下面 MFC 相关, 移除
		/*if (m_bDirty) {
			m_bDirty = false;
			if (!m_bRendering)
				m_pTrackerView->PostMessage(WM_USER_PLAYER, m_iPlayFrame, m_iPlayRow);
		}*/
	}

	/**
	 * 执行 Jump 这个效果, 跳到指定的 pattern 的开头播放
	 * @param frame
	 */
	void playerJumpTo(int frame) {
		final int frames = m_pDocument.getFrameCount(m_iPlayTrack);

		m_bFramePlayed[m_iPlayFrame] = true;

		m_iPlayFrame = frame;

		if (m_iPlayFrame >= frames)
			m_iPlayFrame = frames - 1;

		m_iPlayRow = 0;

		++m_iFramesPlayed;

		m_bDirty = true;
	}
	
	/**
	 * 执行 Skip 这个效果, 跳到下一个的 pattern 的指定行开始播放
	 * @param row
	 */
	void playerSkipTo(int row) {
		final int frames = m_pDocument.getFrameCount(m_iPlayTrack);
		final int rows = m_pDocument.getPatternLength(m_iPlayTrack);
		
		m_bFramePlayed[m_iPlayFrame] = true;

		if (++m_iPlayFrame >= frames) // 如果现在播放的 frame 是歌曲最后一个
			m_iPlayFrame = 0; // 则从歌曲最开头开始

		m_iPlayRow = row;

		if (m_iPlayRow >= rows)
			m_iPlayRow = rows - 1;

		++m_iFramesPlayed;

		m_bDirty = true;
	}

	/**
	 * 效果 Dxx: Skip to next track and start at row xx
	 * @param i
	 */
	public void setSkipRow(int i) {
		m_iSkipToRow = i;
	}

	/**
	 * <p>效果 JUMP, 将跳到一个新的 pattern 的开头进行播放
	 * @param i
	 */
	public void setJumpPattern(int i) {
		m_iJumpToPattern = i;
	}
	
	/**
	 * 重新计算 {@link #m_iTempoDecrement} 和 {@link #m_iTempoRemainder}
	 */
	private void setupSpeed() {
		int i = m_iTempo * 24;
		m_iTempoDecrement = i / m_iSpeed;
		m_iTempoRemainder = i % m_iSpeed;
	}
	
	/**
	 * <p>该方法是在判断一行播放完毕的情况下才会调用的.<br>
	 * 上一行播放完, 开始播放下一行.
	 * <p>如果检测到这是该 frame 的最后一行, 需要从下一个 frame 的第一行开始播放 (调用 {@link #playerStepFrame()}).
	 */
	void playerStepRow() {
		final int PatternLen = m_pDocument.getPatternLength(m_iPlayTrack);

		if (++m_iPlayRow >= PatternLen) {
			m_iPlayRow = 0;
			if (!m_bPlayLooping)
				playerStepFrame();
		}

		m_bDirty = true;
	}
	
	/**
	 * <p>该方法是在判断一个 frame 播放完毕的情况下才会调用的.<br>
	 * 上一个 frame 播放完, 开始播放下一个 frame.
	 * <p>如果检测到这是整首曲子最后一个 frame, 需要从曲子开头循环播放.
	 */
	void playerStepFrame() {
		final int frames = m_pDocument.getFrameCount(m_iPlayTrack);

		m_bFramePlayed[m_iPlayFrame] = true;

		if (m_iQueuedFrame == -1) {
			if (++m_iPlayFrame >= frames)
				m_iPlayFrame = 0;
		} else {
			m_iPlayFrame = m_iQueuedFrame;
			m_iQueuedFrame = -1;
		}

		++m_iFramesPlayed;

		m_bDirty = true;
	}
	
	// TODO 暂时用于测试
	public BytesPlayer player = new BytesPlayer();

	@Override
	public void flushBuffer(byte[] buffer, int offset, int length) {
//		System.out.println("length: " + (length));
		player.writeSamples(buffer, offset, length * 2); // 单声道, 16 位
		
		FamitrackerLogger.instance.logToDo("调用了还没有完成的方法");
	}

	public void ready(FamiTrackerDoc doc) {
		ready(doc, 0, 0);
	}

	public void ready(FamiTrackerDoc doc, int track, int selectFrame) {
		this.m_pDocument = doc;
		
		// 原来这个方法要在新的线程的开始调用的, 但现在并不这样.
		init();
		
		beginPlayer(MODE_PLAY_START, track, selectFrame);
		
		m_iJumpToPattern = -1;
		
		// CSoundGen::LoadMachineSettings
		loadMachineSettings();
	}
	
	void beginPlayer(byte mode, int track, int selectFrame) {
		switch (mode) {
			// Play from top of pattern
			case MODE_PLAY:
				m_bPlayLooping = false;
				m_iPlayFrame = selectFrame;
				m_iPlayRow = 0;
				break;
			// Repeat pattern
			case MODE_PLAY_REPEAT:
				m_bPlayLooping = true;
				m_iPlayFrame = selectFrame;
				m_iPlayRow = 0;
				break;
			// Start of song
			case MODE_PLAY_START:
				m_bPlayLooping = false;
				m_iPlayFrame = 0;
				m_iPlayRow = 0;
				break;
			// From cursor
			// 下面那个视为无效
			/*case MODE_PLAY_CURSOR:
				m_bPlayLooping = false;
				m_iPlayFrame = selectFrame;
				m_iPlayRow = m_pTrackerView->GetSelectedRow();
				break;*/
		}

		m_bPlaying = true;
		m_bHaltRequest = false;
		m_iPlayTicks = 0;
		m_iFramesPlayed = 0;
		m_iJumpToPattern = -1;
		m_iSkipToRow = -1;
		m_bUpdateRow = true;
		m_iPlayMode = mode;
		m_bDirty = true;
		m_iPlayTrack = track;

		Arrays.fill(m_bFramePlayed, false);

		resetTempo();
		resetAPU();

		makeSilent();
	}
	
	// TODO 测试参数
	int count;

	public void checkFinish() {
		count++;
		
		/*if (count > 600) {
			System.out.println(600);
			haltPlayer();
		}*/
	}

	/**
	 * 就是 c++ 文件中的 OnIdle
	 */
	public void playFrame() {
		
		++m_iFrameCounter;
		FamitrackerLogger.instance.notifyFrame(m_iFrameCounter, m_iPlayFrame, m_iPlayRow);
		
		// Read module framerate
		m_iFrameRate = m_pDocument.getFrameRate();
		
		runFrame();
		
		playChannelNotes();
		
		// Update player
		updatePlayer();

		// Channel updates (instruments, effects etc)
		updateChannels();
		
		// Update APU registers
		updateAPU();
		
		if (m_bHaltRequest) {
			// Halt has been requested, abort playback here
			haltPlayer();
		}

		// Rendering
		if (m_bRendering && m_bRequestRenderStop) {
			if (m_iDelayedEnd == 0)
				stopRendering();
			else
				--m_iDelayedEnd;
		}

		if (m_iDelayedStart > 0) {
			--m_iDelayedStart;
			if (m_iDelayedStart == 0) {
				log.log(Level.INFO, "开始渲染");
			}
		}

		// Check if a previewed sample should be removed
		if (m_pPreviewSample != null && previewDone()) {
			m_pPreviewSample = null;
		}
		
		FamitrackerLogger.instance.logToDo("后面要写这些");
	}

	private void runFrame() {
		// m_pTrackerView.playerTick();

		if (m_bPlaying) {
			++m_iPlayTicks;

			if (m_bRendering) {
				if (m_iRenderEndWhen == SONG_TIME_LIMIT) {
					if (m_iPlayTicks >= m_iRenderEndParam)
						m_bRequestRenderStop = true;
				} else if (m_iRenderEndWhen == SONG_LOOP_LIMIT) {
					if (m_iFramesPlayed >= m_iRenderEndParam)
						m_bRequestRenderStop = true;
				}
				if (m_bRequestRenderStop)
					m_bHaltRequest = true;
			}

			m_iStepRows = 0;

			// Fetch next row
			if (m_iTempoAccum <= 0) {
				// Enable this to skip rows on high tempos
				m_iStepRows++;
				m_iTempoFrames = 0;
				
				m_bUpdateRow = true;
				readPatternRow();
				++m_iRenderRow;
			} else {
				m_bUpdateRow = false;
			}
		}

		// TODO 测试使用
		if (m_iPlayRow == 7) {
			m_iPlayRow += 0;
		}
		
	}

	void playChannelNotes() {
		// Feed queued notes into channels
		final int channels = m_pDocument.getChannelCount();

		// Read notes
		for (int i = 0; i < channels; ++i) {
			byte channel = m_pDocument.getChannelType(i);
			
			// Run auto-arpeggio, if enabled
//			int arpeggio = m_pTrackerView->GetAutoArpeggio(i);
//			if (Arpeggio > 0) {
//				m_pChannels[channel].arpeggiate(0);
//			}

			// Check if new note data has been queued for playing
			if (m_pTrackerChannels[channel].newNoteData()) {
				StChanNote note = m_pTrackerChannels[channel].getNote();
				FamitrackerLogger.instance.logNote(note, i, m_iPlayFrame, m_iPlayRow);
				playNote(channel, note, m_pDocument.getEffColumns(m_iPlayTrack, i) + 1);
			}

			// Pitch wheel
			int pitch = m_pTrackerChannels[channel].getPitch();
			m_pChannels[channel].setPitch(pitch);

			// Update volume meters
			m_pTrackerChannels[channel].setVolumeMeter(m_pAPU.getVol(channel));
		}

		// Instrument sequence visualization
		// 这部分代码和 MFC 相关, 所以去掉.
		
	}

	/**
	 * 更新播放器的状态
	 */
	private void updatePlayer() {
		if (m_bUpdateRow && !m_bHaltRequest)
			checkControl();

		if (m_bPlaying) {
			if (m_iTempoAccum <= 0) {
				int TicksPerSec = m_pDocument.getFrameRate();
				m_iTempoAccum += (60 * TicksPerSec) - m_iTempoRemainder;
			}
			m_iTempoAccum -= m_iTempoDecrement;
			++m_iTempoFrames;
		}
	}

	private void updateChannels() {
		// Update channels
		for (int i = 0; i < CHANNELS; ++i) {
			if (m_pChannels[i] != null) {
				if (m_bHaltRequest)
					m_pChannels[i].resetChannel();
				else
					m_pChannels[i].processChannel();
			}
		}
	}

	/**
	 * Write to APU registers
	 */
	private void updateAPU() {
		// final int CHANNEL_DELAY = 250;

		m_iConsumedCycles = 0;

		// Copy wave changed flag TODO 下面两行代码与 FDS & N163 相关
		// m_bInternalWaveChanged = m_bWaveChanged;
		// m_bWaveChanged = false;

		// Update APU channel registers
		for (int i = 0; i < CHANNELS; ++i) {
			if (m_pChannels[i] != null) {
				m_pChannels[i].refreshChannel();
				m_pAPU.process();
				// Add some delay between each channel update
				/*if (m_iFrameRate == APU.FRAME_RATE_NTSC || m_iFrameRate == APU.FRAME_RATE_PAL)
					addCycles(CHANNEL_DELAY);*/
			}
		}

		// Finish the audio frame
		m_pAPU.addTime(m_iUpdateCycles - m_iConsumedCycles);
		m_pAPU.process();
	}
	
	private void readPatternRow() {
		final int Channels = m_pDocument.getChannelCount();

		for (int i = 0; i < Channels; ++i) {
			StChanNote NoteData = playerGetNote(m_iPlayTrack, m_iPlayFrame, i, m_iPlayRow);
			if (NoteData != null)
				queueNote(m_pDocument.getChannelType(i), NoteData, TrackerChannel.NOTE_PRIO_1);
		}
	}

	/**
	 * 这个方法原本在 CFamiTrackerView 类中.
	 * @param track
	 * @param frame
	 * @param channel
	 * @param row
	 * @return
	 */
	StChanNote playerGetNote(int track, int frame, int channel, int row) {
		StChanNote note = m_pDocument.getNoteData(track, frame, channel, row);
		
		/*if (!isChannelMuted(channel)) {
			// Let view know what is about to play
			playerPlayNote(channel, note);
		} else {
			// These effects will pass even if the channel is muted
			final int PASS_EFFECTS[] = {EF_HALT, EF_JUMP, EF_SPEED, EF_SKIP};
			int Columns = pDoc.getEffColumns(track, channel) + 1;
			
			note.note = HALT;
			note.octave = 0;
			note.instrument = 0;

			for (int j = 0; j < Columns; ++j) {
				bool Clear = true;
				for (int k = 0; k < 4; ++k) {
					if (NoteData.EffNumber[j] == PASS_EFFECTS[k]) {
						ValidCommand = true;
						Clear = false;
					}
				}
				if (Clear)
					NoteData.EffNumber[j] = EF_NONE;
			}
		}*/
		
		// playerPlayNote(channel, note);
		
		return note;
	}
	
	/**
	 * @param channel
	 *   轨道号, 而非轨道序号
	 * @param note
	 * @param prior
	 */
	private void queueNote(byte channel, StChanNote note, int prior) {
		// Queue a note for play
		TrackerChannel ch = this.m_pTrackerChannels[channel];
		if (ch == null) {
			return;
		}
		ch.setNote(note, prior);
	}

	/**
	 * Cxx 效果的实现, 就是不让唱下去了, 立刻停止
	 */
	void haltPlayer() {
		// Move player to non-playing state
		m_bPlaying = false;
		m_bHaltRequest = false;

		makeSilent();

		m_pSampleMem.setMem(0, 0);
		
		// Signal that playback has stopped
		// MFC 相关, 已删除
	}
	
	public final boolean isRendering() {
		return m_bRendering;
	}
	
	void stopRendering() {
		assert(m_bRendering);

		if (!isRendering())
			return;

		m_bPlaying = false;
		m_bRendering = false;
		m_iPlayFrame = 0;
		m_iPlayRow = 0;
//		m_wfWaveFile.CloseFile();

		makeSilent();
		resetBuffer();
	}
	
	void resetBuffer() {
		m_iBufferPtr = 0;
		
		FamitrackerLogger.instance.logToDo("这里不幸地和 DSoundChannel 产生了关系: ");

//		if (m_pDSoundChannel)
//			m_pDSoundChannel->ClearBuffer();

		m_pAPU.reset();
	}
	
	private void playNote(byte channel, StChanNote note, int effCount) {
		m_pChannels[channel].playNote(note, effCount);
	}
	
	public final boolean previewDone() {
		return (m_pAPU.DPCMPlaying() == false);
	}
	
	void makeSilent() {
		m_pAPU.reset();
		m_pSampleMem.clear();

		for (int i = 0; i < CHANNELS; ++i) {
			if (m_pChannels[i] != null)
				m_pChannels[i].resetChannel();
			if (m_pTrackerChannels[i] != null)
				m_pTrackerChannels[i].reset();
		}
	}
	
	void resetState() {
		m_iPlayTrack = 0;
	}
	
	void resetTempo() {
		assert(m_pDocument != null);

		m_iSpeed = m_pDocument.getSongSpeed(m_iPlayTrack);
		m_iTempo = m_pDocument.getSongTempo(m_iPlayTrack);
		
		setupSpeed();
		m_iTempoAccum = 0;
		m_iTempoFrames = 0;

		m_bUpdateRow = false;
	}
	
	private void loadMachineSettings() {
		byte machine = this.m_pDocument.getMachine();
		int rate = m_pDocument.getFrameRate();
		
		FamitrackerLogger.instance.logToDo("rate: " + rate);
		
		final double BASE_FREQ = 32.7032;
		int baseFreq = (machine == NTSC) ? APU.BASE_FREQ_NTSC  : APU.BASE_FREQ_PAL;
		int defaultRate = (machine == NTSC) ? APU.FRAME_RATE_NTSC : APU.FRAME_RATE_PAL;
		
		m_iMachineType = machine;
		m_pAPU.changeMachine(machine == NTSC ? MACHINE_NTSC : MACHINE_PAL);
		
		// Choose a default rate if not predefined
		if (rate == 0)
			rate = defaultRate;

		double clock_ntsc = APU.BASE_FREQ_NTSC / 16.0;
		double clock_pal = APU.BASE_FREQ_PAL / 16.0;
		
		for (int i = 0; i < NOTE_COUNT; ++i) {
			// Frequency (in Hz)
			double freq = BASE_FREQ * Math.pow(2.0, i / 12.0);
			double pitch;

			// 2A07
			pitch = (clock_pal / freq) - 0.5;
			m_iNoteLookupTablePAL[i] = (int) pitch;

			// 2A03 / MMC5 / VRC6
			pitch = (clock_ntsc / freq) - 0.5;
			m_iNoteLookupTableNTSC[i] = (int)pitch;

			// VRC6 Saw
			pitch = ((clock_ntsc * 16.0) / (freq * 14.0)) - 0.5;
			m_iNoteLookupTableSaw[i] = (int) pitch;

			// FDS
//			m_iNoteLookupTableFDS[i] = (int) pitch;
//			pitch = (freq * 65536.0) / (clock_ntsc / 1.0) + 0.5;

			// N163
//			pitch = (Freq * double(NamcoChannels) * 983040.0) / clock_ntsc;
//			m_iNoteLookupTableN163[i] = (int)(pitch) / 4;

//			if (m_iNoteLookupTableN163[i] > 0xFFFF)	// 0x3FFFF
//				m_iNoteLookupTableN163[i] = 0xFFFF;	// 0x3FFFF

			// Sunsoft 5B
//			pitch = (clock_ntsc / Freq) - 0.5;
//			m_iNoteLookupTableS5B[i] = (unsigned int)pitch;
		}
		
		if (machine == NTSC)
			m_pNoteLookupTable = m_iNoteLookupTableNTSC;
		else
			m_pNoteLookupTable = m_iNoteLookupTablePAL;
		
		// Number of cycles between each APU update
		m_iUpdateCycles = baseFreq / rate;
		
		// Setup note tables
		m_pChannels[CHANID_SQUARE1].setNoteTable(m_pNoteLookupTable);
		m_pChannels[CHANID_SQUARE2].setNoteTable(m_pNoteLookupTable);
		m_pChannels[CHANID_TRIANGLE].setNoteTable(m_pNoteLookupTable);

		// VRC6
		m_pChannels[CHANID_VRC6_PULSE1].setNoteTable(m_iNoteLookupTableNTSC);
		m_pChannels[CHANID_VRC6_PULSE2].setNoteTable(m_iNoteLookupTableNTSC);
		m_pChannels[CHANID_VRC6_SAWTOOTH].setNoteTable(m_iNoteLookupTableSaw);
		
		// MMC5
		m_pChannels[CHANID_MMC5_SQUARE1].setNoteTable(m_iNoteLookupTableNTSC);
		m_pChannels[CHANID_MMC5_SQUARE2].setNoteTable(m_iNoteLookupTableNTSC);
		
//		省略 FDS N163
		
	}
	
	private void setUpChip() {
		m_pAPU.setExternalSound(m_pDocument.getExpansionChip());
		
		m_pAPU.write(0x4015, (byte) 0x0F);
		m_pAPU.write(0x4017, (byte) 0x00);
		
		// MMC5
		m_pAPU.externalWrite(0x5015, (byte) 0x03);
	}
	
}
