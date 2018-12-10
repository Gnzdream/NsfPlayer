package zdream.nsfplayer.ftm.renderer;

import static java.util.Objects.requireNonNull;
import static zdream.nsfplayer.core.NsfStatic.BASE_FREQ_NTSC;

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
import zdream.nsfplayer.core.NsfStatic;
import zdream.nsfplayer.ftm.audio.FtmAudio;
import zdream.nsfplayer.ftm.executor.FamiTrackerExecutor;
import zdream.nsfplayer.ftm.executor.hook.IFtmExecutedListener;
import zdream.nsfplayer.ftm.executor.hook.IFtmFetchListener;
import zdream.nsfplayer.mixer.EmptyMixerChannel;
import zdream.nsfplayer.mixer.IMixerConfig;
import zdream.nsfplayer.mixer.ISoundMixer;
import zdream.nsfplayer.mixer.xgm.XgmMixerConfig;
import zdream.nsfplayer.sound.AbstractNsfSound;

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
	 * 速率转换器, 也给主执行器使用
	 */
	private final NsfRateConverter rate;
	
	/**
	 * 音频混音器
	 */
	private ISoundMixer mixer;
	
	private FamiTrackerConfig config;
	
	/**
	 * 参数, 也给主执行器使用
	 */
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
		// 采用实际的 frameRate.
		// 帧率实际以主执行器的帧率为准, 其它的执行器将强制使用此帧率.
		param.frameRate = NsfStatic.FRAME_RATE_NTSC;
		param.freqPerSec = NsfStatic.BASE_FREQ_NTSC;
		
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
		this.mixer.reset();
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
		eParams[masterExecutorId].enable = true;
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
		eParams[masterExecutorId].enable = true;
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
		eParams[masterExecutorId].enable = true;
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
		eParams[masterExecutorId].enable = true;
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

	/**
	 * 当所有渲染器均停止, 标志着曲目渲染完成.
	 */
	public boolean isFinished() {
		for (ExecutorParam ep : eParams) {
			if (ep == null || !ep.enable) {
				continue;
			}
			if (!ep.stop) {
				return false;
			}
		}
		return true;
	}
	
	/* **********
	 * 渲染部分 *
	 ********** */

	/**
	 * <p>所有未停止的执行器渲染一帧.
	 * 已经停止的执行器将进入停止状态, 直到用户重置、唤醒或者删除.
	 * </p>
	 * @return
	 *   本函数已渲染的采样数 (按单声道计算)
	 */
	protected int renderFrame() {
		int ret = countNextFrame();
		param.sampleInCurFrame = ret;
		rate.doConvert();
		mixer.readyBuffer();
		
		handleDelay();
		tickExeutors();
		triggerSounds();
		
		// 从 mixer 中读取数据
		readMixer();
		
		return ret;
	}

	@Override
	protected int skipFrame() {
		int ret = countNextFrame();
		param.sampleInCurFrame = ret;
		rate.doConvert();
		
		tickExeutors();
		
		return ret;
	}
	
	/**
	 * 让所有未停止的执行器执行一帧, 并判断是否停止, 如果是, 更新停止状态
	 */
	private void tickExeutors() {
		for (ExecutorParam ep : eParams) {
			if (ep == null || !ep.enable || ep.stop) {
				continue;
			}
			ep.executor.tick();
			if (ep.executor.isFinished()) {
				ep.stop = true;
			}
		}
	}
	
	/**
	 * <p>处理延迟写. 后一个轨道比前一个轨道晚 100 时钟写入数据.
	 * <p>由于每个轨道的触发时间不同可以有效避免轨道之间共振情况的发生,
	 * 因此这里需要采用轨道先后写入数据的方式.
	 * </p>
	 * @see #triggerSounds()
	 */
	private void handleDelay() {
		int delay = 0;
		final int clock = param.freqPerFrame;
		for (int i = 0; i < this.cParams.length; i++) {
			ChannelParam cp = cParams[i];
			if (cp == null) {
				continue;
			}
			
			ExecutorParam ep = eParams[cp.executorId];
			if (!ep.enable || ep.stop) {
				continue;
			}
			
			byte channelCode = cp.channelCode;
			AbstractNsfSound s = ep.executor.getSound(channelCode);
			s.process(cp.delay = delay);
			
			delay += 100;
			if (delay >= clock) {
				delay = clock - 1;
			}
		}
	}
	
	/**
	 * <p>让发声器逐个进行工作.
	 * <p>工作的时钟数, 为该帧需要工作的时钟数, 减去延迟时钟数.
	 * </p>
	 * @see #handleDelay()
	 */
	private void triggerSounds() {
		final int clock = param.freqPerFrame;
		for (int i = 0; i < this.cParams.length; i++) {
			ChannelParam cp = cParams[i];
			if (cp == null) {
				continue;
			}
			
			ExecutorParam ep = eParams[cp.executorId];
			if (!ep.enable || ep.stop) {
				continue;
			}
			
			byte channelCode = cp.channelCode;
			AbstractNsfSound s = ep.executor.getSound(channelCode);
			s.process(clock - cp.delay);
			s.endFrame();
			cp.delay = 0;
		}
	}
	
	/**
	 * 从 Mixer 中读取音频数据
	 */
	private void readMixer() {
		mixer.finishBuffer();
		mixer.readBuffer(data, 0, data.length);
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
		/**
		 * 轨道延迟. 单位 clock
		 */
		int delay;
		
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
	 * 原有正在渲染的曲目不终止, 不暂停, 与该新的执行器同时执行.
	 * <p>如果主执行器没有启动, 优先分配给主执行器.
	 * </p>
	 * @param audio
	 *   音频数据
	 * @return
	 */
	public int allocateAll(FtmAudio audio) {
		return allocateAll(audio, 0, 0, 0);
	}
	
	/**
	 * <p>为一个新的 audio 分配一个新的执行器, 指定该开始渲染的曲目.
	 * 原有正在渲染的曲目不终止, 不暂停, 与该新的执行器同时执行.
	 * <p>如果主执行器没有启动, 优先分配给主执行器.
	 * </p>
	 * @param audio
	 *   音频数据
	 * @param track
	 *   曲目号, 从 0 开始
	 * @return
	 */
	public int allocateAll(FtmAudio audio, int track) {
		return allocateAll(audio, track, 0, 0);
	}
	
	/**
	 * <p>为一个新的 audio 分配一个新的执行器, 指定该开始渲染的曲目以及初始段.
	 * 原有正在渲染的曲目不终止, 不暂停, 与该新的执行器同时执行.
	 * <p>如果主执行器没有启动, 优先分配给主执行器.
	 * </p>
	 * @param audio
	 *   音频数据
	 * @param track
	 *   曲目号, 从 0 开始
	 * @param section
	 *   段号, 从 0 开始
	 * @return
	 */
	public int allocateAll(FtmAudio audio, int track, int section) {
		return allocateAll(audio, track, section, 0);
	}
	
	/**
	 * <p>为一个新的 audio 分配一个新的执行器, 指定该开始渲染的曲目以及初始行.
	 * 原有正在渲染的曲目不终止, 不暂停, 与该新的执行器同时执行.
	 * <p>如果主执行器没有启动, 优先分配给主执行器.
	 * </p>
	 * @param audio
	 *   音频数据
	 * @param track
	 *   曲目号, 从 0 开始
	 * @param section
	 *   段号, 从 0 开始
	 * @param row
	 *   行号, 从 0 开始
	 * @return
	 */
	public int allocateAll(FtmAudio audio, int track, int section, int row) {
		// 主执行器
		ExecutorParam p = eParams[masterExecutorId];
		if (!p.enable) {
			updateAudio(masterExecutorId, audio, track, section, row, true);
			p.enable = true;
			return masterExecutorId;
		}
		
		// 新的执行器
		p = createExecutorParam();
		p.audio = audio;
		updateAudio(p.id, audio, track, section, row, true);
		
		// 锁定频率
		p.executor.lockFrameRate(param.frameRate);
		// 重置入采样计数器
		
		return p.id;
	}
	
	/**
	 * <p>为一个新的 audio 分配一个新的执行器, 并指定哪些轨道需要渲染. 其它的所有轨道将设置为不渲染.
	 * 原有正在渲染的曲目不终止, 不暂停, 与该新的执行器同时执行.
	 * <p>如果主执行器没有启动, 优先分配给主执行器.
	 * </p>
	 * @param audio
	 *   音频数据
	 * @param channelCodes
	 *   所有需要渲染的 NSF 轨道号
	 * @return
	 */
	public int allocate(FtmAudio audio, byte... channelCodes) {
		return allocate(audio, 0, 0, 0, channelCodes);
	}
	
	/**
	 * <p>为一个新的 audio 分配一个新的执行器, 指定该开始渲染的曲目.
	 * 此外, 指定哪些轨道需要渲染, 其它的所有轨道将设置为不渲染.
	 * 原有正在渲染的曲目不终止, 不暂停, 与该新的执行器同时执行.
	 * <p>如果主执行器没有启动, 优先分配给主执行器.
	 * </p>
	 * @param audio
	 *   音频数据
	 * @param channelCodes
	 *   所有需要渲染的 NSF 轨道号
	 * @return
	 */
	public int allocate(FtmAudio audio, int track, byte... channelCodes) {
		return allocate(audio, track, 0, 0, channelCodes);
	}
	
	/**
	 * <p>为一个新的 audio 分配一个新的执行器, 指定该开始渲染的曲目以及初始段.
	 * 此外, 指定哪些轨道需要渲染, 其它的所有轨道将设置为不渲染.
	 * 原有正在渲染的曲目不终止, 不暂停, 与该新的执行器同时执行.
	 * <p>如果主执行器没有启动, 优先分配给主执行器.
	 * </p>
	 * @param audio
	 *   音频数据
	 * @param channelCodes
	 *   所有需要渲染的 NSF 轨道号
	 * @return
	 */
	public int allocate(FtmAudio audio, int track, int section, byte... channelCodes) {
		return allocate(audio, track, section, 0, channelCodes);
	}
	
	/**
	 * <p>为一个新的 audio 分配一个新的执行器, 指定该开始渲染的曲目以及初始行.
	 * 此外, 指定哪些轨道需要渲染, 其它的所有轨道将设置为不渲染.
	 * 原有正在渲染的曲目不终止, 不暂停, 与该新的执行器同时执行.
	 * <p>如果主执行器没有启动, 优先分配给主执行器.
	 * </p>
	 * @param audio
	 *   音频数据
	 * @param channelCodes
	 *   所有需要渲染的 NSF 轨道号
	 * @return
	 */
	public int allocate(
			FtmAudio audio,
			int track,
			int section,
			int row,
			byte... channelCodes) {
		// 主执行器
		ExecutorParam p = eParams[masterExecutorId];
		if (!p.enable) {
			updateAudio(masterExecutorId, audio, track, section, row, channelCodes);
			p.enable = true;
			return masterExecutorId;
		}
		
		// 新的执行器
		p = createExecutorParam();
		p.audio = audio;
		updateAudio(p.id, audio, track, section, row, channelCodes);
		
		// 需要锁定频率
		p.executor.lockFrameRate(param.frameRate);
		
		return p.id;
	}
	
	/**
	 * <p>为一个已经在工作的执行器, 指定哪个轨道需要补充渲染.
	 * 如果这个轨道已经在渲染了, 返回 false.
	 * 否则创建一个新的轨道, 返回 true
	 * </p>
	 * @param exeId
	 *   执行器标识号
	 * @param channelCode
	 *   NSF 的轨道号
	 * @return
	 *   是否产生新的轨道. true 表示产生了新的轨道, false 表示原来该轨道已经存在
	 * @throws NsfPlayerException
	 *   当不存在 exeId 对应的执行器时
	 */
	public boolean allocate(int exeId, byte channelCode) {
		getExecutorParam(exeId);
		
		// 查询部分
		for (ChannelParam cp : this.cParams) {
			if (cp == null) {
				continue;
			}
			if (cp.executorId == exeId && cp.channelCode == channelCode) {
				return false;
			}
		}
		
		// 创建一个
		ChannelParam cp = createChannelParam(1)[0];
		cp.executorId = exeId;
		cp.channelCode = channelCode;
		mixerChannelConnect(cp.id);
		
		return true;
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
		getExecutorParam(exeId);
		
		// 查询部分
		for (ChannelParam cp : this.cParams) {
			if (cp == null) {
				continue;
			}
			if (cp.executorId == exeId && cp.channelCode == channelCode) {
				mixerChannelDisconnect(cp.id);
				removeChannelParam(cp.id);
				return true;
			}
		}
		return false;
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
		getExecutorParam(exeId);
		
		// TODO 其它规则如果涉及到它的, 要进行更改
		
		// 删除执行器
		removeExecutorParam(exeId);
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
		getExecutorParam(exeId);
		
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
	 * 询问指定执行器、轨道号的轨道是否已经在渲染了
	 * @param exeId
	 *   执行器标识号
	 * @param channelCode
	 *   NSF 的轨道号
	 * @return
	 *   指定轨道是否正在渲染. 如果已经在渲染了, 返回 true
	 * @throws NsfPlayerException
	 *   当不存在 exeId 对应的执行器时
	 */
	public boolean isAllocated(int exeId, byte channelCode) {
		return getChannelId(exeId, channelCode) != -1;
	}
	
	/**
	 * <p>为指定的执行器更新播放音频位置, 从指定曲目开始播放,
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
	public void updateAudio(int exeId, int track) {
		ExecutorParam ep = this.eParams[exeId];
		ep.executor.ready(track);
	}
	
	/**
	 * <p>为指定的执行器更新播放音频位置, 从指定段开始播放,
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
	 * @throws NullPointerException
	 *   当先前没有指定音频数据时
	 */
	public void updateAudio(
			int exeId,
			int track,
			int section) {
		ExecutorParam ep = this.eParams[exeId];
		ep.executor.ready(track, section);
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
	 * @param row
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
		ep.executor.ready(track, section, row);
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
	 * @param row
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
			mixerChannelDisconnect(cp.id);
		}
		
		if (remapperChannel) {
			// 回收它们
			for (ChannelParam cp : cps) {
				removeChannelParam(cp.id);
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
			mixerChannelConnect(cp.id);
		}
		
		// 帧率
		if (exeId == masterExecutorId) {
			param.frameRate = ep.executor.getFrameRate();
			onFrameRateUpdated();
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
	 * @param row
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
			mixerChannelDisconnect(cp.id);
		}
		
		// 回收它们
		for (ChannelParam cp : cps) {
			removeChannelParam(cp.id);
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
			mixerChannelConnect(cp.id);
		}
		
		// 帧率
		if (exeId == masterExecutorId) {
			param.frameRate = ep.executor.getFrameRate();
			onFrameRateUpdated();
		}
	}
	
	/**
	 * 获取执行器参数. 如果没有对应的执行器, 抛出 NsfPlayerException 异常.
	 * 该方法也用来检查 exeId 是否合理
	 */
	private ExecutorParam getExecutorParam(int exeId) {
		if (exeId >= eParams.length || exeId < 0 || eParams[exeId] == null) {
			throw new NsfPlayerException("不存在 " + exeId + " 对应的执行器");
		}
		return eParams[exeId];
	}
	
	/**
	 * 获取轨道参数. 如果没有对应的轨道, 返回 null.
	 */
	private ChannelParam getChannelParam(int exeId, byte channelCode) {
		for (ChannelParam cp : this.cParams) {
			if (cp == null) {
				continue;
			}
			if (cp.executorId == exeId && cp.channelCode == channelCode) {
				return cp;
			}
		}
		return null;
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
	 * 创建一个新的执行参数类
	 * @return
	 *   执行参数类实体
	 */
	private ExecutorParam createExecutorParam() {
		int i;
		for (i = 0; i < eParams.length; i++) {
			ExecutorParam ep = eParams[i];
			if (ep == null) {
				ep = eParams[i] = new ExecutorParam(i);
				ep.enable = true;
				ep.executor = new FamiTrackerExecutor();
				return ep;
			}
		}
		
		eParams = Arrays.copyOf(eParams, i + 1);
		ExecutorParam ep = eParams[i] = new ExecutorParam(i);
		ep.enable = true;
		ep.executor = new FamiTrackerExecutor();
		return ep;
	}
	
	/**
	 * <p>删除一个已有的执行参数类, 以及它下属的所有相关轨道.
	 * <p>如果是主执行器, 仅清空轨道, 将执行器置为【未使用】状态, 而不会删除.
	 * </p>
	 * @param exeId
	 *   执行器标识号
	 */
	private void removeExecutorParam(int exeId) {
		// 清空轨道
		for (ChannelParam cp : cParams) {
			if (cp.executorId == exeId) {
				mixerChannelDisconnect(cp.id);
				this.removeChannelParam(cp.id);
			}
		}
		
		// 处理执行参数类
		if (exeId == masterExecutorId) {
			this.eParams[masterExecutorId].enable = false;
			this.eParams[masterExecutorId].audio = null;
		} else {
			this.eParams[exeId] = null;
			if (exeId + 1 == eParams.length) {
				// cParams 数组大小需要改变
				int newLen = 0;
				for (int i = exeId - 1; i >= 0; i--) {
					if (eParams[i] != null) {
						newLen = i + 1;
						break;
					}
				}
				
				eParams = Arrays.copyOf(eParams, newLen);
			}
		}
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
		int maxLen = idxs[count - 1] + 1;
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
	private void mixerChannelDisconnect(int channelId) {
		ChannelParam cp = this.cParams[channelId];
		
		// 回收 mixer 的轨道
		mixer.detach(cp.mixerId);
		cp.mixerId = -1;
		
		ExecutorParam ep = eParams[cp.executorId];
		ep.executor.getSound(cp.channelCode).setOut(EmptyMixerChannel.INSTANCE);
	}
	
	/**
	 * 创建 mixer 的连接
	 * @param channelId
	 */
	private void mixerChannelConnect(int channelId) {
		ChannelParam cp = this.cParams[channelId];
		
		// 创建 mixer 的轨道
		int mixerId = mixer.allocateChannel(cp.channelCode);
		cp.mixerId = mixerId;
		
		ExecutorParam ep = eParams[cp.executorId];
		ep.executor.getSound(cp.channelCode).setOut(mixer.getMixerChannel(mixerId));
	}
	
	/**
	 * 回收轨道 id 的 ChannelParam
	 * @param channelId
	 */
	private void removeChannelParam(int channelId) {
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
	 * <p>当帧率发生变化的时候调用.
	 * <p>以下事件产生时, 帧率会发生变化:
	 * 当主渲染器更换音频时.
	 * <p>该方法工作内容是, 所有非主执行器将强制锁定该帧率、重置帧率和采样的相关计算
	 * </p>
	 */
	private void onFrameRateUpdated() {
		int frameRate = param.frameRate;
		for (ExecutorParam ep : this.eParams) {
			if (ep.id != masterExecutorId) {
				ep.executor.lockFrameRate(frameRate);
			}
		}
		
		resetCounterParam(frameRate, param.sampleRate);
		clearBuffer();
		rate.onParamUpdate(frameRate, BASE_FREQ_NTSC);
	}
	
	/* **********
	 *  仪表盘  *
	 ********** */
	
	/**
	 * <p>询问指定执行器, 当前帧是否更新了行.
	 * <p>无论是手动切换执行位置, 还是执行器自动切换下一行,
	 * 均认为是更新了行
	 * </p>
	 * @param exeId
	 *   执行器标识号
	 * @return
	 *   true, 如果指定执行器当前帧更新了行
	 */
	public boolean isRowUpdated(int exeId) {
		ExecutorParam ep = getExecutorParam(exeId);
		return ep.executor.isRowUpdated();
	}

	/**
	 * <p>为一个已经在工作的执行器, 询问该执行器正在渲染的曲目号.
	 * </p>
	 * @param exeId
	 *   执行器标识号
	 * @return
	 *   对应执行器渲染的曲目号, 从 0 开始
	 */
	public int getCurrentTrack(int exeId) {
		ExecutorParam ep = getExecutorParam(exeId);
		return ep.executor.getCurrentTrack();
	}
	
	/**
	 * <p>为一个已经在工作的执行器, 询问该执行器正在渲染的段号.
	 * </p>
	 * @param exeId
	 *   执行器标识号
	 * @return
	 *   对应执行器渲染的段号, 从 0 开始
	 */
	public int getCurrentSection(int exeId) {
		ExecutorParam ep = getExecutorParam(exeId);
		return ep.executor.getCurrentSection();
	}
	
	/**
	 * <p>为一个已经在工作的执行器, 询问该执行器正在渲染的行号.
	 * </p>
	 * @param exeId
	 *   执行器标识号
	 * @return
	 *   对应执行器渲染的行号, 从 0 开始
	 */
	public int getCurrentRow(int exeId) {
		ExecutorParam ep = getExecutorParam(exeId);
		return ep.executor.getCurrentRow();
	}
	
	/**
	 * 询问指定执行器, 当前行是否播放完毕, 需要跳到下一行 (不是询问当前帧是否播放完)
	 * @param exeId
	 *   执行器标识号
	 * @return
	 *   true, 如果当前行已经播放完毕
	 */
	public boolean currentRowRunOut(int exeId) {
		ExecutorParam ep = getExecutorParam(exeId);
		return ep.executor.currentRowRunOut();
	}
	
	/**
	 * <p>获取如果指定执行器, 若跳到下一行（不是下一帧）, 跳到的位置所对应的段号.
	 * <p>如果侦测到有跳转的效果正在触发, 按触发后的结果返回.
	 * </p>
	 * @param exeId
	 *   执行器标识号
	 * @return
	 *   指定执行器下一次跳行后的段号位置
	 */
	public int getNextSection(int exeId) {
		ExecutorParam ep = getExecutorParam(exeId);
		return ep.executor.getNextSection();
	}
	
	/**
	 * <p>获取如果指定执行器, 若跳到下一行（不是下一帧）, 跳到的位置所对应的行号.
	 * <p>如果侦测到有跳转的效果正在触发, 按触发后的结果返回.
	 * </p>
	 * @param exeId
	 *   执行器标识号
	 * @return
	 *   指定执行器下一次跳行后的行号位置
	 */
	public int getNextRow(int exeId) {
		ExecutorParam ep = getExecutorParam(exeId);
		return ep.executor.getNextRow();
	}
	
	/**
	 * 返回指定执行器的所有的轨道号的集合. 轨道号的参数在 {@link INsfChannelCode} 里面写出
	 * @param exeId
	 *   执行器标识号
	 * @return
	 *   指定执行器的所有的轨道号的集合. 如果没有调用 ready(...) 方法时, 返回空集合.
	 */
	public Set<Byte> allChannelSet(int exeId) {
		ExecutorParam ep = getExecutorParam(exeId);
		return ep.executor.allChannelSet();
	}
	
	/**
	 * 设置某个轨道的音量
	 * @param exeId
	 *   执行器标识号
	 * @param channelCode
	 *   轨道号
	 * @param level
	 *   音量. 范围 [0, 1]
	 */
	public void setLevel(int exeId, byte channelCode, float level) {
		ChannelParam cp = getChannelParam(exeId, channelCode);
		if (cp == null) {
			return;
		}
		int mixerId = cp.mixerId;
		if (mixerId == -1) {
			return;
		}
		
		if (level < 0) {
			level = 0;
		} else if (level > 1) {
			level = 1;
		}
		mixer.setLevel(mixerId, level);
	}
	
	/**
	 * 获得某个轨道的音量
	 * @param exeId
	 *   执行器标识号
	 * @param channelCode
	 *   轨道号
	 * @return
	 *   音量. 范围 [0, 1]
	 * @throws NullPointerException
	 *   当不存在 <code>channelCode</code> 对应的轨道时
	 */
	public float getLevel(int exeId, byte channelCode) {
		ChannelParam cp = getChannelParam(exeId, channelCode);
		if (cp == null) {
			throw new NullPointerException(
					"不存在 exeId: " + exeId + ", channelCode: " + channelCode + " 对应的轨道");
		}
		int mixerId = cp.mixerId;
		if (mixerId == -1) {
			throw new NullPointerException(
					"不存在 exeId: " + exeId + ", channelCode: " + channelCode + " 对应的轨道");
		}
		return mixer.getLevel(mixerId);
	}
	
	/**
	 * 设置轨道是否发出声音
	 * @param exeId
	 *   执行器标识号
	 * @param channelCode
	 *   轨道号
	 * @param muted
	 *   false, 使该轨道发声; true, 则静音
	 */
	public void setChannelMuted(int exeId, byte channelCode, boolean muted) {
		ExecutorParam ep = getExecutorParam(exeId);
		AbstractNsfSound sound = ep.executor.getSound(channelCode);
		if (sound != null) {
			sound.setMuted(muted);
		}
	}
	
	/**
	 * 查看轨道是否能发出声音
	 * @param exeId
	 *   执行器标识号
	 * @param channelCode
	 *   轨道号
	 * @return
	 *   false, 说明该轨道没有被屏蔽; true, 则已经被屏蔽
	 * @throws NullPointerException
	 *   当不存在 <code>channelCode</code> 对应的轨道时
	 */
	public boolean isChannelMuted(int exeId, byte channelCode) throws NullPointerException {
		ExecutorParam ep = getExecutorParam(exeId);
		return ep.executor.getSound(channelCode).isMuted();
	}
	
	@Override
	public float getSpeed() {
		return param.speed;
	}

	@Override
	public void setSpeed(float speed) {
		if (speed > 10) {
			speed = 10;
		} else if (speed < 0.1f) {
			speed = 0.1f;
		}
		
		param.speed = speed;
		
		int frameRate = param.frameRate;
		resetCounterParam(frameRate, param.sampleRate);
		rate.onParamUpdate();
	}
	
	/* **********
	 *  监听器  *
	 ********** */
	
	/**
	 * <p>为指定的执行器添加获取音键的监听器.
	 * <p>如果指定执行器被删除, 监听器将被一并删除.
	 * </p>
	 * @param exeId
	 *   执行器标识号
	 * @param l
	 *   获取音键的监听器
	 * @throws NullPointerException
	 *   当监听器 <code>l == null</code> 时
	 * @throws NsfPlayerException
	 *   当不存在 exeId 对应的执行器时
	 */
	public void addFetchListener(int exeId, IFtmFetchListener l) {
		ExecutorParam ep = getExecutorParam(exeId);
		ep.executor.addFetchListener(l);
	}
	
	/**
	 * 为指定的执行器移除获取音键的监听器
	 * @param exeId
	 *   执行器标识号
	 * @param l
	 *   移除音键的监听器
	 * @throws NsfPlayerException
	 *   当不存在 exeId 对应的执行器时
	 */
	public void removeFetchListener(int exeId, IFtmFetchListener l) {
		ExecutorParam ep = getExecutorParam(exeId);
		ep.executor.removeFetchListener(l);
	}
	
	/**
	 * <p>为指定的执行器添加执行结束的监听器.
	 * 该监听器会在效果执行结束, 但还未写入 sound 时唤醒.
	 * <p>如果指定执行器被删除, 监听器将被一并删除.
	 * </p>
	 * @param exeId
	 *   执行器标识号
	 * @param l
	 *   执行结束的监听器
	 * @throws NullPointerException
	 *   当监听器 <code>l == null</code> 时
	 * @throws NsfPlayerException
	 *   当不存在 exeId 对应的执行器时
	 */
	public void addExecuteFinishedListener(int exeId, IFtmExecutedListener l) {
		ExecutorParam ep = getExecutorParam(exeId);
		ep.executor.addExecuteFinishedListener(l);
	}
	
	/**
	 * 为指定的执行器移除执行结束的监听器
	 * @param exeId
	 *   执行器标识号
	 * @param l
	 *   执行结束的监听器
	 * @throws NsfPlayerException
	 *   当不存在 exeId 对应的执行器时
	 */
	public void removeExecuteFinishedListener(int exeId, IFtmExecutedListener l) {
		ExecutorParam ep = getExecutorParam(exeId);
		ep.executor.removeExecuteFinishedListener(l);
	}

}
