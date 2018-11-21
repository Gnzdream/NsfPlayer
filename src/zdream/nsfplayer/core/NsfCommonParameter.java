package zdream.nsfplayer.core;

import static zdream.nsfplayer.core.NsfStatic.BASE_FREQ_NTSC;

/**
 * <p>NSF 以及常量及常量计算相关, 存储更多与 NSF 结构运行时需要依赖的参数、常量数值.
 * </p>
 * 
 * @author Zdream
 * @since v0.2.9
 */
public class NsfCommonParameter {
	
	protected NsfCommonParameter() {
		
	}
	
	/* **********
	 * 时钟周期 *
	 ********** */
	
	/**
	 * 每帧的时钟周期数, 现 FTM 为精确值, NSF 为模糊值
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
	@Deprecated
	public void calcFreq(int frameRate) {
		this.frameRate = frameRate;
		freqPerSec = BASE_FREQ_NTSC;
		freqPerFrame = freqPerSec / /*runtime.querier.getFrameRate()*/frameRate;
	}
	
	/* **********
	 *   音量   *
	 ********** */
	public float level2A03Pules1 = 1.0f;
	public float level2A03Pules2 = 1.0f;
	public float level2A03Triangle = 1.0f;
	public float level2A03Noise = 1.0f;
	public float level2A03DPCM = 1.0f;
	
	public float levelVRC6Pules1 = 1.0f;
	public float levelVRC6Pules2 = 1.0f;
	public float levelVRC6Sawtooth = 1.0f;
	
	public float levelMMC5Pules1 = 1.0f;
	public float levelMMC5Pules2 = 1.0f;
	
	public float levelFDS = 1.0f;
	
	public float levelN163Namco1 = 1.0f;
	public float levelN163Namco2 = 1.0f;
	public float levelN163Namco3 = 1.0f;
	public float levelN163Namco4 = 1.0f;
	public float levelN163Namco5 = 1.0f;
	public float levelN163Namco6 = 1.0f;
	public float levelN163Namco7 = 1.0f;
	public float levelN163Namco8 = 1.0f;
	
	public float levelVRC7FM1 = 1.0f;
	public float levelVRC7FM2 = 1.0f;
	public float levelVRC7FM3 = 1.0f;
	public float levelVRC7FM4 = 1.0f;
	public float levelVRC7FM5 = 1.0f;
	public float levelVRC7FM6 = 1.0f;
	
	public float levelS5BSquare1 = 1.0f;
	public float levelS5BSquare2 = 1.0f;
	public float levelS5BSquare3 = 1.0f;
	
	/* **********
	 * 播放参数 *
	 ********** */
	
	/**
	 * 正播放的曲目号
	 */
	public int trackIdx;
	
	/**
	 * 是否结束的标志
	 */
	public boolean finished;
	
	/**
	 * 播放速度. 默认 1.0f, 有效范围为正数
	 * @since v0.2.9
	 */
	public float speed = 1.0f;

}
