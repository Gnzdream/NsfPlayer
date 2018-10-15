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
		// TODO Auto-generated constructor stub
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public AbstractNsfSound getSound() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int periodTable(int note) {
		// TODO Auto-generated method stub
		return 0;
	}

}
