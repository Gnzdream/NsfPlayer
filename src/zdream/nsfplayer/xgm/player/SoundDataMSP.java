package zdream.nsfplayer.xgm.player;

/**
 * 具有多首歌曲的演出数据
 * @author Zdream
 */
public abstract class SoundDataMSP extends SoundData {

	public boolean enable_multi_tracks = false;

	/**
	 * @return
	 *   正在播放的曲号
	 */
	public abstract int getSong();

	public abstract void setSong(int song);

	/**
	 * @return
	 *   总曲数
	 */
	public abstract int getSongNum();
	
}
