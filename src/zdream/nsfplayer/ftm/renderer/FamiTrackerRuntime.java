package zdream.nsfplayer.ftm.renderer;

import java.util.HashMap;
import java.util.Map;

import zdream.nsfplayer.ftm.FamiTrackerSetting;
import zdream.nsfplayer.ftm.document.FamiTrackerQuerier;
import zdream.nsfplayer.ftm.renderer.effect.FtmEffectType;
import zdream.nsfplayer.ftm.renderer.effect.IFtmEffect;
import zdream.nsfplayer.ftm.renderer.effect.IFtmEffectConverter;
import zdream.nsfplayer.ftm.renderer.mixer.FtmSoundMixer;
import zdream.nsfplayer.ftm.renderer.tools.FamiTrackerParameter;
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
	public FamiTrackerSetting setting;
	public FamiTrackerParameter param = new FamiTrackerParameter(this);
	
	/**
	 * <p>FTM 轨道.
	 * <p>发声器在轨道中, 可以使用 {@link AbstractFtmChannel#getSound()} 方法获得
	 * </p>
	 */
	public final HashMap<Byte, AbstractFtmChannel> channels = new HashMap<>();
	
	/**
	 * 音频合成器
	 */
	public SoundMixer mixer;
	
	void init() {
		mixer = new FtmSoundMixer(this);
		
		param.init();
		mixer.init();
	}
	
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
	
	public void resetAllChannels() {
		channels.forEach((channelCode, ch) -> ch.reset());
	}
	
}
