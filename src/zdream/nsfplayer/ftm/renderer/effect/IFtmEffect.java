package zdream.nsfplayer.ftm.renderer.effect;

import zdream.nsfplayer.ftm.renderer.FamiTrackerRuntime;

/**
 * Ftm 效果
 * 
 * @author Zdream
 * @since 0.2.1
 */
public interface IFtmEffect {
	
	/**
	 * 效果种类
	 * @return
	 */
	public FtmEffectType type();
	
	/**
	 * 执行效果
	 * @param channelCode
	 *   当前轨道号码
	 * @param rumtime
	 */
	default public void execute(byte channelCode, FamiTrackerRuntime rumtime) {}

}
