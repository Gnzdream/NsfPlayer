package com.zdream.nsfplayer.ctrl.task;

import com.zdream.nsfplayer.ctrl.INsfPlayerEnv;
import com.zdream.nsfplayer.xgm.player.nsf.NsfPlayer;

/**
 * 播放任务
 * @author Zdream
 */
public class PlayTask implements ITask {
	
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
		t.reset();
		return t;
	}
	
	public static final String OPT_REPLAY = "replay";
	
	/**
	 * 如果是从暂停中恢复播放, 这个参数要置为 true
	 */
	boolean replay;
	
	private PlayTask() {
		
	}

	@Override
	public void setOption(String key, Object arg) {
		if (OPT_REPLAY.equals(key)) {
			replay = (Boolean) arg;
		}
	}

	@Override
	public void execute(INsfPlayerEnv env) {
		
		// 检查是否播放完毕
		NsfPlayer player = env.getPlayer();
		if (player.isStopped()) {
			// 是否需要切歌
			if (!player.getStatus().replace) {
				// 不需要担心曲目号超过上限. 因为有检查, 最终曲目号需要除余后决定.
				ChooseSongTask t = new ChooseSongTask(player.getSong() + 1);
				
				t.setOption("needReset", true);
				env.putTask(t);
				player.getStatus().replace = true;
			}
			
			return;
		}
		
		if (replay) {
			player.getStatus().pause = false;
		} else if (player.isPaused()) {
			return;
		}
		
		// 循环播放, 所以要再放一个 task
		ITask t = env.nextTask();
		if (t == null || t.getClass() != getClass()) {
			env.putTask(getOne());
		}
		
		byte[] b = env.getLastSampleBytes();
		int samples = env.getPlayer().render(b, 0, b.length);
		env.writeSamples(0, samples);
	}
	
	public void reset() {
		replay = false;
	}

}
