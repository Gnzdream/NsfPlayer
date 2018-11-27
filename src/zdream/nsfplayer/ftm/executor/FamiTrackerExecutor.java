package zdream.nsfplayer.ftm.executor;

import static java.util.Objects.requireNonNull;

import zdream.nsfplayer.core.AbstractNsfExecutor;
import zdream.nsfplayer.ftm.audio.FamiTrackerException;
import zdream.nsfplayer.ftm.audio.FtmAudio;

/**
 * <p>FamiTracker 的执行构件.
 * <p>在 0.2.x 版本中, FamiTracker 的执行部分是直接写在 FamiTrackerRenderer 中的,
 * 从版本 0.3.0 开始, 执行构件从 renderer 中分离出来, 单独构成一个类.
 * 它交接了原本是需要 FamiTrackerRuntime 或 FamiTrackerRenderer 完成的任务中, 与执行相关的任务.
 * </p>
 * 
 * @author Zdream
 * @since v0.3.0
 */
public class FamiTrackerExecutor extends AbstractNsfExecutor<FtmAudio> {
	
	/* **********
	 *   成员   *
	 ********** */
	private final FamiTrackerRuntime runtime;
	
	/* **********
	 * 准备部分 *
	 ********** */

	/**
	 * <p>让该执行器读取对应的 audio 数据.
	 * <p>设置播放暂停位置为第 1 个曲目 (曲目 0) 的第一段 (段 0)
	 * </p>
	 * @param audio
	 *   FamiTracker 的封装的曲目
	 */
	public void ready(FtmAudio audio) throws FamiTrackerException {
		ready(audio, 0, 0);
	}
	
	/**
	 * <p>让该执行器读取对应的 audio 数据.
	 * <p>设置播放暂停位置为指定曲目的第一段 (段 0)
	 * </p>
	 * @param audio
	 *   FamiTracker 的封装的曲目
	 * @param track
	 *   曲目号, 从 0 开始
	 */
	public void ready(FtmAudio audio, int track) throws FamiTrackerException {
		ready(audio, track, 0);
	}
	
	/**
	 * <p>让该执行器读取对应的 audio 数据.
	 * <p>设置播放暂停位置为指定曲目的指定段
	 * </p>
	 * @param audio
	 *   FamiTracker 的封装的曲目
	 * @param track
	 *   曲目号, 从 0 开始
	 * @param section
	 *   段号, 从 0 开始
	 */
	public void ready(
			FtmAudio audio,
			int track,
			int section)
			throws FamiTrackerException {
		requireNonNull(audio, "FamiTracker 曲目 audio = null");
		
		runtime.ready(audio, track, section);
		
		
		
//		// 重置播放相关的数据
//		int frameRate = runtime.fetcher.getFrameRate();
//		resetCounterParam(frameRate, runtime.param.sampleRate);
//		clearBuffer();
//		runtime.rate.onParamUpdate(frameRate, BASE_FREQ_NTSC);
//		
//		initMixer();
//		initChannels();
	}
	
	/* **********
	 *   其它   *
	 ********** */

	public FamiTrackerExecutor() {
		this(new FamiTrackerRuntime());
	}

	public FamiTrackerExecutor(FamiTrackerRuntime runtime) {
		this.runtime = runtime;
	}

	@Override
	public void tick() {
		// TODO Auto-generated method stub

	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

}
