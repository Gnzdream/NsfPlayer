package zdream.nsfplayer.mixer.xgm;

import java.util.ArrayList;

import zdream.nsfplayer.mixer.interceptor.ISoundInterceptor;

/**
 * 合并轨道父类
 * 
 * @author Zdream
 * @since v0.2.3
 */
public abstract class AbstractXgmMultiMixer implements IXgmMultiChannelMixer {

	protected final ArrayList<ISoundInterceptor> interceptors = new ArrayList<>();
	
	/**
	 * 缓存, 性能考虑
	 */
	private ISoundInterceptor[] interceptorArray;
	
	/**
	 * @param value
	 * @param time
	 *   过去的时间数, 一般为 1. 单位为一个出采样率的时间间隔
	 * @return
	 */
	protected int intercept(int value, int time) {
		int ret = value;
		final int length = interceptorArray.length;
		for (int i = 0; i < length; i++) {
			ISoundInterceptor interceptor = interceptorArray[i];
			if (interceptor.isEnable()) {
				ret = interceptor.execute(ret, time);
			}
		}
		return ret;
	}
	
	/**
	 * 添加音频数据的拦截器
	 * @param interceptor
	 */
	public void attachIntercept(ISoundInterceptor interceptor) {
		if (interceptor != null) {
			interceptors.add(interceptor);
		}
	}
	
	@Override
	public void reset() {
		for (ISoundInterceptor i : interceptors) {
			i.reset();
		}
	}
	
	@Override
	public void beforeRender() {
		if (interceptorArray == null || interceptorArray.length != interceptors.size()) {
			interceptorArray = new ISoundInterceptor[interceptors.size()];
		}
		interceptors.toArray(interceptorArray);
	}
	
	/**
	 * <p>设置是否启用
	 * <p>考虑到 AbstractXgmAudioChannel 可以被用户拿到,
	 * 用户可以用 enable 方法直接修改匹配 AbstractXgmAudioChannel 的逻辑,
	 * 导致 Mixer 不稳定, 因此从版本 v0.3.0 开始起, enable 参数将挪到
	 * 合并轨道中设置.
	 * </p>
	 * @see #isEnable(AbstractXgmAudioChannel)
	 * @param channel
	 *   轨道实例
	 * @param enable
	 *   启用标识
	 * @since v0.3.0
	 */
	public abstract void setEnable(AbstractXgmAudioChannel channel, boolean enable);
	
	/**
	 * <p>获取是否启用
	 * </p>
	 * @see #setEnable(AbstractXgmAudioChannel, boolean)
	 * @param channel
	 *   轨道实例
	 * @since v0.3.0
	 */
	public abstract boolean isEnable(AbstractXgmAudioChannel channel);
	
}
