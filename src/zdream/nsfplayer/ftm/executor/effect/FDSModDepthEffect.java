package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.channel.ChannelFDS;

/**
 * <p>控制 FDS 频率调制器, 该音源深度 (Modulation Depth) 的效果, Hxx
 * </p>
 * 
 * @author Zdream
 * @since v0.2.4
 */
public class FDSModDepthEffect implements IFtmEffect {
	
	public final int depth;

	private FDSModDepthEffect(int depth) {
		this.depth = depth;
	}

	@Override
	public FtmEffectType type() {
		return FtmEffectType.FDS_MOD_DEPTH;
	}
	
	/**
	 * 形成控制 FDS 频率调制器深度的效果
	 * @param depth
	 *   深度值. 范围: [0, 63]
	 * @return
	 *   效果实例
	 * @throws IllegalArgumentException
	 *   当音色值 <code>depth</code> 不在指定范围内时
	 */
	public static FDSModDepthEffect of(int depth) throws IllegalArgumentException {
		if (depth < 0 || depth > 63) {
			throw new IllegalArgumentException("深度必须是是 [0, 63] 范围内的整数");
		}
		return new FDSModDepthEffect(depth);
	}
	
	@Override
	public void execute(byte channelCode, FamiTrackerRuntime runtime) {
		if (channelCode != INsfChannelCode.CHANNEL_FDS) {
			throw new IllegalStateException("修改 FDS 频率调制器音源深度的效果只能在 FDS 轨道上触发, 无法在 "
					+ channelCode + " 轨道上触发.");
		}
		
		ChannelFDS ch = (ChannelFDS) runtime.channels.get(channelCode);
		ch.setModDepth(depth);
	}
	
	@Override
	public String toString() {
		return "Depth:" + depth;
	}
}
