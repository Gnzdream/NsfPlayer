package zdream.nsfplayer.ftm.task;

import zdream.nsfplayer.core.AbstractNsfAudio;
import zdream.nsfplayer.core.AbstractNsfRenderer;
import zdream.nsfplayer.ftm.FtmPlayerConsole;

/**
 * 发起切歌动作的任务
 * @author Zdream
 * @date 2017-09-22
 */
public class ChooseSongTask implements IFtmTask {
	
	int song;
	
	/**
	 * 是否需要在切歌时重置播放器参数.<br>
	 * 现在一般而言如果刚刚打开某个音乐文件, 还未播放 (刚刚完成了 OpenTask) 时, 这个值为默认值 false;
	 * 如果已经播放过, 这时就需要 reset. 这时请设置该参数为 true.
	 */
	boolean needReset;
	
	public ChooseSongTask() { }
	
	public ChooseSongTask(int song) {
		super();
		this.song = song;
	}

	@Override
	public void setOption(String key, Object arg) {
		if ("song".equals(key)) {
			song = (Integer) arg;
		} else if ("needReset".equals(key)) {
			needReset = (Boolean) arg;
		}
	}

	@Override
	public void execute(FtmPlayerConsole env) {
		AbstractNsfRenderer<?> renderer = env.getRenderer();
		AbstractNsfAudio audio = env.getAudio();
		
		if (song < 0) {
			song = 0;
		} else if (song >= audio.getTrackCount()) {
			song = song % audio.getTrackCount();
		}
		
		renderer.ready(song);
		
		// 输出提示文字
		env.printOut("[SONG] 切换到歌曲: %s", song);
		
		// 放入播放的 task
		env.putTask(PlayTask.getOne());
	}

}
