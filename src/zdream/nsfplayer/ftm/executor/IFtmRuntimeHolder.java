package zdream.nsfplayer.ftm.executor;

/**
 * Famitracker 运行时环境的持有者
 * 
 * @author Zdream
 * @since 0.2.1
 */
public interface IFtmRuntimeHolder {
	
	/**
	 * 获得运行环境实例
	 * @return
	 */
	public FamiTrackerRuntime getRuntime();

}
