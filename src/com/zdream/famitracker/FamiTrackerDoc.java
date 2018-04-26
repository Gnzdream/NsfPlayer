package com.zdream.famitracker;

import java.io.IOException;
import java.util.ArrayList;

import com.zdream.famitracker.components.DocumentFile;
import com.zdream.famitracker.document.DSample;
import com.zdream.famitracker.document.PatternData;
import com.zdream.famitracker.document.Sequence;
import com.zdream.famitracker.document.StChanNote;
import com.zdream.famitracker.document.instrument.Instrument;
import com.zdream.famitracker.document.instrument.Instrument2A03;
import com.zdream.famitracker.document.instrument.InstrumentFDS;
import com.zdream.famitracker.document.instrument.InstrumentN163;
import com.zdream.famitracker.document.instrument.InstrumentVRC6;
import com.zdream.famitracker.document.instrument.InstrumentVRC7;
import com.zdream.famitracker.holder.IntHolder;
import com.zdream.famitracker.older.StSequence;
import com.zdream.famitracker.sound.emulation.APU;

import static com.zdream.famitracker.FamitrackerTypes.*;
import static com.zdream.famitracker.document.instrument.Instrument.*;
import static com.zdream.famitracker.sound.emulation.Types.*;

/**
 * <p>这里面放置了整个 Famitracker 音频文档所包含的所有数据.
 * <p>不过我将源 C++ 文件中的和音频输出相关的方法和类全部移除并移到上层执行, 保证其独立性
 * @author Zdream
 */
public class FamiTrackerDoc {
	
	public static final int DEFAULT_TEMPO_NTSC = 150;
	public static final int DEFAULT_TEMPO_PAL = 125;
	public static final int DEFAULT_SPEED = 6;
	public static final int DEFAULT_MACHINE_TYPE = NTSC;
	public static final int DEFAULT_SPEED_SPLIT_POINT = 32;
	public static final int OLD_SPEED_SPLIT_POINT = 21;
	
	// Cursor columns
	public static final int
		C_NOTE = 0, 
		C_INSTRUMENT1 = 1, 
		C_INSTRUMENT2 = 2, 
		C_VOLUME = 3, 
		C_EFF_NUM = 4, 
		C_EFF_PARAM1 = 5, 
		C_EFF_PARAM2 = 6,
		C_EFF2_NUM = 7, 
		C_EFF2_PARAM1 = 8, 
		C_EFF2_PARAM2 = 9, 
		C_EFF3_NUM = 10, 
		C_EFF3_PARAM1 = 11, 
		C_EFF3_PARAM2 = 12, 
		C_EFF4_NUM = 13, 
		C_EFF4_PARAM1 = 14, 
		C_EFF4_PARAM2 = 15;

	public static final int COLUMNS = 7;
	
	/**
	 * View update modes (TODO check these and remove inappropriate flags)
	 */
	public static final int
			UPDATE_NONE = 0, // No update
			UPDATE_TRACK = 1, // Track has been added, removed or changed
			UPDATE_PATTERN = 2, // Pattern data has been edited
			UPDATE_FRAME = 3, // Frame data has been edited
			UPDATE_INSTRUMENT = 4, // Instrument has been added / removed
			UPDATE_PROPERTIES = 5, // Module properties has changed (including channel count)
			UPDATE_HIGHLIGHT = 6, // Row highlight option has changed
			UPDATE_COLUMNS = 7, // Effect columns has changed
			UPDATE_CLOSE = 8; // Document is closing (TODO remove)
	
	/**
	 * File blocks
	 */
	public static final int
		FB_INSTRUMENTS = 0,
		FB_SEQUENCES = 1,
		FB_PATTERN_ROWS = 2,
		FB_PATTERNS = 3,
		FB_SPEED = 4,
		FB_CHANNELS = 5,
		FB_DSAMPLES = 6,
		FB_EOF = 7,
		FB_MACHINE = 8,
		FB_ENGINESPEED = 9,
		FB_SONGNAME = 10,
		FB_SONGARTIST = 11,
		FB_SONGCOPYRIGHT = 12;
	
	/**
	 * Convert an instrument type to sound chip
	 */
	public static int getChipFromInstrument(int type) {
		switch (type) {
		case INST_2A03:
			return SNDCHIP_NONE;
		case INST_VRC6:
			return SNDCHIP_VRC6;
		case INST_VRC7:
			return SNDCHIP_VRC7;
		case INST_S5B:
			return SNDCHIP_S5B;
		case INST_FDS:
			return SNDCHIP_FDS;
		case INST_N163:
			return SNDCHIP_N163;
		}

		return SNDCHIP_NONE;
	} 
	
	// create from serialization only
	public FamiTrackerDoc() {
//		m_bFileLoaded = false;
//		m_bFileLoadFailed = false;
		m_iChannelsCount = 0;
		m_iNamcoChannels = DEFAULT_NAMCO_CHANS;
//		m_bDisplayComment = false;
		
		// Register this object to the sound generator
		// 这部分的代码由于解耦, 被移到上层 FamiTrackerApp 来工作.
	}
	
	// DECLARE_DYNCREATE(CFamiTrackerDoc)

	// Static functions
	/*public static FamiTrackerDoc getDoc() {
//		CFrameWnd *pFrame = static_cast<CFrameWnd*>(AfxGetApp()->m_pMainWnd);
//		ASSERT_VALID(pFrame);
//
//		return static_cast<CFamiTrackerDoc*>(pFrame->GetActiveDocument());
		throw new RuntimeException("该方法和 MFC 直接相关, 所以直接报错");
	}*/

	//
	// Public functions
	//
	
	public final String getFileTitle() {
		// TODO
		return null;
	}

	//
	// Document file I/O
	//
	public final boolean isFileLoaded() {
		// TODO
		return false;
	}
	
	public boolean hasLastLoadFailed() {
		// TODO
		return false;
	}
	
	//
	// Import
	//

	public FamiTrackerDoc loadImportFile(String lpszPathName) {
		// TODO
		return null;
	}
	
	public boolean importInstruments(FamiTrackerDoc pImported, IntHolder pInstTable) {
		// TODO
		return false;
	}
	
	public boolean ImportTrack(int track, FamiTrackerDoc pImported, IntHolder pInstTable) {
		// TODO
		return false;
	}

	//
	// Interface functions (not related to document data) TODO move this?
	//
	public void resetChannels() {
		// TODO
	}
	
	public final int getChannelIndex(int channel) {
		// TODO
		return 0;
	}
	
	public final int getChipType(int channel) {
		// TODO
		return 0;
	}
	
	public final int getChannelCount() {
		return m_iChannelsCount;
	}

	//
	// Document data access functions
	//

	// Local (song) data
	public void setPatternLength(int track, int length) {
		// TODO
	}
	
	public void setFrameCount(int track, int count) {
		// TODO
	}
	
	public void setSongSpeed(int track, int speed) {
		// TODO
	}
	
	public void setSongTempo(int track, int tempo) {
		// TODO
	}

	public final int getPatternLength(int track) {
		assert(track < MAX_TRACKS);
		return getTrack1(track).getPatternLength(); 
	}
	
	/**
	 * @param track
	 *   曲目号
	 * @return
	 *   {@link PatternData#getFrameCount()}
	 */
	public final int getFrameCount(int track) {
		assert(track < MAX_TRACKS);
		return getTrack1(track).getFrameCount();
	}
	
	public final int getSongSpeed(int track) {
		return getTrack1(track).getSongSpeed();
	}
	
	public final int getSongTempo(int track) {
		return getTrack1(track).getSongTempo();
	}

	public final int getEffColumns(int track, int channel) {
		return getTrack1(track).getEffectColumnCount(channel);
	}
	
	public void setEffColumns(int track, int channel, int columns) {
		// TODO
	}

	public final int getPatternAtFrame(int track, int frame, int channel) {
		// TODO
		return 0;
	}
	
	public void setPatternAtFrame(int track, int frame, int channel, int pattern) {
		// TODO
	}

