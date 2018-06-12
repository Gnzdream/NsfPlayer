package zdream.nsfplayer.ftm.renderer.effect;

import zdream.nsfplayer.ftm.renderer.FamiTrackerRuntime;

/**
 * 修改音量的效果
 * 
 * @author Zdream
 * @since 0.2.1
 */
public class VolumnEffect implements IFtmEffect {
	
	public int volumn;

	private VolumnEffect(int volumn) {
		this.volumn = volumn;
	}

	@Override
	public final FtmEffectType type() {
		return FtmEffectType.VOLUME;
	}
	
	/**
	 * 形成一个修改音量的效果
	 * @param volumn
	 *   音量值. 音量值必须在 [0, 15] 范围内
	 * @return
	 *   效果实例
	 * @throws IllegalArgumentException
	 *   当音量值 <code>volumn</code> 不在指定范围内时
	 */
	public static VolumnEffect of(int volumn) throws IllegalArgumentException {
		if (volumn > 15 || volumn < 0) {
			throw new IllegalArgumentException("音量必须是 0 - 15 之间的整数数值");
		}
		return new VolumnEffect(volumn);
	}
	
	@Override
	public void execute(byte channelCode, FamiTrackerRuntime rumtime) {
		// TODO Auto-generated method stub
		IFtmEffect.super.execute(channelCode, rumtime);
	}

}
