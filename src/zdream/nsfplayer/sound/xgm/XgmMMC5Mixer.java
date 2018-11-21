package zdream.nsfplayer.sound.xgm;

/**
 * <p>MMC5 两个轨道的合并轨道
 * 
 * <p>这里不考虑 MMC5 的另外一个轨道: PCM
 * </p>
 * 
 * @author Zdream
 * @since v0.2.3
 */
public class XgmMMC5Mixer extends AbstractXgmMultiMixer {

	XgmAudioChannel pulse1, pulse2;

	public XgmMMC5Mixer() {
		
	}
	
	@Override
	public void reset() {
		if (pulse1 != null) {
			pulse1.reset();
		}
		if (pulse2 != null) {
			pulse1.reset();
		}
	}
	
	@Override
	public void setAudioChannel(byte channelCode, XgmAudioChannel ch) {
		switch (channelCode) {
		case CHANNEL_MMC5_PULSE1:
			pulse1 = ch;
			break;
		case CHANNEL_MMC5_PULSE2:
			pulse2 = ch;
			break;
		}
	}

	@Override
	public XgmAudioChannel getAudioChannel(byte channelCode) {
		switch (channelCode) {
		case CHANNEL_MMC5_PULSE1:
			return pulse1;
		case CHANNEL_MMC5_PULSE2:
			return pulse2;
		}
		return null;
	}
	
	@Override
	public void checkCapacity(int size) {
		if (pulse1 != null) {
			pulse1.checkCapacity(size);
		}
		if (pulse2 != null) {
			pulse2.checkCapacity(size);
		}
	}
	
	@Override
	public void beforeRender() {
		pulse1.beforeSubmit();
		pulse2.beforeSubmit();
	}

	@Override
	public int render(int index, int fromIdx, int toIdx) {
		int time = toIdx - fromIdx;
		int idx = (fromIdx + toIdx) / 2;
		
		int sum = (int) (pulse1.buffer[idx] * pulse1.getLevel()
				+ pulse2.buffer[idx] * pulse2.getLevel());
		int value = (int) ((8192.0 * 95.88) / (8128.0 / sum + 100));
		return (intercept(value, time));
	}

}
