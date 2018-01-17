package com.zdream.nsfplayer.xgm.device.audio;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.zdream.nsfplayer.xgm.device.IRenderable0;

public class Mixer implements IRenderable0 {
	
	/**
	 * 在 1 首歌播放即将结束后, 会进入渐出阶段. 这个参数将记录从开始渐出算起, 歌曲已经播放的采样数.<br>
	 * 如果该参数为 0, 则说明歌曲播放未进入渐出阶段.
	 */
	int fadePos;
	
	/**
	 * 表示渐出需要的总时间 (采样数).<br>
	 * 歌曲播放未进入渐出阶段时, 这个参数为 1. 一般为无效值;
	 * 当歌曲播放进入渐出阶段, 这个数值将计算真正的有效值.
	 */
	int fadeEnd;
	
	List<IRenderable0> dlist = new ArrayList<IRenderable0>();
	
	public Mixer() {
		reset();
	}
	
	public final void reset() {
		fadePos = 0;
		fadeEnd = 1;
	}
	
	public final void detachAll() {
		dlist.clear();
	}
	
	public final void attach(IRenderable0 dev) {
		dlist.add(dev);
	}
	
	public final boolean isFadeEnd() {
		return (fadePos >= fadeEnd);
	}
	
	public final boolean isFading() {
		return (fadePos > 0);
	}
	
	public void fadeStart(double rate, int fadeInMs) {
		if (fadeInMs != 0) {
			double samples = (double) fadeInMs * rate / 1000.0;
			if (samples < Integer.MAX_VALUE) {
				fadeEnd = (int) (samples);
			} else {
				fadeEnd = Integer.MAX_VALUE;
			}
		} else {
			fadeEnd = 1;
		}
		fadePos = 1; // begin fade
	}
	
	public final void skip(int length) {
		if (fadePos > 0) {
			if (fadePos < fadeEnd)
				++fadePos;
			else
				fadePos = fadeEnd;
		}
	}

	@Override
	public void tick(int clocks) {
		for (Iterator<IRenderable0> it = dlist.iterator(); it.hasNext();) {
			IRenderable0 r = it.next();
			r.tick(clocks);
		}
	}

	@Override
	public int render(int[] bs) {
		int[] tmp = new int[2];
		bs[1] = bs[0] = 0;

		for (Iterator<IRenderable0> it = dlist.iterator(); it.hasNext();) {
			IRenderable0 r = it.next();
			r.render(tmp);
			bs[0] += tmp[0];
			bs[1] += tmp[1];
		}

		if (fadePos > 0) {
			double fadeAmount = (double) (fadeEnd - fadePos) / (double) (fadeEnd);
			bs[0] = (int) (fadeAmount * bs[0]);
			bs[1] = (int) (fadeAmount * bs[1]);

			if (fadePos < fadeEnd)
				++fadePos;
			else
				fadePos = fadeEnd;
		}
		return 2;
	}

}
