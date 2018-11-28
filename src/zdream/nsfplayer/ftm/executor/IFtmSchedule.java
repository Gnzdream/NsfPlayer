package zdream.nsfplayer.ftm.executor;

/**
 * <p>每帧开始前触发的任务. 仅在第一帧的效果触发之前执行一次, 执行完后就删除
 * </p>
 * 
 * @author Zdream
 * @since 0.2.2
 */
public interface IFtmSchedule {
	
	/**
	 * 每帧触发的方法体
	 * @param channelCode
	 *   当前轨道号码
	 * @param runtime
	 *   运行环境
	 */
	public void trigger(byte channelCode, FamiTrackerRuntime runtime);

}
