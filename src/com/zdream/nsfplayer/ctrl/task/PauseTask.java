package com.zdream.nsfplayer.ctrl.task;

import com.zdream.nsfplayer.ctrl.INsfPlayerEnv;

/**
 * 暂停播放任务
 * @author Zdream
 * @date 2017-09-23
 */
public class PauseTask implements ITask {

	@Override
	public void setOption(String key, Object arg) {
		
	}

	@Override
	public void execute(INsfPlayerEnv env) {
		env.getPlayer().getStatus().pause = true;
	}

}
