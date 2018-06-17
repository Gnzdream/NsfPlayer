package zdream.nsfplayer.ftm.renderer.effect;

import zdream.nsfplayer.ftm.renderer.FamiTrackerRuntime;

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
		// TODO Auto-generated method stub
	}
	
	@Override
	public String toString() {
		return "Note:---";
	}

}
