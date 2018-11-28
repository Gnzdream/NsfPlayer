package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.channel.ChannelDPCM;

/**
 * <p>修改 DPCM 的 DAC 值
 * <p>该效果只在 DPCM 轨道上使用
 * </p>
 * 
 * @author Zdream
 * @since v0.2.3
 */
public class DPCM_DACSettingEffect implements IFtmEffect {
	
	/**
	 * 重置的 DAC 值
	 */
	public final int dac;

	private DPCM_DACSettingEffect(int dac) {
		this.dac = dac;
	}

	@Override
	public FtmEffectType type() {
		return FtmEffectType.DAC;
	}
	
	/**
	 * 形成一个修改 DPCM 的 DAC 值的效果
	 * @param dac
	 *   DAC 值. 必须在 [0, 127] 范围内
	 * @return
	 *   效果实例
	 * @throws IllegalArgumentException
	 *   当 <code>dac</code> 不在指定范围内时
	 */
	public static DPCM_DACSettingEffect of(int dac) throws IllegalArgumentException {
		if (dac > 127 || dac < 0) {
			throw new IllegalArgumentException("错误值: " + dac + ", DAC 必须是 0 - 127 之间的整数数值");
		}
		return new DPCM_DACSettingEffect(dac);
	}

	@Override
	public void execute(byte channelCode, FamiTrackerRuntime runtime) {
		if (channelCode != INsfChannelCode.CHANNEL_2A03_DPCM) {
			throw new IllegalStateException("修改 DAC 值的效果只能在 DPCM 轨道上触发, 无法在 "
					+ channelCode + " 轨道上触发.");
		}
		
		ChannelDPCM ch = (ChannelDPCM) runtime.channels.get(channelCode);
		ch.setDeltaCounter(dac);
	}
	
	@Override
	public String toString() {
		return "DAC:" + dac;
	}

}
