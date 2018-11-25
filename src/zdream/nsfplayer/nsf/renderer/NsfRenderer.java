package zdream.nsfplayer.nsf.renderer;

import java.util.HashSet;
import java.util.Set;

import zdream.nsfplayer.core.AbstractNsfRenderer;
import zdream.nsfplayer.core.NsfStatic;
import zdream.nsfplayer.nsf.audio.NsfAudio;
import zdream.nsfplayer.nsf.device.AbstractSoundChip;
import zdream.nsfplayer.sound.mixer.IMixerHandler;

/**
 * <p>NSF 渲染器.
 * <p>该类在 v0.2.3 版本以前基本处于不可用的状态, 直到 v0.2.4 版本进行了大量的改造.
 * </p>
 * 
 * @author Zdream
 * @since v0.1
 */
public class NsfRenderer extends AbstractNsfRenderer<NsfAudio> {
	
	private NsfRuntime runtime;
	
	public NsfRenderer() {
		this.runtime = new NsfRuntime();
		runtime.init();
	}
	
	public NsfRenderer(NsfRendererConfig config) {
		this.runtime = new NsfRuntime(config);
		runtime.init();
	}
	
	/* **********
	 * 准备部分 *
	 ********** */
	
	/**
	 * 这里其实选 50 和 60 是不影响的,
	 * 因为 Nsf 播放器是以一个采样为计算步长的, 而非帧.
	 */
	private int frameRate = NsfStatic.FRAME_RATE_NTSC;
	
