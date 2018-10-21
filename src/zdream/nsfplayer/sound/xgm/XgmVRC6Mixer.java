package zdream.nsfplayer.sound.xgm;

/**
 * VRC6 三个轨道的合并轨道
 * 
 * @author Zdream
 * @since v0.2.3
 */
public class XgmVRC6Mixer extends AbstractXgmMultiMixer {
	
	XgmAudioChannel pulse1, pulse2, sawtooth;
	private final int MASTER = (int) (256.0 * 1223.0 / 1920.0);

	public XgmVRC6Mixer() {
		
	}
	
	@Override
	public void reset() {
		if (pulse1 != null) {
			pulse1.reset();
		}
		if (pulse2 != null) {
			pulse1.reset();
		}
		if (sawtooth != null) {
			sawtooth.reset();
		}
	}

	@Override
	public void setAudioChannel(byte channelCode, XgmAudioChannel ch) {
		switch (channelCode) {
		case CHANNEL_VRC6_PULSE1:
			pulse1 = ch;
			break;
		case CHANNEL_VRC6_PULSE2:
			pulse2 = ch;
			break;
		case CHANNEL_VRC6_SAWTOOTH:
			sawtooth = ch;
			break;
		}
	}

	@Override
	public XgmAudioChannel getAudioChannel(byte channelCode) {
		switch (channelCode) {
		case CHANNEL_VRC6_PULSE1:
			return pulse1;
		case CHANNEL_VRC6_PULSE2:
			return pulse2;
		case CHANNEL_VRC6_SAWTOOTH:
			return sawtooth;
		}
		return null;
	}
	
	@Override
	public void beforeRender() {
		pulse1.beforeSubmit();
		pulse2.beforeSubmit();
		sawtooth.beforeSubmit();
	}

	@Override
	public int render(int index, int fromIdx, int toIdx) {
		int time = toIdx - fromIdx;
		int idx = (fromIdx + toIdx) / 2;
		
		int sum = (int) (pulse1.buffer[idx] * pulse1.getLevel()
				+ pulse2.buffer[idx] * pulse2.getLevel()
				+ sawtooth.buffer[idx] * sawtooth.getLevel());
		int value = (int) (sum * MASTER) >> 1;
		value = intercept(value, time);
		return value;
	}

}
