package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.IFtmState;

/**
 * <p>音量累计状态
 * <p>当某个轨道有该状态时, 音量会随着时间变化. 变轻或变响由 delta 为负或者为正来决定.
 * 如果 delta = 0 意味着音量不再变化,
 * 但是如果积累量不为 0, 则说明整个轨道实际产生的音量与原来产生的音量仍会有不同,
 * 它们的差值就由 accum 数值来决定.
 * <p>每当轨道中触发了音量重置的效果 {@link VolumeEffect}, 累积量清零.
 * </p>
 * 
 * @version 0.2.2
 *   <br>原先该状态的定位只是随时间变化修改音量效果的状态, 仅仅完成 Axx 的效果.
 *   从该版本开始, 所有的音量累积效果、累积值储存均由它来完成.
 * 
 * @see VolumeSlideEffect
 * 
 * @author Zdream
 * @since 0.2.1
 */
public class VolumeAccumulateState implements IFtmState {
	
	public static final String NAME = "Volume Accumulated";
	
	/**
	 * 每帧的变化量.
	 * 除了 VRC7 轨道外, 变化量大于零的, 轨道音量会增大. VRC7 轨道相反
	 */
	public int delta;
	
	/**
	 * 累积量. 状态触发的第一帧影响的音量为 0, 第二帧影响一个 delta, 第三帧影响两个 delta,
	 * 以此类推
	 */
	private int accum = 0;

	public VolumeAccumulateState(int slide) {
		delta = slide;
	}

	@Override
	public String name() {
		return NAME;
	}

	@Override
	public void trigger(byte channelCode, FamiTrackerRuntime runtime) {
		if (delta == 0 && accum == 0) {
			runtime.channels.get(channelCode).removeState(this);
			return;
		}
		
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
