package zdream.nsfplayer.nsf.renderer;

import java.util.Set;

import zdream.nsfplayer.core.AbstractNsfRenderer;
import zdream.nsfplayer.core.NsfRateConverter;
import zdream.nsfplayer.core.NsfStatic;
import zdream.nsfplayer.nsf.audio.NsfAudio;
import zdream.nsfplayer.nsf.device.AbstractSoundChip;
import zdream.nsfplayer.nsf.device.chip.NesN163;
import zdream.nsfplayer.nsf.executor.IN163ReattachListener;
import zdream.nsfplayer.nsf.executor.NsfExecutor;
import zdream.nsfplayer.sound.AbstractNsfSound;
import zdream.nsfplayer.sound.blip.BlipMixerConfig;
import zdream.nsfplayer.sound.blip.BlipSoundMixer;
import zdream.nsfplayer.sound.mixer.IMixerChannel;
import zdream.nsfplayer.sound.mixer.IMixerConfig;
import zdream.nsfplayer.sound.mixer.IMixerHandler;
import zdream.nsfplayer.sound.mixer.SoundMixer;
import zdream.nsfplayer.sound.xgm.XgmMixerConfig;
import zdream.nsfplayer.sound.xgm.XgmSoundMixer;

/**
 * <p>NSF 渲染器.
 * <p>该类在 v0.2.3 版本以前基本处于不可用的状态, 直到 v0.2.4 版本进行了大量的改造.
 * <p>该渲染器是线程不安全的, 请注意不要在渲染途中设置参数.
 * </p>
 * 
 * @author Zdream
 * @since v0.1
 */
public class NsfRenderer extends AbstractNsfRenderer<NsfAudio> {
	
	private final NsfExecutor executor = new NsfExecutor();
	
	public final NsfRateConverter rate;
	
	private NsfRuntime runtime;
	
//	private final NsfCommonParameter param = new NsfCommonParameter();
	
	NsfRendererConfig config;
	
	/**
	 * 音频混音器
	 */
	public SoundMixer mixer;
	
	public NsfRenderer() {
		this(new NsfRendererConfig());
	}
	
	public NsfRenderer(NsfRendererConfig config) {
		this.config = config;
		
		executor.setRegion(config.region);
		executor.setRate(config.sampleRate);
		executor.addN163ReattachListener(n163lsner);
		
		runtime = executor.getRuntime();
		initMixer();
		rate = new NsfRateConverter(runtime.param);
//		param.levels.copyFrom(config.channelLevels);
	}
	
	public void initMixer() {
		IMixerConfig mixerConfig = config.mixerConfig;
		if (mixerConfig == null) {
			mixerConfig = new XgmMixerConfig();
		}
		
		if (mixerConfig instanceof XgmMixerConfig) {
			// 采用 Xgm 音频混合器 (原 NsfPlayer 使用的)
			XgmSoundMixer mixer = new XgmSoundMixer();
			mixer.setConfig((XgmMixerConfig) mixerConfig);
			mixer.param = runtime.param;
			this.mixer = mixer;
		} else if (mixerConfig instanceof BlipMixerConfig) {
			// 采用 Blip 音频混合器 (原 FamiTracker 使用的)
			BlipSoundMixer mixer = new BlipSoundMixer();
			mixer.frameRate = 50; // 帧率在最低值, 这样可以保证高帧率 (比如 60) 也能兼容
			mixer.sampleRate = config.sampleRate;
			mixer.setConfig((BlipMixerConfig) mixerConfig);
			mixer.param = runtime.param;
			this.mixer = mixer;
		} else {
			// TODO 暂时不支持 xgm 和 blip 之外的 mixerConfig
		}

		this.mixer.init();
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
		runtime.param.sampleRate = this.config.sampleRate;
		runtime.param.frameRate = frameRate;
		
		executor.ready(audio, track);
		
		mixer.reset();
		connectChannels();
		
		super.resetCounterParam(frameRate, config.sampleRate);
		clearBuffer();
		runtime.clockCounter.onParamUpdate(config.sampleRate, runtime.param.freqPerSec);
		rate.onParamUpdate(frameRate, runtime.param.freqPerSec);
	}
	
	private void connectChannels() {
		mixer.detachAll();
		Set<Byte> channels = executor.allChannelSet();
		for (byte channelCode: channels) {
			AbstractNsfSound sound = executor.getSound(channelCode);
			if (sound != null) {
				IMixerChannel mix = mixer.allocateChannel(channelCode);
				sound.setOut(mix);
				
				// 音量
				mix.setLevel(getInitLevel(channelCode));
				
				// TODO 告诉混音器更多的信息, 包括发声器的输出采样率 (NSF 的为 177万, mpeg 的为 44100 或 48000 等)
				
			}
		}
		
		
	}
	
	/* **********
	 * 渲染部分 *
	 ********** */

	@Override
	protected int renderFrame() {
		int ret = countNextFrame();
		runtime.param.sampleInCurFrame = ret;
		rate.doConvert();
		mixerReady();
		
		runtime.manager.tickCPU(true);

		// 从 mixer 中读取数据
		readMixer();
		
		return ret;
	}
	
	@Override
	protected int skipFrame() {
		int ret = countNextFrame();
		runtime.param.sampleInCurFrame = ret;
		rate.doConvert();
		
		runtime.manager.tickCPU(false);

		return ret;
	}
	
