package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.IFtmState;
import zdream.nsfplayer.ftm.executor.tools.VibratoTable;

/**
 * <p>音量颤音状态, 7xy
 * </p>
 * 
 * @see TremoloEffect
 * 
 * @author Zdream
 * @since 0.2.2
 */
public class TremoloState implements IFtmState {
	
	public static final String NAME = "Tremolo";
	
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

	public TremoloState(int speed, int depth) {
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
		int x = phase >> 1; // [0, 31]
		
		int delta = VibratoTable.vibratoValue(depth, x) << 3;
		runtime.channels.get(channelCode).addCurrentVolume(-delta); // 影响是, 音量一定不大于原值
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
