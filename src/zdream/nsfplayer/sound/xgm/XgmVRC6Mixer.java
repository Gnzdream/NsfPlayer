package zdream.nsfplayer.sound.xgm;

/**
 * VRC6 三个轨道的合并轨道
 * 
 * @author Zdream
 * @since v0.2.3
 */
public class XgmVRC6Mixer extends AbstractXgmMultiMixer {
	
	final XgmLinearChannel pulse1, pulse2, sawtooth;
	private final int MASTER = (int) (256.0 * 1223.0 / 1920.0);

	public XgmVRC6Mixer() {
		pulse1 = new XgmLinearChannel();
		pulse2 = new XgmLinearChannel();
		sawtooth = new XgmLinearChannel();
	}
	
	@Override
	public void reset() {
		super.reset();
		pulse1.reset();
		pulse2.reset();
		sawtooth.reset();
	}

	@Override
	public AbstractXgmAudioChannel getAudioChannel(byte channelCode) {
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
	public void checkCapacity(int size) {
		pulse1.checkCapacity(size);
		pulse2.checkCapacity(size);
		sawtooth.checkCapacity(size);
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
		
		int sum = (int) (pulse1.readValue(idx) * pulse1.getLevel()
				+ pulse2.readValue(idx) * pulse2.getLevel()
				+ sawtooth.readValue(idx) * sawtooth.getLevel());
		int value = (int) (sum * MASTER) >> 1;
		value = intercept(value, time);
		return value;
	}

}
