package zdream.nsfplayer.mixer.xgm;

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
	 * <p>获取剩余对应类型的音频轨道实例.
	 * <p>如果该合并轨道支持该类型, 并且还有空余的、未使用的轨道, 返回该音频管道.
	 * </p>
	 * @param type
	 *   轨道类型.
	 * @return
	 *   音频轨道实例.
	 *   如果该合并轨道不支持该类型的轨道, 或者没有剩余的该类型的轨道, 返回 null.
	 */
	public AbstractXgmAudioChannel getRemainAudioChannel(byte type);
	
	/**
	 * 渲染前调用的方法
	 */
	public void beforeRender();
	
	/**
	 * 采样数据提交
	 * @param index
	 * @return
	 *   该采样的值
	 */
	public int render(int index);

}
