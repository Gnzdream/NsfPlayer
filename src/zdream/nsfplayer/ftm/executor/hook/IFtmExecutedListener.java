package zdream.nsfplayer.ftm.executor.hook;

import zdream.nsfplayer.ftm.executor.FamiTrackerExecutorHandler;

/**
 * <p>效果执行结束的监听器
 * <p>当 FamiTracker 执行构件执行完一帧的工作,
 * 还没有向 Sound 内写数据时唤醒这类监听器. 一帧只会调用一次.
 * </p>
 * 
 * @author Zdream
 * @since v0.3.0
 */
public interface IFtmExecutedListener {
	
	/**
	 * 轨道效果执行完成, 还未将数据写入 sound 或其它部分,
	 * 这时是数据渲染前最后一次修改 channel 数据的机会.
	 * @param handler
	 */
	public void onExecuteFinished(FamiTrackerExecutorHandler handler);

}
