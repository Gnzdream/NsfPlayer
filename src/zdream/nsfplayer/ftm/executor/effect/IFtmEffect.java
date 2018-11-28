package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;

/**
 * Ftm 效果
 * 
 * @version 0.2.2
 * 从该版本开始, 效果可以根据优先度进行排序
 * 
 * @author Zdream
 * @since 0.2.1
 */
public interface IFtmEffect extends Comparable<IFtmEffect> {
	
	/**
	 * <p>效果种类.
	 * <p>允许不同效果类有相同的种类, 比如 {@link NoteEffect} 和 {@link NoiseEffect}.
	 * 它们在不同的轨道使用.
	 * </p>
	 * @return
	 */
	public FtmEffectType type();
	
	/**
	 * 执行效果
	 * @param channelCode
	 *   当前轨道号码
	 * @param runtime
	 */
	public void execute(byte channelCode, FamiTrackerRuntime runtime);
	
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
	default int compareTo(IFtmEffect o) {
		return o.priority() - priority();
	}

}
