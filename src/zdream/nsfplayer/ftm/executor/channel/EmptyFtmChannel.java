package zdream.nsfplayer.ftm.executor.channel;

import zdream.nsfplayer.ftm.executor.AbstractFtmChannel;
import zdream.nsfplayer.sound.AbstractNsfSound;

/**
 * 测试使用的空轨道
 * @author Zdream
 * @since 0.2.1
 */
public class EmptyFtmChannel extends AbstractFtmChannel {

	public EmptyFtmChannel(byte channelCode) {
		super(channelCode);
	}

	@Override
	public void reset() {
		
	}

	@Override
	public AbstractNsfSound getSound() {
		return null;
	}
	
	@Override
	public int periodTable(int note) {
		return 0;
	}

	@Override
	public void writeToSound() {
		
	}

}
