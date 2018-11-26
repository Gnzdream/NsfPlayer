package zdream.nsfplayer.sound.xgm;

import java.util.List;

import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.sound.interceptor.ISoundInterceptor;
import zdream.nsfplayer.sound.mixer.IMixerHandler;

/**
 * Xgm 混音器的操作类
 * 
 * @author Zdream
 * @since v0.2.10
 */
public class XgmMixerHandler implements IMixerHandler {

	private XgmSoundMixer mixer;

	XgmMixerHandler(XgmSoundMixer mixer) {
		this.mixer = mixer;
	}
	
	/**
	 * 返回全局的混音拦截器组
	 * @return
	 *   Xgm 混音器的全局拦截器组
	 */
	public List<ISoundInterceptor> getGlobalInterceptors() {
		return mixer.interceptors;
	}
	
	/**
	 * 返回各个芯片对应的拦截器组
	 * @param chip
	 *   <br>芯片号, 为 {@link INsfChannelCode} 的静态成员 CHIP_*. 
	 *   <br>另外 2A03 的三角波、噪音、DPCM 的芯片号为 0xF, 而非 CHIP_2A03, 需要注意.
	 * @return
	 *   对应芯片的混音拦截器组
	 */
	public List<ISoundInterceptor> getChipInterceptors(byte chip) {
		AbstractXgmMultiMixer multi = mixer.multis.get(chip);
		
		if (multi == null) {
			return null;
		}
		return multi.interceptors;
	}

}
