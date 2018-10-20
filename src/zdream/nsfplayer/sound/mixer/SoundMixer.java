package zdream.nsfplayer.sound.mixer;

import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.core.IResetable;
import zdream.nsfplayer.sound.AbstractNsfSound;

/**
 * 音频合成器
 * @author Zdream
 * @since v0.2.1
 */
public abstract class SoundMixer implements IResetable, INsfChannelCode {

	public SoundMixer() {
		
	}
	
	public void init() {
		// do nothing
	}

	@Override
	public void reset() {
		// do nothing
	}
	
	/* **********
	 * 音频管道 *
	 ********** */
	
	/**
	 * <p>调用该方法后, 所有与发声器 {@link AbstractNsfSound} 相连的音频管道全部拆开, 不再使用.
	 * </p>
	 */
	public abstract void detachAll();
	
	/**
	 * 分配轨道
	 * @param code
	 *   轨道号. 见静态成员 CHANNEL_*
	 * @return
	 *   轨道的实例
	 * @since v0.2.3
	 */
	public abstract IMixerChannel allocateChannel(byte code);
	
	/**
	 * 获得轨道实例. 如果没有调用 {@link #allocateChannel(byte)} 创建轨道, 则返回 null.
	 * @param code
	 *   轨道号. 见静态成员 CHANNEL_*
	 * @return
	 *   轨道的实例, 或者 null
	 * @since v0.2.3
	 */
	public abstract IMixerChannel getMixerChannel(byte code);
	
	/**
	 * 设置某个轨道的音量
	 * @param code
	 *   轨道号
	 * @param level
	 *   音量. 范围 [0, 1.0f]
	 * @since v0.2.3
	 */
	public void setLevel(byte code, float level) {
		IMixerChannel ch = getMixerChannel(code);
		if (ch != null) {
			ch.setLevel(level);
		}
	}
	
	/**
	 * 结束该帧.
	 * @return
	 *   返回有多少音频采样数
	 */
	public abstract int finishBuffer();
	
	/**
	 * 外界得到音频数据的接口. 音频数据将填充 buf 数组.
	 * @param buf
	 *   用于盛放音频数据的数组
	 * @param offset
	 * @param length
	 * @return
	 */
	public abstract int readBuffer(short[] buf, int offset, int length);

}
