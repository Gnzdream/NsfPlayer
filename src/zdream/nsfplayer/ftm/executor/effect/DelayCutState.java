package zdream.nsfplayer.ftm.executor.effect;

import java.util.Map;

import zdream.nsfplayer.ftm.executor.AbstractFtmChannel;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.IFtmState;

/**
 * <p>延迟静音状态
 * <p>如果某帧产生了静音效果 {@link NoteHaltEffect}, 该状态删除
 * </p>
 * 
 * <br>
 * <p><b>补充规则</b>
 * <p>如果该帧有 {@link NoteEffect} 或者 {@link NoiseEffect} 效果触发,
 * 而且现在不是该状态建立的第一帧, 删除该状态
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
	
	/**
	 * 记录现在是否是该状态建立的第一帧.
	 */
	private boolean startFrame = true;

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
		Map<FtmEffectType, IFtmEffect> map = runtime.effects.get(channelCode);
		if (map.get(FtmEffectType.HALT) != null) {
			ch.removeState(this);
			return;
		}
		if (!startFrame && map.get(FtmEffectType.NOTE) != null) {
			ch.removeState(this);
			return;
		}
		
		if (frames <= 0) {
			ch.doHalt();
			ch.removeState(this);
			return;
		}
		
		frames--;
		startFrame = false;
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