	/**
	 * 读取 Nsf 音频, 并以默认曲目进行准备
	 * @param audio
	 * @throws NullPointerException
	 *   当 audio 为 null 时
	 */
	public void ready(NsfAudio audio) {
		if (audio == null) {
			throw new NullPointerException("audio = null");
		}
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
	public void ready(NsfAudio audio, int track) {
		if (audio == null) {
			throw new NullPointerException("audio = null");
		}
		if (track < 0 || track >= audio.total_songs) {
			throw new IllegalArgumentException(
					"曲目号 track 需要在范围 [0, " + audio.total_songs + ") 内, " + track + " 是非法值");
		}
		
		this.ready0(audio, track);
	}
	
	/**
	 * <p>在不更改 Nsf 音频的同时, 切换到指定曲目的开头.
	 * <p>第一次播放时需要指定 Nsf 音频数据.
	 * 因此第一次需要调用含 {@link NsfAudio} 参数的重载方法
	 * </p>
	 * @param track
	 *   曲目号, 从 0 开始
	 * @throws NullPointerException
	 *   当调用该方法前未指定 {@link NsfAudio} 音频时
	 * @throws IllegalArgumentException
	 *   当曲目号 track 在范围 [0, audio.total_songs) 之外时.
	 */
	public void ready(int track) throws NullPointerException {
		NsfAudio audio = runtime.audio;
		
		if (track < 0 || track >= audio.total_songs) {
			throw new IllegalArgumentException(
					"曲目号 track 需要在范围 [0, " + audio.total_songs + ") 内");
		}
		
		runtime.manager.setSong(track);
		runtime.reset();
	}
	
	private void ready0(NsfAudio audio, int track) {
		if (track < 0 || track >= audio.total_songs) {
			track = 0;
		}
		
		runtime.param.sampleRate = this.runtime.config.sampleRate;
		runtime.param.frameRate = frameRate;
		
		runtime.audio = audio;
		runtime.manager.setSong(track);
		
		runtime.reset();
		
		super.resetCounterParam(frameRate, runtime.config.sampleRate);
		clearBuffer();
		runtime.clockCounter.onParamUpdate(runtime.config.sampleRate, runtime.param.freqPerSec);
		runtime.rate.onParamUpdate(frameRate, runtime.param.freqPerSec);
	}
	
	/* **********
	 * 渲染部分 *
	 ********** */

	@Override
	protected int renderFrame() {
		int ret = countNextFrame();
		runtime.param.sampleInCurFrame = ret;
		runtime.rate.doConvert();
		runtime.mixerReady();
		
		runtime.manager.tickCPU(true);

		// 从 mixer 中读取数据
		readMixer();
		
		return ret;
	}
	
	@Override
	protected int skipFrame() {
		int ret = countNextFrame();
		runtime.param.sampleInCurFrame = ret;
		runtime.rate.doConvert();
		
		runtime.manager.tickCPU(false);

		return ret;
	}
	
	/**
	 * 从 Mixer 中读取音频数据
	 */
	private void readMixer() {
		runtime.mixer.finishBuffer();
		runtime.mixer.readBuffer(data, 0, data.length);
	}
	
	/**
	 * <p>询问是否整个乐曲已经渲染完成. 由于 NSF 没有明确的结束播放的结点, 该方法永远返回 false.
	 * <p>如果要设置结束播放结点的触发器, 需要对 {@link #render(short[], int, int)} 返回的采样数据
	 * 进行扫描, 通过查看所有采样数据是否相同判断该时段 NSF 没有发出声音.
	 * <p>当 NSF 已经连续多帧出现该情况（推荐 3 秒, 大约 180 帧）即可判断乐曲渲染结束.
	 * </p>
	 */
	public boolean isFinished() {
		return false;
	}

	/* **********
	 * 仪表盘区 *
	 ********** */
	/*
	 * 用于控制实际播放数据的部分.
	 * 其中有: 控制音量、控制是否播放
	 */
	
	/**
	 * @return
	 *   当前正在播放的曲目号
	 * @since v0.2.8
	 */
	public int getCurrentTrack() {
		return runtime.manager.getSong();
	}
	
	/**
	 * @since v0.2.8
	 */
	@Override
	public Set<Byte> allChannelSet() {
		return new HashSet<>(runtime.chips.keySet());
	}
	
	/**
	 * 设置某个轨道的音量
	 * @param channelCode
	 *   轨道号
	 * @param level
	 *   音量. 范围 [0, 1] 
	 * @since v0.2.4
	 */
	public void setLevel(byte channelCode, float level) {
		if (level < 0) {
			level = 0;
		} else if (level > 1) {
			level = 1;
		}
		runtime.mixer.setLevel(channelCode, level);
	}
	
	/**
	 * 获得某个轨道的音量
	 * @param channelCode
	 *   轨道号
	 * @return
	 *   音量. 范围 [0, 1]
	 * @throws NullPointerException
	 *   当不存在 <code>channelCode</code> 对应的轨道时
	 * @since v0.2.4
	 */
	public float getLevel(byte channelCode) throws NullPointerException {
		return runtime.mixer.getLevel(channelCode);
	}
	
	/**
	 * 设置轨道是否发出声音
	 * @param channelCode
	 *   轨道号
	 * @param mask
	 *   false, 使该轨道发声; true, 则静音
	 * @since v0.2.4
	 */
	public void setChannelMask(byte channelCode, boolean mask) {
		AbstractSoundChip chip = runtime.chips.get(channelCode);
		if (chip != null) {
			chip.getSound(channelCode).setMask(mask);
		}
	}
	
	/**
	 * 查看轨道是否能发出声音
	 * @param channelCode
	 *   轨道号
	 * @return
	 *   false, 说明该轨道没有被屏蔽; true, 则已经被屏蔽
	 * @throws NullPointerException
	 *   当不存在 <code>channelCode</code> 对应的轨道时
	 * @since v0.2.4
	 */
	public boolean isChannelMask(byte channelCode) throws NullPointerException {
		return runtime.chips.get(channelCode).getSound(channelCode).isMask();
	}
	
	@Override
	public void setSpeed(float speed) {
		if (speed > 10) {
			speed = 10;
		} else if (speed < 0.1f) {
			speed = 0.1f;
		}
		
		runtime.param.speed = speed;
		
		resetCounterParam(frameRate, runtime.config.sampleRate);
		runtime.clockCounter.onAPUParamUpdate();
		runtime.rate.onParamUpdate();
	}
	
	@Override
	public float getSpeed() {
		return runtime.param.speed;
	}
	
	/**
	 * 获得混音器的操作者（工具类）. 通过它可以对所使用的混音器进行简单的操作.
	 * @return
	 *   混音器的操作者
	 * @since v0.2.10
	 */
	public IMixerHandler getMixerHandler() {
		return runtime.mixer.getHandler();
	}

}
