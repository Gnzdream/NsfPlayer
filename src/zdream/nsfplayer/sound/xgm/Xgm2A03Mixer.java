package zdream.nsfplayer.sound.xgm;

/**
 * 2A03 矩形轨道 1 和 2 的合并轨道
 * 
 * @author Zdream
 * @since v0.2.3
 */
public class Xgm2A03Mixer extends AbstractXgmMultiMixer {

	final XgmLinearChannel pulse1, pulse2;

	public Xgm2A03Mixer() {
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
	public AbstractXgmAudioChannel getAudioChannel(byte channelCode) {
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
		pulse1.checkCapacity(size);
		pulse2.checkCapacity(size);
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
