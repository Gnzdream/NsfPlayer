package com.zdream.nsfplayer.ctrl.task;

import java.io.IOException;

import com.zdream.nsfplayer.ctrl.INsfPlayerEnv;

/**
 * 打开文件任务
 * @author Zdream
 */
public class OpenTask implements ITask {
	
	String filename;
	int beginSong;

	@Override
	public void setOption(String key, Object arg) {
		if ("filename".equals(key) || "f".equals(key)) {
			filename = arg.toString();
		}
	}

	@Override
	public void execute(INsfPlayerEnv env) {
		boolean result = false;
		try {
			result = env.getNsf().loadFile(filename);
		} catch (IOException e) {
			System.out.println("读取文件失败");
			return;
		}
		
		if (!result) {
			// 报告错误
			return;
		}
		
		env.getPlayer().setSong(beginSong);
		// 放入播放的 task
		env.putTask(new PlayTask());
	}

}
