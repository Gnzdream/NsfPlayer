package zdream.nsfplayer.xgm.device.misc;

import zdream.nsfplayer.nsf.device.IDevice;

public interface ILoopDetector extends IDevice {
	
	public boolean isLooped (int timeInMs, int matchSecond, int matchInterval);
	
	public int getLoopStart();
	
	public int getLoopEnd();
	
	public boolean isEmpty();

}
