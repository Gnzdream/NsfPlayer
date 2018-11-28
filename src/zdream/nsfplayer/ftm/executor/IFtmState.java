package zdream.nsfplayer.ftm.executor;

/**
 * <p>状态. 每个轨道和全局在播放时, 每帧都进行触发的.
 * </p>
 * 
 * @version 0.2.2
 * 从该版本开始, 状态可以根据优先度进行排序
 * 
 * @author Zdream
 * @since 0.2.1
 */
public interface IFtmState extends Comparable<IFtmState> {
	
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
	 * 即被轨道或全局状态机装配时触发 (在添加后)
	 * @param channelCode
	 *   当前轨道号码
	 * @param runtime
	 */
	default public void onAttach(byte channelCode, FamiTrackerRuntime runtime) {}
	
	/**
	 * 当该 state 被拆除时,
	 * 即被轨道或全局状态机拆除时触发 (在拆除前)
	 * @param channelCode
	 *   当前轨道号码
	 * @param runtime
	 */
	default public void onDetach(byte channelCode, FamiTrackerRuntime runtime) {}
	
	/**
	 * 优先度. 优先度越大的越先执行.
	 * @return
	 * @since 0.2.2
	 */
	default int priority() {
		return 0;
	}
	
	/**
	 * 默认是按照从高到低的顺序进行排序
	 * @since 0.2.2
	 */
	default int compareTo(IFtmState o) {
		return o.priority() - priority();
	}
	
}
