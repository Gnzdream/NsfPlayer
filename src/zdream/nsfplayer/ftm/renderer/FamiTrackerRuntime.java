package zdream.nsfplayer.ftm.renderer;

import java.util.HashMap;
import java.util.Map;

import zdream.nsfplayer.ftm.document.FamiTrackerQuerier;
import zdream.nsfplayer.ftm.renderer.effect.FtmEffectType;
import zdream.nsfplayer.ftm.renderer.effect.IFtmEffect;
import zdream.nsfplayer.ftm.renderer.effect.IFtmEffectConverter;
import zdream.nsfplayer.sound.AbstractNsfSound;
import zdream.nsfplayer.sound.mixer.SoundMixer;

/**
 * Famitracker 运行时状态
 * 
 * @author Zdream
 * @since v0.2.1
 */
public class FamiTrackerRuntime {
	
	/* **********
	 *   成员   *
	 ********** */
	public FtmRowFetcher fetcher;
	public IFtmEffectConverter converter;
	
	/**
	 * FTM 轨道
	 */
	public final HashMap<Byte, AbstractFtmChannel> channels = new HashMap<>();
	
	/**
	 * 发声器
	 */
	public final HashMap<Byte, AbstractNsfSound> sounds = new HashMap<>();
	
	/**
	 * 音频合成器
	 */
	public final SoundMixer mixer = new SoundMixer();
	
	/* **********
	 *   工具   *
	 ********** */

	/**
	 * 查询器.
	 * <br>由 Fetcher 生成, 管理
	 */
	public FamiTrackerQuerier querier;
	
	/* **********
	 *   数据   *
	 ********** */
	/**
	 * <p>放着正解释的行里面的所有键的效果集合, 已经经过加工和分拣.
	 * <p>结构: 轨道号 - 效果集合 (可能为空)
	 * </p>
	 */
	public final HashMap<Byte, Map<FtmEffectType, IFtmEffect>> effects = new HashMap<>();
	
	/**
	 * 全局范围的效果集
	 */
	public final Map<FtmEffectType, IFtmEffect> geffect = new HashMap<>();
	
}