	public boolean isPatternEmpty(int track, int channel, int pattern) {
		// TODO
		return false;
	}

	// Pattern editing
	public void setNoteData(int track, int frame, int channel, int row, final StChanNote pData) {
		// TODO
	}
	
	/**
	 * 原方法 public final void getNoteData(int track, int frame, int channel, int row, StChanNote pData);
	 * @return
	 */
	public final StChanNote getNoteData(int track, int frame, int channel, int row) {
		assert(track < MAX_TRACKS);
		assert(frame < MAX_FRAMES);
		assert(channel < MAX_CHANNELS);
		assert(row < MAX_PATTERN_LENGTH);
		
		// Sets the notes of the pattern
		PatternData pTrack = getTrack1(track);
		int Pattern = pTrack.getFramePattern(frame, channel);
		
		return pTrack.getPatternData(channel, Pattern, row);
	}

	public void setDataAtPattern(int track, int pattern, int channel, int row, final StChanNote pData) {
		// TODO
	}
	
	/**
	 * 原方法 public final void getDataAtPattern(int track, int pattern, int channel, int row, StChanNote pData);
	 */
	public final StChanNote getDataAtPattern(int track, int pattern, int channel, int row) {
		// TODO
		return null;
	}

	public void clearPatterns(int track) {
		// TODO
	}
	
	public void clearPattern(int track, int frame, int channel) {
		// TODO
	}

	public boolean insertRow(int track, int frame, int channel, int row) {
		// TODO
		return false;
	}
	
	public boolean deleteNote(int track, int frame, int channel, int row, int column) {
		// TODO
		return false;
	}
	
	public boolean clearRow(int track, int frame, int channel, int row) {
		// TODO
		return false;
	}
	
	public boolean clearRowField(int track, int frame, int channel, int row, int column) {
		// TODO
		return false;
	}
	
	public boolean removeNote(int track, int frame, int channel, int row) {
		// TODO
		return false;
	}
	
	public boolean pullUp(int track, int frame, int channel, int row) {
		// TODO
		return false;
	}
	
	public void copyPattern(int track, int target, int Source, int channel) {
		// TODO
	}

	// Frame editing
	public boolean insertFrame(int track, int frame) {
		// TODO
		return false;
	}
	public boolean removeFrame(int track, int frame) {
		// TODO
		return false;
	}
	public boolean duplicateFrame(int track, int frame) {
		// TODO
		return false;
	}
	public boolean duplicatePatterns(int track, int frame) {
		// TODO
		return false;
	}
	public boolean moveFrameDown(int track, int frame) {
		// TODO
		return false;
	}
	public boolean moveFrameUp(int track, int frame) {
		// TODO
		return false;
	}
	public void deleteFrames(int track, int frame, int Count) {
		// TODO
	}

	// Global (module) data
	public void setEngineSpeed(int speed) {
		// TODO
	}
	
	public void setMachine(byte machine) {
		assert(machine == PAL || machine == NTSC);
		m_iMachine = machine;
		// setModifiedFlag(); MFC 相关, 不调用
	}
	
	public final byte getMachine() {
		return m_iMachine;
	}
	
	public final int getEngineSpeed() {
		return m_iEngineSpeed;
	}
	
	public final int getFrameRate() {
		if (m_iEngineSpeed == 0)
			return (m_iMachine == NTSC) ? APU.FRAME_RATE_NTSC : APU.FRAME_RATE_PAL;
		
		return m_iEngineSpeed;
	}

	public void selectExpansionChip(byte chip) {
		System.out.println("FamiTrackerDoc.selectExpansionChip(byte)");
		// TODO
	}
	
	public final byte getExpansionChip() {
		return m_iExpansionChip;
	}
	
	public final boolean expansionEnabled(byte chip) {
		// TODO
		return false;
	}

	public final int getNamcoChannels() {
		// TODO
		return 0;
	}
	
	public void setNamcoChannels(int channels) {
		// TODO
	}

	public final String getSongName() {
		return m_strName;
	}
	
	public final String getSongArtist() {
		return m_strArtist;
	}
	
	public final String getSongCopyright() {
		return m_strCopyright;
	}
	
	public void setSongName(final String pName) {
		m_strName = pName;
	}
	
	public void setSongArtist(final String pArtist) {
		m_strArtist = pArtist;
	}
	
	public void setSongCopyright(final String pCopyright) {
		m_strCopyright = pCopyright;
	}

	public final int getVibratoStyle() {
		// TODO
		return 0;
	}
	
	public final void setVibratoStyle(int style) {
		// TODO
	}

	public final boolean getLinearPitch() {
		// TODO
		return false;
	}
	
	public void setLinearPitch(boolean enable) {
		// TODO
	}

	/**
	 * 
	 * @param comment
	 *   原来的 C++ 文件中, 这个是引用, 因此可能被修改
	 * @param bShowOnLoad
	 * @return
	 */
	public String setComment(String comment, boolean bShowOnLoad) {
		// TODO
		return comment;
	}
	
	public final String getComment() {
		// TODO
		return null;
	}
	
	public final boolean showCommentOnOpen() {
		// TODO
		return false;
	}

	public void setSpeedSplitPoint(int splitPoint) {
		// TODO
	}
	
	public final int getSpeedSplitPoint() {
		return m_iSpeedSplitPoint;
	}

	public void setHighlight(int track, int first, int second) {
		// TODO
	}
	
	public final int getFirstHighlight(int track) {
		// TODO
		return 0;
	}
	
	public final int getSecondHighlight(int track) {
		// TODO
		return 0;
	}

	public void setHighlight(int first, int second) {
		// TODO
	}
	
	public final int getFirstHighlight() {
		// TODO
		return 0;
	}
	
	public final int getSecondHighlight() {
		// TODO
		return 0;
	}

	// Track management functions
	public int addTrack() {
		// TODO
		return 0;
	}
	
	public void removeTrack(int track) {
		// TODO
	}
	
	public final int getTrackCount() {
		// TODO
		return 0;
	}
	
	public final String getTrackTitle(int track) {
		// TODO
		return null;
	}
	
	/**
	 * 
	 * @param track
	 * @param title
	 *   原来的 C++ 文件中, 这个是引用, 因此可能被修改
	 */
	public void setTrackTitle(int track, final String title) {
		// TODO
	}
	
	public void moveTrackUp(int track) {
		// TODO
	}
	
	public void moveTrackDown(int track) {
		// TODO
	}

	// Instruments functions
	
	public final Instrument getInstrument(int index) {
		Instrument pInstrument = m_pInstruments[index];

		return pInstrument;
	}
	
	public final int getInstrumentCount() {
		int count = 0;
		for (int i = 0; i < MAX_INSTRUMENTS; ++i) {
			if (isInstrumentUsed(i)) 
				++count;
		}
		return count;
	}
	
	public final boolean isInstrumentUsed(int index) {
		return m_pInstruments[index] != null;
	}
	
	/**
	 * Add a new instrument
	 * @param pName
	 * @param ChipType
	 * @return
	 */
	public int addInstrument(final String pName, int chipType) {
		// TODO
		return 0;
	}
	
	public int addInstrument(Instrument pInstrument) {
		// TODO
		return 0;
	}
	
	public void addInstrument(Instrument pInstrument, int Slot) {
		// TODO
	}
	
	/**
	 * Remove an instrument
	 * @param index
	 */
	public void removeInstrument(int index) {
		// TODO
	}
	
	/**
	 * Set the name of an instrument
	 * @param index
	 * @param pName
	 */
	public void setInstrumentName(int index, final String pName) {
		// TODO
	}
	
	/**
	 * Get the name of an instrument
	 * @param index
	 * @param pName
	 */
	public final void getInstrumentName(int index, String pName) {
		// TODO
	}
	
	/**
	 * Create a copy of an instrument
	 * @param Index
	 * @return
	 */
	public int cloneInstrument(int index) {
		// TODO
		return 0;
	}
	
