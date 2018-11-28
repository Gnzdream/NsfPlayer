package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.channel.ChannelDPCM;

/**
 * <p>使 DPCM 的采样循环播放的效果.
 * 一个这样的效果实例最多只能多触发一次循环, 如果多次循环需要多次产生该效果的实例
 * <p>该效果只在 DPCM 轨道上使用
 * </p>
 * 
 * @author Zdream
 * @since v0.2.3
 */
public class DPCMRetriggerEffect implements IFtmEffect {
	
	/**
	 * 循环的时长. 过多久循环一遍, 单位: 帧
	 */
	public final int duration;

	private DPCMRetriggerEffect(int duration) {
		this.duration = duration;
	}

	@Override
	public FtmEffectType type() {
		return FtmEffectType.RETRIGGER;
	}
	
	/**
	 * 形成一个使 DPCM 的采样循环播放的效果
	 * @param duration
	 *   循环时长, 范围非负数
	 * @return
	 *   效果实例
	 * @throws IllegalArgumentException
	 *   当 <code>duration</code> 不在指定范围内时
	 */
	public static DPCMRetriggerEffect of(int duration) throws IllegalArgumentException {
		if (duration < 0) {
			throw new IllegalArgumentException("循环时长 duration 必须是非负整数数值");
		}
		return new DPCMRetriggerEffect(duration);
	}

	@Override
	public void execute(byte channelCode, FamiTrackerRuntime runtime) {
		if (channelCode != INsfChannelCode.CHANNEL_2A03_DPCM) {
			throw new IllegalStateException("修改采样起始读取位的效果只能在 DPCM 轨道上触发, 无法在 "
					+ channelCode + " 轨道上触发.");
		}
		
		ChannelDPCM ch = (ChannelDPCM) runtime.channels.get(channelCode);
		ch.setRetrigger(duration);
	}
	
	@Override
	public String toString() {
		return "Retrigger:" + duration;
	}

}
