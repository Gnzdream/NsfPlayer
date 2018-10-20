package zdream.nsfplayer.sound.xgm;

/**
 * 2A03 矩形轨道 1 和 2 的合并轨道
 * 
 * @author Zdream
 * @since v0.2.3
 */
public class Xgm2A03Mixer extends AbstractXgmMultiMixer {

	XgmAudioChannel pulse1, pulse2;

	public Xgm2A03Mixer() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void setAudioChannel(byte channelCode, XgmAudioChannel ch) {
		switch (channelCode) {
		case CHANNEL_2A03_PULSE1:
			pulse1 = ch;
			break;
		case CHANNEL_2A03_PULSE2:
			pulse2 = ch;
			break;
		}
	}

	@Override
	public XgmAudioChannel getAudioChannel(byte channelCode) {
		switch (channelCode) {
		case CHANNEL_2A03_PULSE1:
			return pulse1;
		case CHANNEL_2A03_PULSE2:
			return pulse2;
		}
		return null;
	}
	
	@Override
	public void render(short[] buf, int length, int clockPerFrame) {
		pulse1.beforeSubmit();
		pulse2.beforeSubmit();
		
		for (int i = 0; i < length; i++) {
			int fromIdx = (clockPerFrame * (i) / length);
			int toIdx = (clockPerFrame * (i + 1) / length);
			int time = toIdx - fromIdx;
			int idx = (fromIdx + toIdx) / 2;
			
			int sum = pulse1.buffer[idx] + pulse2.buffer[idx];
			int value = (int) ((8192.0 * 95.88) / (8128.0 / sum + 100));
			buf[i] += (short) (intercept(value, time));
		}
	}
	
	

}
