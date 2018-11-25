package zdream.nsfplayer.sound.xgm;

import java.util.Arrays;

import zdream.nsfplayer.sound.AbstractNsfSound;

public final class XgmAudioChannel extends AbstractXgmAudioChannel {
	
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
	
	public XgmAudioChannel() {
		
	}

	@Override
	public void reset() {
		if (buffer != null) {
			Arrays.fill(buffer, (short) 0);
		}
		lastValue = 0;
		lastTime = 0;
	}

	@Override
	public void mix(int value, int time) {
		if (value == lastValue) {
			return;
		}
		
		if (buffer == null) {
			this.lastValue = (short) value;
			return;
		}
		
		if (time > buffer.length) {
			time = buffer.length;
		}
		
		Arrays.fill(buffer, lastTime, time, lastValue);
		this.lastTime = time;
		this.lastValue = (short) value;
	}
	
	@Override
	protected void beforeSubmit() {
		Arrays.fill(buffer, lastTime, buffer.length, lastValue);
		this.lastTime = 0;
	}

	@Override
	protected void checkCapacity(int size) {
		if (this.buffer != null) {
			int length = this.buffer.length;
			if (length < size || length > size + 32) {
				this.buffer = new short[size + 16];
			}
		} else {
			this.buffer = new short[size + 16];
		}
	}
}
