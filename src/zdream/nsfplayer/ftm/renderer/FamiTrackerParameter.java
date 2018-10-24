package zdream.nsfplayer.ftm.renderer;

import static zdream.nsfplayer.core.NsfStatic.BASE_FREQ_NTSC;

/**
 * 常量及常量计算相关, 比如储存每一帧的时钟周期数等等
 * @author Zdream
 * @since 0.2.1
 */
public class FamiTrackerParameter {
	
	/**
	 * 现在仅允许包内进行实例化
	 */
	public FamiTrackerParameter() {
		
	}
	
	/* **********
	 * 时钟周期 *
	 ********** */
	
	/**
	 * 每帧的时钟周期数, 非精确值
	 */
	public int freqPerFrame;
	
	/**
	 * 每秒的时钟周期数, 精确值
	 */
	public int freqPerSec;
	
	/**
	 * 当前帧的采样数
	 */
	public int sampleInCurFrame;
	
	/**
	 * 采样率, 每秒的采样数
	 */
	public int sampleRate;
	
	/**
	 * 帧率, 每秒多少帧.
	 * 现阶段只有 NsfPlayer 使用这个参数, FamiTracker 不从这里拿帧率
	 */
	public int frameRate;
	
	/**
	 * 计算时钟周期数等相关数据
	 * TODO 现在全部使用 NTSC 制式
	 */
	public void calcFreq(int frameRate) {
		this.frameRate = frameRate;
		freqPerSec = BASE_FREQ_NTSC;
		freqPerFrame = freqPerSec / /*runtime.querier.getFrameRate()*/frameRate;
	}
	
	/* **********
	 *   音量   *
	 ********** */

}
