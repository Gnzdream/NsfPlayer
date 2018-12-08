package zdream.nsfplayer.ftm.executor;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import zdream.nsfplayer.core.AbstractNsfExecutor;
import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.core.NsfPlayerException;
import zdream.nsfplayer.ftm.agreement.FtmPosition;
import zdream.nsfplayer.ftm.audio.FamiTrackerQuerier;
import zdream.nsfplayer.ftm.audio.FtmAudio;
import zdream.nsfplayer.ftm.executor.effect.IFtmEffect;
import zdream.nsfplayer.ftm.executor.hook.IFtmExecutedListener;
import zdream.nsfplayer.ftm.executor.hook.IFtmFetchListener;
import zdream.nsfplayer.sound.AbstractNsfSound;

/**
 * <p>FamiTracker 的执行构件.
 * <p>在 0.2.x 版本中, FamiTracker 的执行部分是直接写在 FamiTrackerRenderer 中的,
 * 从版本 0.3.0 开始, 执行构件从 renderer 中分离出来, 单独构成一个类.
 * 它交接了原本是需要 FamiTrackerRuntime 或 FamiTrackerRenderer 完成的任务中, 与执行相关的任务.
 * </p>
 * 
 * @author Zdream
 * @since v0.3.0
 */
public class FamiTrackerExecutor extends AbstractNsfExecutor<FtmAudio> {

	/**
	 * 执行上下文
	 */
	private final FamiTrackerRuntime runtime;
	
	public FamiTrackerExecutor() {
		runtime = new FamiTrackerRuntime();
		this.runtime.init();
	}
	
	/* **********
	 * 准备部分 *
	 ********** */

	/**
	 * <p>让该执行器读取对应的 audio 数据.
	 * <p>设置播放暂停位置为第 1 个曲目 (曲目 0) 的第一段 (段 0)
	 * </p>
	 * @param audio
	 *   FamiTracker 的封装的曲目
	 */
	public void ready(FtmAudio audio) throws NsfPlayerException {
		ready(audio, 0, 0, 0);
	}
	
	/**
	 * <p>让该执行器读取对应的 audio 数据.
	 * <p>设置播放暂停位置为指定曲目的第一段 (段 0)
	 * </p>
	 * @param audio
	 *   FamiTracker 的封装的曲目
	 * @param track
	 *   曲目号, 从 0 开始
	 */
	public void ready(FtmAudio audio, int track) throws NsfPlayerException {
		ready(audio, track, 0, 0);
	}
	
	/**
	 * <p>让该执行器读取对应的 audio 数据.
	 * <p>设置播放暂停位置为指定曲目的指定段
	 * </p>
	 * @param audio
	 *   FamiTracker 的封装的曲目
	 * @param track
	 *   曲目号, 从 0 开始
	 * @param section
	 *   段号, 从 0 开始
	 */
	public void ready(
			FtmAudio audio,
			int track,
			int section)
			throws NsfPlayerException {
		ready(audio, track, section, 0);
	}
	
	/**
	 * <p>让该执行器读取对应的 audio 数据.
	 * <p>设置播放暂停位置为指定曲目的指定行
	 * </p>
	 * @param audio
	 *   FamiTracker 的封装的曲目
	 * @param track
	 *   曲目号, 从 0 开始
	 * @param section
	 *   段号, 从 0 开始
	 * @param row
	 *   行号, 从 0 开始
	 * @since v0.3.1
	 */
	public void ready(
			FtmAudio audio,
			int track,
			int section,
			int row)
			throws NsfPlayerException {
		requireNonNull(audio, "FamiTracker 曲目 audio = null");
		
		runtime.ready(audio, track, section, row);
		readyChannels();
	}
	
	/**
	 * <p>在不更改 Ftm 音频的同时, 重置当前曲目, 让执行的位置重置到曲目 0 的开头
	 * <p>第一次调用时需要指定 Ftm 音频数据.
	 * 因此第一次需要调用含 {@link FtmAudio} 参数的重载方法
	 * </p>
	 * @throws NullPointerException
	 *   当调用该方法前未指定 {@link FtmAudio} 音频时
	 */
	public void ready() throws NsfPlayerException {
		ready(0, 0, 0);
	}
	
