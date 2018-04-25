package zdream.nsfplayer.xgm.device.audio;

import java.util.Arrays;

/**
 * 中值滤波器
 * @author Zdream
 */
public class MedianFilter {
	
	private int[] tap;
	private int tapIndex;
	private boolean dirty;
	
	public MedianFilter(int tapSize) {
		this.tap = new int[tapSize];
	}
	
	public final void reset() {
		Arrays.fill(tap, 0);
		dirty = false;
		tapIndex = 0;
	}
	
	public final void put(int data) {
		tapIndex = (tapIndex + 1) % tap.length;
		tap[tapIndex] = data;
		dirty = true;
	}
	
	public final int get() {
		if (dirty) {
			Arrays.sort(tap);
			dirty = false;
		}
		return tap[tap.length >> 1];
	}

}
