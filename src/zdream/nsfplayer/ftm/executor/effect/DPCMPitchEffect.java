package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.channel.ChannelDPCM;

/**
 * <p>修改 DPCM 采样的音高
 * <p>该效果只在 DPCM 轨道上使用
 * </p>
 * 
 * @author Zdream
 * @since v0.2.3
 */
public class DPCMPitchEffect implements IFtmEffect {
	
	/**
	 * DPCM 采样的音高
	 */
	public final int pitch;

	private DPCMPitchEffect(int pitch) {
		this.pitch = pitch;
	}

	@Override
	public FtmEffectType type() {
		return FtmEffectType.DPCM_PITCH;
	}
	
	/**
	 * 形成一个修改 DPCM 采样音高的效果
	 * @param pitch
	 *   DPCM 采样的音高, 范围 [0, 15]
	 * @return
	 *   效果实例
	 * @throws IllegalArgumentException
	 *   当 <code>pitch</code> 不在指定范围内时
	 */
	public static DPCMPitchEffect of(int pitch) throws IllegalArgumentException {
		if (pitch < 0 || pitch > 15) {
			throw new IllegalArgumentException("音高 pitch 必须是 [0, 15] 范围内的整数");
		}
		return new DPCMPitchEffect(pitch);
	}

	@Override
	public void execute(byte channelCode, FamiTrackerRuntime runtime) {
		if (channelCode != INsfChannelCode.CHANNEL_2A03_DPCM) {
			throw new IllegalStateException("修改采样起始读取位的效果只能在 DPCM 轨道上触发, 无法在 "
					+ channelCode + " 轨道上触发.");
		}
		
		ChannelDPCM ch = (ChannelDPCM) runtime.channels.get(channelCode);
		ch.setMasterPitch(pitch);
	}
	
	@Override
	public String toString() {
		return "DPCMPitch:" + pitch;
	}

}