	/**
	 * <p>在不更改 Ftm 文件的同时, 切换到指定曲目的开头.
	 * <p>第一次调用时需要指定 Ftm 音频数据.
	 * 因此第一次需要调用含 {@link FtmAudio} 参数的重载方法
	 * </p>
	 * @param track
	 *   曲目号, 从 0 开始
	 * @throws NullPointerException
	 *   当调用该方法前未指定 {@link FtmAudio} 音频时
	 */
	public void ready(int track) throws NsfPlayerException {
		ready(track, 0, 0);
	}
	
	/**
	 * <p>在不更改 Ftm 文件的同时, 切换曲目、段号
	 * <p>第一次调用时需要指定 Ftm 音频数据.
	 * 因此第一次需要调用含 {@link FtmAudio} 参数的重载方法
	 * </p>
	 * @param track
	 *   曲目号, 从 0 开始
	 * @param section
	 *   段号, 从 0 开始
	 * @throws NullPointerException
	 *   当调用该方法前未指定 {@link FtmAudio} 音频时
	 */
	public void ready(int track, int section) throws NsfPlayerException {
		ready(track, section, 0);
	}
	
	/**
	 * <p>在不更改 Ftm 文件的同时, 切换曲目、段号
	 * <p>第一次调用时需要指定 Ftm 音频数据.
	 * 因此第一次需要调用含 {@link FtmAudio} 参数的重载方法
	 * </p>
	 * @param track
	 *   曲目号, 从 0 开始
	 * @param section
	 *   段号, 从 0 开始
	 * @param row
	 *   行号, 从 0 开始
	 * @throws NullPointerException
	 *   当调用该方法前未指定 {@link FtmAudio} 音频时
	 * @since v0.3.1
	 */
	public void ready(int track, int section, int row) throws NsfPlayerException {
		requireNonNull(runtime.querier, "FamiTracker 曲目 audio = null");
		
		runtime.ready(track, section, row);
		runtime.resetAllChannels();
	}
	
	/**
	 * <p>在不更改 Ftm 文件、曲目号的同时, 切换段号、行号
	 * <p>第一次调用时需要指定 Ftm 音频数据.
	 * 因此第一次需要调用含 {@link FtmAudio} 参数的重载方法
	 * @param pos
	 *   播放位置, 不为 null
	 * @throws NullPointerException
	 *   当调用该方法前未指定 {@link FtmAudio} 音频时, 或 pos == null 时
	 * @since v0.3.1
	 */
	public void ready(FtmPosition pos) {
		requireNonNull(pos, "FamiTracker 位置 pos = null");
		ready(getCurrentTrack(), pos.section, pos.row);
	}
	
	/**
	 * <p>不改变各个轨道参数的情况下, 切换到指定位置向下执行.
	 * 切换时, 各轨道的播放音高、音量、效果等均不改变, 这也包括延迟效果 Gxx.
	 * 混音器不会重置, 这也意味着上一帧播放的音可能继续延长播放下去.
	 * 而 FTM 文档的播放速度（不是播放速度 speed）会重新根据 tempo 等数值重置.
	 * <p>请谨慎使用该方法. 如果前面使用了颤音 4xy 或者其它效果, 而没有消除时,
	 * 切换位置后, 这些效果会仍然保留下来, 导致后面播放会很奇怪.
	 * 如果想使用更加稳健的方式切换播放位置, 而不会使播放效果发生较大变化,
	 * 请使用 {@link #ready(int, int)} 或 {@link #skip(int)} 方法.
	 * <p>需要在调用前确定该渲染器已经成功加载了 {@link FtmAudio} 音频.
	 * </p>
	 * @param track
	 *   曲目号, 从 0 开始
	 * @param section
	 *   段号, 从 0 开始
	 * @throws NullPointerException
	 *   当调用该方法前未成功加载 {@link FtmAudio} 音频时
	 * @see #ready(int, int)
	 * @see #skip(int)
	 * @since v0.2.9
	 */
	public void switchTo(int track, int section) {
		switchTo(track, section, 0);
	}
	
