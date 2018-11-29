package zdream.nsfplayer.nsf.renderer;

import zdream.nsfplayer.core.NsfCommonParameter;

/**
 * <p>NSF 以及常量及常量计算相关, 比如储存每一帧的时钟周期数等等
 * </p>
 * 
 * @author Zdream
 * @since v0.2.1
 */
public class NsfParameter extends NsfCommonParameter {
	
	/**
	 * 现在仅允许包内进行实例化
	 */
	NsfParameter() {
		super();
	}
	
	/**
	 * 用户指定用哪种制式进行播放, NTSC 或者 PAL
	 */
	public int region;
	
	/* **********
	 * 时钟周期 *
	 ********** */
	
	/**
	 * CPU 在当前采样时间内需要工作的时钟数.
	 * 不考虑播放速度变化的影响
	 * @since v0.2.9
	 */
	public int cpuClockInCurSample;
	
	/**
	 * APU 在当前采样时间内需要工作的时钟数.
	 * 它需要考虑播放速度变化的影响
	 * @since v0.2.9
	 */
	public int apuClockInCurSample;
	
}
