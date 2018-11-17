package zdream.nsfplayer.sound.xgm;

/**
 * VRC6 三个轨道的合并轨道
 * 
 * @author Zdream
 * @since v0.2.3
 */
public class XgmS5BMixer extends AbstractXgmMultiMixer {
	
	XgmAudioChannel ch1, ch2, ch3;
	private final int MASTER = 15; // 8 * 0.64 * 3 = 15.36

	public XgmS5BMixer() {
		
	}
	
	@Override
	public void reset() {
		if (ch1 != null) {
			ch1.reset();
		}
		if (ch2 != null) {
			ch2.reset();
		}
		if (ch3 != null) {
			ch3.reset();
		}
	}

	@Override
	public void setAudioChannel(byte channelCode, XgmAudioChannel ch) {
		switch (channelCode) {
		case CHANNEL_S5B_SQUARE1:
			ch1 = ch;
			break;
		case CHANNEL_S5B_SQUARE2:
			ch2 = ch;
			break;
		case CHANNEL_S5B_SQUARE3:
			ch3 = ch;
			break;
		}
	}

	@Override
	public XgmAudioChannel getAudioChannel(byte channelCode) {
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
	public void beforeRender() {
		ch1.beforeSubmit();
		ch2.beforeSubmit();
		ch3.beforeSubmit();
	}

	@Override
	public int render(int index, int fromIdx, int toIdx) {
		int time = toIdx - fromIdx;
		int idx = (fromIdx + toIdx) / 2;
		
		int sum = (int) (ch1.buffer[idx] * ch1.getLevel()
				+ ch2.buffer[idx] * ch2.getLevel()
				+ ch3.buffer[idx] * ch3.getLevel());
		int value = (int) (sum * MASTER);
		value = intercept(value, time);
		return value;
	}

}
