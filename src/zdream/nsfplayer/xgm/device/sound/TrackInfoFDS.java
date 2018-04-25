package zdream.nsfplayer.xgm.device.sound;

import zdream.nsfplayer.xgm.device.ITrackInfo;
import zdream.nsfplayer.xgm.device.TrackInfoBasic;

public class TrackInfoFDS extends TrackInfoBasic {
	
	public int[] wave = new int[64];

	@Override
	public ITrackInfo clone() {
		TrackInfoFDS o = new TrackInfoFDS();
		super.clone(o);
		System.arraycopy(wave, 0, o.wave, 0, wave.length);
		return o;
	}
	
}
