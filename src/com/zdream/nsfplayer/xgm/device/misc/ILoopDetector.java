package com.zdream.nsfplayer.xgm.device.misc;

import com.zdream.nsfplayer.nsf.device.IDevice;

public interface ILoopDetector extends IDevice {
	
	public boolean isLooped (int timeInMs, int matchSecond, int matchInterval);
	
	public int getLoopStart();
	
	public int getLoopEnd();
	
	public boolean isEmpty();

}
