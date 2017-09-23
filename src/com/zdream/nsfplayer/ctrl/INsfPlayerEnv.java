package com.zdream.nsfplayer.ctrl;

import com.zdream.nsfplayer.ctrl.task.ITask;
import com.zdream.nsfplayer.xgm.player.nsf.NsfAudio;
import com.zdream.nsfplayer.xgm.player.nsf.NsfPlayer;
import com.zdream.nsfplayer.xgm.player.nsf.NsfPlayerConfig;

public interface INsfPlayerEnv {
	
	public NsfPlayer getPlayer();
	
	public NsfAudio getNsf();
	
	public NsfPlayerConfig getConfig();
	
	/**
	 * 获得采样数组
	 * @return
	 */
	public byte[] getLastSampleBytes();
	
	public void putTask(ITask task);
	
	/**
	 * <p>查看下一个 task</p>
	 * 这个方法不会删除队列里面的 task. 如果没有, 返回 null
	 */
	public ITask nextTask();
	
	/**
	 * 写入采样数据, 并播放.<br>
	 * 数组在方法 <code>getLastSampleBytes()</code> 中取得.
	 * @param off
	 * @param len
	 * @return
	 */
	public int writeSamples(int off, int len);
	
	/**
	 * 文本输出
	 */
	public void printOut(String text, Object...args);

}
