package com.zdream.nsfplayer.xgm.device.audio;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.zdream.nsfplayer.xgm.device.IRenderable;

public class Mixer implements IRenderable {
	
	int fadePos, fadeEnd;
	List<IRenderable> dlist = new ArrayList<IRenderable>();
	
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
	
	public final void attach(IRenderable dev) {
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
		for (Iterator<IRenderable> it = dlist.iterator(); it.hasNext();) {
			IRenderable r = it.next();
			r.tick(clocks);
		}
	}

	@Override
	public int render(int[] bs) {
		int[] tmp = new int[2];
		bs[1] = bs[0] = 0;

		for (Iterator<IRenderable> it = dlist.iterator(); it.hasNext();) {
			IRenderable r = it.next();
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
