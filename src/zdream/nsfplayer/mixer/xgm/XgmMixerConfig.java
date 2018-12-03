package zdream.nsfplayer.mixer.xgm;

import zdream.nsfplayer.mixer.IMixerConfig;

/**
 * Xgm 混音器的配置项
 * 
 * @author Zdream
 * @since v0.2.5
 */
public class XgmMixerConfig implements IMixerConfig {
	
	public static final int TYPE_MULTI = 0;
	public static final int TYPE_SINGER = 1;
	
	/**
	 * 选择使用单轨还是合并轨
	 */
	public int channelType = TYPE_MULTI;
	
	@Override
	public XgmMixerConfig clone() {
		try {
			return (XgmMixerConfig) super.clone();
		} catch (CloneNotSupportedException e) {}
		return null;
	}
}
