package zdream.nsfplayer.ftm.executor.effect;

import java.util.HashSet;

import zdream.nsfplayer.ftm.executor.AbstractFtmChannel;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.IFtmState;

/**
 * 琶音效果, 0xy
 * 
 * @see ArpeggioState
 * 
 * @author Zdream
 * @since 0.2.2
 */
public class ArpeggioEffect implements IFtmEffect {
	
	/**
	 * 两个琶音在原音高上增加几个半音
	 */
	public final int x, y;

	private ArpeggioEffect(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public FtmEffectType type() {
		return FtmEffectType.ARPEGGIO;
	}
	
	/**
	 * 形成一个琶音的效果
	 * @param x
	 *   范围 [0, 15]
	 * @param y
	 *   范围 [0, 15]
	 * @return
	 *   效果实例
	 * @throws IllegalArgumentException
	 *   当音色值 <code>x</code> 或 <code>y</code> 不在指定范围内时
	 */
	public static ArpeggioEffect of(int x, int y) throws IllegalArgumentException {
		if (x < 0 || x > 15) {
			throw new IllegalArgumentException("x 必须是 0 - 15 之间的整数数值");
		}
		if (y < 0 || y > 15) {
			throw new IllegalArgumentException("y 必须是 0 - 15 之间的整数数值");
		}
		return new ArpeggioEffect(x, y);
	}

	@Override
	public void execute(byte channelCode, FamiTrackerRuntime runtime) {
		AbstractFtmChannel ch = runtime.channels.get(channelCode);
		
		// 延迟静音效果在每个轨道只有一个
		HashSet<IFtmState> set = ch.filterStates(ArpeggioState.NAME);
		ArpeggioState s = null;
		
		if (!set.isEmpty()) {
			s = (ArpeggioState) set.iterator().next();
			
			if (x == 0 && y == 0) {
				ch.removeState(s);
			} else {
				s.x = x;
				s.y = y;
			}
		} else {
			if (x != 0 || y != 0) {
				s = new ArpeggioState(x, y);
				ch.addState(s);
			}
		}
	}
	
	@Override
	public String toString() {
		return "Arpeggio:" + x + "&" + y;
	}
	
	@Override
	public int priority() {
		return -3;
	}

}
