package zdream.nsfplayer.ftm.task;

import zdream.nsfplayer.ftm.FtmPlayerConsole;
import zdream.nsfplayer.ftm.renderer.FamiTrackerRenderer;

/**
 * 播放任务
 * @author Zdream
 */
public class PlayTask implements IFtmTask {
	
	// 模拟播放池
	private static final PlayTask[] POOL = new PlayTask[5];
	private static int getPtr = 0;
	static {
		for (int i = 0; i < POOL.length; i++) {
			POOL[i] = new PlayTask();
		}
	}
	
	public static PlayTask getOne() {
		getPtr = (getPtr + 1) % POOL.length;
		PlayTask t = POOL[getPtr];
		return t;
	}
	
	public static final String OPT_REPLAY = "replay";
	
	private PlayTask() {
		
	}

	@Override
	public void setOption(String key, Object arg) { }

	@Override
	public void execute(FtmPlayerConsole env) {
		
		FamiTrackerRenderer renderer = env.getRenderer();
		
		byte[] bs = env.getLastSampleBytes();
		int size = renderer.render(bs, 0, bs.length);
		env.writeSamples(0, size);
		
		// 放完了, 就切歌, 换到下一首
		if (renderer.isFinished()) {
			ChooseSongTask t = new ChooseSongTask(renderer.getCurrentTrack() + 1);
			t.setOption("needReset", true);
			env.putTask(t);
		}
		
		// 循环播放, 所以要再放一个 task
		IFtmTask t = env.nextTask();
		if (t == null || t.getClass() != getClass()) {
			PlayTask p = getOne();
			env.putTask(p);
		}
	}

}
