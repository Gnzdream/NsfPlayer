package zdream.nsfplayer.sound.mixer;

import zdream.nsfplayer.sound.IResetable;

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

	void mix(int value, int time);

}
