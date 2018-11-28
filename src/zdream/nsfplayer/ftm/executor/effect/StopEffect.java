package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;

/**
 * <p>停止播放的效果 (在 FamiTracker 中称为 Halt 效果), Cxx
 * <p>属全局效果
 * </p>
 * 
 * @author Zdream
 * @since v0.2.2
 */
public class StopEffect implements IFtmEffect {
	
	private static StopEffect instance = new StopEffect();

	private StopEffect() {
		// 单例
	}
	
	public static StopEffect of() {
		return instance;
	}

	@Override
	public FtmEffectType type() {
		return FtmEffectType.STOP;
	}
	
	@Override
	public void execute(byte channelCode, FamiTrackerRuntime runtime) {
		runtime.param.finished = true;
	}
	
	@Override
	public String toString() {
		return "Stop";
	}
	
	public int priority() {
		return 9;
	}

}
