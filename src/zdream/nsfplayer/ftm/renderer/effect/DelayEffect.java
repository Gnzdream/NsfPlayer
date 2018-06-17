package zdream.nsfplayer.ftm.renderer.effect;

import java.util.Collection;
import java.util.HashSet;

import zdream.nsfplayer.ftm.renderer.FamiTrackerRuntime;

/**
 * <p>延迟播放的效果, Gxx
 * </p>
 * 
 * @author Zdream
 * @since 0.2.1
 */
public class DelayEffect implements IFtmEffect {
	
	public final int duration;
	
	private HashSet<IFtmEffect> effects = new HashSet<>();

	private DelayEffect(int duration) {
		this.duration = duration;
	}

	@Override
	public FtmEffectType type() {
		return FtmEffectType.DELAY;
	}
	
	/**
	 * 形成一个延迟播放的效果
	 * @param duration
	 *   延迟的帧数. 仅允许正数.
	 * @param effects
	 *   延迟触发后, 所有的效果的集合. 不能为 null
	 * @return
	 *   效果实例
	 * @throws IllegalArgumentException
	 *   当延迟的帧数 <code>duration</code> 不在指定范围内时
	 * @throws NullPointerException
	 *   当 effects = null 时
	 */
	public static DelayEffect of(int duration, Collection<IFtmEffect> effects)
			throws IllegalArgumentException, NullPointerException {
		if (duration < 0) {
			throw new IllegalArgumentException("延迟的帧数必须是正数数值");
		}
		if (effects == null) {
			throw new NullPointerException("效果集合 effects = null");
		}
		DelayEffect d = new DelayEffect(duration);
		d.effects.addAll(effects);
		return d;
	}
	
	@Override
	public void execute(byte channelCode, FamiTrackerRuntime rumtime) {
		// TODO Auto-generated method stub
		IFtmEffect.super.execute(channelCode, rumtime);
	}
	
	@Override
	public String toString() {
		return "Delay:" + duration;
	}

}
