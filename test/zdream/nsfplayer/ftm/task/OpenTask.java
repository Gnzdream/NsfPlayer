package zdream.nsfplayer.ftm.task;

import java.io.IOException;

import zdream.nsfplayer.ftm.FamiTrackerApplication;
import zdream.nsfplayer.ftm.FtmPlayerConsole;
import zdream.nsfplayer.ftm.audio.FtmAudio;

/**
 * 打开文件任务
 * @author Zdream
 */
public class OpenTask implements IFtmTask {
	
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
	public void execute(FtmPlayerConsole env) {
		try {
			FtmAudio audio = FamiTrackerApplication.app.open(filename);
			env.setAudio(audio);
			env.getRenderer().ready(audio);
		} catch (IOException | RuntimeException e) {
			env.printOut("[OPEN] 读取错误原因: %s", e.getMessage());
			env.printOut("[OPEN] 读取文件: %s 失败. 继续播放原音频", filename);
			return;
		}
		
		// 输出提示文字
		env.printOut("[OPEN] 尝试打开文件: %s", filename);
		
		if (beginSong <= 0) {
			// 不更换曲目号, 直接放入播放的 task
			env.putTask(PlayTask.getOne());
		} else {
			// 更换曲目号
			IFtmTask t = new ChooseSongTask(beginSong);
			t.setOption("needReset", true);
			env.putTask(t);
		}
	}

}
