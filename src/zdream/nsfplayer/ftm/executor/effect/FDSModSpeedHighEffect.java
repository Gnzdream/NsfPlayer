package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.channel.ChannelFDS;

/**
 * <p>设置 FDS 调制器频率高 4 位 (共 12 位) 的效果, Ixx
 * </p>
 * 
 * @author Zdream
 * @since v0.2.4
 */
public class FDSModSpeedHighEffect implements IFtmEffect {
	
	public final int freq;

	private FDSModSpeedHighEffect(int freq) {
		this.freq = freq;
	}

	@Override
	public FtmEffectType type() {
		return FtmEffectType.FDS_MOD_SPEED_HIGH;
	}
	
	/**
	 * 形成控制 FDS 调制器频率高 4 位的效果
	 * @param freq
	 *   深度值. 范围: [0, 15]
	 * @return
	 *   效果实例
	 * @throws IllegalArgumentException
	 *   当音色值 <code>freq</code> 不在指定范围内时
	 */
	public static FDSModSpeedHighEffect of(int freq) throws IllegalArgumentException {
		if (freq < 0 || freq > 15) {
			throw new IllegalArgumentException("频率 (高 4 位) 必须是是 [0, 15] 范围内的整数");
		}
		return new FDSModSpeedHighEffect(freq);
	}
	
	@Override
	public void execute(byte channelCode, FamiTrackerRuntime runtime) {
		if (channelCode != INsfChannelCode.CHANNEL_FDS) {
			throw new IllegalStateException("修改 FDS 调制器频率的效果只能在 FDS 轨道上触发, 无法在 "
					+ channelCode + " 轨道上触发.");
		}
		
		ChannelFDS ch = (ChannelFDS) runtime.channels.get(channelCode);
		ch.setModFreqHigh(freq);
	}
	
	@Override
	public String toString() {
		return "Freq High:" + Integer.toHexString(freq);
	}

}
