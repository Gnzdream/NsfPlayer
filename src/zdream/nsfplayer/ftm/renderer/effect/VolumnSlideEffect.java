package zdream.nsfplayer.ftm.renderer.effect;

import zdream.nsfplayer.ftm.renderer.FamiTrackerRuntime;

/**
 * <p>随时间变化修改音量的效果, Axx
 * </p>
 * 
 * @author Zdream
 * @since 0.2.1
 */
public class VolumnSlideEffect implements IFtmEffect {
	
	public final int delta;

	private VolumnSlideEffect(int slide) {
		this.delta = slide;
	}

	@Override
	public FtmEffectType type() {
		return FtmEffectType.VOLUME_SLIDE;
	}
	
	/**
	 * 形成一个随时间变化修改音量的效果
	 * @param delta
	 *   变化量. 单位时间 (TODO ???) 变化的音量数, 范围 [-15, 15]
	 *   <br>正数, 则随时间变化音量不断增大;
	 *   <br>负数, 则随时间变化音量不断减小;
	 *   <br>0, 则音量不随时间变化而变化, 也可以禁掉原来作用在轨道上的随时间变化修改音量的效果;
	 * @return
	 *   效果实例
	 * @throws IllegalArgumentException
	 *   当变化量 <code>slide</code> 不在指定范围内时
	 */
	public static VolumnSlideEffect of(int delta) throws IllegalArgumentException {
		if (delta < -15 || delta > 15) {
			throw new IllegalArgumentException("音量变化量必须在 -15 到 15 之间");
		}
		return new VolumnSlideEffect(delta);
	}
	
	/**
	 * @return
	 * 是否是音量上升的效果
	 */
	public boolean slideUp() {
		return delta > 0;
	}
	
	/**
	 * @return
	 * 是否是音量下降的效果
	 */
	public boolean slideDown() {
		return delta < 0;
	}
	
	/**
	 * @return
	 * 是否是音量变化的效果
	 */
	public boolean slide() {
		return delta != 0;
	}
	
	@Override
	public void execute(byte channelCode, FamiTrackerRuntime runtime) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public String toString() {
		return "VolumnSlide:" + delta;
	}

}
