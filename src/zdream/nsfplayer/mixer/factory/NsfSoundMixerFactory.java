package zdream.nsfplayer.mixer.factory;

import static java.util.Objects.requireNonNull;

import zdream.nsfplayer.core.NsfCommonParameter;
import zdream.nsfplayer.core.NsfPlayerException;
import zdream.nsfplayer.mixer.IMixerConfig;
import zdream.nsfplayer.mixer.ISoundMixer;
import zdream.nsfplayer.mixer.blip.BlipMixerConfig;
import zdream.nsfplayer.mixer.blip.BlipSoundMixer;
import zdream.nsfplayer.mixer.xgm.XgmMixerConfig;
import zdream.nsfplayer.mixer.xgm.XgmMultiSoundMixer;

/**
 * <p>NSF 的混音器产生工厂
 * 
 * @author Zdream
 * @since v0.3.0
 */
public class NsfSoundMixerFactory {
	
	public ISoundMixer create(IMixerConfig config, NsfCommonParameter param) {
		requireNonNull(config, "config = null");
		requireNonNull(param, "param = null");
		
		ISoundMixer m;
		if (config instanceof XgmMixerConfig) {
			// 采用 Xgm 音频混合器 (原 NsfPlayer 使用的)
			XgmMultiSoundMixer mixer = new XgmMultiSoundMixer();
			mixer.setConfig((XgmMixerConfig) config);
			mixer.param = param;
			m = mixer;
		} else if (config instanceof BlipMixerConfig) {
			// 采用 Blip 音频混合器 (原 FamiTracker 使用的)
			BlipMixerConfig c = (BlipMixerConfig) config;
			
			BlipSoundMixer mixer = new BlipSoundMixer();
			mixer.sampleRate = param.sampleRate;
			mixer.setConfig(c);
			mixer.param = param;
			m = mixer;
		} else {
			// TODO 暂时不支持 xgm 和 blip 之外的 mixerConfig
			throw new NsfPlayerException("暂时不支持 xgm 和 blip 之外的 mixerConfig");
		}

		m.init();
		return m;
	}

}
