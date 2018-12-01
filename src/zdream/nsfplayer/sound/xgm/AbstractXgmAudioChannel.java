package zdream.nsfplayer.sound.xgm;

import zdream.nsfplayer.mixer.IMixerChannel;

/**
 * 抽象的 Xgm 混音器轨道
 * 
 * @author Zdream
 * @since v0.2.10
 */
public abstract class AbstractXgmAudioChannel implements IMixerChannel {

	/**
	 * 音量. 虽然音量在这里存储,
	 * 但是实际上音量的作用位置不在这个类, 而是在 IXgmMultiChannelMixer 中
	 */
	protected float level = 1.0f;
	
	@Override
	public void setLevel(float level) {
		this.level = level;
	}

	@Override
	public float getLevel() {
		return level;
	}

	/* **********
	 * XGM混音器 *
	 ********** */
	
	/**
	 * 写操作完成之后, 读操作开始之前调用
	 */
	protected abstract void beforeSubmit();
	
	/**
	 * 每帧写操作之前调用, 让管道自己检查容量大小是否合适, 并进行修改
	 * @param size
	 *   容量大小. 一般单位为时钟
	 * @param sample
	 *   采样数
	 */
	protected abstract void checkCapacity(int size, int sample);
	
}
