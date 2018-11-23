package zdream.nsfplayer.sound.xgm;

/**
 * 2A03 矩形轨道 1 和 2 的合并轨道
 * 
 * @author Zdream
 * @since v0.2.3
 */
public class Xgm2A03Mixer extends AbstractXgmMultiMixer {

	XgmAudioChannel pulse1, pulse2;

	public Xgm2A03Mixer() {
		
	}
	
	@Override
	public void reset() {
		super.reset();
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
		case CHANNEL_2A03_PULSE1:
			pulse1 = ch;
			break;
		case CHANNEL_2A03_PULSE2:
			pulse2 = ch;
			break;
		}
	}

	@Override
	public XgmAudioChannel getAudioChannel(byte channelCode) {
		switch (channelCode) {
		case CHANNEL_2A03_PULSE1:
			return pulse1;
		case CHANNEL_2A03_PULSE2:
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
