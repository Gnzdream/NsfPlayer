package zdream.nsfplayer.ftm.cmd;

import zdream.nsfplayer.ftm.task.ChooseSongTask;
import zdream.nsfplayer.ftm.FtmPlayerConsole;

/**
 * <p>关于歌曲切换的处理器
 * 
 * <p>基本命令:
 * <li>song &lt;曲号,int&gt; 切换曲目
 * <li>next 上一首
 * <li>prev 下一首
 * </li>
 * </p>
 * 
 * @author Zdream
 * @date 2017-09-25
 */
public class SongHandler implements ICommandHandler {
	
	public static final String
			CMD_CHOOSE_SONG = "song",
			CMD_NEXT_SONG = "next",
			CMD_PREV_SONG = "prev";

	@Override
	public String[] canHandle() {
		return new String[] {CMD_CHOOSE_SONG, CMD_NEXT_SONG, CMD_PREV_SONG};
	}

	@Override
	public void handle(String[] args, FtmPlayerConsole env) {
		String cmd = args[0];
		if (CMD_CHOOSE_SONG.equals(cmd)) {
			handleChoose(args, env);
		} else if (CMD_NEXT_SONG.equals(cmd)) {
			handleNext(args, env);
		} else if (CMD_PREV_SONG.equals(cmd)) {
			handlePrev(args, env);
		}
	}

	private void handleChoose(String[] args, FtmPlayerConsole env) {
		if (args.length != 2) {
			env.printOut("[SONG] 使用 \"song <曲目号>\". 切换曲目。 [ 0 - %d ]\n[SONG] 现在播放曲目 %d。",
					env.getAudio().getTrackCount() - 1, env.getRenderer().getCurrentTrack());
			return;
		}
		
		chooseSong(Integer.parseInt(args[1]), env);
	}

	private void handleNext(String[] args, FtmPlayerConsole env) {
		chooseSong(env.getRenderer().getCurrentTrack() + 1, env);
	}

	private void handlePrev(String[] args, FtmPlayerConsole env) {
		int song = env.getRenderer().getCurrentTrack() - 1;
		if (song < 0) {
			song = env.getAudio().getTrackCount() - 1;
		}
		chooseSong(song, env);
	}
	
	private void chooseSong(int song, FtmPlayerConsole env) {
		ChooseSongTask t = new ChooseSongTask(song);
		
		t.setOption("needReset", true);
		env.putTask(t);
	}

}
