package zdream.nsfplayer.mixer.xgm;

/**
 * <p>VRC7 六个轨道的合并轨道
 * </p>
 * 
 * @author Zdream
 * @since v0.2.7
 */
public class XgmVRC7Mixer extends AbstractXgmMultiMixer {

	final XgmAudioChannel[] chs = new XgmAudioChannel[6];
	private boolean[] enables = new boolean[6];

	public XgmVRC7Mixer() {
		for (int i = 0; i < chs.length; i++) {
			chs[i] = new XgmAudioChannel();
		}
	}

	@Override
	public void reset() {
		super.reset();
		for (int i = 0; i < chs.length; i++) {
			chs[i].reset();
		}
	}

	@Override
	public XgmAudioChannel getRemainAudioChannel(byte type) {
		if (type != CHANNEL_TYPE_VRC7) {
			return null;
		}
		
		for (int i = 0; i < enables.length; i++) {
			if (!enables[i]) {
				return chs[i];
			}
		}
		
		return null;
	}
	
	@Override
	public void setEnable(AbstractXgmAudioChannel channel, boolean enable) {
		for (int i = 0; i < chs.length; i++) {
			if (channel == chs[i]) {
				enables[i] = enable;
				break;
			}
		}
	}
	
	@Override
	public boolean isEnable(AbstractXgmAudioChannel channel) {
		for (int i = 0; i < chs.length; i++) {
			if (channel == chs[i]) {
				return enables[i];
			}
		}
		return false;
	}
	
	@Override
	public void beforeRender() {
		super.beforeRender();
		for (int i = 0; i < enables.length; i++) {
			if (enables[i])
				chs[i].beforeSubmit();
		}
	}

	@Override
	public int render(int index) {
		float sum = 0;
		
		for (int i = 0; i < enables.length; i++) {
			if (enables[i]) {
				XgmAudioChannel ch = chs[i];
				sum += (ch.read(index) * ch.getLevel());
			}
		}
		
		final int MASTER = 205; // 0.8 * 256.0 = 204.8
		int value = (int) ((sum * MASTER) / 16);
		
		return (intercept(value, 1));
	}

}
