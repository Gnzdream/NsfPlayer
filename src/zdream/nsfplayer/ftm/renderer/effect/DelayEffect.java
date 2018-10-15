package zdream.nsfplayer.ftm.renderer.effect;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import zdream.nsfplayer.ftm.renderer.AbstractFtmChannel;
import zdream.nsfplayer.ftm.renderer.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.renderer.IFtmState;

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
		
		state = new DelayState(duration);
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
	public void execute(byte channelCode, FamiTrackerRuntime runtime) {
		AbstractFtmChannel channel = runtime.channels.get(channelCode);
		
		// 如果该当前轨道有延迟状态, 则立即触发
		HashSet<IFtmState> set = channel.filterStates(state.name());
		if (!set.isEmpty()) {
			for (IFtmState s : set) {
				channel.removeState(s);
			}
		}
		
		channel.addState(state);
	}
	
	@Override
	public String toString() {
		return "Delay:" + duration;
	}
	
	/**
	 * 内部类, 延迟计数器
	 * 
	 * @author Zdream
	 * @since 0.2.1
	 */
	class DelayState implements IFtmState {
		
		/**
		 * 每一帧扣 1, 扣到 0 时延迟的键播放.
		 */
		private int delayCounter;
		
		public DelayState(int duration) {
			this.delayCounter = duration;
		}

		@Override
		public String name() {
			return "DELAY";
		}

		@Override
		public void trigger(byte channelCode, FamiTrackerRuntime runtime) {
			if (delayCounter > 0) {
				delayCounter --;
			} else {
				// 触发
				Map<FtmEffectType, IFtmEffect> map = runtime.effects.get(channelCode);
				for (IFtmEffect eff : effects) {
					map.put(eff.type(), eff);
				}
				
				AbstractFtmChannel channel = runtime.channels.get(channelCode);
				channel.forceEffect(effects);
				channel.removeState(this);
			}
		}
		
		@Override
		public String toString() {
			return name() + delayCounter;
		}
		
		/**
		 * 最高优先级
		 */
		public final int priority() {
			return 99;
		}
	}
	
	DelayState state;
	
	/**
	 * 最高优先级
	 */
	public final int priority() {
		return 99;
	}

}
