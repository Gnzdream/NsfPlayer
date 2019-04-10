package zdream.nsfplayer.mixer.xgm;

/**
 * VRC6 三个轨道的合并轨道
 * 
 * @author Zdream
 * @since v0.2.3
 */
public class XgmS5BMixer extends AbstractXgmMultiMixer {
	
	final XgmLinearChannel ch1, ch2, ch3;
	private boolean enable1, enable2, enable3;
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
	public XgmLinearChannel getRemainAudioChannel(byte type) {
		if (type != CHANNEL_TYPE_S5B) {
			return null;
		}
		
		if (!enable1) {
			return ch1;
		}
		if (!enable2) {
			return ch2;
		}
		if (!enable3) {
			return ch3;
		}
		return null;
	}
	
	@Override
	public void setEnable(AbstractXgmAudioChannel channel, boolean enable) {
		if (channel == this.ch1) {
			enable1 = enable;
		} else if (channel == this.ch2) {
			enable2 = enable;
		} else if (channel == this.ch3) {
			enable3 = enable;
		}
	}
	
	@Override
	public boolean isEnable(AbstractXgmAudioChannel channel) {
		if (channel == this.ch1) {
			return enable1;
		} else if (channel == this.ch2) {
			return enable2;
		} else if (channel == this.ch3) {
			return enable3;
		}
		return false;
	}
	
	@Override
	public void beforeRender() {
		super.beforeRender();
		if (enable1)
			ch1.beforeSubmit();
		if (enable2)
			ch2.beforeSubmit();
		if (enable3)
			ch3.beforeSubmit();
	}

	@Override
	public int render(int index) {
		float sum = 
				(enable1 ? ch1.read(index) * ch1.getLevel() : 0)
				+ (enable2 ? ch2.read(index) * ch2.getLevel() : 0)
				+ (enable3 ? ch3.read(index) * ch3.getLevel() : 0);
		int value = (int) (sum * MASTER);
		value = intercept(value, 1);
		return value;
	}

}
