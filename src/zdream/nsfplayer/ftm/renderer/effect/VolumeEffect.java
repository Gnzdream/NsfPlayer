package zdream.nsfplayer.ftm.renderer.effect;

import java.util.HashSet;

import zdream.nsfplayer.ftm.renderer.AbstractFtmChannel;
import zdream.nsfplayer.ftm.renderer.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.renderer.IFtmState;

/**
 * <p>修改音量的效果
 * <p>该效果在产生时还会重置随时间变化修改音量效果 (Axx), 但只清除累积数据
 * </p>
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
		AbstractFtmChannel ch = runtime.channels.get(channelCode);
		
		ch.setMasterVolume(volume);
		
		// 如果存在 Volume Slide 效果时, 将其重置 filterStates
		HashSet<IFtmState> set = ch.filterStates(VolumeSlideState.NAME);
		if (!set.isEmpty()) {
			set.forEach((state) -> {
				if (state instanceof VolumeSlideState) {
					((VolumeSlideState) state).resetAccumulation();
				}
			});
		}
	}
	
	@Override
	public String toString() {
		return "Vol:" + Integer.toHexString(volume);
	}

}
