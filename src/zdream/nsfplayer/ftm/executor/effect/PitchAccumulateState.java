package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.ftm.executor.AbstractFtmChannel;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.IFtmState;

/**
 * <p>音高累计状态
 * <p>当某个轨道有该状态时, 音高会随着时间变化. 变高或变低由 {@link #delta} 为负或者为正来决定.
 * 因为 delta 所指的是波长的变化量, 因此 delta 为正, 波长会不断增加, 音高会不断变低;
 * delta 为负, 波长会不断减小, 音高会不断变高.
 * <p>如果 delta = 0 意味着音高不再变化,
 * 但是如果积累量不为 0, 则说明整个轨道实际产生的音高与原来产生的音高仍会有不同,
 * 它们的差值就由 accum 数值来决定.
 * <p>每当轨道中触发了修改音调、音阶的效果 {@link NoteEffect}
 * (Noise 轨道是 {@link NoiseEffect}), 累积量清零.
 * </p>
 * 
 * <br>
 * <p><b>补充规则</b>
 * <p>如果 Qxy 和 Rxy 在该帧产生效果, delta 重置为 0
 * </p>
 * 
 * @see PortamentoEffect
 * 
 * @author Zdream
 * @since 0.2.2
 */
public class PitchAccumulateState implements IFtmState {
	
	public static final String NAME = "Pitch Accumulated";
	
	/**
	 * 每帧波长的变化量
	 */
	public int delta;
	
	/**
	 * 累积量. 状态触发的第一帧影响的音量为 0, 第二帧影响一个 delta, 第三帧影响两个 delta,
	 * 以此类推
	 */
	private int accum = 0;

	public PitchAccumulateState(int slide) {
		delta = slide;
	}

	@Override
	public String name() {
		return NAME;
	}

	@Override
	public void trigger(byte channelCode, FamiTrackerRuntime runtime) {
		AbstractFtmChannel ch = runtime.channels.get(channelCode);
		
		// 每当轨道中触发了修改音调、音阶的效果 NoteEffect / NoiseEffect, 累积量清零
		if (runtime.effects.get(channelCode).get(FtmEffectType.NOTE) != null) {
			resetAccumulation();
		}
		
		// 补充规则, 如果 Qxy 和 Rxy 在该帧产生效果, delta 重置为 0
		if (!ch.filterStates(NoteSlideState.NAME).isEmpty()) {
			delta = 0;
		}
		
		if (delta == 0 && accum == 0) {
			ch.removeState(this);
			return;
		}
		
		accum += delta;
		
		ch.addCurrentPeriod(accum);
	}
	
	/**
	 * 当外部的音高重新设置时, 调用该方法使累积量重置.
	 * 这里 {@link #trigger(byte, FamiTrackerRuntime)} 方法会调用
	 */
	public void resetAccumulation() {
		accum = 0;
	}
	
	@Override
	public String toString() {
		return NAME + ":" + delta;
	}
	
	@Override
	public int priority() {
		return -3;
	}

}
