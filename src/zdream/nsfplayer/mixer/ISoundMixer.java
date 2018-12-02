package zdream.nsfplayer.mixer;

import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.core.IResetable;
import zdream.nsfplayer.sound.AbstractNsfSound;

/**
 * 音频合成器
 * @author Zdream
 * @since v0.2.1
 */
public interface ISoundMixer extends IResetable, INsfChannelCode {

	/**
	 * 一般在 parameter 等重要数据设置完毕之后调用
	 */
	default void init() {
		// do nothing
	}

	/* **********
	 * 音频管道 *
	 ********** */
	
	/**
	 * <p>调用该方法后, 所有与发声器 {@link AbstractNsfSound} 相连的音频管道全部拆开, 不再使用.
	 * 因此, 所有在之前调用过的 {@link #allocateChannel(byte)} 方法的轨道号全部失效,
	 * 后面获取轨道需要重新调用 {@link #allocateChannel(byte)} 方法.
	 * </p>
	 */
	public void detachAll();
	
	/**
	 * <p>取消指定轨道, 让该轨道删除, 不再使用, 对应的轨道标识号也进行回收.
	 * </p>
	 * @param id
	 *   代表对应轨道的标识号
	 * @since v0.3.0
	 */
	public void detach(int id);
	
	/**
	 * <p>分配轨道, 返回同一时刻该轨道的唯一标识.
	 * <p>混音器负责分配、管理标识号, 用户用该标识号, 通过 {@link #getMixerChannel(int)}
	 * 能够拿到对应的轨道实例.
	 * <p>替换原来 v0.2.0 的 allocateChannel 方法.
	 * </p>
	 * @param code
	 *   轨道号, 或者轨道类型号. 见静态成员 CHANNEL_*
	 * @return
	 *   代表对应轨道的标识号
	 * @since v0.3.0
	 */
	public int allocateChannel(byte code);
	
	/**
	 * <p>获得轨道实例. 如果没有调用 {@link #allocateChannel(byte)} 创建轨道, 则返回 null.
	 * <p>替换原来 v0.2.0 的 getMixerChannel(byte) 方法.
	 * </p>
	 * @param id
	 *   轨道标识号
	 * @return
	 *   轨道的实例, 或者 null
	 * @since v0.2.3
	 */
	public IMixerChannel getMixerChannel(int id);
	
	/**
	 * <p>设置下一帧, 或者后面帧, 对应轨道的入采样数.
	 * <p>混音器需要做的是, 将对应入采样率的音频数据转化成出采样率的数据,
	 * 这个方法需要在往 {@link IMixerChannel} 轨道中写入数据前调用, 规定当前帧,
	 * {@link AbstractNsfSound} 会往该轨道输入采样数为多少的音频数据.
	 * 这个需求的出现是因为 NSF 的采样率往往达到 177 万多, 而普通的采样音频,
	 * 例如 MPEG 的采样率, 大都为 44.1 千 (44100 Hz). 需要统筹这两类音频的混音,
	 * 需要事先知道它们的入采样率, 或者相关的数据.
	 * <p>如果 <code>inSample</code> 设置为 0 或者负数, 或者没有调用该方法的,
	 * 默认采用全局的入采样数.
	 * </p>
	 * @param id
	 *   轨道标识号
	 * @param inSample
	 *   下一帧、以及后面所有帧, 每帧的入采样数.
	 *   <br>该值设置后渲染后面的帧时均不会发生变化, 直到下一次设置.
	 *   <br>如果设置为 0 或者负数, 表示清空上一次入采样数设置,
	 *   系统将采用全局的入采样数替换该轨道的入采样数.
	 * @since v0.3.0
	 */
	default public void setInSample(int id, int inSample) {}
	
	/**
	 * 每帧启用混音器前调用
	 */
	public void readyBuffer();
	
	/**
	 * 结束该帧. 在 {@link #readBuffer(short[], int, int)} 之前调用
	 * @return
	 *   返回有多少音频采样数 (按照单声道计)
	 */
	public int finishBuffer();
	
	/**
	 * 外界得到音频数据的接口. 音频数据将填充 buf 数组.
	 * @param buf
	 *   用于盛放音频数据的数组
	 * @param offset
	 * @param length
	 * @return
	 */
	public int readBuffer(short[] buf, int offset, int length);
	
	/* **********
	 * 用户操作 *
	 ********** */
	
	/**
	 * @return
	 *   混音器的操作类
	 * @since v0.2.10
	 */
	default IMixerHandler getHandler() {
		return null;
	}
	
	/**
	 * 设置某个轨道的音量
	 * @param id
	 *   轨道标识号
	 * @param level
	 *   音量. 范围 [0, 1.0f]
	 * @since v0.2.3
	 */
	default void setLevel(int id, float level) {
		IMixerChannel ch = getMixerChannel(id);
		if (ch != null) {
			ch.setLevel(level);
		}
	}
	
	/**
	 * 获得某个轨道的音量
	 * @param id
	 *   轨道标识号
	 * @return
	 *   音量. 范围 [0, 1.0f]
	 * @throws NullPointerException
	 *   当不存在 <code>code</code> 对应的轨道时
	 * @since v0.2.3
	 */
	default float getLevel(int id) throws NullPointerException {
		return getMixerChannel(id).getLevel();
	}

}
