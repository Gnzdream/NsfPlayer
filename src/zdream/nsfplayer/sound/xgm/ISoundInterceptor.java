package zdream.nsfplayer.sound.xgm;

import zdream.nsfplayer.core.IResetable;

/**
 * 音频数据的拦截器
 * 
 * @author Zdream
 * @since v0.2.3
 */
public interface ISoundInterceptor extends IResetable {
	
	public int execute(int value, int time);

}
