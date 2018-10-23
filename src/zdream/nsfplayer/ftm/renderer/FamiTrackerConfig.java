package zdream.nsfplayer.ftm.renderer;

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
	
	public final ChannelLevels channelLevels = new ChannelLevels();
	
}
