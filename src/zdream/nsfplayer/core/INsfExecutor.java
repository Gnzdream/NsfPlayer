package zdream.nsfplayer.core;

/**
 * <p>NSF 执行构件的接口
 * </p>
 * 
 * @param <T>
 *   帧执行器需要依赖某个输入数据作为作初始化的依据.
 *   在原来的 Renderer 体系中, 这个就是各个 Audio 封装类.
 * 
 * @author Zdream
 * @since v0.3.0
 */
public interface INsfExecutor<T> extends IEnable, IResetable {
	
	/**
	 * 以某项输入数据作为初始化依据
	 * @param t
	 *   输入数据
	 */
	public void ready(T t);
	
	/**
	 * 执行一个单位时间. 这个时间长度因不同的构件而异.
	 */
	public void tick();
	
}
