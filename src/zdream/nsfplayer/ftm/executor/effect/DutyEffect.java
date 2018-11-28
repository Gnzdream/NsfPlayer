package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;

/**
 * <p>修改音色的效果, Vxx
 * </p>
 * 
 * @author Zdream
 * @since 0.2.1
 */
public class DutyEffect implements IFtmEffect {
	
	public final int duty;

	private DutyEffect(int duty) {
		this.duty = duty;
	}

	@Override
	public FtmEffectType type() {
		return FtmEffectType.DUTY_CYCLE;
	}
	
	/**
	 * 形成一个修改音色的效果
	 * @param duty
	 *   音色值. 仅允许正数或 0.
	 * @return
	 *   效果实例
	 * @throws IllegalArgumentException
	 *   当音色值 <code>duty</code> 不在指定范围内时
	 */
	public static DutyEffect of(int duty) throws IllegalArgumentException {
		if (duty < 0) {
			throw new IllegalArgumentException("音色必须是非负数数值");
		}
		return new DutyEffect(duty);
	}
	
	@Override
	public void execute(byte channelCode, FamiTrackerRuntime runtime) {
		runtime.channels.get(channelCode).setMasterDuty(duty);
	}
	
	@Override
	public String toString() {
		return "Duty:" + duty;
	}

}
