package zdream.nsfplayer.ftm.renderer.effect;

import java.util.HashSet;

import zdream.nsfplayer.ftm.renderer.AbstractFtmChannel;
import zdream.nsfplayer.ftm.renderer.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.renderer.IFtmState;

/**
 * <p>滑音效果, 1xx 上滑, 2xx 下滑
 * </p>
 * 
 * @see PitchAccumulateState
 * 
 * @author Zdream
 * @since 0.2.2
 */
public class PortamentoEffect implements IFtmEffect {
	
	public final int delta;

	private PortamentoEffect(int slide) {
		this.delta = slide;
	}

	@Override
	public FtmEffectType type() {
		return FtmEffectType.PORTA;
	}
	
	/**
	 * 形成一个随时间变化修改音高的滑音效果
	 * @param delta
	 *   变化量. 每帧变化音高的波长数 (一般是 1/60 s), 范围 [-256, 256]
	 *   <br>正数, 则随时间变化, 波长增大, 声音不断变低;
	 *   <br>负数, 则随时间变化, 波长增小, 声音不断变高;
	 *   <br>0, 则音高不随时间变化而变化, 也可以禁掉原来作用在轨道上的滑音效果
	 *   （无法禁用 3xx 的目的滑音效果）;
	 * @return
	 *   效果实例
	 * @throws IllegalArgumentException
	 *   当变化量 <code>delta</code> 不在指定范围内时
	 */
	public static PortamentoEffect of(int delta) throws IllegalArgumentException {
		if (delta < -256 || delta > 256) {
			throw new IllegalArgumentException("变化量必须在 -256 到 256 之间");
		}
		return new PortamentoEffect(delta);
	}
	
	/**
	 * @return
	 * 是否是波长增大, 声音不断变低的效果
	 */
	public boolean slideUp() {
		return delta > 0;
	}
	
	/**
	 * @return
	 * 是否是波长增小, 声音不断变高的效果
	 */
	public boolean slideDown() {
		return delta < 0;
	}
	
	/**
	 * @return
	 * 是否有滑音的效果
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
		HashSet<IFtmState> set = ch.filterStates(PitchAccumulateState.NAME);
		PitchAccumulateState s = null;
		
		if (!set.isEmpty()) {
			s = (PitchAccumulateState) set.iterator().next();
			s.delta = delta; // 但不重置累积量
		} else if (delta != 0) {
			s = new PitchAccumulateState(delta);
			ch.addState(s);
		}
	}
	
	@Override
	public String toString() {
		return "Portamento:" + delta;
	}
	
	/**
	 * 优先度低于 {@link PitchEffect} 和 {@link VibratoEffect}
	 */
	public final int priority() {
		return -3;
	}

}
