package zdream.nsfplayer.mixer;

/**
 * <p>多声道的音频合成器
 * </p>
 * 
 * @author Zdream
 * @since v0.3.0
 */
public interface ITrackMixer extends ISoundMixer {
	
	/**
	 * 获取声道数量
	 * @return
	 *   声道数量
	 */
	public int getTrackCount();
	
	/**
	 * 设置声道数量
	 * @param trackCount
	 *   声道数量. 该值必须大于 0. 1 表示单声道, 2 表示立体声, 依次类推
	 */
	public void setTrackCount(int trackCount);

}
