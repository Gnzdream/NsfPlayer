package zdream.nsfplayer.core;

/**
 * 常量及常量计算相关, 比如储存每一帧的时钟周期数等等
 * @author Zdream
 * @since 0.2.1
 */
public class FamiTrackerParameter {
	
	/**
	 * 基础频率 NTSC
	 */
	public static final int FRAME_RATE_NTSC = 60;
	
	/**
	 * 基础频率 PAL
	 */
	public static final int FRAME_RATE_PAL = 50;
	
	public static final byte[] LENGTH_TABLE = {
			0x0A, (byte) 0xFE, 0x14, 0x02, 0x28, 0x04, 0x50, 0x06,
			(byte) 0xA0, 0x08, 0x3C, 0x0A, 0x0E, 0x0C, 0x1A, 0x0E,
			0x0C, 0x10, 0x18, 0x12, 0x30, 0x14, 0x60, 0x16,
			(byte) 0xC0, 0x18, 0x48, 0x1A, 0x10, 0x1C, 0x20, 0x1E
	};
	
	/* **********
	 * 时钟周期 *
	 ********** */
	
	/**
	 * NTSC 基础时钟数
	 */
	public static final int BASE_FREQ_NTSC = 1789773;

	/**
	 * PAL 基础时钟数
	 */
	public static final int BASE_FREQ_PAL = 1662607;
	
	/**
	 * 每帧的时钟周期数
	 */
	public int freqPerFrame;
	
	/**
	 * 每秒的时钟周期数
	 */
	public int freqPerSec;
	
	/**
	 * 计算时钟周期数等相关数据
	 * TODO 现在全部使用 NTSC 制式
	 */
	public void calcFreq(int frameRate) {
		freqPerSec = BASE_FREQ_NTSC;
		freqPerFrame = freqPerSec / /*runtime.querier.getFrameRate()*/frameRate;
	}

}
