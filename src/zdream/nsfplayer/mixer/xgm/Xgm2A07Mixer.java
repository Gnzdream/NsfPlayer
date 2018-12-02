package zdream.nsfplayer.mixer.xgm;

/**
 * 2A03 三角、噪音、DPCM 的合并轨道
 * 
 * @author Zdream
 * @since v0.2.3
 */
public class Xgm2A07Mixer extends AbstractXgmMultiMixer {
	
	final XgmAudioChannel noise, dpcm;
	final XgmLinearChannel tri;
	private boolean triEnable, noiseEnable, dpcmEnable;
	
	public Xgm2A07Mixer() {
		noise = new XgmAudioChannel();
		tri = new XgmLinearChannel();
		dpcm = new XgmAudioChannel();
	}
	
	@Override
	public void reset() {
		super.reset();
		tri.reset();
		noise.reset();
		dpcm.reset();
	}
	
	@Override
	public AbstractXgmAudioChannel getRemainAudioChannel(byte type) {
		switch (type) {
		case CHANNEL_TYPE_TRIANGLE:
			return (triEnable) ? null : tri;
		case CHANNEL_TYPE_NOISE:
			return (noiseEnable) ? null : noise;
		case CHANNEL_TYPE_DPCM:
			return (dpcmEnable) ? null : dpcm;
		}
		return null;
	}
	
	@Override
	public void setEnable(AbstractXgmAudioChannel channel, boolean enable) {
		if (channel == tri) {
			triEnable = enable;
		} else if (channel == noise) {
			noiseEnable = enable;
		} else if (channel == dpcm) {
			dpcmEnable = enable;
		}
	}
	
	@Override
	public boolean isEnable(AbstractXgmAudioChannel channel) {
		if (channel == tri) {
			return triEnable;
		} else if (channel == noise) {
			return noiseEnable;
		} else if (channel == dpcm) {
			return dpcmEnable;
		}
		return false;
	}
	
	@Override
	public void beforeRender() {
		super.beforeRender();
		if (triEnable)
			tri.beforeSubmit();
		if (noiseEnable)
			noise.beforeSubmit();
		if (dpcmEnable)
			dpcm.beforeSubmit();
	}
	
	@Override
	public int render(int index) {
		// volume adjusted by 0.75 based on empirical measurements
		// 音量乘上 0.75 是经验测量的结果 ——原 NsfPlayer 工程里面的注释
		// 8192.0 * 0.75 * 159.79 = 981749.76
		final double MASTER = 981750;
		/*
		 * ((MASTER) / (100.0 + 1.0 / ((double) t / 8227 + (double) n / 12241 + (double) d / 22638)));
		 */
		float v0 = (triEnable) ? tri.read(index) * tri.getLevel() / 8227 : 0;
		float v1 = (noiseEnable) ? noise.read(index) * noise.getLevel() / 12241 : 0;
		float v2 = (dpcmEnable) ? dpcm.read(index) * dpcm.getLevel() / 22638 : 0;
		float v = v0 + v1 + v2;
		int value = (v != 0) ? (int) ((MASTER) / (100.0 + 1.0 / v)) : 0;
		
//		int value = (int) (8192.0 * 0.75 *
//				(3.0 * tri.buffer[idx] * tri.getLevel()
//						+ 2.0 * noise.buffer[idx] * noise.getLevel()
//						+ dpcm.buffer[idx] * dpcm.getLevel()) / 208.0);
		return (intercept(value, 1));
	}
	
}
