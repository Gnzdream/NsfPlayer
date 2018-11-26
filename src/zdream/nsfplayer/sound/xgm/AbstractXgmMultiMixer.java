package zdream.nsfplayer.sound.xgm;

import java.util.ArrayList;

import zdream.nsfplayer.sound.interceptor.ISoundInterceptor;

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
	 *   过去的时钟周期数
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
	
}
