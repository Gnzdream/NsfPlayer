package zdream.nsfplayer.mixer.blip;

import zdream.nsfplayer.mixer.IMixerConfig;

/**
 * Blip 混音器的配置项
 * 
 * @author Zdream
 * @since v0.2.5
 */
public class BlipMixerConfig implements IMixerConfig {

	public int bassFilter = 30,
			trebleFilter = 12000,
			trebleDamping = 24;
	
	@Override
	public BlipMixerConfig clone() {
		try {
			return (BlipMixerConfig) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

}
