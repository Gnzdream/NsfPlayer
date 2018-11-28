package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;

/**
 * 音键停止播放声音的效果. 单例
 * 
 * @author Zdream
 * @since 0.2.1
 */
public class NoteHaltEffect implements IFtmEffect {
	
	private static NoteHaltEffect instance = new NoteHaltEffect();

	private NoteHaltEffect() {
		// 单例
	}
	
	public static NoteHaltEffect of() {
		return instance;
	}

	@Override
	public FtmEffectType type() {
		return FtmEffectType.HALT;
	}
	
	@Override
	public void execute(byte channelCode, FamiTrackerRuntime runtime) {
		runtime.channels.get(channelCode).doHalt();
	}
	
	@Override
	public String toString() {
		return "Note:---";
	}
	
	/**
	 * 优先度必须大于 {@link NoteEffect} 和 {@link NoiseEffect},
	 * 以便静音之后, 允许放声音的效果重写
	 */
	public int priority() {
		return 1;
	}

}
