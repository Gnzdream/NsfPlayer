package zdream.nsfplayer.mixer.xgm;

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
	private boolean pulse1Enable, pulse2Enable;

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
	public XgmLinearChannel getRemainAudioChannel(byte type) {
		if (type != CHANNEL_TYPE_MMC5_PULSE) {
			return null;
		}
		
		if (pulse1Enable && pulse2Enable) {
			return null;
		}
		
		return (pulse1Enable) ? pulse2 : pulse1;
	}
	
	@Override
	public void setEnable(AbstractXgmAudioChannel channel, boolean enable) {
		if (channel == pulse1) {
			pulse1Enable = enable;
		} else if (channel == pulse2) {
			pulse2Enable = enable;
		}
	}
	
	@Override
	public boolean isEnable(AbstractXgmAudioChannel channel) {
		if (channel == pulse1) {
			return pulse1Enable;
		} else if (channel == pulse2) {
			return pulse2Enable;
		}
		return false;
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
		
		float sum = 
				(pulse1Enable ? pulse1.readValue(idx) * pulse1.getLevel() : 0)
				+ (pulse2Enable ? pulse2.readValue(idx) * pulse2.getLevel() : 0);
		int value = (sum == 0) ? 0 : (int) ((8192.0 * 95.88) / (8128.0 / sum + 100));
		return (intercept(value, time));
	}

}
