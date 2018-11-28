package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;

/**
 * 音键释放的效果. 单例
 * 
 * @author Zdream
 * @since 0.2.1
 */
public class NoteReleaseEffect implements IFtmEffect {
	
	private static NoteReleaseEffect instance = new NoteReleaseEffect();

	private NoteReleaseEffect() {
		// 单例
	}
	
	public static NoteReleaseEffect of() {
		return instance;
	}

	@Override
	public FtmEffectType type() {
		return FtmEffectType.RELEASE;
	}
	
	@Override
	public void execute(byte channelCode, FamiTrackerRuntime runtime) {
		runtime.channels.get(channelCode).doRelease();
	}
	
	@Override
	public String toString() {
		return "Note:===";
	}
	
	/**
	 * 优先度必须大于 {@link NoteEffect} 和 {@link NoiseEffect},
	 * 以便释放之后, 允许放声音的效果重写
	 */
	public int priority() {
		return 1;
	}

}
