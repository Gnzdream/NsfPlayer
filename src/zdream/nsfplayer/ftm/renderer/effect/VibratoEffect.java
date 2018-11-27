package zdream.nsfplayer.ftm.renderer.effect;

import java.util.HashSet;

import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.renderer.AbstractFtmChannel;
import zdream.nsfplayer.ftm.renderer.IFtmState;

/**
 * <p>颤音效果, 4xy
 * </p>
 * 
 * @see VibratoState
 * 
 * @author Zdream
 * @since 0.2.2
 */
public class VibratoEffect implements IFtmEffect {
	
	/**
	 * 颤音正弦波的速度
	 */
	public final int speed;
	
	/**
	 * 振幅
	 */
	public final int depth;

	private VibratoEffect(int speed, int depth) {
		this.speed = speed;
		this.depth = depth;
	}

	@Override
	public FtmEffectType type() {
		return FtmEffectType.VIBRATO;
	}
	
	/**
	 * 形成一个颤音效果
	 * @param speed
	 *   颤音速度. 范围 [0, 15]
	 * @param depth
	 *   振幅. 范围 [0, 15]
	 * @return
	 *   效果实例
	 * @throws IllegalArgumentException
	 *   当变化量 <code>speed</code> 与 <code>depth</code> 不在指定范围内时
	 */
	public static VibratoEffect of(int speed, int depth) throws IllegalArgumentException {
		if (speed < -15 || speed > 15) {
			throw new IllegalArgumentException("颤音速度必须在 0 到 15 之间");
		}
		if (depth < -15 || depth > 15) {
			throw new IllegalArgumentException("振幅必须在 0 到 15 之间");
		}
		return new VibratoEffect(speed, depth);
	}
	
	/**
	 * 是否是关闭颤音的效果. 当 depth == 0 时, 颤音关闭
	 * @return
	 */
	public boolean isClose() {
		return depth == 0;
	}

	@Override
	public void execute(byte channelCode, FamiTrackerRuntime runtime) {
		AbstractFtmChannel ch = runtime.channels.get(channelCode);
		
		if (isClose()) {
			ch.removeStates(VibratoState.NAME);
			return;
		}
		
		/*
		 * 这里要保证一个轨道最多只有一个颤音状态
		 */
		HashSet<IFtmState> set = ch.filterStates(VibratoState.NAME);
		VibratoState s = null;
		
		if (!set.isEmpty()) {
			s = (VibratoState) set.iterator().next();
			s.speed = speed;
			s.depth = depth;
		} else {
			s = new VibratoState(speed, depth);
			ch.addState(s);
		}
	}
	
	@Override
	public String toString() {
		return "Vibrato:" + depth + "#" + speed;
	}
	
	public final int priority() {
		return -2;
	}

}
