package com.zdream.nsfplayer.xgm.player;

/**
 * 具有多首歌曲的演出数据
 * @author Zdream
 */
public abstract class SoundDataMSP extends SoundData {

	public boolean enable_multi_tracks = false;

	public abstract int getSong();

	public abstract void setSong(int song);

	public abstract int getSongNum();
	
}
