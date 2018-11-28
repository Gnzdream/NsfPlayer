package zdream.nsfplayer.ftm.executor.effect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import zdream.nsfplayer.ftm.executor.AbstractFtmChannel;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.IFtmSchedule;
import zdream.nsfplayer.ftm.executor.IFtmState;

/**
 * <p>延迟播放的效果, Gxx
 * </p>
 * 
 * @author Zdream
 * @since v0.2.1
 */
public class DelayEffect implements IFtmEffect {
	
	public final int duration;
	
	private HashSet<IFtmEffect> effects = new HashSet<>();

	private DelayEffect(int duration) {
		this.duration = duration;
		
		state = new DelayState(duration);
		schedule = new DelaySchedule();
		tracer = new DelayTraceSchedule();
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
				// 立即触发: 将效果放入环境的 effects 中
				((DelayState) s).triggerNow(channelCode, runtime);
			}
		}
		
		channel.addState(state);
	}
	
	@Override
	public String toString() {
		return "Delay:" + duration;
	}
	
	/**
	 * <p>内部类, 延迟计数器
	 * </p>
	 * 
	 * <p><b>补充效果</b>
	 * <li>(v0.2.3) 当状态发现某一帧有效果开始触发了, 则准备执行该延迟帧的内容; 执行的规则如下:
	 * <br>
	 * <br>1. 如果在状态创建的该帧, 不会执行该补充效果;
	 * <br>2. 需要抢在该帧的效果触发之前, 触发所有延迟的效果.
	 * </li>
	 * </p>
	 * 
	 * @author Zdream
	 * @since v0.2.1
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
			AbstractFtmChannel channel = runtime.channels.get(channelCode);
			
			if (delayCounter > 1) {
				delayCounter --;
				channel.addSchedule(tracer);
			} else {
				// delayCounter = 1
				channel.addSchedule(schedule);
				// 删除该状态
				channel.removeState(this);
			}
		}
		
		@Override
		public String toString() {
			return name() + delayCounter;
		}
		
		/**
		 * 现在立即将状态中的键触发掉, 并删除该状态
		 * @since v0.2.5
		 */
		public void triggerNow(byte channelCode, FamiTrackerRuntime runtime) {
			Map<FtmEffectType, IFtmEffect> map = runtime.effects.get(channelCode);
			
			ArrayList<IFtmEffect> list = new ArrayList<>(effects);
			list.sort(null);
			
			for (IFtmEffect eff : list) {
				if (!map.containsKey(eff.type()))
					map.put(eff.type(), eff);
			}
			
			AbstractFtmChannel channel = runtime.channels.get(channelCode);
			channel.removeState(this);
		}
		
		/**
		 * 最低优先级
		 */
		public final int priority() {
			return -99;
		}
	}
	
	class DelaySchedule implements IFtmSchedule {

		@Override
		public void trigger(byte channelCode, FamiTrackerRuntime runtime) {
			// 触发
			Map<FtmEffectType, IFtmEffect> map = runtime.effects.get(channelCode);
			
			ArrayList<IFtmEffect> list = new ArrayList<>(effects);
			list.sort(null);
			
			for (IFtmEffect eff : list) {
				if (!map.containsKey(eff.type()))
					map.put(eff.type(), eff);
			}
		}
		
	}
	
	/**
	 * 有延迟状态时, 如果看到该帧需要触发别的效果, 则提前将该延迟效果触发掉.
	 * 
	 * @author Zdream
	 * @since v0.2.3
	 */
	class DelayTraceSchedule implements IFtmSchedule {

		@Override
		public void trigger(byte channelCode, FamiTrackerRuntime runtime) {
			Map<FtmEffectType, IFtmEffect> map = runtime.effects.get(channelCode);
			if (!map.isEmpty()) {
				AbstractFtmChannel channel = runtime.channels.get(channelCode);
				
				// 准备触发
				channel.forceEffect(effects);
				
				// 删除该状态
				channel.removeState(state);
			}
		}
		
	}
	
	DelayState state;
	DelaySchedule schedule;
	DelayTraceSchedule tracer;
	
	/**
	 * 最高优先级
	 */
	public final int priority() {
		return 99;
	}

}