	/**
	 * Creates a new instrument of InstType
	 * @param instType
	 *   这个参数在 Instrument 中定义
	 * @return
	 */
	public final Instrument createInstrument(byte instType) {
		// Creates a new instrument of selected type
		switch (instType) {
			case INST_2A03: 
				return new Instrument2A03();
			case INST_VRC6: 
				return new InstrumentVRC6(); 
			case INST_VRC7: 
				return new InstrumentVRC7();
			case INST_N163:	
				return new InstrumentN163();
			case INST_FDS: 
				return new InstrumentFDS();
			case INST_S5B: 
				return null; //return new CInstrumentS5B();
		}

		return null;
	}
	
	public final int findFreeInstrumentSlot() {
		// TODO
		return 0;
	}
	
	/**
	 * 
	 * @param index
	 * @return
	 *   这个参数在 Instrument 中定义
	 */
	public int getInstrumentType(int index) {
		// TODO
		return 0;
	}
	
	public int deepCloneInstrument(int index) {
		// TODO
		return 0;
	}
	
	public final void saveInstrument(int index, String FileName) {
		// TODO
	}
	
	public int loadInstrument(String FileName) {
		// TODO
		return 0;
	}

	// Sequences functions
	public Sequence getSequence(int chip, int index, int type) {
		switch (chip) {
			case SNDCHIP_NONE: 
				return getSequence0(index, type);
			case SNDCHIP_VRC6: 
				return getSequenceVRC60(index, type);
			case SNDCHIP_N163: 
				return getSequenceN1630(index, type);
			case SNDCHIP_S5B:
				return null;
				//return GetSequenceS5B(Index, Type);
		}

		return null;
	}
	
	public final int getSequenceCount(int type) {
		int count = 0;
		for (int i = 0; i < MAX_SEQUENCES; ++i) {
			if (getSequenceItemCount(i, type) > 0)
				++count;
		}
		return count;
	}

	/**
	 * 非 const 方法
	 * @param index
	 * @param type
	 * @return
	 */
	public Sequence getSequence0(int index, int type) {
		if (m_pSequences2A03[index][type] == null)
			m_pSequences2A03[index][type] = new Sequence();

		return m_pSequences2A03[index][type];
	}

	/**
	 * const 方法
	 * @param index
	 * @param type
	 * @return
	 */
	public final Sequence getSequence1(int index, int type) {
		return m_pSequences2A03[index][type];
	}
	
	public final int getSequenceItemCount(int index, int type) {
		if (m_pSequences2A03[index][type] == null)
			return 0;

		return m_pSequences2A03[index][type].getItemCount();
	}
	
	public final int getFreeSequence(int type) {
		// Return a free sequence slot, or -1 otherwise
		for (int i = 0; i < MAX_SEQUENCES; ++i) {
			if (getSequenceItemCount(i, type) == 0)
				return i;
		}
		return -1;
	}

	/**
	 * 非 const 方法
	 * @param index
	 * @param type
	 * @return
	 */
	public Sequence getSequenceVRC60(int index, int type) {
		if (m_pSequencesVRC6[index][type] == null)
			m_pSequencesVRC6[index][type] = new Sequence();

		return m_pSequencesVRC6[index][type];
	}

	/**
	 * const 方法
	 * @param index
	 * @param type
	 * @return
	 */
	public Sequence getSequenceVRC61(int index, int type) {
		return m_pSequencesVRC6[index][type];
	}
	
	public final int getSequenceItemCountVRC6(int index, int type) {
		if (m_pSequencesVRC6[index][type] == null)
			return 0;

		return m_pSequencesVRC6[index][type].getItemCount();
	}
	
	public final int getFreeSequenceVRC6(int type) {
		for (int i = 0; i < MAX_SEQUENCES; ++i) {
			if (getSequenceItemCountVRC6(i, type) == 0)
				return i;
		}
		return -1;
	}

	/**
	 * 非 const 方法
	 * @param index
	 * @param type
	 * @return
	 */
	public Sequence getSequenceN1630(int index, int type) {
		if (m_pSequencesN163[index][type] == null)
			m_pSequencesN163[index][type] = new Sequence();

		return m_pSequencesN163[index][type];
	}

	/**
	 * const 方法
	 * @param index
	 * @param type
	 * @return
	 */
	public final Sequence getSequenceN1631(int index, int type) {
		return m_pSequencesN163[index][type];
	}
	
	public final int getSequenceItemCountN163(int index, int type) {
		if (m_pSequencesN163[index][type] == null)
			return 0;

		return m_pSequencesN163[index][type].getItemCount();
	}
	
	public final int getFreeSequenceN163(int type) {
		for (int i = 0; i < MAX_SEQUENCES; ++i) {
			if (getSequenceItemCountN163(i, type) == 0)
				return i;
		}
		return -1;
	}

	/**
	 * 非 const 方法
	 * @param index
	 * @param type
	 * @return
	 */
	public Sequence	 getSequenceS5B0(int index, int type) {
		if (m_pSequencesS5B[index][type] == null)
			m_pSequencesS5B[index][type] = new Sequence();

		return m_pSequencesS5B[index][type];
	}
	
	/**
	 * const 方法
	 * @param index
	 * @param type
	 * @return
	 */
	public Sequence	 getSequenceS5B1(int index, int type) {
		return m_pSequencesS5B[index][type];
	}
	
	public final int getSequenceItemCountS5B(int index, int type) {
		if (m_pSequencesS5B[index][type] == null)
			return 0;

		return m_pSequencesS5B[index][type].getItemCount();
	}
	
	public final int getFreeSequenceS5B(int type) {
		for (int i = 0; i < MAX_SEQUENCES; ++i) {
			if (getSequenceItemCountS5B(i, type) == 0)
				return i;
		}
		return -1;
	}

	/**
	 * DPCM samples, 非 const 方法
	 * @param index
	 * @return
	 */
	public DSample getSample0(int index) {
		assert(index < MAX_DSAMPLES);
		if (m_DSamples[index] == null) {
			m_DSamples[index] = new DSample();
		}
		return m_DSamples[index];
	}
	
	/**
	 * DPCM samples, const 方法
	 * @param index
	 * @return
	 */
	public final DSample getSample1(int index) {
		assert(index < MAX_DSAMPLES);
		return m_DSamples[index];
	}
	
	public final boolean isSampleUsed(int index) {
		// TODO
		return false;
	}
	
	public final int getSampleCount() {
		// TODO
		return 0;
	}
	
	public final int getFreeSampleSlot() {
		// TODO
		return 0;
	}
	
	public void removeSample(int index) {
		// TODO
	}
	
	public final int getTotalSampleSize() {
		// TODO
		return 0;
	}

	// Other
	public final int scanActualLength(int track, int count, IntHolder RowCount) {
		// TODO
		return 0;
	}

	// Operations
	public void removeUnusedInstruments() {
		// TODO
	}
	
	public void removeUnusedPatterns() {
		// TODO
	}
	
	public void mergeDuplicatedPatterns() {
		// TODO
	}
	
	public void swapInstruments(int first, int second) {
		// TODO
	}

	/**
	 * For file version compability
	 */
	// public static void	 convertSequence(stSequence *pOldSequence, CSequence *pNewSequence, int type);

	/**
	 * Defaults when creating new modules
	 */
	public static final String DEFAULT_TRACK_NAME = "New song";
	public static final int DEFAULT_ROW_COUNT = 64;
	public static final String NEW_INST_NAME = "New instrument";

	/**
	 * Make 1 channel default since 8 sounds bad
	 */
	public static final int DEFAULT_NAMCO_CHANS = 1; // TODO
	
	/**
	 * TODO
	 * 这个参量很可能在我这里没用
	 */
	public static final int DEFAULT_FIRST_HIGHLIGHT = 4, DEFAULT_SECOND_HIGHLIGHT = 16;

	public static final boolean DEFAULT_LINEAR_PITCH = false;

	// Things below are for compability with older files
	ArrayList<StSequence> m_vTmpSequences;
	/**
	 * 每个元素是 StSequence[FamitrackerTypes.SEQ_COUNT]
	 */
	ArrayList<StSequence[]> m_vSequences;
	
