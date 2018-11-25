package zdream.nsfplayer.sound.xgm;

/**
 * 2A03 三角、噪音、DPCM 的合并轨道
 * 
 * @author Zdream
 * @since v0.2.3
 */
public class Xgm2A07Mixer extends AbstractXgmMultiMixer {
	
	final XgmAudioChannel noise, dpcm;
	final XgmLinearChannel tri;
	
	public Xgm2A07Mixer() {
		noise = new XgmAudioChannel();
		tri = new XgmLinearChannel();
		dpcm = new XgmAudioChannel();
	}
	
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
		super.reset();
		tri.reset();
		noise.reset();
		dpcm.reset();
	}
	
	@Override
	public AbstractXgmAudioChannel getAudioChannel(byte channelCode) {
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
	public void checkCapacity(int size) {
		tri.checkCapacity(size);
		noise.checkCapacity(size);
		dpcm.checkCapacity(size);
	}
	
	@Override
	public int render(int index, int fromIdx, int toIdx) {
		int time = toIdx - fromIdx;
		int idx = (fromIdx + toIdx) / 2;
		
		// volume adjusted by 0.75 based on empirical measurements
		// 音量乘上 0.75 是经验测量的结果 ——原 NsfPlayer 工程里面的注释
		// 8192.0 * 0.75 * 159.79 = 981749.76
		final double MASTER = 981750;
		/*
		 * ((MASTER) / (100.0 + 1.0 / ((double) t / 8227 + (double) n / 12241 + (double) d / 22638)));
		 */
		int value = (int) ((MASTER) / (100.0 + 1.0 / 
				(tri.readValue(idx) * tri.getLevel() / 8227
				+ calcNoise(fromIdx, toIdx) * noise.getLevel() / 12241
				+ dpcm.buffer[idx] * dpcm.getLevel() / 22638)));
		
//		int value = (int) (8192.0 * 0.75 *
//				(3.0 * tri.buffer[idx] * tri.getLevel()
//						+ 2.0 * noise.buffer[idx] * noise.getLevel()
//						+ dpcm.buffer[idx] * dpcm.getLevel()) / 208.0);
		return (intercept(value, time));
	}
	
	/**
	 * 噪音轨和别的轨不同, 它是计算平均值
	 * @param fromIdx
	 * @param toIdx
	 */
	private float calcNoise(int fromIdx, int toIdx) {
		if (toIdx <= fromIdx) {
			return 0;
		}
		if (toIdx > noise.buffer.length) {
			toIdx = noise.buffer.length;
		}
		
		int offset = (toIdx - fromIdx) / 4;
		int end = toIdx - offset;
		int count = 0, sum = 0;
		for (int i = fromIdx + offset; i < end; i++) {
			count++;
			sum += noise.buffer[i];
		}
		
		return (float) sum / count;
	}

}
