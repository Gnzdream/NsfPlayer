package zdream.nsfplayer.ftm.renderer;

/**
 * <p>状态. 每个轨道和全局在播放时, 每帧都进行触发的.
 * </p>
 * 
 * @author Zdream
 * @since 0.2.1
 */
public interface IFtmState {
	
	/**
	 * 标识名称
	 * @return
	 */
	public String name();
	
	/**
	 * 每帧触发的方法体
	 * @param channelCode
	 *   当前轨道号码
	 * @param runtime
	 */
	public void trigger(byte channelCode, FamiTrackerRuntime runtime);
	
	/**
	 * 当该 state 被装配时,
	 * 即被轨道或全局状态机装配时触发
	 * @param channelCode
	 *   当前轨道号码
	 * @param runtime
	 */
	default public void onAttach(byte channelCode, FamiTrackerRuntime runtime) {}
	
	/**
	 * 当该 state 被拆除时,
	 * 即被轨道或全局状态机拆除时触发
	 * @param channelCode
	 *   当前轨道号码
	 * @param runtime
	 */
	default public void onDetach(byte channelCode, FamiTrackerRuntime runtime) {}
	
}