	/**
	 * <p>不改变各个轨道参数的情况下, 切换到指定位置向下执行.
	 * 切换时, 各轨道的播放音高、音量、效果等均不改变, 这也包括延迟效果 Gxx.
	 * 混音器不会重置, 这也意味着上一帧播放的音可能继续延长播放下去.
	 * 而 FTM 文档的播放速度（不是播放速度 speed）会重新根据 tempo 等数值重置.
	 * <p>请谨慎使用该方法. 如果前面使用了颤音 4xy 或者其它效果, 而没有消除时,
	 * 切换位置后, 这些效果会仍然保留下来, 导致后面播放会很奇怪.
	 * 如果想使用更加稳健的方式切换播放位置, 而不会使播放效果发生较大变化,
	 * 请使用 {@link #ready(int, int)} 或 {@link #skip(int)} 方法.
	 * <p>需要在调用前确定该渲染器已经成功加载了 {@link FtmAudio} 音频.
	 * </p>
	 * @param track
	 *   曲目号, 从 0 开始
	 * @param section
	 *   段号, 从 0 开始
	 * @param row
	 *   行号, 从 0 开始
	 * @throws NullPointerException
	 *   当调用该方法前未成功加载 {@link FtmAudio} 音频时
	 * @see #ready(int, int)
	 * @see #skip(int)
	 * @since v0.3.1
	 */
	public void switchTo(int track, int section, int row) {
		requireNonNull(runtime.querier, "FamiTracker 曲目 audio = null");
		
		runtime.ready(track, section, row);
	}
	
	/**
	 * <p>不改变各个轨道参数的情况下, 切换到指定位置向下执行.
	 * <p>需要在调用前确定该渲染器已经成功加载了 {@link FtmAudio} 音频.
	 * </p>
	 * @param pos
	 *   播放位置, 不为 null
	 * @throws NullPointerException
	 *   当调用该方法前未指定 {@link FtmAudio} 音频时, 或 pos == null 时
	 * @see #ready(FtmPosition)
	 * @see #switchTo(int, int)
	 * @see #switchTo(int, int, int)
	 * @since v0.3.1
	 */
	public void switchTo(FtmPosition pos) {
		requireNonNull(pos, "FamiTracker 位置 pos = null");
		switchTo(getCurrentTrack(), pos.section, pos.row);
	}
	
	private void readyChannels() {
		runtime.channels.clear();
		runtime.effects.clear();
		runtime.selector.reset();
		
		final FamiTrackerQuerier querier = runtime.querier;
		
		final int len = querier.channelCount();
		for (int i = 0; i < len; i++) {
			byte code = querier.channelCode(i);
			
			AbstractFtmChannel ch = runtime.selector.selectFtmChannel(code);
			ch.setRuntime(runtime);
			runtime.channels.put(code, ch);
			runtime.effects.put(code, new HashMap<>());
		}
	}
	
	/* **********
	 * 执行部分 *
	 ********** */

	/**
	 * 执行一帧
	 */
	public void tick() {
		runtime.runFrame();
		updateChannels();

		runtime.fetcher.updateState();
	}
	
	/**
	 * <p>阻塞执行一帧.
	 * <p>在这一帧中, 所有前面触发的效果继续执行,
	 * 但是所有跳行、播放位置改变将不会执行, 关于时间的计算暂停.
	 * </p>
	 * @since v0.3.1
	 */
	public void tickBlock() {
		updateChannels();
		runtime.fetcher.updateState();
	}

