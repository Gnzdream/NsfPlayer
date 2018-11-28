package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.channel.ChannelFDS;

/**
 * <p>设置 FDS 调制器频率低 8 位 (共 12 位) 的效果, Jxx
 * </p>
 * 
 * @author Zdream
 * @since v0.2.4
 */
public class FDSModSpeedLowEffect implements IFtmEffect {
	
	public final int freq;

	private FDSModSpeedLowEffect(int freq) {
		this.freq = freq;
	}

	@Override
	public FtmEffectType type() {
		return FtmEffectType.FDS_MOD_SPEED_LOW;
	}
	
	/**
	 * 形成控制 FDS 调制器频率低 8 位的效果
	 * @param freq
	 *   深度值. 范围: [0, 255]
	 * @return
	 *   效果实例
	 * @throws IllegalArgumentException
	 *   当音色值 <code>freq</code> 不在指定范围内时
	 */
	public static FDSModSpeedLowEffect of(int freq) throws IllegalArgumentException {
		if (freq < 0 || freq > 255) {
			throw new IllegalArgumentException("频率 (低 8 位) 必须是是 [0, 255] 范围内的整数");
		}
		return new FDSModSpeedLowEffect(freq);
	}
	
	@Override
	public void execute(byte channelCode, FamiTrackerRuntime runtime) {
		if (channelCode != INsfChannelCode.CHANNEL_FDS) {
			throw new IllegalStateException("修改 FDS 调制器频率的效果只能在 FDS 轨道上触发, 无法在 "
					+ channelCode + " 轨道上触发.");
		}
		
		ChannelFDS ch = (ChannelFDS) runtime.channels.get(channelCode);
		ch.setModFreqLow(freq);
	}
	
	@Override
	public String toString() {
		return String.format("Freq Low:%02x", freq);
	}

}
