package zdream.nsfplayer.ftm.renderer;

import zdream.nsfplayer.sound.mixer.IMixerConfig;

/**
 * 用于设置启动 {@link FamiTrackerRenderer} 的启动参数.
 * 
 * @author Zdream
 * @since v0.2.3
 */
public class FamiTrackerConfig implements Cloneable {

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
	 * 
	 * v0.2.5 现在对该参数没有实际上的控制权.
	 * 无论写多少, 最后输出的一定时 16 bit.
	 * 所以先将其注释掉
	 */
	// public int sampleSize = 16;
	
	/**
	 * Mixer 参数
	 */
	public IMixerConfig mixerConfig;
	
	/* **********
	 *   音量   *
	 ********** */
	
	/**
	 * 默认全是 1
	 */
	public class ChannelLevels {
		
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
	
	@Override
	public FamiTrackerConfig clone() {
		try {
			return (FamiTrackerConfig) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
