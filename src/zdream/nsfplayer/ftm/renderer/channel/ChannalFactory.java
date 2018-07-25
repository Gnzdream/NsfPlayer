package zdream.nsfplayer.ftm.renderer.channel;

import zdream.nsfplayer.ftm.document.IFtmChannelCode;
import zdream.nsfplayer.ftm.renderer.AbstractFtmChannel;
import zdream.nsfplayer.ftm.renderer.TestFtmChannel;

/**
 * 轨道工厂
 * @author Zdream
 * @since 0.2.1
 */
public class ChannalFactory implements IFtmChannelCode {

	/**
	 * 建立各个轨道
	 * @param code
	 * @return
	 */
	public static AbstractFtmChannel create(byte code) {
		switch (code) {
		case CHANNEL_2A03_PULSE1: {
			Square1Channel s = new Square1Channel();
			return s;
		}
		case CHANNEL_2A03_PULSE2:
		case CHANNEL_2A03_TRIANGLE:
		case CHANNEL_2A03_NOISE:
		case CHANNEL_2A03_DPCM:
			// TODO
			return new TestFtmChannel(code);

		default:
			break;
		}
		
		return null;
	}

}
