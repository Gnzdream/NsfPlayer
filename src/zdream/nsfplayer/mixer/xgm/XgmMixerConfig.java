package zdream.nsfplayer.mixer.xgm;

import zdream.nsfplayer.mixer.IMixerConfig;

/**
 * Xgm 混音器的配置项
 * 
 * @author Zdream
 * @since v0.2.5
 */
public class XgmMixerConfig implements IMixerConfig {
	// nothing
	
	@Override
	public XgmMixerConfig clone() {
		try {
			return (XgmMixerConfig) super.clone();
		} catch (CloneNotSupportedException e) {}
		return null;
	}
}