	// 补充参数
	// File I/O constants
	public static final String FILE_HEADER				= "FamiTracker Module";
	public static final String FILE_BLOCK_PARAMS		= "PARAMS";
	public static final String FILE_BLOCK_INFO			= "INFO";
	public static final String FILE_BLOCK_INSTRUMENTS	= "INSTRUMENTS";
	public static final String FILE_BLOCK_SEQUENCES		= "SEQUENCES";
	public static final String FILE_BLOCK_FRAMES		= "FRAMES";
	public static final String FILE_BLOCK_PATTERNS		= "PATTERNS";
	public static final String FILE_BLOCK_DSAMPLES		= "DPCM SAMPLES";
	public static final String FILE_BLOCK_HEADER		= "HEADER";
	public static final String FILE_BLOCK_COMMENTS		= "COMMENTS";

	// VRC6
	public static final String FILE_BLOCK_SEQUENCES_VRC6 = "SEQUENCES_VRC6";

	// N163
	public static final String FILE_BLOCK_SEQUENCES_N163 = "SEQUENCES_N163";
	public static final String FILE_BLOCK_SEQUENCES_N106 = "SEQUENCES_N106";

	// Sunsoft
	public static final String FILE_BLOCK_SEQUENCES_S5B = "SEQUENCES_S5B";

	// FTI instruments files
	public static final String INST_HEADER = "FTI";
	public static final String INST_VERSION = "2.4";

	//
	// File management functions (load/save)
	//

	private void createEmpty() {
		synchronized (this) {
			allocateTrack(0);

			// Auto-select new style vibrato for new modules
//			m_iVibratoStyle = VIBRATO_NEW;
//			m_bLinearPitch = DEFAULT_LINEAR_PITCH;

			m_iNamcoChannels = DEFAULT_NAMCO_CHANS;

			// and select 2A03 only
			selectExpansionChip(SNDCHIP_NONE);

			// Document is avaliable
//			m_bFileLoaded = true;
		}
	}

	private boolean openDocument(String lpszPathName) {
		DocumentFile openFile = new DocumentFile();
		int iVersion;

//		m_bBackupDone = false;
//		m_bFileLoadFailed = true;

		// Open file
		try {
			openFile.open(lpszPathName);			
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("无法打开文件: " + lpszPathName);
			return false;
		}
		
		// Check if empty file
		if (openFile.getLength() == 0) {
			// Setup default settings
			createEmpty();
			return true;
		}

		// Read header ID and version
		if (!openFile.validateFile()) {
			System.err.println("文件格式不正确, 无法确定该文件的头部 ID: " + lpszPathName);
			return false;
		}

		iVersion = openFile.getFileVersion();

		if (iVersion < 0x0200) {
			// Older file version
			if (iVersion < DocumentFile.COMPATIBLE_VER) {
				System.err.println("文件版本太低, 无法打开该文件: " + lpszPathName);
				return false;
			}

			if (!openDocumentOld(openFile))
				return false;

			// Create a backup of this file, since it's an old version 
			// and something might go wrong when converting
//			m_bForceBackup = true;

			// Auto-select old style vibrato for old files
//			m_iVibratoStyle = VIBRATO_OLD;
//			m_bLinearPitch = false;
		}
		else if (iVersion >= 0x0200) {
			// New file version

			// Try to open file, create new if it fails
			if (!openDocumentNew(openFile))
				return false;

			// Backup if files was of an older version
//			m_bForceBackup = m_iFileVersion < DocumentFile.FILE_VER;
		}
		
//		m_bFileLoaded = true;
//		m_bFileLoadFailed = false;
		return true;
	}

	private boolean openDocumentOld(DocumentFile pOpenFile) {
		// TODO
		return false;
	}
	
	private boolean openDocumentNew(DocumentFile documentFile) {
		String blockID;
		boolean fileFinished = false;
		boolean errorFlag = false;

		// # _DEBUG
		// int _msgs_ = 0;

		// # TRANSPOSE_FDS
		m_bAdjustFDSArpeggio = false;

		// File version checking
		m_iFileVersion = documentFile.getFileVersion();

		// From version 2.0, all files should be compatible (though individual blocks may not)
		if (m_iFileVersion < 0x0200) {
			System.err.println("版本太低, 该文件无法加载: " + m_iFileVersion);
			documentFile.close();
			return false;
		}

		// File version is too new
		if (m_iFileVersion > DocumentFile.FILE_VER) {
			System.err.println("版本太高, 该文件无法加载: " + m_iFileVersion);
			documentFile.close();
			return false;
		}

		// Delete loaded document
		deleteContents();

		if (m_iFileVersion < 0x0210) {
			// This has to be done for older files
			allocateTrack(0);
		}

		// Read all blocks
		while (!documentFile.finished() && !fileFinished && !errorFlag) {
			errorFlag = documentFile.readBlock();
			blockID = documentFile.getBlockHeaderID();
			
			switch (blockID) {
			case FILE_BLOCK_PARAMS:
				errorFlag = readBlock_Parameters(documentFile);
				break;
				
			case FILE_BLOCK_INFO: {
				byte[] bs = new byte[32];
				
				documentFile.getBlock(bs);
				m_strName = new String(bs, FamiTrackerApp.defCharset);
			
				documentFile.getBlock(bs);
				m_strArtist = new String(bs, FamiTrackerApp.defCharset);
			
				documentFile.getBlock(bs);
				m_strCopyright = new String(bs, FamiTrackerApp.defCharset);
			} break;
			
			case FILE_BLOCK_INSTRUMENTS: {
				errorFlag = readBlock_Instruments(documentFile);
			} break;
			
			case FILE_BLOCK_SEQUENCES: {
				errorFlag = readBlock_Sequences(documentFile);
			} break;
			
			case FILE_BLOCK_FRAMES: {
				errorFlag = readBlock_Frames(documentFile);
			} break;
			
			case FILE_BLOCK_PATTERNS: {
				errorFlag = readBlock_Patterns(documentFile);
			} break;
			
			case FILE_BLOCK_DSAMPLES: {
				errorFlag = readBlock_DSamples(documentFile);
			} break;
			
			case FILE_BLOCK_HEADER: {
				errorFlag = readBlock_Header(documentFile);
			} break;
			
			case FILE_BLOCK_COMMENTS: {
				errorFlag = readBlock_Comments(documentFile);
			} break;
			
			case FILE_BLOCK_SEQUENCES_VRC6: {
				errorFlag = readBlock_SequencesVRC6(documentFile);
			} break;
			
			// FILE_BLOCK_SEQUENCES_N106 是出于向后兼容的目的
			case FILE_BLOCK_SEQUENCES_N163: case FILE_BLOCK_SEQUENCES_N106: {
				errorFlag = readBlock_SequencesN163(documentFile);
			} break;
			
			case FILE_BLOCK_SEQUENCES_S5B: {
				errorFlag = readBlock_SequencesS5B(documentFile);
			} break;
			
			case "END": {
				fileFinished = true;
			} break;

			default:
				System.err.println("出现了未知的 blockID: " + blockID);
				errorFlag = true;
				break;
			}
		}

		documentFile.close();

		if (errorFlag) {
			System.err.println("文件读取出现了异常.");
			deleteContents();
			return false;
		}

		if (m_iFileVersion <= 0x0201)
			reorderSequences();

		if (m_iFileVersion < 0x0300)
			convertSequences();

		if (m_bAdjustFDSArpeggio) {
			for (int i = 0; i < MAX_INSTRUMENTS; ++i) {
				if (isInstrumentUsed(i) && getInstrumentType(i) == INST_FDS) {
					InstrumentFDS pInstrument = (InstrumentFDS) (getInstrument(i));
					Sequence pSeq = pInstrument.getArpSeq();
					if (pSeq.getItemCount() > 0 && pSeq.getSetting() == Sequence.ARP_SETTING_FIXED) {
						for (int j = 0; j < pSeq.getItemCount(); ++j) {
							pSeq.setItem(j, (byte)(pSeq.getItem(j) + 24));
						}
					}
				}
			}
		}

		return true;
	}

