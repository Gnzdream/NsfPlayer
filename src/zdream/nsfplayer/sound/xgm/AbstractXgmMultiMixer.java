package zdream.nsfplayer.sound.xgm;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * 合并轨道父类
 * 
 * @author Zdream
 * @since v0.2.3
 */
public abstract class AbstractXgmMultiMixer implements IXgmMultiChannelMixer {

	protected ArrayList<ISoundInterceptor> interceptors = new ArrayList<>();
	
	/**
	 * @param value
	 * @param time
	 *   过去的时钟周期数
	 * @return
	 */
	protected int intercept(int value, int time) {
		int i = value;
		for (Iterator<ISoundInterceptor> it = interceptors.iterator(); it.hasNext();) {
			ISoundInterceptor interceptor = it.next();
			i = interceptor.execute(i, time);
			
			if (i == Short.MAX_VALUE) {
				value += 0;
			}
		}
		return i;
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
	
}
