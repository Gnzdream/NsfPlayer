package zdream.nsfplayer.ftm.executor.context;

import java.util.Map;

import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.ftm.audio.FamiTrackerQuerier;
import zdream.nsfplayer.ftm.executor.effect.FtmEffectType;
import zdream.nsfplayer.ftm.executor.effect.IFtmEffect;
import zdream.nsfplayer.ftm.format.FtmNote;

/**
 * Ftm 效果转换器接口, 用于将 {@link FtmNote} 转换成 {@link IFtmEffect} 集合.
 * 它是一个 FTM 运行环境中需要的一个工具.
 * 
 * @version v0.2.9
 *   将该类重新定义为工具而非组件, 因此实现该接口的子类不再强制实现 IFtmRuntimeHolder 接口
 * 
 * @author Zdream
 * @since v0.2.1
 */
public interface IFtmEffectConverter extends INsfChannelCode {
	
	/**
	 * 实现转化, 将效果以 {@link IFtmEffect} 形式转化出来后, 放到 effects 或 geffects 中
	 * @param note
	 *   音键实例
	 * @param channelType
	 *   该音键所在的轨道的类型. 见 {@link INsfChannelCode} 中以 CHANNEL_TYPE_ 开头的 byte 常量
	 * @param effects
	 *   该轨道的效果集
	 * @param geffects
	 *   全局轨道的效果集
	 * @param querier
	 *   查询器
	 * @since v0.2.9
	 */
	public void convert(
			FtmNote note,
			byte channelType,
			Map<FtmEffectType, IFtmEffect> effects,
			Map<FtmEffectType, IFtmEffect> geffects,
			FamiTrackerQuerier querier);

}
