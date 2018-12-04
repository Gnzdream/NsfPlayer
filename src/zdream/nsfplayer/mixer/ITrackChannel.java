package zdream.nsfplayer.mixer;

/**
 * <p>混音器多声道单接收轨道接口
 * </p>
 * 
 * @author Zdream
 * @since v0.3.0
 */
public interface ITrackChannel extends IMixerChannel {
	
	/**
	 * 分声道设置音量. 最后呈现的音量为轨道总音量 * 声道音量
	 * @param level
	 *   声道音量值, [0, 1]
	 * @param track
	 *   声道号
	 */
	public void setTrackLevel(float level, int track);
	
	/**
	 * 获得声道音量值. 最后呈现的音量为轨道总音量 * 声道音量, 而不是单个声道音量.
	 * @param track
	 *   声道号
	 * @return
	 *   声道音量值, [0, 1]
	 */
	public float getTrackLevel(int track);

}
