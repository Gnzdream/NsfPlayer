package zdream.nsfplayer.sound.recorder;

import zdream.nsfplayer.sound.SoundPulse;

public class PulseRecorder {
	
	public PulseItem last;

	public PulseRecorder() {}
	
	public boolean hasChange(SoundPulse p) {
		return last.hasChange(p);
	}
	
	public void update(SoundPulse p, PulseItem item) {
		last.of(p);
		item.of(p);
	}

}
