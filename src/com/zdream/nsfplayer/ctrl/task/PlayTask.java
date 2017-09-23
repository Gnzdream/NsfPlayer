package com.zdream.nsfplayer.ctrl.task;

import com.zdream.nsfplayer.ctrl.INsfPlayerEnv;
import com.zdream.nsfplayer.xgm.player.nsf.NsfPlayer;

/**
 * 播放任务
 * @author Zdream
 */
public class PlayTask implements ITask {
	
	private static final PlayTask[] POOL = new PlayTask[5];
	private static int getPtr = 0;
	static {
		for (int i = 0; i < POOL.length; i++) {
			POOL[i] = new PlayTask();
		}
	}
	
	private PlayTask() {
		
	}
	
	public static PlayTask getOne() {
		getPtr = (getPtr + 1) % POOL.length;
		return POOL[getPtr];
	}

	@Override
	public void setOption(String key, Object arg) {
		
	}

	@Override
	public void execute(INsfPlayerEnv env) {
		
		// 检查是否播放完毕
		NsfPlayer player = env.getPlayer();
		if (player.isStopped()) {
			
			// 不需要担心曲目号超过上限. 因为有检查, 最终曲目号需要除余后决定.
			ChooseSongTask t = new ChooseSongTask(player.getSong() + 1);
			
			t.setOption("needReset", true);
			env.putTask(t);
			return;
		}
		
		// 循环播放, 所以要再放一个 task
		// 这一步如果放到最后, 那播放器听起来可能会很卡
		env.putTask(getOne());
		
		byte[] b = env.getLastSampleBytes();
		int samples = env.getPlayer().render(b, 0, b.length);
		env.writeSamples(0, samples);
		
		this.reset();
	}
	
	public void reset() {
		
	}

}
