package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.IFtmState;
import zdream.nsfplayer.ftm.executor.tools.VibratoTable;

/**
 * <p>颤音状态, 4xy
 * </p>
 * 
 * @see VibratoEffect
 * 
 * @author Zdream
 * @since 0.2.2
 */
public class VibratoState implements IFtmState {
	
	public static final String NAME = "Vibrato";
	
	/**
	 * 颤音正弦波的速度
	 */
	public int speed;
	
	/**
	 * 振幅
	 */
	public int depth;
	
	/**
	 * 相位数. 有效值 [0, 64), 为一个周期
	 */
	private int phase = 0;

	public VibratoState(int speed, int depth) {
		this.speed = speed;
		this.depth = depth;
	}

	@Override
	public String name() {
		return NAME;
	}

	@Override
	public void trigger(byte channelCode, FamiTrackerRuntime runtime) {
		phase = (phase + speed) & 63;
		
		int delta = VibratoTable.vibratoValue(depth, phase);
		runtime.channels.get(channelCode).addCurrentPeriod(delta);
	}
	
	@Override
	public String toString() {
		return NAME + ":" + depth + "#" + speed;
	}
	
	@Override
	public int priority() {
		return -2;
	}

}
