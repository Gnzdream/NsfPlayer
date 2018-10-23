package zdream.nsfplayer.ftm.renderer;

import zdream.nsfplayer.ftm.format.FtmTrack;

/**
 * 用于设置启动 {@link FamiTrackerRenderer} 的启动参数.
 * 
 * @author Zdream
 * @since v0.2.3
 */
public class FamiTrackerConfig {

	public FamiTrackerConfig() {
		
	}
	
	/**
	 * 原 sound.iSampleRate
	 * 渲染出的采样率
	 */
	public int sampleRate = 48000;
	
	/**
	 * 原 sound.iSampleSize
	 * 渲染每个采样点的位深, 以 bit 计
	 */
	public int sampleSize = 16;
	
	/**
	 * 帧率
	 */
	//public int frameRate = 60;
	
	/**
	 * BlipBuffer 参数
	 */
	public int bassFilter = 30,
			trebleFilter = 12000,
			trebleDamping = 24,
			mixVolume = 100;
	
	/**
	 * 默认全是 1
	 */
	public class ChannelLevels{
		
		public float level2A03Pules1 = 1.0f;
		public float level2A03Pules2 = 1.0f;
		public float level2A03Triangle = 1.0f;
		public float level2A03Noise = 1.0f;
		public float level2A03DPCM = 1.0f;
		
		public float levelVRC6Pules1 = 1.0f;
		public float levelVRC6Pules2 = 1.0f;
		public float levelVRC6Sawtooth = 1.0f;
		
		public float levelMMC5Pules1 = 1.0f;
		public float levelMMC5Pules2 = 1.0f;
		
		public float levelFDS = 1.0f;
//		public float levelVRC7 = 1.0f;
//		public float levelN163 = 1.0f;
//		public float levelS5B = 1.0f;
	}
	
	public ChannelLevels channelLevels = new ChannelLevels();
	
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
	public static final int MAX_PATTERN = MAX_FRAMES;
	
	/**
	 * Maximum number of DPCM samples, cannot be increased unless the NSF driver is modified.
	 */
	public static final int MAX_DSAMPLES = 64;
}
