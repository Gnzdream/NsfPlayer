package zdream.nsfplayer.nsf.renderer;

/**
 * <p>NSF 以及常量及常量计算相关, 比如储存每一帧的时钟周期数等等
 * </p>
 * 
 * @author Zdream
 * @since v0.2.1
 */
public class NsfParameter {
	
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
	
	/**
	 * 采样率, 每秒的采样数
	 */
	public int sampleRate;
	
	/**
	 * 每秒的时钟周期数, 精确值
	 */
	public int freqPerSec;
	
}
