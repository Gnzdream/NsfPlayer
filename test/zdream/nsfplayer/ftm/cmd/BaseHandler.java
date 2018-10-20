package zdream.nsfplayer.ftm.cmd;

import zdream.nsfplayer.ftm.task.OpenTask;
import zdream.nsfplayer.ftm.task.PauseTask;
import zdream.nsfplayer.ftm.task.PlayTask;
import zdream.nsfplayer.ftm.FtmPlayerConsole;

/**
 * 基础命令处理器
 * @author Zdream
 * @date 2017-09-23
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
			
			if ("-s".equals(args[2]) || "--beginSong".equals(args[2])) {
				t.setOption("beginSong", Integer.valueOf(args[3]));
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
