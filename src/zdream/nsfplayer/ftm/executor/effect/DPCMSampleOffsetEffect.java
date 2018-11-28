package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.channel.ChannelDPCM;

/**
 * <p>修改 DPCM 采样的起始读取位置
 * <p>该效果只在 DPCM 轨道上使用
 * </p>
 * 
 * @author Zdream
 * @since v0.2.3
 */
public class DPCMSampleOffsetEffect implements IFtmEffect {
	
	/**
	 * 起始读取位, 以 byte 为单位
	 */
	public final int offset;

	private DPCMSampleOffsetEffect(int offset) {
		this.offset = offset;
	}

	@Override
	public FtmEffectType type() {
		return FtmEffectType.SAMPLE_OFFSET;
	}
	
	/**
	 * 形成一个修改 DPCM 采样的起始读取位的效果
	 * @param offset
	 *   起始读取位. 如果是 FTM 读取文件产生的效果, 这个值一般都是 64 的整数倍.
	 *   FamiTracker 原工程里面的解释是, 由于硬件设施的限制. 好吧.
	 *   <br>范围: 非负数即可
	 * @return
	 *   效果实例
	 * @throws IllegalArgumentException
	 *   当 <code>offset</code> 不在指定范围内时
	 */
	public static DPCMSampleOffsetEffect of(int offset) throws IllegalArgumentException {
		if (offset < 0) {
			throw new IllegalArgumentException("起始读取位 offset 必须是非负整数数值");
		}
		return new DPCMSampleOffsetEffect(offset);
	}

	@Override
	public void execute(byte channelCode, FamiTrackerRuntime runtime) {
		if (channelCode != INsfChannelCode.CHANNEL_2A03_DPCM) {
			throw new IllegalStateException("修改采样起始读取位的效果只能在 DPCM 轨道上触发, 无法在 "
					+ channelCode + " 轨道上触发.");
		}
		
		ChannelDPCM ch = (ChannelDPCM) runtime.channels.get(channelCode);
		ch.setOffset(offset);
	}
	
	@Override
	public String toString() {
		return "SampleOffset:" + offset;
	}

}
