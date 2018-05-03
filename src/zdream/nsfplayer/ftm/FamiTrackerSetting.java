package zdream.nsfplayer.ftm;

import zdream.nsfplayer.ftm.document.format.FtmTrack;

/**
 * TODO
 * @author Zdream
 */
public class FamiTrackerSetting {

	public FamiTrackerSetting() {
		// TODO Auto-generated constructor stub
	}
	
	public class General {
		public boolean bNoDPCMReset = false;
	}
	
	public class Sound {
		public int iDevice = 0;
		public int iSampleRate = 48000;
		public int iSampleSize = 16;
		public int iBufferLength = 40;
		public int iBassFilter = 30;
		public int iTrebleFilter = 12000;
		public int iTrebleDamping = 24;
		public int iMixVolume = 100;
	}
	
	/**
	 * 默认全是 0
	 */
	public class ChipLevels{
		public int iLevelAPU1;
		public int iLevelAPU2;
		public int iLevelVRC6;
		public int iLevelVRC7;
		public int iLevelMMC5;
		public int iLevelFDS;
		public int iLevelN163;
		public int iLevelS5B;
	}
	
	public General general = new General();
	public Sound sound = new Sound();
	public ChipLevels chipLevels = new ChipLevels();
	
	// 其它常数

	public static final int OCTAVE_RANGE = 8;

	/**
	 * 序列的最大个数
	 * <br>Maximum number of sequence lists
	 */
	public static final int MAX_SEQUENCES = 128;

	/**
	 * 最大支持的 Frame (段落) 数目. 即每个 {@link FtmTrack} 中支持的 Frame 的数量
	 * <p>Maximum number of frames
	 */
	public static final int MAX_FRAMES = 128;

	/**
	 * 最多支持的乐器数量
	 */
	public static final int MAX_INSTRUMENTS = 128;
	
	/**
	 * Tempo 支持的最大值.
	 */
	public static final int MAX_TEMPO = 255;

	/**
	 * <p>每个 Frame (段落) 的最大行数. 这个值在 NSF 中也有明确定义
	 * <p>Maximum length of patterns (in rows). 256 is max in NSF
	 */
	public static final int MAX_PATTERN_LENGTH = 256;

	/**
	 * <p>每条轨道最大的 Pattern 数. 等同于最大段落数目
	 * <p>Maximum number of patterns per channel
	 */
	public static final int MAX_PATTERN = MAX_FRAMES;
	
	/**
	 * Maximum number of DPCM samples, cannot be increased unless the NSF driver is modified.
	 */
	public static final int MAX_DSAMPLES = 64;
}
