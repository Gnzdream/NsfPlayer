package zdream.nsfplayer.ftm.executor.hook;

import zdream.nsfplayer.ftm.executor.FamiTrackerExecutorHandler;
import zdream.nsfplayer.ftm.format.FtmNote;

/**
 * <p>当 FamiTracker 执行构件取到当前帧 {@link FtmNote} 时, 将会唤醒该类监听器
 * <p>实际上, 这类监听器每帧都会唤醒多次, 无论有没有获取到新的 {@link FtmNote},
 * 次数等于 FamiTracker 的当前轨道个数.
 * 这类监听器有能力修改 {@link FtmNote} 的内容.
 * </p>
 * 
 * @author Zdream
 * @since v0.3.0
 */
public interface IFtmFetchListener {
	
	/**
	 * 当执行构件获取新的 {@link FtmNote} 或者没获取到 Note 时调用.
	 * @param note
	 *   FTM 音键
	 * @param channelCode
	 *   所在的轨道
	 * @param handler
	 */
	public FtmNote onFetch(
			FtmNote note,
			byte channelCode,
			FamiTrackerExecutorHandler handler);
	
}
