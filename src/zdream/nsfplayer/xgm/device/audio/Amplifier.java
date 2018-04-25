package zdream.nsfplayer.xgm.device.audio;

import zdream.nsfplayer.xgm.device.IRenderable0;

/**
 * 放大器
 * @author Zdream
 */
public class Amplifier implements IRenderable0 {
	protected IRenderable0 target;
	protected int mute = 0,
			volume = 64,
			threshold = 32767,
			weight = -1,
			pan = 0;
	protected double th = 20 * Math.log10((double) threshold);
	
	public Amplifier() {}
	
	protected int compress(int d) {
		if (weight < 0) {
			return d;
		}
		
		if (d > threshold)
			return threshold;
		else if (d < -threshold)
			return -threshold;
		else
			return d;
	}
	
	public void attach(IRenderable0 p) {
		this.target = p;
	}
	
	public void tick(int clocks) {
		target.tick(clocks);
	}
	
	/**
	 * @param bs
	 *   左频道和右频道的声音数据, 需要是 int[2]
	 * @return
	 */
	public int render(int[] bs) {
		if (mute > 0) {
			bs[0] = bs[1] = 0;
			return 2;
		}
		target.render(bs);
		bs[0] = compress ((bs[0] * volume) / 16);
		bs[1] = compress ((bs[1] * volume) / 16);

		return 2;
	}
	
	public void setVolume(int v) {
		this.volume = v;
		this.setCompress(threshold, weight);
	}
	
	public int getVolume() {
		return volume;
	}
	
	public void setMute(int mute) {
		this.mute = mute;
	}
	
	public int getMute() {
		return mute;
	}

	public void setCompress(int t, int w) {
		threshold = 32768 * t / 100;
		if (threshold < 32768)
			weight = 0;
		else
			weight = -1;
		th = 20 * Math.log10((double) threshold);
	}
	
	public void setPan(int pan) {
		this.pan = pan;
	}
	
	public int getPan() {
		return pan;
	}

}
