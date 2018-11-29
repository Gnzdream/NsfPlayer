package zdream.nsfplayer.nsf.renderer;

import zdream.nsfplayer.core.ChannelLevelsParameter;
import zdream.nsfplayer.nsf.executor.NsfExecutor;
import zdream.nsfplayer.sound.mixer.IMixerConfig;

/**
 * NSF 渲染器配置
 * @author Zdream
 * @date 2018-05-09
 * @since v0.1
 */
public class NsfRendererConfig implements Cloneable {
	
	/**
	 * 帧率
	 */
	public int sampleRate = 48000;
	
	/**
	 * 用户指定用哪种制式进行播放, NTSC 或者 PAL
	 */
	public int region;
	/**
	 * 跟随 Nsf 文件中指定的制式
	 */
	public static final int REGION_FOLLOW_AUDIO = NsfExecutor.REGION_FOLLOW_AUDIO;
	/**
	 * 强制要求 NTSC
	 */
	public static final int REGION_FORCE_NTSC = NsfExecutor.REGION_FORCE_NTSC;
	/**
	 * 强制要求 PAL
	 */
	public static final int REGION_FORCE_PAL = NsfExecutor.REGION_FORCE_PAL;
	/**
	 * 强制要求 DENDY
	 */
	public static final int REGION_FORCE_DENDY = NsfExecutor.REGION_FORCE_DENDY;
	
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
	public NsfRendererConfig clone() {
		NsfRendererConfig c = new NsfRendererConfig();
		
		c.sampleRate = this.sampleRate;
		c.region = this.region;
		c.channelLevels.copyFrom(channelLevels);
		if (mixerConfig != null) {
			c.mixerConfig = mixerConfig.clone();
		}
		
		return c;
	}

}