	private boolean readBlock_Parameters(DocumentFile pDocFile) {
		int version = pDocFile.getBlockVersion();

		// Get first track for module versions that require that
		PatternData pTrack = getTrack0(0); // 调用非 const

		if (version == 1) {
			pTrack.setSongSpeed(pDocFile.getBlockInt());
		} else {
			m_iExpansionChip = pDocFile.getBlockChar();
		}

		m_iChannelsCount = pDocFile.getBlockInt();
		m_iMachine = (byte) pDocFile.getBlockInt();
		m_iEngineSpeed = pDocFile.getBlockInt();

		assert(m_iMachine == NTSC || m_iMachine == PAL);
		assert(m_iChannelsCount < MAX_CHANNELS);

		if (m_iMachine != NTSC && m_iMachine != PAL)
			m_iMachine = NTSC;

		if (version > 2)
			/*m_iVibratoStyle = */pDocFile.getBlockInt();
		/*else
			m_iVibratoStyle = VIBRATO_OLD;*/

		// TODO read m_bLinearPitch

		m_iFirstHighlight  = DEFAULT_FIRST_HIGHLIGHT;
		m_iSecondHighlight = DEFAULT_SECOND_HIGHLIGHT;

		if (version > 3) {
			m_iFirstHighlight = pDocFile.getBlockInt();
			m_iSecondHighlight = pDocFile.getBlockInt();
		}

		// This is strange. Sometimes expansion chip is set to 0xFF in files
		if (m_iChannelsCount == 5)
			m_iExpansionChip = 0;

		if (m_iFileVersion == 0x0200) {
			int speed = pTrack.getSongSpeed();
			if (speed < 20)
				pTrack.setSongSpeed(speed + 1);
		}

		if (version == 1) {
			if (pTrack.getSongSpeed() > 19) {
				pTrack.setSongTempo(pTrack.getSongSpeed());
				pTrack.setSongSpeed(6);
			} else {
				pTrack.setSongTempo(m_iMachine == NTSC ? DEFAULT_TEMPO_NTSC : DEFAULT_TEMPO_PAL);
			}
		}

		// Read namco channel count
		if (version >= 5 && (m_iExpansionChip & SNDCHIP_N163) != 0) {
			m_iNamcoChannels = pDocFile.getBlockInt();
			assert(m_iNamcoChannels < 9);
		}

		if (version >= 6) {
			m_iSpeedSplitPoint = pDocFile.getBlockInt();
		} else {
			// Determine if new or old split point is preferred
			m_iSpeedSplitPoint = OLD_SPEED_SPLIT_POINT;
		}

		setupChannels(m_iExpansionChip);
		
		return false;
	}
	
	private boolean readBlock_Header(DocumentFile pDocFile) {
		int version = pDocFile.getBlockVersion();

		if (version == 1) {
			// Single track
			m_iTrackCount = 1;
			PatternData pTrack = getTrack0(0);
			for (int i = 0; i < m_iChannelsCount; ++i) {
				// Channel type (unused)
				pDocFile.getBlockChar();
				// Effect columns
				pTrack.setEffectColumnCount(i, pDocFile.getBlockChar());
			}
		}
		else if (version >= 2) {
			// Multiple tracks
			m_iTrackCount = pDocFile.getBlockChar() + 1;  // 0 means one track

			assert(m_iTrackCount <= MAX_TRACKS);

			// Add tracks to document
			for (int i = 0; i < m_iTrackCount; ++i) {
				allocateTrack(i);
			}

			// Track names
			if (version >= 3) {
				for (int i = 0; i < m_iTrackCount; ++i) {
					m_sTrackNames[i] = pDocFile.readString();
				}
			}

			for (int i = 0; i < m_iChannelsCount; ++i) {
				@SuppressWarnings("unused")
				byte channelType = pDocFile.getBlockChar();
				for (int j = 0; j < m_iTrackCount; ++j) {
					PatternData pTrack = getTrack0(j);
					byte columnCount = pDocFile.getBlockChar();
					pTrack.setEffectColumnCount(i, columnCount);  // Effect columns
				}
			}

			if (version >= 4) {
				// Read highlight settings for tracks
				for (int i = 0; i < m_iTrackCount; ++i) {
					PatternData pTrack = getTrack0(i);
					// TODO read highlight
					int FirstHighlight = m_iFirstHighlight;
					int SecondHighlight = m_iSecondHighlight;
					pTrack.setHighlight(FirstHighlight, SecondHighlight);
				}
			}
			else {
				// Use global highlight
				for (int i = 0; i < m_iTrackCount; ++i) {
					PatternData pTrack = getTrack0(i);
					int firstHighlight = m_iFirstHighlight;
					int secondHighlight = m_iSecondHighlight;
					pTrack.setHighlight(firstHighlight, secondHighlight);
				}
			}
		}

		return false;
	}
	
	private boolean readBlock_Instruments(DocumentFile pDocFile) {
		/*
		 * Version changes
		 *
		 *  2 - Extended DPCM octave range
		 *  3 - Added settings to the arpeggio sequence
		 *
		 */
		
		// int version = pDocFile.getBlockVersion();

		// Number of instruments
		int count = pDocFile.getBlockInt();
		assert(count <= MAX_INSTRUMENTS);

		for (int i = 0; i < count; ++i) {
			// Instrument index
			int index = pDocFile.getBlockInt();
			assert(index <= MAX_INSTRUMENTS);

			// Read instrument type and create an instrument
			byte type = pDocFile.getBlockChar();
			Instrument pInstrument = createInstrument(type);
			assert(pInstrument != null);

			// Load the instrument
			boolean valid = pInstrument.load(pDocFile);
			assert(valid);

			// Read name
			int size = pDocFile.getBlockInt();
			byte[] nameBytes = new byte[size];
			pDocFile.getBlock(nameBytes);
			pInstrument.setName(new String(nameBytes, FamiTrackerApp.defCharset));

			// Store instrument
			m_pInstruments[index] = pInstrument;
		}

		return false;
	}
	
