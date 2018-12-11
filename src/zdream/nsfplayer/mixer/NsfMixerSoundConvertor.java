package zdream.nsfplayer.mixer;

import static zdream.nsfplayer.core.NsfChannelCode.typeOfChannel;

import zdream.nsfplayer.core.IExpression;
import zdream.nsfplayer.core.INsfChannelCode;

/**
 * <p>从 Sound 里面的音频数据转成采样数据的工具.
 * <p>现在只支持单轨道转化. 音量的影响放在转化之后.
 * </p>
 * 
 * @author Zdream
 * @since v0.3.0
 */
public class NsfMixerSoundConvertor implements INsfChannelCode {
	
	/**
	 * 适用于 2A03 矩形轨道
	 */
	public static int pulse(final int x) {
		return (x > 0) ? (int) (95.88 * 400 / ((8128.0 / x) + 156.0)) : 0;
	}
	
	/**
	 * 适用于 2A03 三角轨道
	 */
	public static int triangle(final int x) {
		return (x > 0) ? (int) (46159.29 / (1 / (x / 8227.0) + 30.0)) : 0;
	}
	
	/**
	 * 适用于 2A03 噪音轨道
	 */
	public static int noise(final int x) {
		return (x > 0) ? (int) (41543.36 / (1 / (x / 12241.0) + 30.0)) : 0;
	}
	
	/**
	 * 适用于 2A03 DPCM 轨道
	 */
	public static int dpcm(final int x) {
		return (x > 0) ? (int) (33234.69 / (1 / (x / 22638.0) + 30.0)) : 0;
	}
	
	/**
	 * 适用于 VRC6、MMC5 芯片的轨道
	 */
	public static int vrc6(final int x) {
		return (x > 0) ? (int) (96 * 360 / ((8000.0 / x) + 180)) : 0;
	}
	
	/**
	 * 适用于 FDS 轨道
	 * @param x
	 *   范围 [0, 2016]
	 */
	public static int fds(final int x) {
		return (x > 0) ? (int) (x / 21.1f) : 0;
	}
	
	/**
	 * 适用于 N163 芯片的轨道
	 */
	public static int n163(final int x) {
		return (int) (x / 2.4);
	}
	
	/**
	 * 适用于 VRC7 芯片的轨道
	 */
	public static int vrc7(final int x) {
		return (int) (x / 2.0);
	}
	
	/**
	 * 适用于 S5B 芯片的轨道
	 */
	public static int s5b(final int x) {
		return (int) (x * 1.1708);
	}
	
	/**
	 * 适用于其它采样数据的轨道
	 */
	public static int sampled(final int x) {
		return x;
	}
	
	/**
	 * 获取对应的数据转化表达式
	 * @param code
	 *   NSF 轨道号, 或类型号
	 * @return
	 */
	public static IExpression getExpression(byte code) {
		byte type = typeOfChannel(code);
		
		switch (type) {
		case CHANNEL_TYPE_PULSE:
			return NsfMixerSoundConvertor::pulse;
			
		case CHANNEL_TYPE_TRIANGLE:
			return NsfMixerSoundConvertor::triangle;
			
		case CHANNEL_TYPE_NOISE:
			return NsfMixerSoundConvertor::noise;
			
		case CHANNEL_TYPE_DPCM:
			return NsfMixerSoundConvertor::dpcm;
			
		case CHANNEL_TYPE_MMC5_PULSE:
		case CHANNEL_TYPE_VRC6_PULSE:
		case CHANNEL_TYPE_SAWTOOTH:
			return NsfMixerSoundConvertor::vrc6;
			
		case CHANNEL_TYPE_FDS:
			return NsfMixerSoundConvertor::fds;
			
		case CHANNEL_TYPE_N163:
			return NsfMixerSoundConvertor::n163;
			
		case CHANNEL_TYPE_VRC7:
			return NsfMixerSoundConvertor::vrc7;
			
		case CHANNEL_TYPE_S5B:
			return NsfMixerSoundConvertor::s5b;
		
		default:
			return NsfMixerSoundConvertor::sampled;
		}
	}

}
