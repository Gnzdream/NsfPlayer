package zdream.nsfplayer.nsf.device;

import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.nsf.renderer.INsfRuntimeHolder;
import zdream.nsfplayer.nsf.renderer.NsfRuntime;
import zdream.nsfplayer.sound.AbstractNsfSound;

/**
 * <p>虚拟音频设备
 * 
 * @author Zdream
 * @since v0.2.4
 */
public abstract class AbstractSoundChip implements IDevice, INsfChannelCode, INsfRuntimeHolder {
	
	public AbstractSoundChip(NsfRuntime runtime) {
		this.runtime = runtime;
	}
	
	NsfRuntime runtime;
	
	@Override
	public NsfRuntime getRuntime() {
		return runtime;
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
	
	/**
	 * 在 sound 进行播放之前, 还会调用一个方法, 就是这个.
	 * 子类可以重写、覆盖
	 */
	public void beforeRender() {
		
	}
	
}
