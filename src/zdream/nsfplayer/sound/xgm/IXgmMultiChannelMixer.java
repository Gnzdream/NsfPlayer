package zdream.nsfplayer.sound.xgm;

import zdream.nsfplayer.core.INsfChannelCode;

/**
 * 合并轨道
 * 
 * @author Zdream
 * @since v0.2.3
 */
public interface IXgmMultiChannelMixer extends INsfChannelCode {
	
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
	 * 采样数据提交
	 * @param buf
	 * @param length
	 *   等于采样数
	 * @param clockPerFrame
	 *   一帧的时钟数
	 */
	public void render(short[] buf, int length, int clockPerFrame);

}
