package zdream.nsfplayer.ftm.renderer.effect;

import zdream.nsfplayer.ftm.renderer.FamiTrackerRuntime;

/**
 * <p>随时间向上或者向下滑到指定音符的效果, Qxy, Rxy
 * <p>一个轨道在同一时间, Qxy Rxy 3xx 的效果只允许存在一个, 其中 3xx 优先.
 * </p>
 * 
 * @author Zdream
 * @since 0.2.2
 */
public class NoteSlideEffect implements IFtmEffect {
	
	/**
	 * 滑到的目的音符与现在音符的差值
	 */
	public final int delta;
	
	/**
	 * 滑动的速度
	 */
	public final int speed;

	private NoteSlideEffect(int slide, int speed) {
		this.delta = slide;
		this.speed = speed;
	}

	@Override
	public FtmEffectType type() {
		return FtmEffectType.SLIDE;
	}

	@Override
	public void execute(byte channelCode, FamiTrackerRuntime runtime) {
		// TODO Auto-generated method stub

	}
	
	// TODO 未完成

}