	/**
	 * <p>让每个 channel 进行播放操作. 但是这个过程只会将数据写入发声器, 但不会让发声器工作.
	 * <p>所以工作的时间长度、如何工作将交给调用者来实现.
	 * </p>
	 */
	private void updateChannels() {
		final FamiTrackerQuerier querier = runtime.querier;
		
		// 全局效果
		for (IFtmEffect eff : runtime.geffect.values()) {
			eff.execute((byte) 0, runtime);
		}
		
		// 局部效果
		if (runtime.elners.isEmpty()) {
			// 这里是不需要调用监听器的流程（其实就是想快一点，所以两个流程不合并）
			final int len = querier.channelCount();
			for (int i = 0; i < len; i++) {
				byte code = querier.channelCode(i);
				AbstractFtmChannel channel = runtime.channels.get(code);
				
				channel.playNote();
				channel.writeToSound();
			}
		} else {
			// 下面是需要调用监听器的流程
			final int len = querier.channelCount();
			for (int i = 0; i < len; i++) {
				byte code = querier.channelCode(i);
				runtime.channels.get(code).playNote();
			}
			runtime.onExecuteFinished();
			for (int i = 0; i < len; i++) {
				byte code = querier.channelCode(i);
				runtime.channels.get(code).writeToSound();
			}
		}
	}
	
	@Override
	public void reset() {
		ready();
	}
	
	/* **********
	 * 参数指标 *
	 ********** */
	
	/**
	 * <p>询问是否已经播放完毕
	 * <p>如果已经播放完毕的 Ftm 音频尝试再调用 {@link #render(byte[], int, int)}
	 * 或者 {@link #renderOneFrame(byte[], int, int)}, 则会忽略停止符号,
	 * 强制再向下播放.
	 * </p>
	 * @return
	 */
	public boolean isFinished() {
		return runtime.param.finished;
	}
	
	/**
	 * @return
	 *   获取正在播放的曲目号
	 */
	public int getCurrentTrack() {
		return runtime.param.trackIdx;
	}

	/**
	 * @return
	 *   获取正在执行的段号
	 */
	public int getCurrentSection() {
		return runtime.param.curSection;
	}
	
	/**
	 * @return
	 *   获取正在执行的行号
	 */
	public int getCurrentRow() {
		return runtime.param.curRow;
	}
	
	/**
	 * @return
	 *   获取正在执行的位置
	 * @since v0.3.1
	 */
	public FtmPosition currentPosition() {
		return new FtmPosition(runtime.param.curSection, runtime.param.curRow);
	}
	
	/**
	 * 询问当前行是否播放完毕, 需要跳到下一行 (不是询问当前帧是否播放完)
	 * @return
	 *   true, 如果当前行已经播放完毕. 下一帧将进行跳行工作
	 */
	public boolean currentRowRunOut() {
		return runtime.fetcher.needRowUpdate();
	}

	/**
	 * <p>获取如果跳到下一行（不是下一帧）, 跳到的位置所对应的段号.
	 * <p>如果侦测到有跳转的效果正在触发, 按触发后的结果返回.
	 * </p>
	 * @return
	 *   下一行对应的段号
	 */
	public int getNextSection() {
		return runtime.fetcher.getNextSection();
	}
	
	/**
	 * <p>获取如果跳到下一行（不是下一帧）, 跳到的位置所对应的行号.
	 * <p>如果侦测到有跳转的效果正在触发, 按触发后的结果返回.
	 * </p>
	 * @return
	 *   下一行对应的段号
	 */
	public int getNextRow() {
		return runtime.fetcher.getNextRow();
	}
	
	/**
	 * <p>获取如果跳到下一行（不是下一帧）, 跳到的位置.
	 * <p>如果侦测到有跳转的效果正在触发, 按触发后的结果返回.
	 * </p>
	 * @return
	 *   获取下一行执行的位置
	 * @since v0.3.1
	 */
	public FtmPosition nextPosition() {
		return new FtmPosition(runtime.fetcher.getNextSection(), runtime.fetcher.getNextRow());
	}
	
	/**
	 * 获取执行的帧率, 每秒多少帧. 帧率会随着播放的曲目不同而不同.
	 * @return
	 *   实际执行帧率.
	 *   <br>如果事先锁定帧率, 实际执行帧率即锁定的帧率; 否则为音频默认的帧率.
	 * @throws NullPointerException
	 *   当没有调用 {@link #ready(FtmAudio)} 等方法进行初始化时,
	 *   若没有事先锁定帧率会抛出错误.
	 *   <br>帧率优先使用锁定的帧率, 当没有事先锁定帧率时,
	 *   需要通过 {@link FtmAudio} 才能得到.
	 * @see #lockFrameRate(int)
	 */
	public int getFrameRate() {
		return runtime.fetcher.getFrameRate();
	}
	
