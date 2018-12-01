package zdream.nsfplayer.mixer.blip;

import zdream.nsfplayer.mixer.IMixerHandler;

/**
 * Blip 混音器的操作类
 * 
 * @author Zdream
 * @since v0.2.10
 */
public class BlipMixerHandler implements IMixerHandler {
	
	@SuppressWarnings("unused")
	private BlipSoundMixer mixer;
	
	BlipMixerHandler(BlipSoundMixer mixer) {
		this.mixer = mixer;
	}

}
