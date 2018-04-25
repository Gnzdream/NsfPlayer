package com.zdream.famitracker.sound;

/**
 * <p>当缓冲区满的时候, 让系统去播放这段数据
 * <p>Used to play the audio when the buffer is full
 * @author Zdream
 */
public interface IAudioCallback {
	
	/**
	 * 
	 * @param buffer
	 *   音频采样数组
	 * @param offset
	 * @param length
	 */
	void flushBuffer(byte[] buffer, int offset, int length);

}
