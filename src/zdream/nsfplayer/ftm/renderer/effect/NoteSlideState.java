package zdream.nsfplayer.ftm.renderer.effect;

import zdream.nsfplayer.ftm.renderer.AbstractFtmChannel;
import zdream.nsfplayer.ftm.renderer.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.renderer.IFtmState;

/**
 * <p>随时间向上或者向下滑到指定音符效果的状态, Qxy, Rxy
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
		
		AbstractFtmChannel ch = runtime.channels.get(channelCode);
		
		if (delta == 0) {
			// 删除该状态
			ch.removeState(this);
			return;
		}
		
		ch.addCurrentPeriod(delta);
	}

}
