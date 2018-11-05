package zdream.nsfplayer.sound.xgm;

import java.util.Arrays;

import zdream.nsfplayer.sound.AbstractNsfSound;
import zdream.nsfplayer.sound.mixer.IMixerChannel;

public class XgmAudioChannel implements IMixerChannel {
	
	/**
	 * 仅存储从 {@link AbstractNsfSound} 通过方法 {@link #mix(int, int)} 输出的音频数据.
	 * 在 buffer 中存储每一个时钟周期的音频值, 数据格式为 [clock] = value.
	 * 
	 * 原设定是 buffer 长度为每一帧的时钟周期数 + 16.
	 * 因为每一帧的时钟周期数会上下浮动, 所以要加 16 这个参数.
	 */
	short[] buffer;
	
	/**
	 * 上一个存储的数值
	 */
	short lastValue;
	
	/**
	 * 上一个 mix 的 time 值.
	 * 后面要 mix 的位置是 [lastTime, time]
	 */
	int lastTime;
	
	/**
	 * 音量. 虽然音量在这里存储,
	 * 但是实际上音量的作用位置不在这个类, 而是在 IXgmMultiChannelMixer 中
	 */
	float level = 1.0f;
	
	/**
	 * 是否被打开的标志
	 */
	boolean enable = true;

	public XgmAudioChannel() {
		
	}

	@Override
	public void reset() {
		Arrays.fill(buffer, (short) 0);
		lastValue = 0;
		lastTime = 0;
	}

	@Override
	public void setLevel(float level) {
		this.level = level;
	}

	@Override
	public float getLevel() {
		return level;
	}

	@Override
	public void mix(int value, int time) {
		if (time > buffer.length) {
			time = buffer.length;
		}
		
		Arrays.fill(buffer, lastTime, time, lastValue);
		this.lastTime = time;
		this.lastValue = (short) value;
	}
	
	void beforeSubmit() {
		Arrays.fill(buffer, lastTime, buffer.length, lastValue);
		this.lastTime = 0;
	}

	@Override
	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	@Override
	public boolean isEnable() {
		return enable;
	}
}
