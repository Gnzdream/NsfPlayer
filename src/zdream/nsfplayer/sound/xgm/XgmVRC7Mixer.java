package zdream.nsfplayer.sound.xgm;

/**
 * <p>VRC7 六个轨道的合并轨道
 * </p>
 * 
 * @author Zdream
 * @since v0.2.7
 */
public class XgmVRC7Mixer extends AbstractXgmMultiMixer {

	final XgmAudioChannel[] chs = new XgmAudioChannel[6];

	public XgmVRC7Mixer() {
		for (int i = 0; i < chs.length; i++) {
			chs[i] = new XgmAudioChannel();
		}
	}

	@Override
	public void reset() {
		super.reset();
		for (int i = 0; i < chs.length; i++) {
			if (chs[i] != null) {
				chs[i].reset();
			}
		}
	}

	@Override
	public XgmAudioChannel getAudioChannel(byte channelCode) {
		switch (channelCode) {
		case CHANNEL_VRC7_FM1: return chs[0];
		case CHANNEL_VRC7_FM2: return chs[1];
		case CHANNEL_VRC7_FM3: return chs[2];
		case CHANNEL_VRC7_FM4: return chs[3];
		case CHANNEL_VRC7_FM5: return chs[4];
		case CHANNEL_VRC7_FM6: return chs[5];
		}
		return null;
	}
	
	@Override
	public void checkCapacity(int size) {
		for (int i = 0; i < chs.length; i++) {
			if (chs[i] != null) {
				chs[i].checkCapacity(size);
			}
		}
	}

	@Override
	public void beforeRender() {
		super.beforeRender();
		for (int i = 0; i < chs.length; i++) {
			if (chs[i] != null) {
				chs[i].beforeSubmit();
			}
		}
	}

	@Override
	public int render(int index, int fromIdx, int toIdx) {
		int time = toIdx - fromIdx;
		int idx = (fromIdx + toIdx) / 2;
		int sum = 0;
		
		for (int i = 0; i < chs.length; i++) {
			if (chs[i] != null) {
				sum += (int) (chs[i].buffer[idx] * chs[i].getLevel());
			}
		}
		
		final int MASTER = 205; // 0.8 * 256.0 = 204.8
		int value = (sum * MASTER) >> 4;
		
		return (intercept(value, time));
	}

}
