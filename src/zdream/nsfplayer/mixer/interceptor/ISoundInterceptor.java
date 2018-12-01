package zdream.nsfplayer.mixer.interceptor;

import zdream.nsfplayer.core.IEnable;
import zdream.nsfplayer.core.IResetable;

/**
 * 音频数据的拦截器
 * 
 * @author Zdream
 * @since v0.2.3
 */
public interface ISoundInterceptor extends IResetable, IEnable {
	
	public int execute(int value, int time);

}
