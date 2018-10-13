package zdream.nsfplayer.ftm.renderer.effect;

import zdream.nsfplayer.ftm.renderer.FamiTrackerRuntime;

/**
 * Ftm 效果
 * 
 * @author Zdream
 * @since 0.2.1
 */
public interface IFtmEffect {
	
	/**
	 * <p>效果种类.
	 * <p>允许不同效果类有相同的种类, 比如 {@link NoteEffect} 和 {@link NoiseEffect}.
	 * 它们在不同的轨道使用.
	 * </p>
	 * @return
	 */
	public FtmEffectType type();
	
	/**
	 * 执行效果
	 * @param channelCode
	 *   当前轨道号码
	 * @param runtime
	 */
	public void execute(byte channelCode, FamiTrackerRuntime runtime);

}
