package zdream.nsfplayer.ftm.renderer.effect;

import java.util.HashSet;

import zdream.nsfplayer.ftm.renderer.AbstractFtmChannel;
import zdream.nsfplayer.ftm.renderer.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.renderer.IFtmState;

/**
 * <p>持续滑音效果, 3xx
 * </p>
 * 
 * @see PitchAccumulateState TODO
 * 
 * @author Zdream
 * @since 0.2.2
 */
public class PortamentoOnEffect implements IFtmEffect {
	
	/**
	 * 滑动的速度
	 */
	public final int speed;

	private PortamentoOnEffect(int speed) {
		this.speed = speed;
	}

	@Override
	public FtmEffectType type() {
		return FtmEffectType.PORTAMENTO;
	}
	
	/**
	 * 形成持续滑音的效果
	 * @param speed
	 *   音符滑动的速度. 每帧音符变化的周期数. 该值必须为非负数
	 * @return
	 *   效果实例
	 * @throws IllegalArgumentException
	 *   当滑动速度 <code>speed</code> 不在指定范围内时
	 */
	public static PortamentoOnEffect of(int speed) throws IllegalArgumentException {
		if (speed < 0) {
			throw new IllegalArgumentException("音符滑动的速度必须为正数");
		}
		return new PortamentoOnEffect(speed);
	}

	@Override
	public void execute(byte channelCode, FamiTrackerRuntime runtime) {
		AbstractFtmChannel ch = runtime.channels.get(channelCode);
		
		/*
		 * 这里要保证一个轨道最多只有一个 NoteSlideState 的实例
		 */
		HashSet<IFtmState> set = ch.filterStates(PortamentoOnState.NAME);
		NoteSlideState s = null;
		int baseNote = ch.getMasterNote();
		
		if (!set.isEmpty()) {
			s = (NoteSlideState) set.iterator().next();
			
			if (s instanceof PortamentoOnState) {
				// 直接修改 speed 即可, 无论 speed 是否等于 0
				s.speed = this.speed;
			} else {
				int delta = s.delta;
				ch.removeState(s);
				
				s = new PortamentoOnState(speed, delta, baseNote);
				ch.addState(s);
			}
		} else {
			if (speed != 0) {
				s = new PortamentoOnState(speed, baseNote);
				ch.addState(s);
			}
		}
	}
	
	/**
	 * 比 {@link NoteEffect} 和 {@link NoiseEffect} 要更早执行
	 */
	public int priority() {
		return 5;
	}

}
