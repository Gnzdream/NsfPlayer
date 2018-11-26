package zdream.nsfplayer.core;

/**
 * <p>储存轨道号的静态变量
 * </p>
 * 
 * @version v0.2.3
 *   修改接口名 IFtmChannelCode -> INsfChannelCode
 *   并移动接口至 nsfplayer.core 包下
 * 
 * @version v0.2.5
 *   补充 S5B 相关的参数
 *   
 * @version v0.2.7
 *   补充轨道类型编号相关的参数
 *   
 * @version v0.2.10
 *   补充 2A07 芯片号的声明,
 *   现在原 2A03 芯片中的三角、噪音和 DPCM 移到 2A07 下.
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
			CHANNEL_N163_6 = 0x56,
			CHANNEL_N163_7 = 0x57,
			CHANNEL_N163_8 = 0x58,
					
			CHANNEL_S5B_SQUARE1 = 0x61,
			CHANNEL_S5B_SQUARE2 = 0x62,
			CHANNEL_S5B_SQUARE3 = 0x63;
	
	/**
	 * 各个芯片的标识号
	 * @since v0.2.3
	 */
	public static final byte
			CHIP_2A03 = 0,
			CHIP_2A07 = 3,
			CHIP_VRC6 = 0x10,
			CHIP_VRC7 = 0x20,
			CHIP_FDS = 0x30,
			CHIP_MMC5 = 0x40,
			CHIP_N163 = 0x50,
			CHIP_S5B = 0x60;
	
	/**
	 * <p>轨道类型编号
	 * <p>例如 2A03 的两个矩形脉冲轨道, 可以看做一类, 为一个类型;
	 * </p>
	 * 
	 * @since v0.2.7
	 */
	public static final byte
			CHANNEL_TYPE_PULSE = CHANNEL_2A03_PULSE1,
			CHANNEL_TYPE_TRIANGLE = CHANNEL_2A03_TRIANGLE,
			CHANNEL_TYPE_NOISE = CHANNEL_2A03_NOISE,
			CHANNEL_TYPE_DPCM = CHANNEL_2A03_DPCM,
			CHANNEL_TYPE_VRC6_PULSE = CHANNEL_VRC6_PULSE1,
			CHANNEL_TYPE_SAWTOOTH = CHANNEL_VRC6_SAWTOOTH,
			CHANNEL_TYPE_VRC7 = CHANNEL_VRC7_FM1,
			CHANNEL_TYPE_FDS = CHANNEL_FDS,
			CHANNEL_TYPE_MMC5_PULSE = CHANNEL_MMC5_PULSE1,
			CHANNEL_TYPE_N163 = CHANNEL_N163_1,
			CHANNEL_TYPE_S5B = CHANNEL_S5B_SQUARE1,
			CHANNEL_TYPE_CUSTOM = 0x70;
	
}
