package zdream.nsfplayer.ftm.renderer.effect;

import zdream.nsfplayer.ftm.renderer.FamiTrackerRuntime;

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
	public void execute(byte channelCode, FamiTrackerRuntime rumtime) {
		// TODO Auto-generated method stub
		IFtmEffect.super.execute(channelCode, rumtime);
	}
	
	@Override
	public String toString() {
		return "Note:===";
	}

}
