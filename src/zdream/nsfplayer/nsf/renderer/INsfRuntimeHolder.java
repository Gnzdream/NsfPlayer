package zdream.nsfplayer.nsf.renderer;

/**
 * Nsf 运行时环境的持有者
 * 
 * @author Zdream
 * @since v0.2.4
 */
public interface INsfRuntimeHolder {
	
	/**
	 * 获得运行环境实例
	 * @return
	 */
	public NsfRuntime getRuntime();

}