	private boolean readBlock_Sequences(DocumentFile pDocFile) {
		int releasePoint = -1, settings = 0;
		int version = pDocFile.getBlockVersion();

		int count = pDocFile.getBlockInt();
		assert(count < (MAX_SEQUENCES * SEQ_COUNT));

		if (version == 1) {
			for (int i = 0; i < MAX_SEQUENCES; i++) {
				m_vTmpSequences.add(new StSequence());
			}
			
			for (int i = 0; i < count; ++i) {
				int index = pDocFile.getBlockInt();
				byte seqCount = pDocFile.getBlockChar();
				assert(index < MAX_SEQUENCES);
				assert(seqCount < MAX_SEQUENCE_ITEMS);
				
				StSequence s = m_vTmpSequences.get(index);
				s.count = seqCount;
				for (int j = 0; j < seqCount; ++j) {
					s.value[j] = pDocFile.getBlockChar();
					s.length[j] = pDocFile.getBlockChar();
				}
			}
		} else if (version == 2) {
			for (int i = 0; i < MAX_SEQUENCES; i++) {
				StSequence[] ss = new StSequence[SEQ_COUNT];
				for (int j = 0; j < ss.length; j++) {
					ss[i] = new StSequence();
				}
				m_vSequences.add(ss);
			}
			
			for (int i = 0; i < count; ++i) {
				int index = pDocFile.getBlockInt();
				int type = pDocFile.getBlockInt();
				byte seqCount = pDocFile.getBlockChar();
				assert(index < MAX_SEQUENCES);
				assert(type < SEQ_COUNT);
				assert(seqCount < MAX_SEQUENCE_ITEMS);
				StSequence s = m_vSequences.get(index)[type];
				
				for (int j = 0; j < seqCount; ++j) {
					byte value = pDocFile.getBlockChar();
					byte length = pDocFile.getBlockChar();
					s.value[j] = value;
					s.length[j] = length;
				}
				s.count = seqCount;
			}
		} else if (version >= 3) {
			int[] indices = new int[MAX_SEQUENCES * SEQ_COUNT];
			int[] types = new int[MAX_SEQUENCES * SEQ_COUNT];

			for (int i = 0; i < count; ++i) {
				int index = pDocFile.getBlockInt();
				int type = pDocFile.getBlockInt();
				int seqCount = pDocFile.getBlockChar() & 0xFF;
				int loopPoint = pDocFile.getBlockInt();

				// Work-around for some older files
				if (loopPoint == seqCount)
					loopPoint = -1;

				indices[i] = index;
				types[i] = type;

				assert(index < MAX_SEQUENCES);
				assert(type < SEQ_COUNT);

				Sequence pSeq = getSequence0(index, type);

				pSeq.clear();
				pSeq.setItemCount(seqCount < MAX_SEQUENCE_ITEMS ? seqCount : MAX_SEQUENCE_ITEMS);
				pSeq.setLoopPoint(loopPoint);

				if (version == 4) {
					releasePoint = pDocFile.getBlockInt();
					settings = pDocFile.getBlockInt();
					pSeq.setReleasePoint(releasePoint);
					pSeq.setSetting(settings);
				}

				for (int j = 0; j < seqCount; ++j) {
					byte value = pDocFile.getBlockChar();
					if (j <= MAX_SEQUENCE_ITEMS)
						pSeq.setItem(j, value);
				}
			}

			if (version == 5) {
				// Version 5 saved the release points incorrectly, this is fixed in ver 6
				for (int i = 0; i < MAX_SEQUENCES; ++i) {
					for (int j = 0; j < SEQ_COUNT; ++j) {
						releasePoint = pDocFile.getBlockInt();
						settings = pDocFile.getBlockInt();
						if (getSequenceItemCount(i, j) > 0) {
							Sequence pSeq = getSequence0(i, j);
							pSeq.setReleasePoint(releasePoint);
							pSeq.setSetting(settings);
						}
					}
				}
			} else if (version >= 6) {
				// Read release points correctly stored
				for (int i = 0; i < count; ++i) {
					releasePoint = pDocFile.getBlockInt();
					settings = pDocFile.getBlockInt();
					int index = indices[i];
					int type = types[i];
					Sequence pSeq = getSequence0(index, type);
					pSeq.setReleasePoint(releasePoint);
					pSeq.setSetting(settings);
				}
			}
		}

		return false;
	}
	private boolean readBlock_Frames(DocumentFile pDocFile) {
		int version = pDocFile.getBlockVersion();

		if (version == 1) {
			int frameCount = pDocFile.getBlockInt();
			PatternData pTrack = getTrack0(0);
			pTrack.setFrameCount(frameCount);
			m_iChannelsCount = pDocFile.getBlockInt();
			assert(frameCount <= MAX_FRAMES);
			assert(m_iChannelsCount <= MAX_CHANNELS);
			for (int i = 0; i < frameCount; ++i) {
				for (int j = 0; j < m_iChannelsCount; ++j) {
					int pattern = pDocFile.getBlockChar() & 0xFF;
					assert(pattern < MAX_FRAMES);
					pTrack.setFramePattern(i, j, pattern);
				}
			}
		} else if (version > 1) {
			for (int y = 0; y < m_iTrackCount; ++y) {
				int frameCount = pDocFile.getBlockInt();
				int speed = pDocFile.getBlockInt();
				assert(frameCount > 0 && frameCount <= MAX_FRAMES);
				assert(speed > 0);

				PatternData pTrack = getTrack0(y);
				pTrack.setFrameCount(frameCount);

				if (version == 3) {
					int tempo = pDocFile.getBlockInt();
					assert(speed >= 0);
					assert(tempo >= 0);
					pTrack.setSongTempo(tempo);
					pTrack.setSongSpeed(speed);
				} else {
					if (speed < 20) {
						int tempo = (m_iMachine == NTSC) ? DEFAULT_TEMPO_NTSC : DEFAULT_TEMPO_PAL;
						assert(tempo >= 0 && tempo <= MAX_TEMPO);
						//ASSERT_FILE_DATA(Speed >= 0 && Speed <= MAX_SPEED);
						assert(speed >= 0);
						pTrack.setSongTempo(tempo);
						pTrack.setSongSpeed(speed);
					} else {
						assert(speed >= 0 && speed <= MAX_TEMPO);
						pTrack.setSongTempo(speed);
						pTrack.setSongSpeed(DEFAULT_SPEED);
					}
				}

				int patternLength = pDocFile.getBlockInt();
				assert(patternLength > 0 && patternLength <= MAX_PATTERN_LENGTH);

				pTrack.setPatternLength(patternLength);
				
				for (int i = 0; i < frameCount; ++i) {
					for (int j = 0; j < m_iChannelsCount; ++j) {
						// Read pattern index
						int pattern = pDocFile.getBlockChar() & 0xFF;
						assert(pattern < MAX_PATTERN);
						pTrack.setFramePattern(i, j, pattern);
					}
				}
			}
		}

		return false;
	}
	private boolean readBlock_Patterns(DocumentFile pDocFile) {
		int version = pDocFile.getBlockVersion();

		if (version == 1) {
			int patternLen = pDocFile.getBlockInt();
			assert(patternLen <= MAX_PATTERN_LENGTH);
			PatternData pTrack = getTrack0(0);
			pTrack.setPatternLength(patternLen);
		}

		while (!pDocFile.blockDone()) {
			int track = 0;
			if (version > 1)
				track = pDocFile.getBlockInt();
			else if (version == 1)
				track = 0;

			int channel = pDocFile.getBlockInt();
			int pattern = pDocFile.getBlockInt();
			int items = pDocFile.getBlockInt();

			if (channel > MAX_CHANNELS)
				return false;

			assert(track < MAX_TRACKS);
			assert(channel < MAX_CHANNELS);
			assert(pattern < MAX_PATTERN);
			assert((items - 1) < MAX_PATTERN_LENGTH);

			PatternData pTrack = getTrack0(track);

			for (int i = 0; i < items; ++i) {
				int row;
				if (m_iFileVersion == 0x0200)
					row = pDocFile.getBlockChar();
				else
					row = pDocFile.getBlockInt();

				assert(row < MAX_PATTERN_LENGTH);

				StChanNote note = pTrack.getPatternData(channel, pattern, row);
				note.note = pDocFile.getBlockChar();
				note.octave = pDocFile.getBlockChar();
				note.instrument = pDocFile.getBlockChar();
				note.vol = pDocFile.getBlockChar();

				if (m_iFileVersion == 0x0200) {
					int effectNumber, effectParam;
					effectNumber = pDocFile.getBlockChar() & 0xFF;
					effectParam = pDocFile.getBlockChar() & 0xFF;
					if (version < 3) {
						if (effectNumber == EF_PORTAOFF) {
							effectNumber = EF_PORTAMENTO;
							effectParam = 0;
						} else if (effectNumber == EF_PORTAMENTO) {
							if (effectParam < 0xFF)
								effectParam++;
						}
					}

					StChanNote note2 = pTrack.getPatternData(channel, pattern, row);

					note2.effNumber[0] = effectNumber;
					note2.effParam[0] = effectParam & 0xFF;
				} else {
					for (int n = 0; n < (pTrack.getEffectColumnCount(channel) + 1); ++n) {
						int effectNumber, effectParam;
						effectNumber = pDocFile.getBlockChar();
						effectParam = pDocFile.getBlockChar();

						if (version < 3) {
							if (effectNumber == EF_PORTAOFF) {
								effectNumber = EF_PORTAMENTO;
								effectParam = 0;
							} else if (effectNumber == EF_PORTAMENTO) {
								if (effectParam < 0xFF)
									effectParam++;
							}
						}

						note.effNumber[n] = effectNumber;
						note.effParam[n] = effectParam & 0xFF;
					}
				}

				if (note.vol > MAX_VOLUME)
					note.vol &= 0x0F;

				// Specific for version 2.0
				if (m_iFileVersion == 0x0200) {

					if (note.effNumber[0] == EF_SPEED && note.effParam[0] < 20)
						note.effParam[0]++;
					
					if (note.vol == 0)
						note.vol = MAX_VOLUME;
					else {
						note.vol--;
						note.vol &= 0x0F;
					}

					if (note.note == 0)
						note.instrument = MAX_INSTRUMENTS;
				}

				if (version == 3) {
					// Fix for VRC7 portamento
					if (expansionEnabled(SNDCHIP_VRC7) && channel > 4) {
						for (int n = 0; n < MAX_EFFECT_COLUMNS; ++n) {
							switch (note.effNumber[n]) {
								case EF_PORTA_DOWN:
									note.effNumber[n] = EF_PORTA_UP;
									break;
								case EF_PORTA_UP:
									note.effNumber[n] = EF_PORTA_DOWN;
									break;
							}
						}
					}
					// TODO FDS pitch effect fix
					/*else if (expansionEnabled(SNDCHIP_FDS) && getChannelType(channel) == CHANID_FDS) {
						for (int n = 0; n < MAX_EFFECT_COLUMNS; ++n) {
							switch (note.effNumber[n]) {
								case EF_PITCH:
									if (note.effParam[n] != 0x80)
										note.effParam[n] = (0x100 - note.effParam[n]) & 0xFF;
									break;
							}
						}
					}*/
				}

				if (version < 5) {
					// TODO FDS octave
					/*if (expansionEnabled(SNDCHIP_FDS) && getChannelType(channel) == CHANID_FDS && note.octave < 6) {
						note.octave += 2;
						m_bAdjustFDSArpeggio = true;
					}*/
				}
				/* TRANSPOSE_FDS */
				
			}
		}
		
		return false;
	}
	private boolean readBlock_DSamples(DocumentFile pDocFile) {
		// int version = pDocFile.getBlockVersion();

		int count = pDocFile.getBlockChar();
		assert(count <= MAX_DSAMPLES);
		
		for (int i = 0; i < count; ++i) {
			int index = pDocFile.getBlockChar();
			assert(index < MAX_DSAMPLES);
			DSample pSample = getSample0(index);
			int len = pDocFile.getBlockInt();
			
			byte[] bs = new byte[len];
			pDocFile.getBlock(bs);
			
			pSample.setName(new String(bs, FamiTrackerApp.defCharset));
			int size = pDocFile.getBlockInt();
			assert(size < 0x8000);
			pSample.allocate(size);
			pDocFile.getBlock(pSample.getData());
		}

		return false;
	}
	private boolean readBlock_Comments(DocumentFile pDocFile) {
//		m_bDisplayComment = (pDocFile.getBlockInt() == 1) ? true : false;
//		m_strComment = pDocFile.readString();
		return false;
	}
	/*private boolean readBlock_ChannelLayout(DocumentFile pDocFile) {
		return false;
	}*/
	private boolean readBlock_SequencesVRC6(DocumentFile pDocFile) {
		int version = pDocFile.getBlockVersion();

		int count = pDocFile.getBlockInt();
		assert(count < (MAX_SEQUENCES * SEQ_COUNT));

		if (version < 4) {
			for (int i = 0; i < count; ++i) {
				int index	  = pDocFile.getBlockInt();
				int type	  = pDocFile.getBlockInt();
				byte seqCount = pDocFile.getBlockChar();
				int loopPoint = pDocFile.getBlockInt();
				
				assert(index < MAX_SEQUENCES);
				assert(type < SEQ_COUNT);
				
				Sequence pSeq = getSequenceVRC60(index, type);
				pSeq.clear();
				pSeq.setItemCount(seqCount < MAX_SEQUENCE_ITEMS ? seqCount : MAX_SEQUENCE_ITEMS);
				pSeq.setLoopPoint(loopPoint);
				for (int j = 0; j < seqCount; ++j) {
					byte value = pDocFile.getBlockChar();
					if (j <= MAX_SEQUENCE_ITEMS)
						pSeq.setItem(j, value);
				}
			}
		} else {
			int[] indices = new int[MAX_SEQUENCES];
			int[] types = new int[MAX_SEQUENCES];
			int releasePoint = -1, settings = 0;

			for (int i = 0; i < count; ++i) {
				int index = pDocFile.getBlockInt();
				int type = pDocFile.getBlockInt();
				int seqCount = pDocFile.getBlockChar() & 0xFF;
				int loopPoint = pDocFile.getBlockInt();

				indices[i] = index;
				types[i] = type;
				
				assert(index < MAX_SEQUENCES);
				assert(type < SEQ_COUNT);

				Sequence pSeq = getSequenceVRC60(index, type);

				pSeq.clear();
				pSeq.setItemCount(seqCount);
				pSeq.setLoopPoint(loopPoint);

				if (version == 4) {
					releasePoint = pDocFile.getBlockInt();
					settings = pDocFile.getBlockInt();
					pSeq.setReleasePoint(releasePoint);
					pSeq.setSetting(settings);
				}

				for (int j = 0; j < seqCount; ++j) {
					byte value = pDocFile.getBlockChar();
					if (j <= MAX_SEQUENCE_ITEMS)
						pSeq.setItem(j, value);
				}
			}

			if (version == 5) {
				// Version 5 saved the release points incorrectly, this is fixed in ver 6
				for (int i = 0; i < MAX_SEQUENCES; ++i) {
					for (int j = 0; j < SEQ_COUNT; ++j) {
						releasePoint = pDocFile.getBlockInt();
						settings = pDocFile.getBlockInt();
						if (getSequenceItemCountVRC6(i, j) > 0) {
							Sequence pSeq = getSequenceVRC60(i, j);
							pSeq.setReleasePoint(releasePoint);
							pSeq.setSetting(settings);
						}
					}
				}
			} else if (version >= 6) {
				for (int i = 0; i < count; ++i) {
					releasePoint = pDocFile.getBlockInt();
					settings = pDocFile.getBlockInt();
					int index = indices[i];
					int type = types[i];
					Sequence pSeq = getSequenceVRC60(index, type);
					pSeq.setReleasePoint(releasePoint);
					pSeq.setSetting(settings);
				}
			}
		}

		return false;
	}
	private boolean readBlock_SequencesN163(DocumentFile pDocFile) {
		// int version = pDocFile.getBlockVersion();

		int count = pDocFile.getBlockInt();
		assert(count < (MAX_SEQUENCES * SEQ_COUNT));

		for (int i = 0; i < count; i++) {
			int index = pDocFile.getBlockInt();
			int type = pDocFile.getBlockInt();
			byte seqCount = pDocFile.getBlockChar();
			int loopPoint = pDocFile.getBlockInt();
			int releasePoint = pDocFile.getBlockInt();
			int setting = pDocFile.getBlockInt();

			assert(index < MAX_SEQUENCES);
			assert(type < SEQ_COUNT);

			Sequence pSeq = getSequenceN1630(index, type);

			pSeq.clear();
			pSeq.setItemCount(seqCount);
			pSeq.setLoopPoint(loopPoint);
			pSeq.setReleasePoint(releasePoint);
			pSeq.setSetting(setting);

			for (int j = 0; j < seqCount; ++j) {
				byte value = pDocFile.getBlockChar();
				if (j <= MAX_SEQUENCE_ITEMS)
					pSeq.setItem(j, value);
			}
		}

		return false;
	}
	private boolean readBlock_SequencesS5B(DocumentFile pDocFile) {
		// int version = pDocFile->GetBlockVersion();

		int count = pDocFile.getBlockInt();
		assert(count < (MAX_SEQUENCES * SEQ_COUNT));

		for (int i = 0; i < count; i++) {
			int index = pDocFile.getBlockInt();
			int type = pDocFile.getBlockInt();
			byte seqCount = pDocFile.getBlockChar();
			int loopPoint = pDocFile.getBlockInt();
			int releasePoint = pDocFile.getBlockInt();
			int setting = pDocFile.getBlockInt();

			assert(index < MAX_SEQUENCES);
			assert(type < SEQ_COUNT);

			Sequence pSeq = getSequenceS5B0(index, type);

			pSeq.clear();
			pSeq.setItemCount(seqCount);
			pSeq.setLoopPoint(loopPoint);
			pSeq.setReleasePoint(releasePoint);
			pSeq.setSetting(setting);

			for (int j = 0; j < seqCount; ++j) {
				byte value = pDocFile.getBlockChar();
				if (j <= MAX_SEQUENCE_ITEMS)
					pSeq.setItem(j, value);
			}
		}

		return false;
	}

