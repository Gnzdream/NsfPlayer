package zdream.nsfplayer.sound.mixer;

import zdream.nsfplayer.core.IResetable;

/**
 * <p>混音器单接收轨道接口
 * </p>
 * 
 * @author Zdream
 * @since v0.2
 */
public interface IMixerChannel extends IResetable {

	/**
	 * 修正总体音量值
	 * @param level
	 *   音量值, [0, 1]
	 */
	public void setLevel(float level);
	
	/**
	 * 获得总体音量值
	 * @return
	 *   音量值, [0, 1]
	 */
	public float getLevel();
	
	/**
	 * 设置是否打开 / 关闭轨道
	 * @param enable
	 *   false, 则关闭该轨道; true, 则打开轨道
	 * @since v0.2.6
	 */
	public void setEnable(boolean enable);
	
	/**
	 * 查看该轨道是否已经被打开 / 关闭了
	 * @return
	 *   false, 则轨道已关闭; true, 则轨道已打开
	 * @since v0.2.6
	 */
	public boolean isEnable();

	void mix(int value, int time);

}
