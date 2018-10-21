package zdream.nsfplayer.core;

/**
 * <p>储存轨道号的静态变量
 * </p>
 * 
 * @version v0.2.3
 *   修改接口名 IFtmChannelCode -> INsfChannelCode
 *   并移动接口至 nsfplayer.core 包下
 * 
 * @author Zdream
 * @since v0.2.1
 */
public interface INsfChannelCode {

	/**
	 * 各个轨道的标识号
	 */
	public static final byte
			CHANNEL_2A03_PULSE1 = 1,
			CHANNEL_2A03_PULSE2 = 2,
			CHANNEL_2A03_TRIANGLE = 3,
			CHANNEL_2A03_NOISE = 4,
			CHANNEL_2A03_DPCM = 5,
			
			CHANNEL_VRC6_PULSE1 = 0x11,
			CHANNEL_VRC6_PULSE2 = 0x12,
			CHANNEL_VRC6_SAWTOOTH = 0x13,
			
			CHANNEL_VRC7_FM1 = 0x21,
			CHANNEL_VRC7_FM2 = 0x22,
			CHANNEL_VRC7_FM3 = 0x23,
			CHANNEL_VRC7_FM4 = 0x24,
			CHANNEL_VRC7_FM5 = 0x25,
			CHANNEL_VRC7_FM6 = 0x26,
			
			CHANNEL_FDS = 0x31,
			
			CHANNEL_MMC5_PULSE1 = 0x41,
			CHANNEL_MMC5_PULSE2 = 0x42,
			
			CHANNEL_N163_1 = 0x51,
			CHANNEL_N163_2 = 0x52,
			CHANNEL_N163_3 = 0x53,
			CHANNEL_N163_4 = 0x54,
			CHANNEL_N163_5 = 0x55,
			CHANNEL_N163_6 = 0x56;
	
	/**
	 * 各个芯片的标识号
	 * @since v0.2.3
	 */
	public static final byte
			CHIP_2A03 = 0,
			CHIP_VRC6 = 0x10,
			CHIP_VRC7 = 0x20,
			CHIP_FDS = 0x30,
			CHIP_MMC5 = 0x40,
			CHIP_N163 = 0x50;

}