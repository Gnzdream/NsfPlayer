package zdream.nsfplayer.ftm.task;

import zdream.nsfplayer.ftm.FtmPlayerConsole;

/**
 * 暂停播放任务
 * @author Zdream
 * @date 2017-09-23
 */
public class PauseTask implements IFtmTask {

	@Override
	public void setOption(String key, Object arg) {
		
	}

	@Override
	public void execute(FtmPlayerConsole env) {
		env.clearTask(); // 只要把 PlayTask 清空即可
	}

}
