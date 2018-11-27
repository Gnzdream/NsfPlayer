package zdream.nsfplayer.core;

/**
 * <p>Mixer 在渲染过程中, 需要获得的 NSF 参数、常量数值类.
 * </p>
 * 
 * @author Zdream
 * @since v0.3.0
 */
public class MixerParameter {
	
	/* **********
	 *   采样   *
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
	 * 播放速度. 默认 1.0f, 有效范围为正数
	 * @since v0.2.9
	 */
	public float speed = 1.0f;
	
	/* **********
	 *   音量   *
	 ********** */
	public final ChannelLevelsParameter levels = new ChannelLevelsParameter();

}
