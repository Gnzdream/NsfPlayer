package zdream.nsfplayer.ftm.renderer;

import zdream.nsfplayer.core.ChannelLevelsParameter;
import zdream.nsfplayer.mixer.IMixerConfig;

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
	public final ChannelLevelsParameter channelLevels = new ChannelLevelsParameter();
	
	@Override
	public FamiTrackerConfig clone() {
		FamiTrackerConfig c = new FamiTrackerConfig();
		
		c.sampleRate = this.sampleRate;
		c.channelLevels.copyFrom(channelLevels);
		if (mixerConfig != null) {
			c.mixerConfig = mixerConfig.clone();
		}
		
		return c;
	}
	
}
