package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;

/**
 * <p>修改速度 (speed) 的效果, Fxx
 * <p>属全局效果
 * </p>
 * 
 * @author Zdream
 * @since v0.2.1
 */
public class SpeedEffect implements IFtmEffect {
	
	public final int speed;

	private SpeedEffect(int speed) {
		this.speed = speed;
	}

	@Override
	public FtmEffectType type() {
		return FtmEffectType.SPEED;
	}
	
	/**
	 * 形成一个修改速度的效果
	 * @param speed
	 *   速度值. 速度值必须是正数
	 * @return
	 *   效果实例
	 * @throws IllegalArgumentException
	 *   当速度值 <code>speed</code> 不在指定范围内时
	 */
	public static SpeedEffect of(int speed) throws IllegalArgumentException {
		if (speed <= 0) {
			throw new IllegalArgumentException("速度必须是非负数数值");
		}
		return new SpeedEffect(speed);
	}
	
	@Override
	public void execute(byte channelCode, FamiTrackerRuntime runtime) {
		runtime.fetcher.setSpeed(speed);
	}
	
	@Override
	public String toString() {
		return "Speed:" + speed;
	}

}
