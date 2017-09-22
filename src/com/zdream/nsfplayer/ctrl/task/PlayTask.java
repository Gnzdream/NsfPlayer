package com.zdream.nsfplayer.ctrl.task;

import com.zdream.nsfplayer.ctrl.INsfPlayerEnv;

/**
 * 播放任务
 * @author Zdream
 */
public class PlayTask implements ITask {

	@Override
	public void setOption(String key, Object arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void execute(INsfPlayerEnv env) {
		byte[] b = env.getLastSampleBytes();
		int samples = env.getPlayer().render(b, 0, b.length);
		
		env.writeSamples(0, samples);
		
		// TODO 检查是否播放完毕
		// TODO 循环播放, 所以要再放一个 task
	}

}
