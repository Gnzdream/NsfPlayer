package zdream.nsfplayer.ftm.renderer.tools;

import zdream.nsfplayer.ftm.renderer.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.renderer.IFtmRuntimeHolder;

/**
 * 常量及常量计算相关, 比如储存每一帧的时钟周期数等等
 * @author Zdream
 * @since 0.2.1
 */
public class FamiTrackerParameter implements IFtmRuntimeHolder {
	
	/**
	 * 基础频率 NTSC
	 */
	public static final int FRAME_RATE_NTSC = 60;
	
	/**
	 * 基础频率 PAL
	 */
	public static final int FRAME_RATE_PAL = 50;
	
	/**
	 * NTSC 基础时钟数
	 */
	public static final int BASE_FREQ_NTSC = 1789773;

	/**
	 * PAL 基础时钟数
	 */
	public static final int BASE_FREQ_PAL = 1662607;
	
	FamiTrackerRuntime runtime;

	@Override
	public FamiTrackerRuntime getRuntime() {
		return runtime;
	}
	
	public FamiTrackerParameter(FamiTrackerRuntime runtime) {
		this.runtime = runtime;
	}
	
	public void init() {
		// TODO
	}

}
