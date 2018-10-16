package zdream.nsfplayer.ftm.renderer.effect;

import zdream.nsfplayer.ftm.renderer.AbstractFtmChannel;
import zdream.nsfplayer.ftm.renderer.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.renderer.IFtmState;

/**
 * <p>延迟静音状态
 * <p>如果某帧产生了静音效果 {@link NoteHaltEffect}, 该状态删除
 * </p>
 * 
 * @see CutEffect
 * 
 * @author Zdream
 * @since 0.2.2
 */
public class DelayCutState implements IFtmState {
	
	public static final String NAME = "Delay Cut";
	
	/**
	 * 在几帧之后触发静音效果
	 */
	public int frames;

	public DelayCutState(int frames) {
		this.frames = frames;
	}

	@Override
	public String name() {
		return NAME;
	}

	@Override
	public void trigger(byte channelCode, FamiTrackerRuntime runtime) {
		AbstractFtmChannel ch = runtime.channels.get(channelCode);

		// 每当轨道中触发了静音效果 NoteHaltEffect, 该效果删除
		if (runtime.effects.get(channelCode).get(FtmEffectType.HALT) != null) {
			ch.removeState(this);
			return;
		}
		
		if (frames <= 0) {
			ch.doHalt();
			ch.removeState(this);
			return;
		}
		
		frames--;
	}
	
	@Override
	public String toString() {
		return NAME + ":" + frames;
	}
	
	@Override
	public int priority() {
		return -1;
	}

}
