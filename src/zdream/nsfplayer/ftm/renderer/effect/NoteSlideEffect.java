package zdream.nsfplayer.ftm.renderer.effect;

import static zdream.nsfplayer.ftm.document.IFtmChannelCode.CHANNEL_2A03_NOISE;

import java.util.HashSet;

import zdream.nsfplayer.ftm.renderer.AbstractFtmChannel;
import zdream.nsfplayer.ftm.renderer.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.renderer.IFtmState;

/**
 * <p>随时间向上或者向下滑到指定音符的效果, Qxy, Rxy
 * <p>一个轨道在同一时间, Qxy Rxy 3xx 的效果只允许存在一个, 后出现的优先.
 * 如果三个效果同帧出现, 3xx 先触发, 然后让 Qxy Rxy 效果进行改写.
 * <br>Qxy Rxy 效果一旦出现, 如果有 3xx 状态, 直接替换掉 3xx 状态.
 * </p>
 * 
 * @see NoteSlideState
 * 
 * @author Zdream
 * @since v0.2.2
 */
public class NoteSlideEffect implements IFtmEffect {
	
	/**
	 * 滑到的目的音符与现在音符的差值
	 */
	public final int delta;
	
	/**
	 * 滑动的速度
	 */
	public final int speed;

	private NoteSlideEffect(int slide, int speed) {
		this.delta = slide;
		this.speed = speed;
	}

	@Override
	public FtmEffectType type() {
		return FtmEffectType.SLIDE;
	}
	
	/**
	 * 形成一个随时间变化向上或者向下滑到指定音符的效果
	 * @param delta
	 *   变化量. 滑动效果停止时, 滑动的音符数量.
	 *   <br>正数, 说明音符向上滑动;
	 *   <br>负数, 说明音符向下滑动;
	 *   <br>0, 说明为上一个未完成的音符滑动效果修改滑动速度;
	 * @param speed
	 *   音符滑动的速度. 每帧音符变化的周期数. 该值必须为正数
	 * @return
	 *   效果实例
	 * @throws IllegalArgumentException
	 *   当滑动速度 <code>speed</code> 不在指定范围内时
	 */
	public static NoteSlideEffect of(int delta, int speed) throws IllegalArgumentException {
		if (speed <= 0) {
			throw new IllegalArgumentException("音符滑动的速度必须为正数");
		}
		return new NoteSlideEffect(delta, speed);
	}
	
	/**
	 * @return
	 * 是否是音符向上滑动的效果
	 */
	public boolean slideUp() {
		return delta > 0;
	}
	
	/**
	 * @return
	 * 是否是音符向下滑动的效果
	 */
	public boolean slideDown() {
		return delta < 0;
	}

	@Override
	public void execute(byte channelCode, FamiTrackerRuntime runtime) {
		AbstractFtmChannel ch = runtime.channels.get(channelCode);
		int snode = ch.getMasterNote();
		int dnode = snode + this.delta;
		
		// note 的范围是 [1, 96]
		if (channelCode == CHANNEL_2A03_NOISE) {
			if (dnode > 16) {
				dnode = 16;
			} else if (dnode < 1) {
				dnode = 1;
			}
		} else {
			if (dnode > 96) {
				dnode = 96;
			} else if (dnode < 1) {
				dnode = 1;
			}
		}
		
		int pitchDelta;
		
		if (snode == dnode) {
			// 没有变化
			pitchDelta = 0;
		} else {
			ch.setMasterNote(dnode);
			pitchDelta = (channelCode == CHANNEL_2A03_NOISE) ?
					snode - dnode : ch.periodTable(snode) - ch.periodTable(dnode);
		}
		
		// 如果有未完成的音符滑动效果, 这里修改它的速度
		HashSet<IFtmState> set = ch.filterStates(NoteSlideState.NAME);
		NoteSlideState s = null;
		
		if (!set.isEmpty()) {
			s = (NoteSlideState) set.iterator().next();
			if (s instanceof PortamentoOnState) {
				ch.removeState(s);
				s = new NoteSlideState(speed, pitchDelta);
				ch.addState(s);
			} else {
				s.delta += pitchDelta;
				s.speed = speed;
			}
		} else {
			s = new NoteSlideState(speed, pitchDelta);
			ch.addState(s);
		}
	}
	
	@Override
	public String toString() {
		return "NoteSlide:" + delta + "#" + speed;
	}
	
	/**
	 * 优先度低于修改音符的效果 {@link NoteEffect} 和 {@link NoiseEffect}
	 */
	public final int priority() {
		return -1;
	}

}
