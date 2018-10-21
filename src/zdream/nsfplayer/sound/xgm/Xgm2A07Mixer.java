package zdream.nsfplayer.sound.xgm;

/**
 * 2A03 三角、噪音、DPCM 的合并轨道
 * 
 * @author Zdream
 * @since v0.2.3
 */
public class Xgm2A07Mixer extends AbstractXgmMultiMixer {
	
	XgmAudioChannel tri, noise, dpcm;
	
	/*protected int[][][] tnd_table = new int[16][16][128];
	protected int[][][] tnd_table_non = new int[16][16][128];

	public Xgm2A07Mixer() {
		// volume adjusted by 0.75 based on empirical measurements
		// 音量乘上 0.75 是经验测量的结果 ——原 NsfPlayer 工程里面的注释
		final double MASTER = 8192.0 * 0.75;
		double wt = 8227, wn = 12241, wd = 22638;
		
		for (int t = 0; t < 16; t++) {
			for (int n = 0; n < 16; n++) {
				for (int d = 0; d < 128; d++) {
					tnd_table[t][n][d] = (int) (MASTER * (3.0 * t + 2.0 * n + d) / 208.0);
				}
			}
		}
		
		for (int t = 0; t < 16; t++) {
			for (int n = 0; n < 16; n++) {
				for (int d = 0; d < 128; d++) {
					tnd_table_non[t][n][d] = (int) ((MASTER * 159.79)
							/ (100.0 + 1.0 / (t / wt + n / wn + d / wd)));
				}
			}
		}
	}*/
	
	@Override
	public void reset() {
		if (tri != null) {
			tri.reset();
		}
		if (noise != null) {
			noise.reset();
		}
		if (dpcm != null) {
			dpcm.reset();
		}
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
	public void beforeRender() {
		tri.beforeSubmit();
		noise.beforeSubmit();
		dpcm.beforeSubmit();
	}
	
	@Override
	public int render(int index, int fromIdx, int toIdx) {
		int time = toIdx - fromIdx;
		int idx = (fromIdx + toIdx) / 2;
		
		// volume adjusted by 0.75 based on empirical measurements
		// 音量乘上 0.75 是经验测量的结果 ——原 NsfPlayer 工程里面的注释
		// 8192.0 * 0.75 = 6144
		final double MASTER = 6144.0;
		
		int value = (int) (MASTER *
				(3.0 * tri.buffer[idx] * tri.getLevel()
						+ 2.0 * noise.buffer[idx] * noise.getLevel()
						+ dpcm.buffer[idx] * dpcm.getLevel()) / 208.0);
		return (intercept(value, time));
	}

}
