package com.zdream.nsfplayer.xgm.player.nsf;

/**
 * <p>该类用于存放 {@link NsfPlayer} 的播放状态数据以及相关参数.</p>
 * 
 * <p>原本这其中的数据都是在 {@link NsfAudio} 中存放的, 现在拆分成两个类.
 * NsfPlayerStatus 就是其中之一.</p>
 * 
 * <p>这里面使用的时间单位没有说明都是一个采样段.
 * 如果使用 48000 Hz 的采样率进行播放, 那么一个单位为 1 / 48000 秒.</p>
 * 
 * @author Zdream
 * @date 2017-09-21
 */
public class NsfPlayerStatus {
	
	/**
	 * 现在正在选择的歌曲号，从 0 开始
	 */
	public int song;
	
	/**
	 * 采样率
	 */
	public double rate;
	
	/**
	 * 已经演唱的时间 | 秒<br>
	 * 该数据会在将来删除.
	 * @see NsfPlayerStatus#time
	 */
	public int time_in_ms;
	
	/**
	 * 已经演唱的采样数
	 */
	public int time;
	
	
	/** 默认的播放时间 */
	public int default_playtime;
	/** 循环时间 */
	public int loop_in_ms;
	/** 渐出时间 */
	public int fade_in_ms, default_fadetime;
	/** 循环次数 */
	public int loop_num, default_loopnum;
	/** 演奏时间不明的时候启用（默认的演奏时间） */
	public boolean playtime_unknown;
	
	NsfPlayer player;
	
	public NsfPlayerStatus(NsfPlayer player) {
		this.player = player;
	}

}
