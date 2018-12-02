package zdream.nsfplayer.core;

/**
 * <p>NSF 以及常量及常量计算相关, 存储更多与 NSF 结构运行时需要依赖的参数、常量数值.
 * </p>
 * 
 * @author Zdream
 * @since v0.2.9
 */
public class NsfCommonParameter {
	
	public NsfCommonParameter() {
		
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
	 */
	public int frameRate;
	
	/* **********
	 *   音量   *
	 ********** */
	public final ChannelLevelsParameter levels = new ChannelLevelsParameter();
	
	/* **********
	 * 播放参数 *
	 ********** */
	
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
