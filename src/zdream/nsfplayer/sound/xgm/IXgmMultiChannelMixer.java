package zdream.nsfplayer.sound.xgm;

import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.core.IResetable;

/**
 * 合并轨道
 * 
 * @author Zdream
 * @since v0.2.3
 */
public interface IXgmMultiChannelMixer extends INsfChannelCode, IResetable {
	
	/**
	 * 获取音频轨道
	 * @param channelCode
	 *   轨道号
	 * @return
	 */
	public AbstractXgmAudioChannel getAudioChannel(byte channelCode);
	
	/**
	 * 通知所有音频轨道, 需要重设容量大小
	 * @param clock
	 *   需要的容量大小, 时钟数
	 * @param sample
	 *   采样数
	 * @since v0.2.9
	 */
	public void checkCapacity(int clock, int sample);
	
	/**
	 * 渲染前调用的方法
	 */
	public void beforeRender();
	
	/**
	 * 采样数据提交
	 * @param index
	 * @param fromIdx
	 *   在时钟数 - 音频值的数组对应中, 这一帧对应的时钟周期开始的索引（包含）
	 * @param toIdx
	 *   在时钟数 - 音频值的数组对应中, 这一帧对应的时钟周期结束的索引（不包含）
	 * @return
	 *   该采样的值
	 */
	public int render(int index, int fromIdx, int toIdx);

}
