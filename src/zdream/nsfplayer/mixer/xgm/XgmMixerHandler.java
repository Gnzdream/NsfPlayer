package zdream.nsfplayer.mixer.xgm;

import java.util.List;

import zdream.nsfplayer.mixer.IMixerHandler;
import zdream.nsfplayer.mixer.interceptor.ISoundInterceptor;
import zdream.nsfplayer.mixer.xgm.XgmMultiSoundMixer.XgmMultiChannelAttr;

/**
 * Xgm 混音器的操作类
 * 
 * @author Zdream
 * @since v0.2.10
 */
public class XgmMixerHandler implements IMixerHandler {

	private XgmMultiSoundMixer mixer;

	XgmMixerHandler(XgmMultiSoundMixer mixer) {
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
	 * <p>返回轨道标识号对应轨道的拦截器组.
	 * <p>如果选用的混音器是 {@link XgmMultiSoundMixer},
	 * 那么返回的是轨道标识号对应合并轨道的拦截器组.
	 * </p>
	 * @param id
	 *   <br>代表对应轨道的标识号
	 * @return
	 *   拦截器组
	 */
	public List<ISoundInterceptor> getChipInterceptors(int id) {
		XgmMultiChannelAttr attr = mixer.getAttr(id);
		AbstractXgmMultiMixer multi = attr.multi;
		
		if (multi == null) {
			return null;
		}
		return multi.interceptors;
	}

}
