package com.zdream.nsfplayer.nsf.xgm;

import com.zdream.nsfplayer.nsf.audio.NsfAudio;
import com.zdream.nsfplayer.nsf.audio.NsfAudioException;

/**
 * <p>任务是将 NSF 格式的数据 {@link NsfAudio} 输出为 byte[]
 * @author Zdream
 * @version v0.1
 * @date 2018-01-16
 */
public class NsfProducer {
	
	/**
	 * 每秒采样数
	 */
	public final int sampleRate;
	
	/**
	 * 每个采样的数据大小, 按位计数
	 */
	public final int sampleSizeInBits;
	
	/**
	 * 轨道数, 1 是单声道, 2 是立体声, 等等
	 */
	public final int channels;
	
	public static final byte
		NTSC = 0,
		PAL = 1;
	
	/**
	 * 机器类型. 只能为 {@link #NTSC} 或 {@link #PAL}. 默认 {@link #NTSC}
	 */
	byte machine = NTSC;
	
	/**
	 * @param sampleRate
	 *   每秒采样数, 默认 48000
	 * @param sampleSizeInBits
	 *   每个采样的数据大小, 按位计数, 默认 16
	 * @param channels
	 *   轨道数, 1 是单声道, 2 是立体声, 等等, 默认 2
	 */
	public NsfProducer(int sampleRate, int sampleSizeInBits, int channels) {
		this.sampleRate = sampleRate;
		this.sampleSizeInBits = sampleSizeInBits;
		this.channels = channels;
	}
	
	/**
	 * @return
	 * 机器类型. {@link #NTSC} 或 {@link #PAL}
	 */
	public byte getMachine() {
		return machine;
	}
	
	/**
	 * @param machine
	 * 机器类型. 只能为 {@link #NTSC} 或 {@link #PAL}
	 */
	public void setMachine(byte machine) throws NsfAudioException {
		if (machine != NTSC || machine != PAL) {
			throw new NsfAudioException("machine: " + machine + " 数值非法");
		}
		this.machine = machine;
	}

}
