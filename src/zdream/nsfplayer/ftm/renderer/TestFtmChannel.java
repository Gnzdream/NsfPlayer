package zdream.nsfplayer.ftm.renderer;

import zdream.nsfplayer.sound.AbstractNsfSound;

/**
 * 测试使用
 * @author Zdream
 *
 */
public class TestFtmChannel extends AbstractFtmChannel {

	public TestFtmChannel(byte channelCode) {
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

}
