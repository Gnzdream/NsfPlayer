package zdream.nsfplayer.ftm.executor.effect;

import java.util.HashSet;

import zdream.nsfplayer.ftm.executor.AbstractFtmChannel;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.IFtmState;

/**
 * 延迟静音效果, Sxx
 * 
 * @see DelayCutState
 * 
 * @author Zdream
 * @since 0.2.2
 */
public class CutEffect implements IFtmEffect {
	
	/**
	 * 在几帧之后触发静音效果
	 */
	public final int frames;

	private CutEffect(int frames) {
		this.frames = frames;
	}

	@Override
	public FtmEffectType type() {
		return FtmEffectType.NOTE_CUT;
	}
	
	/**
	 * 形成一个延迟静音的效果
	 * @param frames
	 *   几帧之后触发静音效果. 必须在 [0, 255] 范围内.
	 * @return
	 *   效果实例
	 * @throws IllegalArgumentException
	 *   当 <code>frames</code> 不在指定范围内时
	 */
	public static CutEffect of(int frames) throws IllegalArgumentException {
		if (frames > 255 || frames < 0) {
			throw new IllegalArgumentException("frames 必须是 0 - 255 之间的整数数值");
		}
		return new CutEffect(frames);
	}

	@Override
	public void execute(byte channelCode, FamiTrackerRuntime runtime) {
		AbstractFtmChannel ch = runtime.channels.get(channelCode);
		
		// 延迟静音效果在每个轨道只有一个
		HashSet<IFtmState> set = ch.filterStates(DelayCutState.NAME);
		DelayCutState s = null;
		
		if (!set.isEmpty()) {
			s = (DelayCutState) set.iterator().next();
			
			if (frames == 0) {
				ch.removeState(s);
				ch.doHalt();
			} else {
				s.frames = frames;
			}
		} else {
			if (frames == 0) {
				ch.doHalt();
			} else {
				s = new DelayCutState(frames);
				ch.addState(s);
			}
		}
	}
	
	@Override
	public String toString() {
		return "Cut:" + frames;
	}
	
	@Override
	public int priority() {
		return -4;
	}

}
