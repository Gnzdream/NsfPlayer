package zdream.nsfplayer.sound.xgm;

/**
 * 2A03 三角、噪音、DPCM 的合并轨道
 * 
 * @author Zdream
 * @since v0.2.3
 */
public class Xgm2A07Mixer extends AbstractXgmMultiMixer {
	
	XgmAudioChannel tri, noise, dpcm;

	public Xgm2A07Mixer() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void setAudioChannel(byte channelCode, XgmAudioChannel ch) {
		switch (channelCode) {
		case CHANNEL_2A03_TRIANGLE:
			tri = ch;
			break;
		case CHANNEL_2A03_NOISE:
			noise = ch;
			break;
		case CHANNEL_2A03_DPCM:
			dpcm = ch;
			break;
		}
	}

	@Override
	public XgmAudioChannel getAudioChannel(byte channelCode) {
		switch (channelCode) {
		case CHANNEL_2A03_TRIANGLE:
			return tri;
		case CHANNEL_2A03_NOISE:
			return noise;
		case CHANNEL_2A03_DPCM:
			return dpcm;
		}
		return null;
	}
	
	@Override
	public void render(short[] buf, int length, int clockPerFrame) {
		tri.beforeSubmit();
		noise.beforeSubmit();
		dpcm.beforeSubmit();
		
		// volume adjusted by 0.75 based on empirical measurements
		// 音量乘上 0.75 是经验测量的结果 ——原 NsfPlayer 工程里面的注释
		final double MASTER = 8192.0 * 0.75;
		
		for (int i = 0; i < length; i++) {
			int fromIdx = (clockPerFrame * (i) / length);
			int toIdx = (clockPerFrame * (i + 1) / length);
			int time = toIdx - fromIdx;
			int idx = (fromIdx + toIdx) / 2;
			
			int value = (int) (MASTER *
					(3.0 * tri.buffer[idx] + 2.0 * noise.buffer[idx] + dpcm.buffer[idx]) / 208.0);
			buf[i] += (short) (intercept(value, time));
		}
	}

}
