package zdream.nsfplayer.ftm.renderer.effect;

import java.util.HashSet;

import zdream.nsfplayer.ftm.executor.AbstractFtmChannel;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.renderer.IFtmState;

/**
 * <p>随时间变化修改音量的效果, Axx
 * </p>
 * 
 * @see VolumeAccumulateState
 * 
 * @author Zdream
 * @since 0.2.1
 */
public class VolumeSlideEffect implements IFtmEffect {
	
	public final int delta;

	protected VolumeSlideEffect(int slide) {
		this.delta = slide;
	}

	@Override
	public FtmEffectType type() {
		return FtmEffectType.VOLUME_SLIDE;
	}
	
	/**
	 * 形成一个随时间变化修改音量的效果
	 * @param delta
	 *   变化量. 每帧变化的音量数 (一般是 1/60 s), 范围 [-30, 30]
	 *   <br>正数, 则随时间变化音量不断增大;
	 *   <br>负数, 则随时间变化音量不断减小;
	 *   <br>0, 则音量不随时间变化而变化, 也可以禁掉原来作用在轨道上的随时间变化修改音量的效果;
	 * @return
	 *   效果实例
	 * @throws IllegalArgumentException
	 *   当变化量 <code>delta</code> 不在指定范围内时
	 */
	public static VolumeSlideEffect of(int delta) throws IllegalArgumentException {
		if (delta < -30 || delta > 30) {
			throw new IllegalArgumentException("音量变化量必须在 -30 到 30 之间");
		}
		return new VolumeSlideEffect(delta);
	}
	
	/**
	 * @return
	 * 是否是音量上升的效果
	 */
	public boolean slideUp() {
		return delta > 0;
	}
	
	/**
	 * @return
	 * 是否是音量下降的效果
	 */
	public boolean slideDown() {
		return delta < 0;
	}
	
	/**
	 * @return
	 * 是否是音量变化的效果
	 */
	public boolean slide() {
		return delta != 0;
	}
	
	@Override
	public void execute(byte channelCode, FamiTrackerRuntime runtime) {
		AbstractFtmChannel ch = runtime.channels.get(channelCode);
		
		/*
		 * 这里要保证一个轨道最多只有一个随时间变化修改音量的状态
		 */
		HashSet<IFtmState> set = ch.filterStates(VolumeAccumulateState.NAME);
		VolumeAccumulateState s = null;
		
		if (!set.isEmpty()) {
			s = (VolumeAccumulateState) set.iterator().next();
			s.delta = delta; // 但不重置累积量
		} else if (delta != 0) {
			s = new VolumeAccumulateState(delta);
			ch.addState(s);
		}
	}
	
	@Override
	public String toString() {
		return "VolumeSlide:" + delta;
	}
	
	/**
	 * 优先度低于修改音量的效果 {@link VolumeEffect}
	 */
	public final int priority() {
		return -1;
	}

}
