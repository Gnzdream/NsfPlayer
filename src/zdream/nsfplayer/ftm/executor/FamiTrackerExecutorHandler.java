package zdream.nsfplayer.ftm.executor;

import static java.util.Objects.requireNonNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.ftm.audio.FtmAudio;
import zdream.nsfplayer.ftm.executor.effect.IFtmEffect;
import zdream.nsfplayer.sound.AbstractNsfSound;

/**
 * <p>FamiTracker 的控制杆, 用于查看 FamiTracker 执行构件的执行情况,
 * 允许修改 FamiTracker 执行构件的运作方式和数据.
 * </p>
 * 
 * @author Zdream
 * @since v0.3.0
 */
public final class FamiTrackerExecutorHandler implements INsfChannelCode {
	
	private FamiTrackerRuntime runtime;
	
	FamiTrackerExecutorHandler(FamiTrackerRuntime runtime) {
		requireNonNull(runtime);
		this.runtime = runtime;
	}
	
	void destroy() {
		this.runtime = null;
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
	 *   获取正在播放的段号
	 */
	public int getCurrentSection() {
		return runtime.param.curSection;
	}
	
	/**
	 * @return
	 *   获取正在播放的行号
	 */
	public int getCurrentRow() {
		return runtime.param.curRow;
	}
	
	/**
	 * 询问当前行是否播放完毕, 需要跳到下一行 (不是询问当前帧是否播放完)
	 * @return
	 *   true, 如果当前行已经播放完毕
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
	 * 获取执行的帧率, 每秒多少帧. 帧率会随着播放的曲目不同而不同.
	 * @return
	 *   帧率
	 * @throws NullPointerException
	 *   当没有调用 {@link #ready(FtmAudio)} 等方法进行初始化时, 会抛出错误.
	 *   帧率当执行构件感知到 {@link FtmAudio} 时才能计算.
	 */
	public int getFrameRate() {
		return runtime.fetcher.getFrameRate();
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
		AbstractFtmChannel ch = getChannel(channelCode);
		if (ch == null) {
			return null;
		}
		return ch.getSound();
	}
	
	/* **********
	 * 效果集合 *
	 ********** */
	
	/**
	 * 获取指定轨道的效果迭代器
	 * @param channelCode
	 *   轨道号
	 */
	public Iterator<IFtmEffect> channelEffects(byte channelCode) {
		return runtime.effects.get(channelCode).values().iterator();
	}
	
	/**
	 * 获取全局轨道的效果迭代器
	 */
	public Iterator<IFtmEffect> globalEffects() {
		return runtime.geffect.values().iterator();
	}
	
	/* **********
	 * 执行轨道 *
	 ********** */
	
	/**
	 * 获取指定轨道的主音量
	 * @param channelCode
	 *   轨道号
	 */
	public int masterVolume(byte channelCode) {
		return getChannel(channelCode).getMasterVolume();
	}
	
	/**
	 * 获取指定轨道的当前音量
	 * @param channelCode
	 *   轨道号
	 */
	public int currentVolume(byte channelCode) {
		return getChannel(channelCode).getCurrentVolume();
	}
	
	/**
	 * 获取指定轨道的主音键
	 * @param channelCode
	 *   轨道号
	 */
	public int masterNote(byte channelCode) {
		return getChannel(channelCode).getMasterNote();
	}
	
	/**
	 * 获取指定轨道的当前音键
	 * @param channelCode
	 *   轨道号
	 */
	public int currentNote(byte channelCode) {
		return getChannel(channelCode).getCurrentNote();
	}
	
	/**
	 * 获取指定轨道的主音高. 为效果 Pxx 的数值.
	 * @param channelCode
	 *   轨道号
	 */
	public int masterPitch(byte channelCode) {
		return getChannel(channelCode).getMasterPitch();
	}
	
	/**
	 * 获取指定轨道的当前音高计算数值.
	 * 音高和音高数值不一定是正相关关系, 仅具有参考意义.
	 * @param channelCode
	 *   轨道号
	 */
	public int currentPeriod(byte channelCode) {
		return getChannel(channelCode).getCurrentPeriod();
	}
	
	/**
	 * 获取指定轨道的主音色
	 * @param channelCode
	 *   轨道号
	 */
	public int masterDuty(byte channelCode) {
		return getChannel(channelCode).getMasterDuty();
	}
	
	/**
	 * 获取指定轨道的当前音色
	 * @param channelCode
	 *   轨道号
	 */
	public int currentDuty(byte channelCode) {
		return getChannel(channelCode).getCurrentDuty();
	}
	
	/**
	 * 获取指定轨道的当前使用的乐器
	 * @param channelCode
	 *   轨道号
	 */
	public int currentInstrument(byte channelCode) {
		return getChannel(channelCode).getInstrument();
	}
	
	/**
	 * 询问某个轨道是否正在播放
	 * @param channelCode
	 *   轨道号
	 */
	public boolean isChannelPlaying(byte channelCode) {
		return getChannel(channelCode).isPlaying();
	}
	
	private AbstractFtmChannel getChannel(byte channelCode) {
		return runtime.channels.get(channelCode);
	}

}
