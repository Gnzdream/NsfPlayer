package zdream.nsfplayer.sound.xgm;

import zdream.nsfplayer.sound.mixer.IMixerChannel;
import zdream.nsfplayer.sound.mixer.SoundMixer;

/**
 * Nsf 的音频合成器
 * 
 * @author Zdream
 * @since 0.2.1
 */
public class NsfSoundMixer extends SoundMixer {

	public NsfSoundMixer() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void detachAll() {
		// TODO Auto-generated method stub

	}

	@Override
	public IMixerChannel allocateChannel(byte code) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IMixerChannel getMixerChannel(byte code) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int finishBuffer() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int readBuffer(short[] buf, int offset, int length) {
		// TODO Auto-generated method stub
		return 0;
	}

}
