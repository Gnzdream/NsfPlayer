package zdream.nsfplayer.core;

/**
 * 轨道号的静态变量相关的数据转换
 * 
 * @author Zdream
 * @since 0.2.3
 */
public class NsfChannelCode implements INsfChannelCode {

	/**
	 * 查看轨道号对应的芯片号码. 如果不存在 channelCode 对应的轨道, 返回 -1
	 * @param channelCode
	 *   轨道号 / 轨道类型号. 静态变量 CHANNEL_*
	 * @return
	 */
	public static byte chipOfChannel(byte channelCode) {
		switch (channelCode) {
		case CHANNEL_2A03_PULSE1:
		case CHANNEL_2A03_PULSE2:
			return CHIP_2A03;
			
		case CHANNEL_2A03_TRIANGLE:
		case CHANNEL_2A03_NOISE:
		case CHANNEL_2A03_DPCM:
			return CHIP_2A07;
			
		case CHANNEL_VRC6_PULSE1:
		case CHANNEL_VRC6_PULSE2:
		case CHANNEL_VRC6_SAWTOOTH:
			return CHIP_VRC6;
			
		case CHANNEL_VRC7_FM1:
		case CHANNEL_VRC7_FM2:
		case CHANNEL_VRC7_FM3:
		case CHANNEL_VRC7_FM4:
		case CHANNEL_VRC7_FM5:
		case CHANNEL_VRC7_FM6:
			return CHIP_VRC7;
			
		case CHANNEL_FDS:
			return CHIP_FDS;
			
		case CHANNEL_MMC5_PULSE1:
		case CHANNEL_MMC5_PULSE2:
			return CHIP_MMC5;
			
		case CHANNEL_N163_1:
		case CHANNEL_N163_2:
		case CHANNEL_N163_3:
		case CHANNEL_N163_4:
		case CHANNEL_N163_5:
		case CHANNEL_N163_6:
		case CHANNEL_N163_7:
		case CHANNEL_N163_8:
			return CHIP_N163;
			
		case CHANNEL_S5B_SQUARE1:
		case CHANNEL_S5B_SQUARE2:
		case CHANNEL_S5B_SQUARE3:
			return CHIP_S5B;

		}
		
		return -1;
	}

	/**
	 * 查看轨道号对应的轨道类型. 如果不存在 channelCode 对应的轨道, 返回 -1
	 * @param channelCode
	 *   轨道号. 静态变量 CHANNEL_*
	 * @return
	 * @since v0.2.7
	 */
	public static byte typeOfChannel(byte channelCode) {
		switch (channelCode) {
		case CHANNEL_2A03_PULSE1:
		case CHANNEL_2A03_PULSE2:
			return CHANNEL_TYPE_PULSE;
		case CHANNEL_2A03_TRIANGLE:
			return CHANNEL_TYPE_TRIANGLE;
		case CHANNEL_2A03_NOISE:
			return CHANNEL_TYPE_NOISE;
		case CHANNEL_2A03_DPCM:
			return CHANNEL_TYPE_DPCM;
			
		case CHANNEL_VRC6_PULSE1:
		case CHANNEL_VRC6_PULSE2:
			return CHANNEL_TYPE_VRC6_PULSE;
		case CHANNEL_VRC6_SAWTOOTH:
			return CHANNEL_TYPE_SAWTOOTH;
			
		case CHANNEL_VRC7_FM1:
		case CHANNEL_VRC7_FM2:
		case CHANNEL_VRC7_FM3:
		case CHANNEL_VRC7_FM4:
		case CHANNEL_VRC7_FM5:
		case CHANNEL_VRC7_FM6:
			return CHANNEL_TYPE_VRC7;
			
		case CHANNEL_FDS:
			return CHANNEL_TYPE_FDS;
			
		case CHANNEL_MMC5_PULSE1:
		case CHANNEL_MMC5_PULSE2:
			return CHANNEL_TYPE_MMC5_PULSE;
			
		case CHANNEL_N163_1:
		case CHANNEL_N163_2:
		case CHANNEL_N163_3:
		case CHANNEL_N163_4:
		case CHANNEL_N163_5:
		case CHANNEL_N163_6:
		case CHANNEL_N163_7:
		case CHANNEL_N163_8:
			return CHANNEL_TYPE_N163;
			
		case CHANNEL_S5B_SQUARE1:
		case CHANNEL_S5B_SQUARE2:
		case CHANNEL_S5B_SQUARE3:
			return CHANNEL_TYPE_S5B;
		}
		
		return -1;
	}

}
