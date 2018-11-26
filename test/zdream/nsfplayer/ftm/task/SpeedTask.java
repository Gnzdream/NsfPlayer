package zdream.nsfplayer.ftm.task;

import zdream.nsfplayer.ftm.FtmPlayerConsole;

/**
 * 修改播放速度任务
 * @author Zdream
 * @since v0.2.10-test
 */
public class SpeedTask implements IFtmTask {
	
	float speed;

	@Override
	public void setOption(String key, Object arg) {
		if ("speed".equals(key)) {
			speed = ((Number)arg).floatValue();
		}
	}

	@Override
	public void execute(FtmPlayerConsole env) {
		env.getRenderer().setSpeed(speed);
	}

}
