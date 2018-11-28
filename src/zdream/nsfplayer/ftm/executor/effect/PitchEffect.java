package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.format.FtmNote;

/**
 * <p>修改音高 (pitch) 的效果, Pxx
 * </p>
 * 
 * @author Zdream
 * @since 0.2.1
 */
public class PitchEffect implements IFtmEffect {
	
	public final int pitch;

	private PitchEffect(int pitch) {
		this.pitch = pitch;
	}

	@Override
	public FtmEffectType type() {
		return FtmEffectType.PITCH;
	}
	
	/**
	 * 形成一个修改速度的效果
	 * @param pitch
	 *   音高值. 允许正数、负数, 或 0.<br>
	 *   在 {@link FtmNote} 中表示的 0x80 就是音高为 0 的情况.
	 * @return
	 *   效果实例
	 */
	public static PitchEffect of(int pitch) {
		return new PitchEffect(pitch);
	}
	
	@Override
	public void execute(byte channelCode, FamiTrackerRuntime runtime) {
		runtime.channels.get(channelCode).setMasterPitch(pitch);
	}
	
	@Override
	public String toString() {
		return "Pitch:" + pitch;
	}

}
