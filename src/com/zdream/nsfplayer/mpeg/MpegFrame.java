package com.zdream.nsfplayer.mpeg;

/**
 * <p>Mpeg 格式的音频数据帧
 * 
 * @author Zdream
 * @since v0.1
 * @date 2018-01-16
 */
public class MpegFrame {

	MpegFrame(MpegAudio audio, int seq) {
		this.audio = audio;
		this.seq = seq;
	}
	
	/**
	 * 序号
	 */
	public final int seq;
	
	/**
	 * 属于哪个 audio 的
	 */
	public final MpegAudio audio;
	
	int offset;
	int length;

}
