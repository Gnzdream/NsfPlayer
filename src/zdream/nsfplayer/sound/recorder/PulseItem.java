package zdream.nsfplayer.sound.recorder;

import zdream.nsfplayer.sound.SoundPulse;

public class PulseItem {
	
	public boolean enable = false;
	public int period = -1;
	public int duty = -1;
	public int volume = -1;
	
	public PulseItem of(SoundPulse sound) {
		period = sound.period;
		duty = sound.dutyLength;
		volume = sound.fixedVolume;
		enable = sound.isEnable();
		return this;
	}
	
	public boolean hasChange(SoundPulse p) {
		return period != p.period || duty != p.dutyLength
				|| volume != p.fixedVolume || enable != p.isEnable();
	}
	
	public PulseItem clone() {
		return this.clone(new PulseItem());
	}
	
	public PulseItem clone(PulseItem item) {
		item.enable = this.enable;
		item.period = this.period;
		item.duty = this.duty;
		item.volume = this.volume;
		return item;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + duty;
		result = prime * result + (enable ? 1231 : 1237);
		result = prime * result + period;
		result = prime * result + volume;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return equals((PulseItem) obj);
	}
	
	public boolean equals(PulseItem p) {
		return period == p.period && duty == p.duty
				&& volume == p.volume && enable == p.enable;
	}

}
