package zdream.nsfplayer.nsf.executor;

import static java.util.Objects.requireNonNull;

import java.util.HashSet;
import java.util.Set;

import zdream.nsfplayer.core.AbstractNsfExecutor;
import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.nsf.audio.NsfAudio;
import zdream.nsfplayer.nsf.device.AbstractSoundChip;
import zdream.nsfplayer.nsf.renderer.NsfRuntime;
import zdream.nsfplayer.sound.AbstractNsfSound;

/**
 * <p>Nsf 的执行构件.
 * <p>在 0.2.x 版本中, Nsf 的执行部分是直接写在 NsfRenderer 中的,
 * 从版本 0.3.0 开始, 执行构件从 renderer 中分离出来, 单独构成一个类.
 * 它交接了原本是需要 NsfRuntime 或 NsfRenderer 完成的任务中, 与执行相关的任务.
 * </p>
 * 
 * @author Zdream
 * @since v0.3.0
 */
public class NsfExecutor extends AbstractNsfExecutor<NsfAudio> {
	
	private final NsfRuntime runtime;
	
	public NsfExecutor() {
		this.runtime = new NsfRuntime();
		runtime.init();
	}
	
	/**
	 * TODO 待移交
	 * @return
	 */
	public NsfRuntime getRuntime() {
		return runtime;
	}
	
	/* **********
	 * 准备部分 *
	 ********** */
	
	/**
	 * 跟随 Nsf 文件中指定的制式
	 */
	public static final int REGION_FOLLOW_AUDIO = 0;
	/**
	 * 强制要求 NTSC
	 */
	public static final int REGION_FORCE_NTSC = 1;
	/**
	 * 强制要求 PAL
	 */
	public static final int REGION_FORCE_PAL = 2;
	/**
	 * 强制要求 DENDY
	 */
	public static final int REGION_FORCE_DENDY = 3;
	
	/**
	 * 设置播放的制式要求
	 * @param region
	 *   制式要求
	 * @see #REGION_FOLLOW_AUDIO
	 * @see #REGION_FORCE_NTSC
	 * @see #REGION_FORCE_PAL
	 * @see #REGION_FORCE_DENDY
	 * @throws IllegalArgumentException
	 *   当制式要求不为预设的这四个时
	 */
	public void setRegion(int region) {
		switch (region) {
		case REGION_FOLLOW_AUDIO:
		case REGION_FORCE_NTSC:
		case REGION_FORCE_PAL:
		case REGION_FORCE_DENDY:
			runtime.param.region = region;
			break;

		default:
			throw new IllegalArgumentException("制式要求: " + region + " 不可解析");
		}
	}
	
	/**
	 * 设置 tick() 的执行的速率.
	 * @param rate
	 *   执行速率. 一般这个值等于 sampleRate
	 */
	public void setRate(int rate) {
		runtime.param.sampleRate = rate; // 默认: 48000
	}

	/**
	 * 读取 Nsf 音频, 并以默认曲目进行准备
	 * @param audio
	 * @throws NullPointerException
	 *   当 audio 为 null 时
	 */
	public void ready(NsfAudio audio) throws NullPointerException {
		requireNonNull(audio, "NSF 曲目 audio = null");
		ready0(audio, audio.start);
	}
	
	/**
	 * 读取 Nsf 音频, 以指定曲目进行准备
	 * @param audio
	 *   Nsf 音频实例
	 * @param track
	 *   曲目号
	 * @throws NullPointerException
	 *   当 audio 为 null 时
	 * @throws IllegalArgumentException
	 *   当曲目号 track 在范围 [0, audio.total_songs) 之外时.
	 */
	public void ready(NsfAudio audio, int track) throws NullPointerException, IllegalArgumentException {
		requireNonNull(audio, "NSF 曲目 audio = null");
		if (track < 0 || track >= audio.total_songs) {
			throw new IllegalArgumentException(
					"曲目号 track 需要在范围 [0, " + audio.total_songs + ") 内, " + track + " 是非法值");
		}
		
		this.ready0(audio, track);
	}
	
	private void ready0(NsfAudio audio, int track) {
		if (track < 0 || track >= audio.total_songs) {
			track = 0;
		}
		
		runtime.audio = audio;
		runtime.manager.setSong(track);
		
		runtime.reset();
//		mixer.reset();
//		connectChannels();
//		
//		super.resetCounterParam(frameRate, config.sampleRate);
//		clearBuffer();
//		runtime.clockCounter.onParamUpdate(config.sampleRate, runtime.param.freqPerSec);
//		rate.onParamUpdate(frameRate, runtime.param.freqPerSec);
	}
	
	/* **********
	 * 渲染部分 *
	 ********** */

	@Override
	public void tick() {
		runtime.manager.tickCPU();
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}
	
	/* **********
	 * 参数指标 *
	 ********** */
	
	/**
	 * @return
	 *   每秒的时钟数
	 */
	public int cycleRate() {
		return runtime.param.freqPerSec;
	}
	
	/**
	 * 返回所有的轨道号的集合. 轨道号的参数在 {@link INsfChannelCode} 里面写出
	 * @return
	 *   所有的轨道号的集合. 如果没有调用 ready(...) 方法时, 返回空集合.
	 */
	public Set<Byte> allChannelSet() {
		return new HashSet<>(runtime.chips.keySet());
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
		AbstractSoundChip chip = runtime.chips.get(channelCode);
		if (chip == null) {
			return null;
		}
		return chip.getSound(channelCode);
	}
	
	/* **********
	 *  监听器  *
	 ********** */
	
	/**
	 * 添加 N163 重连的监听器
	 * @param listener
	 */
	public void addN163ReattachListener(IN163ReattachListener listener) {
		runtime.n163Lsners.add(listener);
	}
	
	/**
	 * 删除 N163 重连的监听器
	 * @param listener
	 */
	public void removeReattachListener(IN163ReattachListener listener) {
		runtime.n163Lsners.remove(listener);
	}
	
	/**
	 * 清空 N163 重连的监听器
	 */
	public void clearReattachListeners() {
		runtime.n163Lsners.clear();
	}

}