	/**
	 * 从 Mixer 中读取音频数据
	 */
	private void readMixer() {
		mixer.finishBuffer();
		mixer.readBuffer(data, 0, data.length);
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
	
	/**
	 * <p>通知混音器, 当前帧的渲染开始了.
	 * <p>这个方法原本用于通知混音器, 如果本帧的渲染速度需要变化,
	 * 可以通过该方法, 让混音器提前对此做好准备, 修改存储的采样数容量, 从而调节播放速度.
	 * </p>
	 * @since v0.2.9
	 */
	private void mixerReady() {
		mixer.readyBuffer();
	}

	/* **********
	 * 仪表盘区 *
	 ********** */
	/*
	 * 用于控制实际播放数据的部分.
	 * 其中有: 控制音量、控制是否播放、控制渲染组件等
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
		return executor.allChannelSet();
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
		mixer.setLevel(channelCode, level);
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
		return mixer.getLevel(channelCode);
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
		
		resetCounterParam(frameRate, config.sampleRate);
		runtime.clockCounter.onAPUParamUpdate();
		rate.onParamUpdate();
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
		return mixer.getHandler();
	}
	
	class N163ReattachListener implements IN163ReattachListener {

		@Override
		public void onReattach(int n163ChannelCount) {
			for (int i = 0; i < 8; i++) {
				byte channelCode = (byte) (NesN163.CHANNEL_N163_1 + i);
				AbstractNsfSound sound = executor.getSound(channelCode);
				if (sound != null) {
					IMixerChannel mix = mixer.allocateChannel(channelCode);
					sound.setOut(mix);
					mix.setLevel(getInitLevel(channelCode));
				} else {
					IMixerChannel mix = mixer.getMixerChannel(channelCode);
					mix.setEnable(false);
				}
			}
		}
		
	}
	private final N163ReattachListener n163lsner = new N163ReattachListener();
	
	/**
	 * 获取每个轨道的音量. 这个值应该是从参数 {@link NsfParameter} 中去取.
	 * @param channelCode
	 * @return
	 */
	private float getInitLevel(byte channelCode) {
		float level = 0;
		switch (channelCode) {
		case CHANNEL_2A03_PULSE1: level = runtime.param.levels.level2A03Pules1; break;
		case CHANNEL_2A03_PULSE2: level = runtime.param.levels.level2A03Pules2; break;
		case CHANNEL_2A03_TRIANGLE: level = runtime.param.levels.level2A03Triangle; break;
		case CHANNEL_2A03_NOISE: level = runtime.param.levels.level2A03Noise; break;
		case CHANNEL_2A03_DPCM: level = runtime.param.levels.level2A03DPCM; break;

		case CHANNEL_VRC6_PULSE1: level = runtime.param.levels.levelVRC6Pules1; break;
		case CHANNEL_VRC6_PULSE2: level = runtime.param.levels.levelVRC6Pules2; break;
		case CHANNEL_VRC6_SAWTOOTH: level = runtime.param.levels.levelVRC6Sawtooth; break;

		case CHANNEL_MMC5_PULSE1: level = runtime.param.levels.levelMMC5Pules1; break;
		case CHANNEL_MMC5_PULSE2: level = runtime.param.levels.levelMMC5Pules2; break;
		
		case CHANNEL_FDS: level = runtime.param.levels.levelFDS; break;
		
		case CHANNEL_N163_1: level = runtime.param.levels.levelN163Namco1; break;
		case CHANNEL_N163_2: level = runtime.param.levels.levelN163Namco2; break;
		case CHANNEL_N163_3: level = runtime.param.levels.levelN163Namco3; break;
		case CHANNEL_N163_4: level = runtime.param.levels.levelN163Namco4; break;
		case CHANNEL_N163_5: level = runtime.param.levels.levelN163Namco5; break;
		case CHANNEL_N163_6: level = runtime.param.levels.levelN163Namco6; break;
		case CHANNEL_N163_7: level = runtime.param.levels.levelN163Namco7; break;
		case CHANNEL_N163_8: level = runtime.param.levels.levelN163Namco8; break;
		
		case CHANNEL_VRC7_FM1: level = runtime.param.levels.levelVRC7FM1; break;
		case CHANNEL_VRC7_FM2: level = runtime.param.levels.levelVRC7FM2; break;
		case CHANNEL_VRC7_FM3: level = runtime.param.levels.levelVRC7FM3; break;
		case CHANNEL_VRC7_FM4: level = runtime.param.levels.levelVRC7FM4; break;
		case CHANNEL_VRC7_FM5: level = runtime.param.levels.levelVRC7FM5; break;
		case CHANNEL_VRC7_FM6: level = runtime.param.levels.levelVRC7FM6; break;
		
		case CHANNEL_S5B_SQUARE1: level = runtime.param.levels.levelS5BSquare1; break;
		case CHANNEL_S5B_SQUARE2: level = runtime.param.levels.levelS5BSquare2; break;
		case CHANNEL_S5B_SQUARE3: level = runtime.param.levels.levelS5BSquare3; break;
		
		default: level = 1.0f; break;
		}
		
		if (level > 1) {
			level = 1.0f;
		} else if (level < 0) {
			level = 0;
		}
		
		return level;
	}

}
