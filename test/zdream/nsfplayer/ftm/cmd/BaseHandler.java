package zdream.nsfplayer.ftm.cmd;

import zdream.nsfplayer.ftm.task.OpenTask;
import zdream.nsfplayer.ftm.task.PauseTask;
import zdream.nsfplayer.ftm.task.PlayTask;
import zdream.nsfplayer.ftm.FtmPlayerConsole;

/**
 * <p>基础命令处理器
 * 
 * <p>
 * <b>open</b> 命令:
 * <li><code>open [filePath]</code>
 * <br>打开某个文件.
 * <li><code>open [filePath] --beginSong [song]</code>
 * <li><code>open [filePath] -s [song]</code>
 * <br>打开某个文件, 并指定开始播放的曲目号
 * <li><code>open [filePath] --format txt</code>
 * <li><code>open [filePath] -fm txt</code>
 * <br>打开某个 txt 格式的文件
 * </li>
 * <br>注: 上面的选项可以联合使用
 * </p>
 * 
 * <p>
 * <b>pause</b> 命令:
 * <br>暂停
 * </p>
 * 
 * <p>
 * <b>play</b> 命令:
 * <br>暂停后播放
 * </p>
 * 
 * @version v0.2.5-test
 * <br>补充对打开 txt 格式的文件功能的支持
 * 
 * @author Zdream
 * @date 2017-09-23
 * @since v0.2
 */
public class BaseHandler implements ICommandHandler {
	
	public static final String
			CMD_OPEN = "open",
			CMD_PAUSE = "pause",
			CMD_PLAY = "play";

	@Override
	public String[] canHandle() {
		return new String[] {CMD_OPEN, CMD_PAUSE, CMD_PLAY};
	}

	@Override
	public void handle(String[] args, FtmPlayerConsole env) {
		String cmd = args[0];
		if (CMD_OPEN.equals(cmd)) {
			handleOpen(args, env);
		} else if (CMD_PAUSE.equals(cmd)) {
			handlePause(args, env);
		} else if (CMD_PLAY.equals(cmd)) {
			handlePlay(args, env);
		}
	}
	
	private void handleOpen(String[] args, FtmPlayerConsole env) {
		if (args.length < 2) {
			return;
		}
		
		OpenTask t = new OpenTask(args[1]);
		
		A: {
			if (args.length == 2) {
				break A;
			}
			
			for (int i = 2; i < args.length;) {
				if ("-s".equals(args[i]) || "--beginSong".equals(args[i])) {
					t.setOption("beginSong", Integer.valueOf(args[i + 1]));
					i += 2;
				} else if ("-fm".equals(args[i]) || "--format".equals(args[i])) {
					t.setOption("format", args[i + 1]);
					i += 2;
				} else {
					i++;
				}
			}
			
		}
		
		env.putTask(t);
		
	}
	
	private void handlePause(String[] args, FtmPlayerConsole env) {
		env.putTask(new PauseTask());
	}
	
	private void handlePlay(String[] args, FtmPlayerConsole env) {
		PlayTask t = PlayTask.getOne();
		t.setOption(PlayTask.OPT_REPLAY, true);
		env.putTask(t);
	}

}
