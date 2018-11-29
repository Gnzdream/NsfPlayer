package zdream.nsfplayer.nsf.device;

import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.nsf.renderer.INsfRuntimeHolder;
import zdream.nsfplayer.nsf.renderer.NsfRuntime;
import zdream.nsfplayer.sound.AbstractNsfSound;

/**
 * <p>虚拟音频设备 (相当于虚拟声卡)
 * 
 * @author Zdream
 * @since v0.2.4
 */
public abstract class AbstractSoundChip implements IDevice, INsfChannelCode, INsfRuntimeHolder {
	
	public AbstractSoundChip(NsfRuntime runtime) {
		this.runtime = runtime;
	}
	
	NsfRuntime runtime;
	
	/**
	 * 是否开始渲染的标识.
	 * 如果为 true, 说明准备开始进行渲染工作
	 */
	boolean startRender;
	
	@Override
	public NsfRuntime getRuntime() {
		return runtime;
	}
	
	/**
	 * <p>整个渲染的工作分为初始化、执行和渲染三个部分,
	 * 通过该值能够确定是否已经进入了渲染阶段.
	 * </p>
	 * @return
	 *   是否开始渲染的标识.
	 * @see #startRender
	 * @since v0.2.9
	 */
	public boolean isStartRender() {
		return startRender;
	}

	/**
	 * 获得指定轨道的音频发声器
	 * @param channelCode
	 *   轨道号
	 * @return
	 */
	public abstract AbstractNsfSound getSound(byte channelCode);
	
	/**
	 * 获得支持的所有轨道的轨道号列表
	 */
	public abstract byte[] getAllChannelCodes();
	
}
