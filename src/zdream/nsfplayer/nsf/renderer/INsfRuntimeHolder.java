package zdream.nsfplayer.nsf.renderer;

/**
 * Nsf 运行时状态的持有者
 * 
 * @author Zdream
 * @since v0.2.4
 */
public interface INsfRuntimeHolder {
	
	/**
	 * 获得运行状态数据
	 * @return
	 */
	public NsfRuntime getRuntime();

}
