package zdream.nsfplayer.sound.xgm;

/**
 * VRC6 三个轨道的合并轨道
 * 
 * @author Zdream
 * @since v0.2.3
 */
public class XgmS5BMixer extends AbstractXgmMultiMixer {
	
	final XgmLinearChannel ch1, ch2, ch3;
	private final int MASTER = 15; // 8 * 0.64 * 3 = 15.36

	public XgmS5BMixer() {
		ch1 = new XgmLinearChannel();
		ch2 = new XgmLinearChannel();
		ch3 = new XgmLinearChannel();
	}
	
	@Override
	public void reset() {
		super.reset();
		ch1.reset();
		ch2.reset();
		ch3.reset();
	}

	@Override
	public XgmLinearChannel getAudioChannel(byte channelCode) {
		switch (channelCode) {
		case CHANNEL_S5B_SQUARE1:
			return ch1;
		case CHANNEL_S5B_SQUARE2:
			return ch2;
		case CHANNEL_S5B_SQUARE3:
			return ch3;
		}
		return null;
	}
	
	@Override
	public void checkCapacity(int size, int frame) {
		if (ch1 != null) {
			ch1.checkCapacity(size, frame);
		}
		if (ch2 != null) {
			ch2.checkCapacity(size, frame);
		}
		if (ch3 != null) {
			ch3.checkCapacity(size, frame);
		}
	}
	
	@Override
	public void beforeRender() {
		super.beforeRender();
		ch1.beforeSubmit();
		ch2.beforeSubmit();
		ch3.beforeSubmit();
	}

	@Override
	public int render(int index, int fromIdx, int toIdx) {
		int time = toIdx - fromIdx;
		int idx = (fromIdx + toIdx) / 2;
		
		int sum = (int) (ch1.readValue(idx) * ch1.getLevel()
				+ ch2.readValue(idx) * ch2.getLevel()
				+ ch3.readValue(idx) * ch3.getLevel());
		int value = (int) (sum * MASTER);
		value = intercept(value, time);
		return value;
	}

}
