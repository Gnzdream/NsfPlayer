package zdream.nsfplayer.sound.mixer;

import zdream.nsfplayer.sound.AbstractNsfSound;
import zdream.nsfplayer.sound.IResetable;

/**
 * 音频合成器
 * @author Zdream
 * @since 0.2.1
 */
public abstract class SoundMixer implements IResetable {

	public SoundMixer() {
		
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}
	
	/* **********
	 * 音频管道 *
	 ********** */
	
	/**
	 * <p>调用该方法后, 所有与发声器 {@link AbstractNsfSound} 相连的音频管道全部拆开, 不再使用.
	 * </p>
	 */
	public abstract void detachAll();

}