	// For file version compability
	private void reorderSequences() {
		// TODO
	}
	private void convertSequences() {
		// TODO
	}

	//
	// Internal module operations
	//

	private void allocateTrack(int track) {
		// Allocate a new song if not already done
		if (m_pTracks[track] == null) {
			int tempo = (m_iMachine == NTSC) ? DEFAULT_TEMPO_NTSC : DEFAULT_TEMPO_PAL;
			m_pTracks[track] = new PatternData(DEFAULT_ROW_COUNT, DEFAULT_SPEED, tempo);
			m_sTrackNames[track] = DEFAULT_TRACK_NAME;
		}
	}
	
	/**
	 * 非 const 方法
	 * @param track
	 * @return
	 */
	private PatternData getTrack0(int track) {
		assert(track < MAX_TRACKS);
		// Ensure track is allocated
		allocateTrack(track);
		return m_pTracks[track];
	}
	
	/**
	 * const 方法
	 * @param track
	 * @return
	 */
	private final PatternData getTrack1(int track) {
		// TODO make m_pTracks mutable instead?
		assert(track < MAX_TRACKS);
		assert(m_pTracks[track] != null);

		return m_pTracks[track];
	}
	
	private void setupChannels(byte chip) {
		// This will select a chip in the sound emulator

		if (chip != SNDCHIP_NONE) {
			// Do not allow expansion chips in PAL mode
			setMachine(NTSC);
		}

		// Store the chip
		m_iExpansionChip = chip;

		// Register the channels
		System.err.println(Thread.currentThread().getStackTrace()[1] + ": 我认为文件读取和播放应该独立, 因此不这样写");
		// FamiTrackerApp.getInstance().getSoundGenerator().registerChannels(chip, this);
		// m_iChannelsAvailable = getChannelCount();

		// N163 的先不管
		/*if ((chip & SNDCHIP_N163) != 0) {
			m_iChannelsAvailable -= (8 - m_iNamcoChannels);
		}*/
		System.err.println(Thread.currentThread().getStackTrace()[1] + ": 修改完毕");

		// Must call ApplyExpansionChip after this
	}
	
