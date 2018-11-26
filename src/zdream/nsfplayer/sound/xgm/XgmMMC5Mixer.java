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

	final XgmLinearChannel pulse1, pulse2;

	public XgmMMC5Mixer() {
		pulse1 = new XgmLinearChannel();
		pulse2 = new XgmLinearChannel();
	}
	
	@Override
	public void reset() {
		super.reset();
		pulse1.reset();
		pulse2.reset();
	}
	
	@Override
	public XgmLinearChannel getAudioChannel(byte channelCode) {
		switch (channelCode) {
		case CHANNEL_MMC5_PULSE1:
			return pulse1;
		case CHANNEL_MMC5_PULSE2:
			return pulse2;
		}
		return null;
	}
	
	@Override
	public void checkCapacity(int size, int frame) {
		pulse1.checkCapacity(size, frame);
		pulse2.checkCapacity(size, frame);
	}
	
	@Override
	public void beforeRender() {
		super.beforeRender();
		pulse1.beforeSubmit();
		pulse2.beforeSubmit();
	}

	@Override
	public int render(int index, int fromIdx, int toIdx) {
		int time = toIdx - fromIdx;
		int idx = (fromIdx + toIdx) / 2;
		
		int sum = (int) (pulse1.readValue(idx) * pulse1.getLevel()
				+ pulse2.readValue(idx) * pulse2.getLevel());
		int value = (int) ((8192.0 * 95.88) / (8128.0 / sum + 100));
		return (intercept(value, time));
	}

}
