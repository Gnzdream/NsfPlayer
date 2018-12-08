package zdream.nsfplayer.ftm.format;

/**
 * Ftm 中的常量
 * 
 * @author Zdream
 * @since v0.2.4
 */
public class FtmStatic {
	
	/**
	 * 每行、轨道中, 最大的效果数
	 * Number of effect columns allowed
	 */
	public static final int MAX_EFFECT_COLUMNS = 4;
	
	// 其它常数

	public static final int OCTAVE_RANGE = 8;

	/**
	 * 序列的最大个数
	 * <br>Maximum number of sequence lists
	 */
	public static final int MAX_SEQUENCES = 128;

	/**
	 * 最大支持的 section (段落, 原工程叫 frame) 数目. 即每个 {@link FtmTrack} 中支持的段的数量
	 * <p>Maximum number of sections / frames
	 */
	public static final int MAX_SECTIONS = 128;

	/**
	 * 最多支持的乐器数量
	 */
	public static final int MAX_INSTRUMENTS = 128;
	
	/**
	 * 最大音量
	 */
	public static final byte MAX_VOLUMN = 16;
	
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
	public static final int MAX_PATTERNS = MAX_SECTIONS;
	
	/**
	 * 最大的 DPCM 数
	 * Maximum number of DPCM samples, cannot be increased unless the NSF driver is modified.
	 */
	public static final int MAX_DSAMPLES = 64;
	
	/**
	 * @see FtmSequence#SEQUENCE_COUNT
	 */
	public static final int SEQUENCE_COUNT = FtmSequence.SEQUENCE_COUNT;
	
	/**
	 * @see FtmSequence#SEQUENCE_COUNT_FDS
	 */
	public static final int SEQUENCE_COUNT_FDS = FtmSequence.SEQUENCE_COUNT_FDS;

}
