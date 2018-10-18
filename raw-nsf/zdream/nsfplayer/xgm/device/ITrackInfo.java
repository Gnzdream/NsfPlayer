package zdream.nsfplayer.xgm.device;

public interface ITrackInfo extends Cloneable {
	
	/**
	 * @return
	 * 把现在的输出值
	 */
	public int getOutput();
	
	/**
	 * @return
	 * 以 Hz 为单位的频率数
	 */
	public double getFreqHz();
	
	/**
	 * @return
	 * 频率
	 * [周波数をデバイス依存値で返す]
	 */
	public int getFreq();
	
	/**
	 * @return
	 * 音量
	 */
	public int getVolume();
	
	/**
	 * @return
	 * 最大音量
	 */
	public int getMaxVolumn();
	
	/**
	 * @return
	 * 如果设备被关闭了, 返回 true
	 */
	public boolean getKeyStatus();
	
	/**
	 * @return
	 * 播放的曲目号
	 */
	public int getTone();
	
	/**
	 * @see Cloneable
	 * @return
	 */
	public ITrackInfo clone();
	
}
