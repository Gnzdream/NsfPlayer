package zdream.nsfplayer.ftm.renderer.tools;

import zdream.nsfplayer.ftm.document.IFtmChannelCode;
import zdream.nsfplayer.ftm.renderer.AbstractFtmChannel;
import zdream.nsfplayer.ftm.renderer.TestFtmChannel;
import zdream.nsfplayer.ftm.renderer.channel.NoiseChannel;
import zdream.nsfplayer.ftm.renderer.channel.Square1Channel;
import zdream.nsfplayer.ftm.renderer.channel.Square2Channel;
import zdream.nsfplayer.ftm.renderer.channel.TriangleChannel;
import zdream.nsfplayer.ftm.renderer.mixer.BlipMixerChannel;
import zdream.nsfplayer.sound.AbstractNsfSound;
import zdream.nsfplayer.sound.PulseSound;

/**
 * <p>轨道设备的选择工具
 * <p>原来名称为 <code>zdream.nsfplayer.ftm.renderer.channel.ChannalFactory</code>
 * 后来发现对于每个不同的轨道, 不仅是 Ftm 轨道, 还有发声器、音频轨道等都不相同,
 * 因此将其功能加强, 对每个不同的轨道选择各种设备和类.
 * </p>
 * 
 * @author Zdream
 * @since 0.2.1
 */
public class ChannalDeviceSelector implements IFtmChannelCode {

	/**
	 * 建立各个轨道
	 * @param code
	 *   轨道号
	 * @return
	 */
	public static AbstractFtmChannel selectFtmChannel(byte code) {
		switch (code) {
		case CHANNEL_2A03_PULSE1: {
			Square1Channel s = new Square1Channel();
			return s;
		}
		case CHANNEL_2A03_PULSE2: {
			Square2Channel s = new Square2Channel();
			return s;
		}
		case CHANNEL_2A03_TRIANGLE: {
			TriangleChannel s = new TriangleChannel();
			return s;
		}
		case CHANNEL_2A03_NOISE: {
			NoiseChannel s = new NoiseChannel();
			return s;
		}
		case CHANNEL_2A03_DPCM:
			// TODO
			return new TestFtmChannel(code);

		default:
			break;
		}
		
		return new TestFtmChannel(code);
	}
	
	/**
	 * 建立各个发声器
	 * @param code
	 *   轨道号
	 * @return
	 */
	@Deprecated
	public static AbstractNsfSound selectSound(byte code) {
		switch (code) {
		case CHANNEL_2A03_PULSE1: case CHANNEL_2A03_PULSE2: {
			return new PulseSound();
		}
		default:
			break;
		}
		
		return null;
	}
	
	/**
	 * 配置音频轨道
	 * @param code
	 */
	public static void configMixChannel(byte code, BlipMixerChannel mixer) {
		switch (code) {
		case CHANNEL_2A03_PULSE1: case CHANNEL_2A03_PULSE2:
		{
			mixer.updateSetting(12, -500);
			mixer.setExpression((x) -> (x > 0) ? (int) (95.88 * 400 / ((8128.0 / x) + 156.0)) : 0);
		} break;
		
		case CHANNEL_2A03_TRIANGLE:
		{
			mixer.updateSetting(12, -500);
			mixer.setExpression((x) -> (x > 0) ? (int) (46159.29 / (1 / (x / 8227.0) + 30.0)) : 0);
		} break;
		
		case CHANNEL_2A03_NOISE: case CHANNEL_2A03_DPCM:
		{
			mixer.updateSetting(12, -500);
			mixer.setExpression((x) -> (x > 0) ? (int) (57524.6 / (1 / (x / 12241.0) + 30.0)) : 0);
		}

		default:
			break;
		}
		
	}

}
