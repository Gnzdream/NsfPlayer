package zdream.nsfplayer.ftm.renderer.effect;

import zdream.nsfplayer.ftm.renderer.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.renderer.IFtmState;

/**
 * <p>随时间变化修改音量效果的状态, Axx
 * </p>
 * 
 * @see VolumeSlideEffect
 * 
 * @author Zdream
 * @since 0.2.1
 */
public class VolumeSlideState implements IFtmState {
	
	public static final String NAME = "Volume Slide";
	
	/**
	 * 每帧的变化量
	 */
	public int delta;
	
	/**
	 * 累积量. 状态触发的第一帧影响的音量为 0, 第二帧影响一个 delta, 第三帧影响两个 delta,
	 * 以此类推
	 */
	private int accum = 0;

	public VolumeSlideState(int slide) {
		delta = slide;
	}

	@Override
	public String name() {
		return NAME;
	}

	@Override
	public void trigger(byte channelCode, FamiTrackerRuntime runtime) {
		runtime.channels.get(channelCode).addCurrentVolume(accum);
		
		accum += delta;
	}
	
	/**
	 * 当外部的音量重新设置时, 调用该方法使累积量重置
	 */
	public void resetAccumulation() {
		accum = 0;
	}
	
	@Override
	public String toString() {
		return NAME + ":" + delta;
	}

}
