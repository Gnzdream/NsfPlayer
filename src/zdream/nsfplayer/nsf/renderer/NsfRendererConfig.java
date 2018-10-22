package zdream.nsfplayer.nsf.renderer;

/**
 * NSF 渲染器配置
 * @author Zdream
 * @date 2018-05-09
 * @since v0.1
 */
public class NsfRendererConfig {
	
	/**
	 * 帧率
	 */
	public int frameRate = 48000;
	
	/**
	 * 用户指定用哪种制式进行播放, NTSC 或者 PAL
	 */
	public int region;
	/**
	 * 跟随 Nsf 文件中指定的制式
	 */
	public static final int REGION_FOLLOW_AUDIO = 0;
	/**
	 * 强制要求 NTSC
	 */
	public static final int REGION_FORCE_NTSC = 1;
	/**
	 * 强制要求 PAL
	 */
	public static final int REGION_FORCE_PAL = 2;
	/**
	 * 强制要求 DENDY
	 */
	public static final int REGION_FORCE_DENDY = 3;

}
