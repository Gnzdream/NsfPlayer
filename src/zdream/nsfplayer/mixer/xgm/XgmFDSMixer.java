package zdream.nsfplayer.mixer.xgm;

/**
 * FDS 的 (合并) 轨道
 * 
 * @author Zdream
 * @since v0.2.4
 */
public class XgmFDSMixer extends AbstractXgmMultiMixer {
	
	final XgmAudioChannel fds;
	boolean fdsEnable;
	
	public XgmFDSMixer() {
		fds = new XgmAudioChannel();
	}

	@Override
	public void reset() {
		super.reset();
		if (fdsEnable)
			fds.reset();
	}

	@Override
	public AbstractXgmAudioChannel getRemainAudioChannel(byte type) {
		if (type == CHANNEL_TYPE_FDS) {
			return (fdsEnable) ? null : fds;
		}
		return null;
	}
	
	@Override
	public void setEnable(AbstractXgmAudioChannel channel, boolean enable) {
		if (channel == fds) {
			fdsEnable = enable;
		}
	}
	
	@Override
	public boolean isEnable(AbstractXgmAudioChannel channel) {
		if (channel == fds) {
			return fdsEnable;
		}
		return false;
	}

	@Override
	public void beforeRender() {
		super.beforeRender();
		fds.beforeSubmit();
	}
	
	@Override
	public int render(int index) {
		// 最终输出部分
		// 8 bit approximation of master volume
		final float MASTER_VOL = 2935.2f; // = 2.4 * 1223.0; max FDS vol vs max APU square (arbitrarily 1223)
		final float MAX_OUT = 2016f; // = 32.0f * 63.0f; value that should map to master vol
		
		float v = (fdsEnable) ? fds.read(index) * fds.getLevel() * (MASTER_VOL / MAX_OUT) : 0;
		int value = (int) v;
		
		value = intercept(value, 1);
		return value;
	}

}
