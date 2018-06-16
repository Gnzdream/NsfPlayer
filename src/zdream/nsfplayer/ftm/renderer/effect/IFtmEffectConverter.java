package zdream.nsfplayer.ftm.renderer.effect;

import zdream.nsfplayer.ftm.format.FtmNote;
import zdream.nsfplayer.ftm.renderer.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.renderer.IFtmRuntimeHolder;

/**
 * Ftm 效果转换器接口, 用于将 {@link FtmNote} 转换成 {@link ChannelEffectBatch} 集合
 * @author Zdream
 * @since 0.2.1
 */
public interface IFtmEffectConverter extends IFtmRuntimeHolder {
	
	/**
	 * 要实施转化的信号, 在 {@link #convert(byte, FtmNote)} 之前调用
	 */
	default public void beforeConvert() {
		clear();
	}
	
	/**
	 * 实现转化, 将效果以 {@link ChannelEffectBatch} 形式转化出来后, 放到 {@link FamiTrackerRuntime} 中
	 * @param channelCode
	 * @param note
	 */
	public void convert(byte channelCode, FtmNote note);
	
	/**
	 * 清空转化的数据
	 */
	public void clear();

}
