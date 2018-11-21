package zdream.nsfplayer.ftm.cmd;

import zdream.nsfplayer.ftm.FtmPlayerConsole;

/**
 * <p>关于播放的处理器
 * 
 * <p>
 * speed 命令:
 * <li><code>speed [s]</code>
 * <br>更改速度. s 浮点数, 范围 [0.1, 10], 默认 1
 * <li><code>speed --reset</code>
 * <br>重置播放速度至 1
 * <li><code>speed</code>
 * <br>查看当前播放速度
 * </li>
 * <p>
 * 
 * @author Zdream
 * @since v0.2.9-test
 */
public class PlayHandler implements ICommandHandler {
	
	public static final String
			CMD_SPEED = "speed";
	
	public PlayHandler() {
		
	}

	@Override
	public String[] canHandle() {
		return new String[] {CMD_SPEED};
	}

	@Override
	public void handle(String[] args, FtmPlayerConsole env) {
		String cmd = args[0];
		if (CMD_SPEED.equals(cmd)) {
			handleSpeed(args, env);
		}
	}

	private void handleSpeed(String[] args, FtmPlayerConsole env) {
		if (args.length == 1) {
			// 查看当前播放速度
			env.printOut("[SPEED] 当前播放速度为 %f", env.getRenderer().getSpeed());
			env.printOut("[SPEED] 使用 speed [s] 设置播放速度");
		} else {
			if ("--reset".equals(args[1])) {
				// 重置播放速度
				env.getRenderer().resetSpeed();
			} else {
				// 设置播放速度
				float speed = Float.parseFloat(args[1]);
				env.getRenderer().setSpeed(speed);
			}
		}
	}

}
