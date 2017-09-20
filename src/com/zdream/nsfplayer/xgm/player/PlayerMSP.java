package com.zdream.nsfplayer.xgm.player;

public abstract class PlayerMSP extends Player {

	/**
     * 演奏下一首歌
     * @param step 跳到后面几首歌
     * @return 成功时 true, 失败时 false
     */
	public boolean nextSong(int s) {
		return false;
	}

    /**
     * 演奏前面的歌曲
     * @param step 跳到前面几首歌
     * @return 成功时 true, 失败时 false
     */
	public boolean prevSong(int s) {
		return false;
	}

    /**
     * 设置演奏第几首歌
     * @param song 曲编号
     * @return 成功时 true, 失败时 false
     */
	public boolean setSong(int song) {
		return false;
	}

    /**
     * 得到现在演奏第几首歌
     * @return 曲编号
     */
	public int getSong() {
		return -1;
	}

}