	/**
	 * 强制锁定帧率. 后面的歌曲均采用该帧率来渲染,
	 * 直到下一次调用 {@link #lockFrameRate(int)} 或者 {@link #unlockFrameRate()}
	 * @param frameRate
	 *   帧率. 必须在 [50, 300] 范围内.
	 * @throws NsfPlayerException
	 *   当 frameRate 不在规定范围内时.
	 * @see #unlockFrameRate()
	 * @since v0.3.1
	 */
	public void lockFrameRate(int frameRate) {
		if (frameRate < 50 || frameRate > 300) {
			throw new NsfPlayerException("锁定帧率 : " + frameRate + " 需要在范围 [50, 300] 范围内");
		}
		runtime.fetcher.setFrameRate(frameRate);
	}
	
	/**
	 * 解锁帧率. 后面的歌曲均采用歌曲使用的默认帧率渲染,
	 * 直到下一次调用 {@link #lockFrameRate(int)} 或者 {@link #unlockFrameRate()}
	 * @see #lockFrameRate(int)
	 * @since v0.3.1
	 */
	public void unlockFrameRate() {
		runtime.fetcher.setFrameRate(0);
	}
	
	/**
	 * 返回所有的轨道号的集合. 轨道号的参数在 {@link INsfChannelCode} 里面写出
	 * @return
	 *   所有的轨道号的集合. 如果没有调用 ready(...) 方法时, 返回空集合.
	 */
	public Set<Byte> allChannelSet() {
		return new HashSet<>(runtime.effects.keySet());
	}
	
	/**
	 * <p>获得对应轨道号的发声器.
	 * <p>发声器就是执行体最后的输出, 所有的执行结果将直接写入到发声器中.
	 * </p>
	 * @param channelCode
	 *   轨道号
	 * @return
	 *   对应轨道的发声器实例. 如果没有对应的轨道, 返回 null.
	 */
	public AbstractNsfSound getSound(byte channelCode) {
		AbstractFtmChannel ch = runtime.channels.get(channelCode);
		if (ch == null) {
			return null;
		}
		return ch.getSound();
	}
	
	/* **********
	 *  监听器  *
	 ********** */
	
	/**
	 * 添加获取音键的监听器
	 * @param l
	 *   获取音键的监听器
	 * @throws NullPointerException
	 *   当监听器 <code>l == null</code> 时
	 */
	public void addFetchListener(IFtmFetchListener l) {
		requireNonNull(l, "listener = null");
		runtime.flners.add(l);
	}
	
	/**
	 * 移除获取音键的监听器
	 * @param l
	 *   移除音键的监听器
	 */
	public void removeFetchListener(IFtmFetchListener l) {
		runtime.flners.remove(l);
	}
	
	/**
	 * 清空所有获取音键的监听器
	 */
	public void clearFetchListener() {
		runtime.flners.clear();
	}
	
	/**
	 * 添加执行结束的监听器.
	 * 该监听器会在效果执行结束, 但还未写入 sound 时唤醒.
	 * @param l
	 *   执行结束的监听器
	 * @throws NullPointerException
	 *   当监听器 <code>l == null</code> 时
	 */
	public void addExecuteFinishedListener(IFtmExecutedListener l) {
		requireNonNull(l, "listener = null");
		runtime.elners.add(l);
	}
	
	/**
	 * 移除执行结束的监听器
	 * @param l
	 *   执行结束的监听器
	 */
	public void removeExecuteFinishedListener(IFtmExecutedListener l) {
		runtime.elners.remove(l);
	}
	
	/**
	 * 清空所有执行结束的监听器
	 */
	public void clearExecuteFinishedListener() {
		runtime.elners.clear();
	}
	
}
