package zdream.nsfplayer.mixer.xgm;

/**
 * VRC6 三个轨道的合并轨道
 * 
 * @author Zdream
 * @since v0.2.3
 */
public class XgmVRC6Mixer extends AbstractXgmMultiMixer {
	
	final XgmLinearChannel pulse1, pulse2, sawtooth;
	private boolean enable1, enable2, enableSaw;
	private final int MASTER = (int) (256.0 * 1223.0 / 1920.0); // 163.067

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
	public AbstractXgmAudioChannel getRemainAudioChannel(byte type) {
		if (type == CHANNEL_TYPE_VRC6_PULSE) {
			if (!enable1) {
				return pulse1;
			}
			if (!enable2) {
				return pulse2;
			}
			
		} else if (type == CHANNEL_TYPE_SAWTOOTH) {
			if (!enableSaw) {
				return sawtooth;
			}
		}
		return null;
	}
	
	@Override
	public void setEnable(AbstractXgmAudioChannel channel, boolean enable) {
		if (channel == pulse1) {
			enable1 = enable;
		} else if (channel == pulse2) {
			enable2 = enable;
		} else if (channel == sawtooth) {
			enableSaw = enable;
		}
	}
	
	@Override
	public boolean isEnable(AbstractXgmAudioChannel channel) {
		if (channel == pulse1) {
			return enable1;
		} else if (channel == pulse2) {
			return enable2;
		} else if (channel == sawtooth) {
			return enableSaw;
		}
		return false;
	}
	
	@Override
	public void beforeRender() {
		super.beforeRender();
		if (enable1)
			pulse1.beforeSubmit();
		if (enable2)
			pulse2.beforeSubmit();
		if (enableSaw)
			sawtooth.beforeSubmit();
	}

	@Override
	public int render(int index) {
		float sum = (enable1 ? pulse1.read(index) * pulse1.getLevel() : 0)
				+ (enable2 ? pulse2.read(index) * pulse2.getLevel() : 0)
				+ (enableSaw ? sawtooth.read(index) * sawtooth.getLevel() : 0);
		int value = (int) (sum * MASTER) >> 1;
		value = intercept(value, 1);
		return value;
	}

}
