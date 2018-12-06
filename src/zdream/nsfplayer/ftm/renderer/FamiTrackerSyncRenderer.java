package zdream.nsfplayer.ftm.renderer;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import zdream.nsfplayer.core.AbstractRenderer;
import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.core.NsfCommonParameter;
import zdream.nsfplayer.core.NsfPlayerApplication;
import zdream.nsfplayer.core.NsfPlayerException;
import zdream.nsfplayer.core.NsfRateConverter;
import zdream.nsfplayer.ftm.audio.FtmAudio;
import zdream.nsfplayer.ftm.executor.FamiTrackerExecutor;
import zdream.nsfplayer.mixer.IMixerConfig;
import zdream.nsfplayer.mixer.ISoundMixer;
import zdream.nsfplayer.mixer.xgm.XgmMixerConfig;

/**
 * <p>FamiTracker 同步音频渲染器.
 * <p>支持多个 {@link FtmAudio} 同时进行播放, 并补充部分停等协议.
 * <p>该渲染器是线程不安全的, 请注意不要在渲染途中设置参数.
 * </p>
 * 
 * @author Zdream
 * @since v0.3.1
 */
public class FamiTrackerSyncRenderer extends AbstractRenderer<FtmAudio>
		implements INsfChannelCode {
	
	/**
	 * 速率转换器
	 */
	private final NsfRateConverter rate;
	
	/**
	 * 音频混音器
	 */
	private ISoundMixer mixer;
	
	private FamiTrackerConfig config;
	
	private final NsfCommonParameter param = new NsfCommonParameter();
	
	public FamiTrackerSyncRenderer() {
		this(null);
	}
	
	public FamiTrackerSyncRenderer(FamiTrackerConfig config) {
		if (config == null) {
			this.config = new FamiTrackerConfig();
		} else {
			this.config = config.clone();
		}
		
		// 采样率数据只有渲染构建需要
		param.sampleRate = this.config.sampleRate;
		
		// 音量参数只有渲染构建需要
		param.levels.copyFrom(this.config.channelLevels);
		
		rate = new NsfRateConverter(param);
		initMixer();
		initExecutors();
	}
	
	private void initMixer() {
		IMixerConfig mixerConfig = config.mixerConfig;
		if (mixerConfig == null) {
			mixerConfig = new XgmMixerConfig();
		}
		
		this.mixer = NsfPlayerApplication.app.mixerFactory.create(mixerConfig, param);
	}
	
	/* **********
	 * 准备部分 *
	 ********** */

	/**
	 * <p>让该渲染器的主执行器读取对应的 audio 数据, 设置播放暂停位置为曲目 0 的开头.
	 * <p>该渲染有且只有一个主执行器. 该执行器如果正在渲染其它的音频时,
	 * 就会打断并直接转至新的 audio 的开头位置. 对于其它的执行器, 不受任何影响.
	 * </p>
	 * @param audio
	 *   需要渲染的音频数据
	 * @see #updateAudio(int, FtmAudio, int, int, int, boolean)
	 */
	public void ready(FtmAudio audio) {
		updateAudio(masterExecutorId, audio, 0, 0, 0, true);
	}
	
	/**
	 * <p>让该渲染器的主执行器读取对应的 audio 数据, 设置播放暂停位置为指定曲目的开头.
	 * <p>该渲染有且只有一个主执行器. 该执行器如果正在渲染其它的音频时,
	 * 就会打断并直接转至新的 audio 的开头位置. 对于其它的执行器, 不受任何影响.
	 * </p>
	 * @param audio
	 *   需要渲染的音频数据
	 * @param track
	 *   曲目号, 从 0 开始
	 * @see #updateAudio(int, FtmAudio, int, int, int, boolean)
	 */
	public void ready(FtmAudio audio, int track) {
		updateAudio(masterExecutorId, audio, track, 0, 0, true);
	}
	
	/**
	 * <p>让该渲染器的主执行器读取对应的 audio 数据, 设置播放暂停位置为指定曲目的指定位置.
	 * <p>该渲染有且只有一个主执行器. 该执行器如果正在渲染其它的音频时,
	 * 就会打断并直接转至新的 audio 的开头位置. 对于其它的执行器, 不受任何影响.
	 * </p>
	 * @param audio
	 *   需要渲染的音频数据
	 * @param track
	 *   曲目号, 从 0 开始
	 * @param section
	 *   段号, 从 0 开始
	 * @see #updateAudio(int, FtmAudio, int, int, int, boolean)
	 */
	public void ready(
			FtmAudio audio,
			int track,
			int section) {
		updateAudio(masterExecutorId, audio, track, section, 0, true);
	}
	
	/**
	 * <p>让该渲染器的主执行器读取对应的 audio 数据, 设置播放暂停位置为指定曲目的指定位置.
	 * <p>该渲染有且只有一个主执行器. 该执行器如果正在渲染其它的音频时,
	 * 就会打断并直接转至新的 audio 的开头位置. 对于其它的执行器, 不受任何影响.
	 * </p>
	 * @param audio
	 *   需要渲染的音频数据
	 * @param track
	 *   曲目号, 从 0 开始
	 * @param section
	 *   段号, 从 0 开始
	 * @param row
	 *   行号, 从 0 开始
	 * @see #updateAudio(int, FtmAudio, int, int, int, boolean)
	 */
	public void ready(
			FtmAudio audio,
			int track,
			int section,
			int row) {
		updateAudio(masterExecutorId, audio, track, section, row, true);
	}
	
	/**
	 * <p>让该渲染器的主执行器读取对应的 audio 数据, 设置播放暂停位置为指定曲目的开头.
	 * <p>该渲染有且只有一个主执行器. 该执行器如果正在渲染其它的音频时,
	 * 就会打断并直接转至新的 audio 的开头位置. 对于其它的执行器, 不受任何影响.
	 * </p>
	 * @param audio
	 *   需要渲染的音频数据
	 * @param track
	 *   曲目号, 从 0 开始
	 * @see #updateAudio(int, int, int, int)
	 */
	public void ready(int track) {
		updateAudio(masterExecutorId, track, 0, 0);
	}
	
	/**
	 * <p>让该渲染器的主执行器读取对应的 audio 数据, 设置播放暂停位置为指定曲目的指定位置.
	 * <p>该渲染有且只有一个主执行器. 该执行器如果正在渲染其它的音频时,
	 * 就会打断并直接转至新的 audio 的开头位置. 对于其它的执行器, 不受任何影响.
	 * </p>
	 * @param audio
	 *   需要渲染的音频数据
	 * @param track
	 *   曲目号, 从 0 开始
	 * @param section
	 *   段号, 从 0 开始
	 * @see #updateAudio(int, int, int, int)
	 */
	public void ready(
			int track,
			int section) {
		updateAudio(masterExecutorId, track, section, 0);
	}
	
	/**
	 * <p>让该渲染器的主执行器读取对应的 audio 数据, 设置播放暂停位置为指定曲目的指定位置.
	 * <p>该渲染有且只有一个主执行器. 该执行器如果正在渲染其它的音频时,
	 * 就会打断并直接转至新的 audio 的开头位置. 对于其它的执行器, 不受任何影响.
	 * </p>
	 * @param audio
	 *   需要渲染的音频数据
	 * @param track
	 *   曲目号, 从 0 开始
	 * @param section
	 *   段号, 从 0 开始
	 * @param row
	 *   行号, 从 0 开始
	 * @see #updateAudio(int, int, int, int)
	 */
	public void ready(
			int track,
			int section,
			int row) {
		updateAudio(masterExecutorId, track, section, row);
	}

	@Override
	public boolean isFinished() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected int renderFrame() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int skipFrame() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setSpeed(float speed) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public float getSpeed() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/* **********
	 * 分配轨道 *
	 ********** */
	
	/**
	 * 该渲染器需要管理多个 {@link FamiTrackerExecutor},
	 * 这里储存相应的信息.
	 * @author Zdream
	 */
	class ExecutorParam {
		final int id;
		
		FamiTrackerExecutor executor;
		FtmAudio audio;
		
		/**
		 * 是否已经停止
		 */
		boolean stop;
		/**
		 * 是否正在启用
		 */
		boolean enable;
		
		public ExecutorParam(int id) {
			this.id = id;
		}
	}
	
	/**
	 * 每个轨道的信息
	 * @author Zdream
	 */
	class ChannelParam {
		/**
		 * 轨道标识号
		 */
		final int id;
		/**
		 * 该轨道由哪个执行器执行的
		 */
		int executorId;
		/**
		 * 轨道号.
		 * 该渲染器中, 同一种 executorId 和 channelCode 的组合最多只有一个
		 */
		byte channelCode;
		/**
		 * 混音器对应的轨道号
		 */
		int mixerId = -1;
		
		public ChannelParam(int id) {
			this.id = id;
		}
	}
	
	/**
	 * <p>主执行器的标识号.
	 * <p>渲染器至少有一个执行器, 该执行器为主执行器. 默认为 0 号对应的执行器.
	 * 设置为主执行器的执行器不允许被删除, 除非主执行器发生变化.
	 * </p>
	 */
	int masterExecutorId;
	ExecutorParam[] eParams;
	ChannelParam[] cParams;
	
	void initExecutors() {
		masterExecutorId = 0;
		
		eParams = new ExecutorParam[1];
		eParams[0] = new ExecutorParam(0);
		eParams[0].executor = new FamiTrackerExecutor();
		
		cParams = new ChannelParam[0];
	}
	
	/**
	 * <p>为一个新的 audio 分配一个新的执行器.
	 * <p>如果主执行器没有启动, 优先分配给主执行器.
	 * </p>
	 * @param audio
	 *   音频数据
	 * @return
	 */
	public int allocate(FtmAudio audio) {
		// 主执行器
		ExecutorParam p = eParams[masterExecutorId];
		if (!p.enable) {
			updateAudio(masterExecutorId, audio, 0, 0, 0, true);
			return masterExecutorId;
		}
		
		// TODO 新的执行器
		
		return -1;
	}
	
	/**
	 * <p>为一个新的 audio 分配一个新的执行器, 并指定哪些轨道需要渲染. 其它的所有轨道将设置为不渲染
	 * <p>如果主执行器没有启动, 优先分配给主执行器.
	 * </p>
	 * @param audio
	 *   音频数据
	 * @param channelCodes
	 *   所有需要渲染的 NSF 轨道号
	 * @return
	 */
	public int allocate(FtmAudio audio, byte... channelCodes) {
		// 主执行器
		ExecutorParam p = eParams[masterExecutorId];
		if (!p.enable) {
			updateAudio(masterExecutorId, audio, 0, 0, 0, channelCodes);
			return masterExecutorId;
		}
		
		// TODO 新的执行器
		
		return -1;
		
	}
	
	/**
	 * <p>为一个已经在工作的执行器, 指定哪个轨道需要补充渲染.
	 * 如果这个轨道已经在渲染了, 返回该轨道的轨道标识号.
	 * 否则创建一个新的轨道, 返回新轨道的轨道标识号
	 * </p>
	 * @param exeId
	 *   执行器标识号
	 * @param channelCode
	 *   NSF 的轨道号
	 * @return
	 *   轨道标识号
	 * @throws NsfPlayerException
	 *   当不存在 exeId 对应的执行器时
	 */
	public int allocate(int exeId, byte channelCode) {
		checkExecutorId(exeId);
		
		// 查询部分
		for (ChannelParam cp : this.cParams) {
			if (cp == null) {
				continue;
			}
			if (cp.executorId == exeId && cp.channelCode == channelCode) {
				return cp.id;
			}
		}
		
		// 创建一个
		ChannelParam cp = createChannelParam(1)[0];
		cp.executorId = exeId;
		cp.channelCode = channelCode;
		mixerChannelConnect0(cp.id);
		
		return cp.id;
	}
	
	/**
	 * <p>为一个已经在工作的执行器, 指定哪个轨道需要回收.
	 * 如果这个轨道正在渲染了, 删除并返回 true.
	 * 否则返回 false
	 * </p>
	 * @param exeId
	 *   执行器标识号
	 * @param channelCode
	 *   NSF 的轨道号
	 * @return
	 *   是否删除了一个原有的轨道
	 * @throws NsfPlayerException
	 *   当不存在 exeId 对应的执行器时
	 */
	public boolean free(int exeId, byte channelCode) {
		checkExecutorId(exeId);
		
		// 查询部分
		for (ChannelParam cp : this.cParams) {
			if (cp == null) {
				continue;
			}
			if (cp.executorId == exeId && cp.channelCode == channelCode) {
				mixerChannelDisconnect0(cp.id);
				freeChannel0(cp.id);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * <p>指定哪个已经存在的轨道进行回收.
	 * </p>
	 * @param channelId
	 *   轨道标识号
	 * @throws NsfPlayerException
	 *   当不存在 channelId 对应的轨道时
	 */
	public void free(int channelId) {
		checkChannelId(channelId);
		
		ChannelParam cp = cParams[channelId];
		mixerChannelDisconnect0(cp.id);
		freeChannel0(cp.id);
	}
	
	/**
	 * <p>删除一个正在工作的执行器, 并且该执行器的所有轨道将回收, 并不再渲染.
	 * </p>
	 * @param exeId
	 *   执行器标识号
	 * @throws NsfPlayerException
	 *   当不存在 exeId 对应的执行器时
	 */
	public void remove(int exeId) {
		checkExecutorId(exeId);
		
		// TODO 其它规则如果涉及到它的, 要进行更改
		
		if (exeId == masterExecutorId) {
			this.eParams[masterExecutorId].enable = false;
			this.eParams[masterExecutorId].audio = null;
		} else {
			// TODO 删除执行器
		}
	}
	
	/**
	 * @return
	 *   所有 executorId 组成的列表
	 */
	public int[] getAllExeutorId() {
		int[] ret = new int[eParams.length];
		int nextIdx = 0;
		
		for (ExecutorParam ep : eParams) {
			if (ep != null) {
				ret[nextIdx++] = ep.id;
			}
		}
		
		if (nextIdx != ret.length) {
			ret = Arrays.copyOf(ret, nextIdx);
		}
		return ret;
	}
	
	/**
	 * 获取指定执行器、轨道号的轨道对应的轨道标识号
	 * @param exeId
	 *   执行器标识号
	 * @param channelCode
	 *   NSF 的轨道号
	 * @return
	 *   轨道标识号. 如果没有, 返回 -1
	 * @throws NsfPlayerException
	 *   当不存在 exeId 对应的执行器时
	 */
	public int getChannelId(int exeId, byte channelCode) {
		checkExecutorId(exeId);
		
		// 查询部分
		for (ChannelParam cp : this.cParams) {
			if (cp == null) {
				continue;
			}
			if (cp.executorId == exeId && cp.channelCode == channelCode) {
				return cp.id;
			}
		}
		
		return -1;
	}
	
	/**
	 * <p>为指定的执行器更新播放音频位置, 从指定位置开始播放,
	 * 且不修改已有的轨道配置
	 * <p>第一次播放时需要指定 Ftm 音频数据.
	 * 因此第一次需要调用含 {@link FtmAudio} 参数的重载方法
	 * </p>
	 * @param exeId
	 *   执行器标识号
	 * @param track
	 *   曲目号, 从 0 开始
	 * @param section
	 *   段号, 从 0 开始
	 * @param track
	 *   行号, 从 0 开始
	 * @throws NullPointerException
	 *   当先前没有指定音频数据时
	 */
	public void updateAudio(
			int exeId,
			int track,
			int section,
			int row) {
		ExecutorParam ep = this.eParams[exeId];
		ep.executor.ready(track, section);
		
		resetMixer();
	}
	
	/**
	 * 为指定的执行器更新音频, 并从指定位置开始播放
	 * @param exeId
	 *   执行器标识号
	 * @param audio
	 *   音频数据, 不为 null
	 * @param track
	 *   曲目号, 从 0 开始
	 * @param section
	 *   段号, 从 0 开始
	 * @param track
	 *   行号, 从 0 开始
	 * @param remapperChannel
	 *   是否需要重新分配轨道. 如果重新分配轨道, 就按照 audio 所需要的轨道进行重新分配
	 * @throws NullPointerException
	 *   当 <tt>audio == null</tt> 时
	 */
	public void updateAudio(
			int exeId,
			FtmAudio audio,
			int track,
			int section,
			int row,
			boolean remapperChannel) {
		requireNonNull(audio, "audio = null");
		
		ExecutorParam ep = this.eParams[exeId];
		
		// 所有 channelId
		ChannelParam[] cps = channelsOfExecutor(exeId);
		// 断开它们与 mixer 的连接
		for (ChannelParam cp : cps) {
			mixerChannelDisconnect0(cp.id);
		}
		
		if (remapperChannel) {
			// 回收它们
			for (ChannelParam cp : cps) {
				freeChannel0(cp.id);
			}

			ep.executor.ready(audio, track, section);
			ep.audio = audio;
			
			// 分配 audio 的所有轨道
			Set<Byte> channels = ep.executor.allChannelSet();
			cps = this.createChannelParam(channels.size());
			
			int index = 0;
			for (byte channelCode : channels) {
				ChannelParam cp = cps[index++];
				cp.executorId = exeId;
				cp.channelCode = channelCode;
			}
		} else {
			ep.executor.ready(audio);
			ep.audio = audio;
		}
		
		// 重建它们与 mixer 的连接
		for (ChannelParam cp : cps) {
			mixerChannelConnect0(cp.id);
		}
	}
	
	/**
	 * 为指定的执行器更新音频, 并设置哪些轨道需要渲染
	 * @param exeId
	 *   执行器标识号
	 * @param audio
	 *   音频数据, 不为 null
	 * @param track
	 *   曲目号, 从 0 开始
	 * @param section
	 *   段号, 从 0 开始
	 * @param track
	 *   行号, 从 0 开始
	 * @param channelCodes
	 *   NSF 轨道号, 表示哪些轨道需要重新渲染
	 * @throws NullPointerException
	 *   当 <tt>audio == null</tt> 时
	 */
	public void updateAudio(
			int exeId,
			FtmAudio audio,
			int track,
			int section,
			int row,
			byte... channelCodes) {
		requireNonNull(audio, "audio = null");
		
		ExecutorParam ep = this.eParams[exeId];
		
		// 所有 channelId
		ChannelParam[] cps = channelsOfExecutor(exeId);
		// 断开它们与 mixer 的连接
		for (ChannelParam cp : cps) {
			mixerChannelDisconnect0(cp.id);
		}
		
		// 回收它们
		for (ChannelParam cp : cps) {
			freeChannel0(cp.id);
		}

		ep.executor.ready(audio, track, section);
		ep.audio = audio;
		
		// 分配 audio 的所有轨道
		Set<Byte> channels = new HashSet<>();
		for (byte channelCode : channelCodes) {
			channels.add(channelCode);
		}
		cps = this.createChannelParam(channels.size());
		
		int index = 0;
		for (byte channelCode : channels) {
			ChannelParam cp = cps[index++];
			cp.executorId = exeId;
			cp.channelCode = channelCode;
		}
		
		// 重建它们与 mixer 的连接
		for (ChannelParam cp : cps) {
			mixerChannelConnect0(cp.id);
		}
		
	}
	
	private void checkExecutorId(int exeId) {
		if (exeId >= eParams.length || exeId < 0 || eParams[exeId] == null) {
			throw new NsfPlayerException("不存在 " + exeId + " 对应的执行器");
		}
	}
	
	private void checkChannelId(int channelId) {
		if (channelId >= cParams.length || channelId < 0 || cParams[channelId] == null) {
			throw new NsfPlayerException("不存在 " + channelId + " 对应的轨道号");
		}
	}
	
	/**
	 * 返回指定执行器的所有轨道
	 * @param exeId
	 *   执行器标识号
	 * @return
	 */
	private ChannelParam[] channelsOfExecutor(int exeId) {
		ChannelParam cp;
		ArrayList<ChannelParam> list = new ArrayList<>();
		for (int i = 0; i < cParams.length; i++) {
			cp = cParams[i];
			if (cp == null) {
				continue;
			}
			if (cp.executorId == exeId) {
				list.add(cp);
			}
		}
		return list.toArray(new ChannelParam[list.size()]);
	}
	
	/**
	 * 分配轨道参数类
	 * @param count
	 *   个数, 大于0. 也是返回的 ChannelParam 数组的长度
	 */
	private ChannelParam[] createChannelParam(final int count) {
		ChannelParam[] cps = new ChannelParam[count];
		int[] idxs = new int[count];
		int nextIdx = 0;
		
		// 查询部分
		int len = cParams.length;
		SERCHING: {
			for (int i = 0; i < len; i++) {
				if (cParams[i] == null) {
					idxs[nextIdx++] = i;
					if (nextIdx == count) {
						break SERCHING;
					}
				}
			}
			
			for (int i = len;; i++) {
				idxs[nextIdx++] = i;
				if (nextIdx == count) {
					break SERCHING;
				}
			}
		}
		
		// 重新为 cParams 分配数组大小
		int maxLen = idxs[count - 1];
		if (maxLen > len) {
			cParams = Arrays.copyOf(cParams, maxLen);
		}

		// 创建部分
		for (int i = 0; i < idxs.length; i++) {
			int id = idxs[i];
			cps[i] = cParams[id] = new ChannelParam(id);
		}
		
		return cps;
	}
	
	/**
	 * 删除 mixer 的连接
	 * @param channelId
	 */
	private void mixerChannelDisconnect0(int channelId) {
		ChannelParam cp = this.cParams[channelId];
		// TODO 回收 mixer 的轨道
		
		cp.mixerId = -1;
	}
	
	/**
	 * 创建 mixer 的连接
	 * @param channelId
	 */
	private void mixerChannelConnect0(int channelId) {
		ChannelParam cp = this.cParams[channelId];
		
		// TODO 创建 mixer 的轨道
		
		// 修改 cp.mixerId
		cp.mixerId = -1;
	}
	
	/**
	 * 回收轨道 id 的 ChannelParam
	 * @param channelId
	 */
	private void freeChannel0(int channelId) {
		this.cParams[channelId] = null;
		
		if (channelId + 1 == cParams.length) {
			// cParams 数组大小需要改变
			int newLen = 0;
			for (int i = channelId - 1; i >= 0; i--) {
				if (cParams[i] != null) {
					newLen = i + 1;
					break;
				}
			}
			
			cParams = Arrays.copyOf(cParams, newLen);
		}
	}
	
	/* **********
	 *   重置   *
	 ********** */
	
	/**
	 * 重置 Mixer
	 */
	private void resetMixer() {
		mixer.reset();
	}

}
