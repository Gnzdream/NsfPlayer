package zdream.nsfplayer.xgm.device.sound;

import zdream.nsfplayer.xgm.device.TrackInfoBasic;

public class TrackInfoN106 extends TrackInfoBasic {
	
	public int wavelen;
	public int[] wave = new int[256];
	
	public TrackInfoN106 clone(){
		TrackInfoN106 t = new TrackInfoN106();
		super.clone(t);
		
		t.wavelen = wavelen;
		System.arraycopy(wave, 0, t.wave, 0, wave.length);
		return t;
	}
}
