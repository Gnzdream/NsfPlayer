package zdream.nsfplayer.xgm.device.audio;

import java.util.Arrays;

import zdream.nsfplayer.xgm.device.IRenderable0;

public class EchoUnit implements IRenderable0 {
	
	protected int rate;
	protected int[] echoBuf = new int[1 << 17];
	protected int[] h = new int[32];
	protected int eidx, edelay;
	Filter lpf = new Filter();
	DCFilter hpf = new DCFilter();
	
	public final void reset() {
		// int[16]
		final int[] hdef = new int[] { 0, 0, 0, 0, 64, 32, 16, 8, 32, 16, 8, 4, 16, 8, 4, 2, };
		eidx = 0;
		Arrays.fill(echoBuf, 0);
		for (int i = 0; i < 16; i++) {
			h[i] = hdef[i];
		}
		lpf.setParam(4700, 100);
		lpf.reset();
		hpf.setParam(270, 100);
		hpf.reset();
	}
	
	public final void setRate(double rate) {
		edelay = ((int) rate) / 16;
		lpf.setRate(rate);
		hpf.setRate(rate);
	}
	
	/**
	 * 
	 * @param bs
	 *   长度为 2 的数组
	 * @return
	 */
	public final int fastRender(int[] bs) {
		int[] buf = new int[2];
		
		int tmp = eidx;
		for (int i = 0; i < 16; i++) {
			echoBuf[tmp & ((1 << 17) - 1)] += (bs[0] * h[i]) >> 8;
			tmp += edelay;
		}

		buf[0] = buf[1] = echoBuf[eidx];
		lpf.fastRender(buf);
		hpf.fastRender(buf);
		echoBuf[eidx] = 0;
		eidx = (eidx + 1) & ((1 << 17) - 1);

		bs[0] += buf[0];
		bs[1] += buf[1];

		return 2;
	}

	@Override
	public int render(int[] bs) {
		return fastRender(bs);
	}

	@Override
	public void tick(int clocks) {
		lpf.tick(clocks);
	}

}
