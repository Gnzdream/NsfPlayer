package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.ftm.executor.AbstractFtmChannel;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.IFtmState;

/**
 * <p>琶音状态
 * </p>
 * 
 * @see ArpeggioEffect
 * 
 * @author Zdream
 * @since 0.2.2
 */
public class ArpeggioState implements IFtmState {
	
	public static final String NAME = "Arpeggio";
	
	/**
	 * 两个琶音在原音高上增加几个半音
	 */
	public int x, y;
	
	/**
	 * 相当于相位. 周期为 3, period 的范围是 [0, 2]
	 */
	private int period;

	public ArpeggioState(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}

	@Override
	public String name() {
		return NAME;
	}

	@Override
	public void trigger(byte channelCode, FamiTrackerRuntime runtime) {
		AbstractFtmChannel ch = runtime.channels.get(channelCode);
		
		switch (period) {
		case 1:
			ch.addCurrentNote(x);
			break;
		case 2:
			ch.addCurrentNote(y);
			break;
		}
		
		period = (period + 1) % 3;
	}
	
	@Override
	public String toString() {
		return NAME + ":" + x + "&" + y;
	}
	
	@Override
	public int priority() {
		return -3;
	}

}
