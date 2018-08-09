package zdream.nsfplayer.sound.mixer;

import zdream.nsfplayer.sound.IResetable;

public interface IMixerChannel extends IResetable {

	void mix(int value, int time);

}
