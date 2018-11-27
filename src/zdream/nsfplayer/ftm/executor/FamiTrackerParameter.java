package zdream.nsfplayer.ftm.executor;

import zdream.nsfplayer.core.NsfCommonParameter;

/**
 * <p>NSF 以及常量及常量计算相关, 比如储存每一帧的时钟周期数等等
 * </p>
 * 
 * @author Zdream
 * @since v0.2.1
 */
public class FamiTrackerParameter extends NsfCommonParameter {
	
	/**
	 * 现在仅允许包内进行实例化
	 */
	FamiTrackerParameter() {
		super();
	}
	
	/* **********
	 * 播放参数 *
	 ********** */
	
	/**
	 * 记录正在播放的行号
	 */
	public int curRow;
	
	/**
	 * 正在播放的段号
	 */
	public int curSection;

}
