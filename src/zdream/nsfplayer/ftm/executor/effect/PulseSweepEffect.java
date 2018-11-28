package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.core.NsfChannelCode;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.channel.Channel2A03Pulse;

/**
 * <p>随时间向上或者向下扫音的效果, Hxy, Ixy
 * <p>当扫音在工作时, 所有其它更改音键、音高的效果全部暂时失效, 直到扫音效果结束.
 * <p>该效果只会存在于 2A03 矩形波轨道上
 * </p>
 * 
 * @see PulseSweepState
 * 
 * @author Zdream
 * @since v0.2.9
 */
public class PulseSweepEffect implements IFtmEffect {
	
	/**
	 * 表示每隔多少单位时间段扫音频率变化, 有效值 [0, 7]
	 */
	public final int period;
	
	/**
	 * 表示每个时间段扫音频率的变化量参数, 有效值 [0, 7]
	 */
	public final int shift;
	
	/**
	 * 升音 (Hxy) 为 true, 降音 (Ixy) 为 false
	 */
	public final boolean mode;

	private PulseSweepEffect(int period, int shift, boolean mode) {
		this.period = period;
		this.shift = shift;
		this.mode = mode;
	}
	
	/**
	 * 形成一个随时间向上或者向下扫音的效果
	 * @param period
	 *   表示每隔多少单位时间段扫音频率变化, 有效值 [0, 7]
	 * @param shift
	 *   表示每个时间段扫音频率的变化量参数, 有效值 [0, 7]
	 * @param mode
	 *   模式. 说明该扫音是向上滑动还是向下滑动.
	 *   <br>true, 说明扫音向上滑动;
	 *   <br>false, 说明扫音向下滑动;
	 * @return
	 *   效果实例
	 * @throws IllegalArgumentException
	 *   当滑动速度 <code>speed</code> 不在指定范围内时
	 */
	public static PulseSweepEffect of(int period, int shift, boolean mode) throws IllegalArgumentException {
		if (period < 0 || period > 7) {
			throw new IllegalArgumentException("音符滑动单位时间 period 必须是 0 - 7 之间的整数数值");
		}
		if (shift < 0 || shift > 7) {
			throw new IllegalArgumentException("音符滑动量 shift 必须是 0 - 7 之间的整数数值");
		}
		return new PulseSweepEffect(period, shift, mode);
	}

	@Override
	public FtmEffectType type() {
		return FtmEffectType.SWEEP;
	}

	@Override
	public void execute(byte channelCode, FamiTrackerRuntime runtime) {
		if (NsfChannelCode.typeOfChannel(channelCode) != NsfChannelCode.CHANNEL_TYPE_PULSE) {
			throw new IllegalStateException("扫音效果只能在 2A03 Pulse 轨道上触发, 无法在 "
					+ channelCode + " 轨道上触发.");
		}
		
		Channel2A03Pulse ch = (Channel2A03Pulse) runtime.channels.get(channelCode);
		ch.setSweep(period, mode, shift);
		
		// 守护状态
		ch.addState(new PulseSweepState());
	}
	
	@Override
	public String toString() {
		return "Sweep:" + (mode ? "up#" : "down#") + period + "#" + mode;
	}
	
}
