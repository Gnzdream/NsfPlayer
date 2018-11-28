package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.ftm.executor.AbstractFtmChannel;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.IFtmState;

/**
 * <p>随时间向上或者向下滑到指定音符效果的状态, Qxy, Rxy
 * </p>
 * 
 * <br>
 * <p><b>补充规则</b>
 * <p>如果该帧有 {@link NoteEffect} 或者 {@link NoiseEffect} 效果触发,
 * 而且现在不是该状态建立的第一帧, 删除该状态
 * </p>
 * 
 * @see NoteSlideEffect
 * 
 * @author Zdream
 * @since 0.2.2
 */
public class NoteSlideState implements IFtmState {
	
	public static final String NAME = "Note Slide";
	
	/**
	 * 每帧的音符上滑或下滑的波长值.
	 * 该值必须为正数
	 */
	public int speed;
	
	/**
	 * <p>离目标波长值的差值.
	 * 状态未触发时设定最终上滑或下滑的目标, 每帧向目标滑动 speed 单位的波长值
	 * 在某帧, delta 的值说明离目标相差的波长值. 可正可负.
	 * 
	 * <p>注意, 这个是波长值,
	 * 因此如果 delta > 0, 说明后面音符向上滑动;
	 * 如果 delta < 0, 说明后面音符向下滑动;
	 */
	public int delta;
	
	/**
	 * 记录现在是否是该状态建立的第一帧.
	 */
	private boolean startFrame = true;

	public NoteSlideState(int speed, int delta) {
		super();
		this.speed = speed;
		this.delta = delta;
	}

	@Override
	public String name() {
		return NAME;
	}

	@Override
	public void trigger(byte channelCode, FamiTrackerRuntime runtime) {
		AbstractFtmChannel ch = runtime.channels.get(channelCode);
		
		// 如果该帧的主音键发生变化, 且不是首帧
		if (!startFrame && ch.isNoteUpdated()) {
			// 删除该状态
			ch.removeState(this);
			return;
		}
		
		if (delta > 0) {
			delta -= speed;
			if (delta < 0) {
				delta = 0;
			}
		} else {
			delta += speed;
			if (delta > 0) {
				delta = 0;
			}
		}
		
		if (delta == 0) {
			// 删除该状态
			ch.removeState(this);
			return;
		}
		
		ch.addCurrentPeriod(delta);
		startFrame = false;
	}
	
	@Override
	public String toString() {
		return NAME + ":" + speed;
	}

}
