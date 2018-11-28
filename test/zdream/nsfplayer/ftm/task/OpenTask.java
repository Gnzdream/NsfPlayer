package zdream.nsfplayer.ftm.task;

import static zdream.nsfplayer.ftm.task.OpenType.*;

import java.io.IOException;

import zdream.nsfplayer.ftm.FamiTrackerApplication;
import zdream.nsfplayer.ftm.FtmPlayerConsole;
import zdream.nsfplayer.ftm.audio.FtmAudio;
import zdream.nsfplayer.nsf.audio.NsfAudio;
import zdream.nsfplayer.nsf.audio.NsfAudioFactory;

/**
 * 打开文件任务
 * @author Zdream
 */
public class OpenTask implements IFtmTask {
	
	static NsfAudioFactory nsfFactory = new NsfAudioFactory();
	
	String filename;
	int beginSong = -1;
	
	/**
	 * 是否按照 txt 形式打开文件
	 * @since v0.2.5-test
	 */
	OpenType type = FTM;
	
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
		} else if ("beginSong".equals(key) || "song".equals(key)) {
			beginSong = (Integer) arg;
		} else if ("format".equals(key)) {
			switch (arg.toString().toLowerCase()) {
			case "txt":
				type = TXT;
				break;
			case "nsf":
				type = NSF;
				break;
			default:
				type = FTM;
				break;
			}
		}
	}

	@Override
	public void execute(FtmPlayerConsole env) {
		try {
			switch (type) {
			case FTM: {
				FtmAudio audio = FamiTrackerApplication.app.open(filename);
				env.setFtmAudio(audio);
				env.getFamiTrackerRenderer().ready(audio);
			} break;
			
			case TXT: {
				FtmAudio audio = FamiTrackerApplication.app.openWithTxt(filename);
				env.setFtmAudio(audio);
				env.getFamiTrackerRenderer().ready(audio);
			} break;
			
			case NSF: {
				NsfAudio audio = nsfFactory.createFromFile(filename);
				env.setNsfAudio(audio);
				env.getNsfRenderer().ready(audio);
			} break;

			default:
				break;
			}
			
		} catch (IOException | RuntimeException e) {
			e.printStackTrace();
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
