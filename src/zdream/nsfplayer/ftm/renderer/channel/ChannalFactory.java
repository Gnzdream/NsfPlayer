package zdream.nsfplayer.ftm.renderer.channel;

import zdream.nsfplayer.ftm.document.IFtmChannelCode;
import zdream.nsfplayer.ftm.renderer.AbstractFtmChannel;

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
		case CHANNEL_2A03_PULSE1:
			return new Square1Channel();

		default:
			break;
		}
		
		return null;
	}

}
