package com.zdream.nsfplayer.ctrl.task;

import java.io.IOException;

import com.zdream.nsfplayer.ctrl.INsfPlayerEnv;

/**
 * 打开文件任务
 * @author Zdream
 */
public class OpenTask implements ITask {
	
	String filename;
	int beginSong = -1;
	
	public OpenTask() {}

	/**
	 * 默认从第 0 首歌开始播放
	 * @param filename
	 */
	public OpenTask(String filename) {
		super();
		this.filename = filename;
	}

	@Override
	public void setOption(String key, Object arg) {
		if ("filename".equals(key) || "f".equals(key)) {
			filename = arg.toString();
		} else if ("beginSong".equals(key) || "s".equals(key)) {
			beginSong = (Integer) arg;
		}
	}

	@Override
	public void execute(INsfPlayerEnv env) {
		boolean result = false;
		try {
			result = env.getNsf().loadFile(filename);
		} catch (IOException e) {
			env.printOut("[OPEN] 读取文件: %s 失败. 继续播放原音频", filename);
			return;
		}
		
		if (!result) {
			// 报告错误
			return;
		}
		
		// 输出提示文字
		env.printOut("[OPEN] 尝试打开文件: %s", filename);
		
		if (beginSong <= 0) {
			// 不更换曲目号, 直接放入播放的 task
			env.getPlayer().reset();
			env.putTask(PlayTask.getOne());
		} else {
			// 更换曲目号
			ITask t = new ChooseSongTask(beginSong);
			t.setOption("needReset", true);
			env.putTask(t);
		}
		
		// 不暂停
		env.getPlayer().getStatus().pause = false;
	}

}
