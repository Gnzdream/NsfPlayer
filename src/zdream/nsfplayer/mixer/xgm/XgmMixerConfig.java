package zdream.nsfplayer.mixer.xgm;

import zdream.nsfplayer.mixer.IMixerConfig;

/**
 * Xgm 混音器的配置项
 * 
 * @author Zdream
 * @since v0.2.5
 */
public class XgmMixerConfig implements IMixerConfig {
	
	/**
	 * <p>合并轨道播放. 默认值
	 * </p>
	 */
	public static final int TYPE_MULTI = 0;
	
	/**
	 * <p>单一轨道播放. 所有轨道相互独立, 各不受其它轨道影响.
	 * <p>对 N163 轨道的影响比较大. 设置为单一轨道播放后, 需要对 N163 轨道单独配置音量
	 * </p>
	 */
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
