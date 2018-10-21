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
	 * 设置音频轨道
	 * @param channelCode
	 *   轨道号
	 * @param ch
	 */
	public void setAudioChannel(byte channelCode, XgmAudioChannel ch);
	
	/**
	 * 获取音频轨道
	 * @param channelCode
	 *   轨道号
	 * @return
	 */
	public XgmAudioChannel getAudioChannel(byte channelCode);
	
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
