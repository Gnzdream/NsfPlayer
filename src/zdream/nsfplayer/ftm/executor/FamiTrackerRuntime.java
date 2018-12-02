package zdream.nsfplayer.ftm.executor;

import static zdream.nsfplayer.core.NsfChannelCode.typeOfChannel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import zdream.nsfplayer.ftm.audio.FamiTrackerQuerier;
import zdream.nsfplayer.ftm.audio.FtmAudio;
import zdream.nsfplayer.ftm.executor.context.ChannelDeviceSelector;
import zdream.nsfplayer.ftm.executor.context.DefaultFtmEffectConverter;
import zdream.nsfplayer.ftm.executor.context.IFtmEffectConverter;
import zdream.nsfplayer.ftm.executor.effect.FtmEffectType;
import zdream.nsfplayer.ftm.executor.effect.IFtmEffect;
import zdream.nsfplayer.ftm.executor.hook.IFtmExecutedListener;
import zdream.nsfplayer.ftm.executor.hook.IFtmFetchListener;
import zdream.nsfplayer.ftm.format.FtmNote;

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
	
	public FamiTrackerParameter param = new FamiTrackerParameter();
	
	/**
	 * <p>FTM 轨道.
	 * <p>发声器在轨道中, 可以使用 {@link AbstractFtmChannel#getSound()} 方法获得
	 * </p>
	 */
	public final HashMap<Byte, AbstractFtmChannel> channels = new HashMap<>();
	
	/* **********
	 *  初始化  *
	 ********** */
	
	public void init() {
		selector = new ChannelDeviceSelector();
		fetcher = new FtmRowFetcher(param);
		converter = new DefaultFtmEffectConverter();
	}
	
	/**
	 * 重置播放曲目、位置
	 * @param audio
	 *   播放的曲目
	 * @param track
	 *   曲目号, 从 0 开始
	 * @param section
	 *   段号, 从 0 开始
	 * @since v0.2.9
	 */
	public void ready(FtmAudio audio, int track, int section) {
		querier = new FamiTrackerQuerier(audio);
		fetcher.ready(querier, track, section);
		
		// 向 runtime.effects 中添加 map
		effects.clear();
		final int len = querier.channelCount();
		for (int i = 0; i < len; i++) {
			byte code = querier.channelCode(i);
			effects.put(code, new HashMap<>());
		}
	}
	
	/**
	 * 重置播放位置, 不重置曲目
	 * @param track
	 *   曲目号, 从 0 开始
	 * @param section
	 *   段号, 从 0 开始
	 * @since v0.2.9
	 */
	public void ready(int track, int section) {
		fetcher.ready(track, section);
		for (Map<FtmEffectType, IFtmEffect> map : effects.values()) {
			map.clear();
		}
		geffect.clear();
	}
	
	/* **********
	 *   工具   *
	 ********** */
	
	/**
	 * 行数据获取与播放位置解析工具
	 */
	public FtmRowFetcher fetcher;
	
	/**
	 * 键——效果转换器
	 */
	public IFtmEffectConverter converter;

	/**
	 * 查询器
	 */
	public FamiTrackerQuerier querier;
	
	/**
	 * 环境存储器
	 */
	public ChannelDeviceSelector selector;
	
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
	
	/**
	 * 清空效果集
	 * @since v0.2.9
	 */
	public void clearEffects() {
		for (Map<FtmEffectType, IFtmEffect> effect : effects.values()) {
			effect.clear();
		}
		geffect.clear();
	}
	
	/* **********
	 *   操作   *
	 ********** */
	
	/**
	 * 音乐向前跑一帧. 看看现在跑到 Ftm 的哪一行上
	 */
	public void runFrame() {
		// 重置
		param.finished = false;
		this.clearEffects();
		
		if (fetcher.doFrameUpdate()) {
			storeRow();
		}
	}
	
	/**
	 * 确定现在正在播放的行, 让 {@link IFtmEffectConverter} 获取并处理
	 */
	public void storeRow() {
		final int len = querier.channelCount();
		
		int trackIdx = param.trackIdx;
		int section = param.curSection;
		int row = param.curRow;
		
		for (int i = 0; i < len; i++) {
			byte channel = querier.channelCode(i);
			byte channelType = typeOfChannel(channel);
			
			FtmNote note = querier.getNote(trackIdx, section, i, row);
			note = onFetcher(note, channel);
			
			converter.convert(note, channelType, effects.get(channel), geffect, querier);
		}
	}
	
	/* **********
	 *  监听器  *
	 ********** */
	
	public final ArrayList<IFtmFetchListener> flners = new ArrayList<>();
	public final ArrayList<IFtmExecutedListener> elners = new ArrayList<>();
	
	/**
	 * 调用音键获取监听器
	 * @param note
	 *   原始音键, 可能为 null
	 * @param channelCode
	 *   当前轨道号
	 * @return
	 */
	FtmNote onFetcher(FtmNote note, byte channelCode) {
		if (flners.isEmpty()) {
			return note;
		}
		
		FtmNote n = (note != null) ? note.clone() : null;
		FamiTrackerExecutorHandler h = new FamiTrackerExecutorHandler(this);
		
		for (IFtmFetchListener l : flners) {
			try {
				n = l.onFetch(n, channelCode, h);
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		}
		h.destroy();
		
		return n;
	}
	
	/**
	 * 调用执行完毕监听器
	 */
	void onExecuteFinished() {
		if (elners.isEmpty()) {
			return;
		}
		
		FamiTrackerExecutorHandler h = new FamiTrackerExecutorHandler(this);
		elners.forEach(l -> l.onExecuteFinished(h));
	}
	
}