	//
	// Interface variables
	//

//	private int[] m_iChannelTypes = new int[CHANNELS];
//	private int[] m_iChannelChip = new int[CHANNELS];


	//
	// State variables
	//

	/**
	 * Loaded file version
	 */
	private int m_iFileVersion;

	/*
	 * m_bBackupDone: 是否进行了备份
	 */
//	private boolean m_bForceBackup;
//	private boolean m_bBackupDone;
	private boolean m_bAdjustFDSArpeggio;

	//
	// Document data
	//

	// Patterns and song data
	/**
	 * List of all tracks
	 */
	PatternData[] m_pTracks = new PatternData[MAX_TRACKS];
	String[] m_sTrackNames = new String[MAX_TRACKS];

	/**
	 * Number of tracks added
	 */
	private int m_iTrackCount;
		
	/**
	 * Number of channels added, 原参数名 m_iChannelsAvailable. 它将 m_iRegisteredChannels 参数值合并了.
	 */
	private int m_iChannelsCount;

		// Instruments, samples and sequences
	private Instrument[] m_pInstruments = new Instrument[MAX_INSTRUMENTS];
	
	/**
	 * The DPCM sample list
	 */
	@SuppressWarnings("unused")
	private DSample[] m_DSamples = new DSample[MAX_DSAMPLES];
	
	private Sequence[][] m_pSequences2A03 = new Sequence[MAX_SEQUENCES][SEQ_COUNT];
	private Sequence[][] m_pSequencesVRC6 = new Sequence[MAX_SEQUENCES][SEQ_COUNT];
	private Sequence[][] m_pSequencesN163 = new Sequence[MAX_SEQUENCES][SEQ_COUNT];
	private Sequence[][] m_pSequencesS5B = new Sequence[MAX_SEQUENCES][SEQ_COUNT];

	// Module properties
		
	/**
	 * Expansion chip
	 */
	private byte m_iExpansionChip;
	
	private int m_iNamcoChannels;
	
	/**
	 * 0 = old style, 1 = new style
	 */
//	private int m_iVibratoStyle;

//	private boolean m_bLinearPitch;
	/**
	 * NTSC / PAL
	 */
	private byte m_iMachine;
	
	/**
	 * Refresh rate
	 */
	private int m_iEngineSpeed;
	
	/**
	 * Speed/tempo split-point
	 */
	private int m_iSpeedSplitPoint;

	// NSF info
	/**
	 * Song name
	 */
	private String m_strName;
	/**
	 * Song artist
	 */
	private String m_strArtist;
	/**
	 * Song copyright
	 */
	private String m_strCopyright;

	/**
	 * Comments
	 */
//	private String m_strComment;
//	private boolean m_bDisplayComment;

	/**
	 * Row highlight (TODO remove)
	 */
	private int m_iFirstHighlight, m_iSecondHighlight;

//		// Things below are for compability with older files
//		CArray<stSequence> m_vTmpSequences;
//		CArray<stSequence[SEQ_COUNT]> m_vSequences;

	//
	// End of document data
	//

	/*
	 * 原来 CCriticalSection 和 CMutex 是做线程同步的
	 * private mutable CCriticalSection m_csInstrument;
	 * private mutable CMutex m_csDocumentLock;
	 */

	/**
	 * 这个原本是由 GUI 调用, 因此进行了简化
	 * @return
	 */
	public boolean onNewDocument() {
		// Called by the GUI to create a new file

		// This calls DeleteContents
//		if (!CDocument::OnNewDocument())
//			return false;

		createEmpty();

		return true;
	}
	/**
	 * 现在无法写, 因此全部隐去 TODO
	 * @param lpszPathName
	 * @return
	 */
	public boolean onSaveDocument(String lpszPathName) {
		// This function is called by the GUI to save the file

//		if (!m_bFileLoaded)
//			return false;
//
//		// File backup, now performed on save instead of open
//		if ((m_bForceBackup || theApp.GetSettings()->General.bBackups) && !m_bBackupDone) {
//			CString BakName;
//			BakName.Format(_T("%s.bak"), lpszPathName);
//			CopyFile(lpszPathName, BakName.GetBuffer(), FALSE);
//			m_bBackupDone = true;
//		}
//
//		if (!SaveDocument(lpszPathName))
//			return FALSE;
//
//		// Reset modified flag
//		SetModifiedFlag(FALSE);
//
//		return TRUE;
		return false;
	}
	public boolean onOpenDocument(String lpszPathName) {
		// This function is called by the GUI to load a file

		//DeleteContents();

		synchronized (this) {
			if (!openDocument(lpszPathName)) {
				// Loading failed, create empty document
				//CreateEmpty();
				// and tell doctemplate that loading failed
				return false;
			}
		}

		// Load file

		// Update main frame
		// applyExpansionChip();

		// Remove modified flag
		// setModifiedFlag(false);

		return true;
	}
	
	/**
	 * MFC 相关, TODO
	 */
	public void onCloseDocument() {}
	/**
	 * MFC 相关, TODO
	 */
	public void deleteContents() {}
	
	public void serialize() {
		// TODO
	}

	public void onFileSaveAs() {
		// TODO
	}
	public void onFileSave() {
		// TODO
	}
}
