package zdream.nsfplayer.ftm.renderer.effect;

import zdream.nsfplayer.ftm.renderer.FamiTrackerRuntime;

/**
 * 修改音量的效果
 * 
 * @author Zdream
 * @since 0.2.1
 */
public class VolumeEffect implements IFtmEffect {
	
	public final int volume;

	private VolumeEffect(int volume) {
		this.volume = volume;
	}

	@Override
	public final FtmEffectType type() {
		return FtmEffectType.VOLUME;
	}
	
	/**
	 * 形成一个修改音量的效果
	 * @param volume
	 *   音量值. 音量值必须在 [0, 15] 范围内
	 * @return
	 *   效果实例
	 * @throws IllegalArgumentException
	 *   当音量值 <code>volumn</code> 不在指定范围内时
	 */
	public static VolumeEffect of(int volume) throws IllegalArgumentException {
		if (volume > 15 || volume < 0) {
			throw new IllegalArgumentException("音量必须是 0 - 15 之间的整数数值");
		}
		return new VolumeEffect(volume);
	}
	
	@Override
	public void execute(byte channelCode, FamiTrackerRuntime runtime) {
		runtime.channels.get(channelCode).setMasterVolume(volume);
	}
	
	@Override
	public String toString() {
		return "Vol:" + Integer.toHexString(volume);
	}

}
