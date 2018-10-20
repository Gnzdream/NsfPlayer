package zdream.nsfplayer.ftm.task;

import zdream.nsfplayer.ftm.FtmPlayerConsole;

/**
 * 主线程交给播放线程的任务
 * @author Zdream
 */
public interface IFtmTask {
	
	/**
	 * 设置参数
	 * @param key
	 *   键
	 * @param arg
	 *   值
	 */
	public void setOption(String key, Object arg);
	
	/**
	 * 执行任务
	 * @param env
	 *   环境
	 */
	public void execute(FtmPlayerConsole env);

}
