package zdream.nsfplayer.ftm.renderer;

/**
 * Famitracker 运行时状态的持有者
 * 
 * @author Zdream
 * @since 0.2.1
 */
public interface IFtmRuntimeHolder {
	
	/**
	 * 获得运行状态数据
	 * @return
	 */
	public FamiTrackerRuntime getRuntime();

}
