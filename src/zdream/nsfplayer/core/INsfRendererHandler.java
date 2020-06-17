package zdream.nsfplayer.core;

import java.util.Set;

/**
 * 抽象的 NSF 音源的渲染器, 用于输出以 byte / short 数组组织的 PCM 音频数据
 * 
 * @author Zdream
 * @since v0.2.4
 */
public interface INsfRendererHandler <T extends AbstractNsfAudio>
		extends INsfChannelCode {
	
	/**
	 * <p>设置播放暂停位置为指定曲目的开头.
	 * </p>
	 * @param track
	 *   曲目号, 从 0 开始
	 */
	public abstract void ready(int track);
	
	/**
	 * <p>让该渲染器读取对应的 audio 数据.
	 * <p>设置播放暂停位置为指定曲目的第一段 (段 0)
	 * </p>
	 * @param audio
	 * @param track
	 *   曲目号, 从 0 开始
	 */
	public abstract void ready(T audio, int track);
	
	/* **********
	 * 仪表盘区 *
	 ********** */
	
	/**
	 * @return
	 *   当前正在播放的曲目号
	 * @since v0.2.8
	 */
	public abstract int getCurrentTrack();
	
	/**
	 * 返回所有的轨道号的集合. 轨道号的参数在 {@link INsfChannelCode} 里面写出
	 * @return
	 *   所有的轨道号的集合. 如果没有调用 ready(...) 方法时, 返回空集合.
	 * @since v0.2.8
	 */
	public abstract Set<Byte> allChannelSet();
	
	/**
	 * 设置某个轨道的音量
	 * @param channelCode
	 *   轨道号
	 * @param level
	 *   音量. 范围 [0, 1]
	 * @since v0.2.8
	 */
	public abstract void setLevel(byte channelCode, float level);
	
	/**
	 * 获得某个轨道的音量
	 * @param channelCode
	 *   轨道号
	 * @return
	 *   音量. 范围 [0, 1]
	 * @throws NullPointerException
	 *   当不存在 <code>channelCode</code> 对应的轨道时
	 * @since v0.2.8
	 */
	public abstract float getLevel(byte channelCode) throws NullPointerException;
	
	/**
	 * 设置轨道是否发出声音
	 * @param channelCode
	 *   轨道号
	 * @param mask
	 *   false, 使该轨道发声; true, 则静音
	 * @since v0.2.8
	 */
	public abstract void setChannelMuted(byte channelCode, boolean mask);
	
	/**
	 * 查看轨道是否能发出声音
	 * @param channelCode
	 *   轨道号
	 * @return
	 *   false, 说明该轨道没有被屏蔽; true, 则已经被屏蔽
	 * @throws NullPointerException
	 *   当不存在 <code>channelCode</code> 对应的轨道时
	 * @since v0.2.8
	 */
	public abstract boolean isChannelMuted(byte channelCode) throws NullPointerException;

}
