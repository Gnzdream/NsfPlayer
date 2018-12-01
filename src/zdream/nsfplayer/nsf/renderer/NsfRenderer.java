package zdream.nsfplayer.nsf.renderer;

import java.util.Set;

import zdream.nsfplayer.core.AbstractNsfRenderer;
import zdream.nsfplayer.core.CycleCounter;
import zdream.nsfplayer.core.FloatCycleCounter;
import zdream.nsfplayer.core.NsfCommonParameter;
import zdream.nsfplayer.core.NsfRateConverter;
import zdream.nsfplayer.core.NsfStatic;
import zdream.nsfplayer.mixer.IMixerChannel;
import zdream.nsfplayer.mixer.IMixerConfig;
import zdream.nsfplayer.mixer.IMixerHandler;
import zdream.nsfplayer.mixer.ISoundMixer;
import zdream.nsfplayer.mixer.blip.BlipMixerConfig;
import zdream.nsfplayer.mixer.blip.BlipSoundMixer;
import zdream.nsfplayer.mixer.xgm.XgmMixerConfig;
import zdream.nsfplayer.mixer.xgm.XgmMultiSoundMixer;
import zdream.nsfplayer.nsf.audio.NsfAudio;
import zdream.nsfplayer.nsf.device.chip.NesN163;
import zdream.nsfplayer.nsf.executor.IN163ReattachListener;
import zdream.nsfplayer.nsf.executor.NsfExecutor;
import zdream.nsfplayer.sound.AbstractNsfSound;

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
	
	/**
	 * 算每帧多少时钟, 计入 speed 影响
	 */
	public final NsfRateConverter rate;
	
	/**
	 * 算每帧多少采样, 计入 speed 影响
	 */
	private final FloatCycleCounter apuCounter = new FloatCycleCounter();
	
	private final NsfCommonParameter param = new NsfCommonParameter();
	
	/**
	 * 管理 executor 的 tick 次数
	 * 不计播放速度影响, 计算每帧采样数
	 */
	private final CycleCounter exeCycle = new CycleCounter();
	
	/**
	 * 音频混音器
	 */
	public ISoundMixer mixer;
	
	public NsfRenderer() {
		this(new NsfRendererConfig());
	}
	
	public NsfRenderer(NsfRendererConfig config) {
		param.sampleRate = config.sampleRate;
		param.frameRate = frameRate;
		param.levels.copyFrom(config.channelLevels);
		
		executor.setRegion(config.region);
		executor.setRate(config.sampleRate);
		executor.addN163ReattachListener(n163lsner);
		
		initMixer(config);
		rate = new NsfRateConverter(param);
		exeCycle.setParam(config.sampleRate, this.frameRate);
	}
	
	public void initMixer(NsfRendererConfig config) {
		IMixerConfig mixerConfig = config.mixerConfig;
		if (mixerConfig == null) {
			mixerConfig = new XgmMixerConfig();
		}
		
		if (mixerConfig instanceof XgmMixerConfig) {
			// 采用 Xgm 音频混合器 (原 NsfPlayer 使用的)
			XgmMultiSoundMixer mixer = new XgmMultiSoundMixer();
			mixer.setConfig((XgmMixerConfig) mixerConfig);
			mixer.param = param;
			this.mixer = mixer;
		} else if (mixerConfig instanceof BlipMixerConfig) {
			// 采用 Blip 音频混合器 (原 FamiTracker 使用的)
			BlipSoundMixer mixer = new BlipSoundMixer();
			mixer.frameRate = 50; // 帧率在最低值, 这样可以保证高帧率 (比如 60) 也能兼容
			mixer.sampleRate = config.sampleRate;
			mixer.setConfig((BlipMixerConfig) mixerConfig);
			mixer.param = param;
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
		executor.ready(track);
	}
	
	private void ready0(NsfAudio audio, int track) {
		executor.ready(audio, track);
		
		mixer.reset();
		connectChannels();
		
		super.resetCounterParam(frameRate, param.sampleRate);
		clearBuffer();
		
		rate.onParamUpdate(frameRate, executor.cycleRate());
		apuCounter.setParam(countCycle(param.speed), param.sampleRate);
	}
	
	/**
	 * <p>连接执行构件中的 sound 和渲染构件的轨道.
	 * <p>这个方法可以暂时确定所有轨道号
	 * </p>
	 */
	private void connectChannels() {
		mixer.detachAll();
		Set<Byte> channels = executor.allChannelSet();
		
		// 计算总轨道数. 总数 + 8 为了 N163 轨道的数据能补上.
		this.channels = new ChannelParam[channels.size() + 8];
		
		int index = 0;
		int mixerChannel = -1;
		for (byte channelCode: channels) {
			AbstractNsfSound sound = executor.getSound(channelCode);
			if (sound != null) {
				mixerChannel = mixer.allocateChannel(channelCode);
				IMixerChannel mix = mixer.getMixerChannel(mixerChannel);
				sound.setOut(mix);
				
				// 音量
				mix.setLevel(getInitLevel(channelCode));
				
				// TODO 告诉混音器更多的信息, 包括发声器的输出采样率 (NSF 的为 177万, mpeg 的为 44100 或 48000 等)
			}
			
			// 缓存轨道号
			ChannelParam p = new ChannelParam();
			p.channelCode = channelCode;
			p.mixerChannel = mixerChannel;
			this.channels[index] = p;
			index++;
		}
	}
	
	/* **********
	 * 渲染部分 *
	 ********** */

	@Override
	protected int renderFrame() {
		int ret = countNextFrame();
		param.sampleInCurFrame = ret;
		rate.doConvert();
		mixerReady();
		
		final int exeCount = exeCycle.tick();
		for (int i = 0; i < exeCount; i++) {
			executor.tick();
			processSounds(apuCounter.tick());
		}
		endFrame();

		// 从 mixer 中读取数据
		readMixer();
		
		return ret;
	}
	
	@Override
	protected int skipFrame() {
		int ret = countNextFrame();
		param.sampleInCurFrame = ret;
		rate.doConvert();
		
		final int exeCount = exeCycle.tick();
		for (int i = 0; i < exeCount; i++) {
			executor.tick();
		}
		endFrame();

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

	/**
	 * 所有的 sound 调用 sound.process(freqPerFrame);
	 */
	private void processSounds(int freq) {
		if (channels == null) {
			return;
		}
		
		for (ChannelParam p : channels) {
			if (p == null) {
				continue;
			}
			executor.getSound(p.channelCode).process(freq);
		}
	}
	
	/**
	 * 所有的 sound 调用 sound.endFrame();
	 */
	private void endFrame() {
		if (channels == null) {
			return;
		}
		
		for (ChannelParam p : channels) {
			if (p == null) {
				continue;
			}
			executor.getSound(p.channelCode).endFrame();
		}
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
		return executor.getCurrentTrack();
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
		AbstractNsfSound sound = executor.getSound(channelCode);
		if (sound != null) {
			sound.setMask(mask);
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
		return executor.getSound(channelCode).isMask();
	}
	
	@Override
	public void setSpeed(float speed) {
		if (speed > 10) {
			speed = 10;
		} else if (speed < 0.1f) {
			speed = 0.1f;
		}
		
		param.speed = speed;

		super.resetCounterParam(frameRate, param.sampleRate);
		apuCounter.setParam(countCycle(speed), param.sampleRate);
		rate.onParamUpdate();
	}
	
	@Override
	public float getSpeed() {
		return param.speed;
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
					ChannelParam p = searchParam(channelCode);
					if (p == null) {
						// 创建连接
						int mixerChannel = mixer.allocateChannel(channelCode);
						IMixerChannel mix = mixer.getMixerChannel(mixerChannel);
						sound.setOut(mix);
						mix.setLevel(getInitLevel(channelCode));
						
						p = new ChannelParam();
						p.channelCode = channelCode;
						p.mixerChannel = mixerChannel;
						
						putChannelParam(p);
					}
				} else {
					ChannelParam p = searchParam(channelCode);
					if (p != null) {
						// 删除连接
						
						int mixerChannel = p.mixerChannel;
						mixer.detach(mixerChannel);
						
						removeChannelParam(p);
					}
				}
			}
		}
		
		private ChannelParam searchParam(byte code) {
			for (ChannelParam p : channels) {
				if (p == null) {
					break;
				}
				if (p.channelCode == code) {
					return p;
				}
			}
			
			return null;
		}
		
		private void putChannelParam(ChannelParam p) {
			for (int i = 0; i < channels.length; i++) {
				if (channels[i] == null) {
					channels[i] = p;
					return;
				}
			}
			
			// 数组需要扩充
		}
		
		private void removeChannelParam(ChannelParam p) {
			for (int i = 0; i < channels.length; i++) {
				if (channels[i] == p) {
					channels[i] = null;
					return;
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
		case CHANNEL_2A03_PULSE1: level = param.levels.level2A03Pules1; break;
		case CHANNEL_2A03_PULSE2: level = param.levels.level2A03Pules2; break;
		case CHANNEL_2A03_TRIANGLE: level = param.levels.level2A03Triangle; break;
		case CHANNEL_2A03_NOISE: level = param.levels.level2A03Noise; break;
		case CHANNEL_2A03_DPCM: level = param.levels.level2A03DPCM; break;

		case CHANNEL_VRC6_PULSE1: level = param.levels.levelVRC6Pules1; break;
		case CHANNEL_VRC6_PULSE2: level = param.levels.levelVRC6Pules2; break;
		case CHANNEL_VRC6_SAWTOOTH: level = param.levels.levelVRC6Sawtooth; break;

		case CHANNEL_MMC5_PULSE1: level = param.levels.levelMMC5Pules1; break;
		case CHANNEL_MMC5_PULSE2: level = param.levels.levelMMC5Pules2; break;
		
		case CHANNEL_FDS: level = param.levels.levelFDS; break;
		
		case CHANNEL_N163_1: level = param.levels.levelN163Namco1; break;
		case CHANNEL_N163_2: level = param.levels.levelN163Namco2; break;
		case CHANNEL_N163_3: level = param.levels.levelN163Namco3; break;
		case CHANNEL_N163_4: level = param.levels.levelN163Namco4; break;
		case CHANNEL_N163_5: level = param.levels.levelN163Namco5; break;
		case CHANNEL_N163_6: level = param.levels.levelN163Namco6; break;
		case CHANNEL_N163_7: level = param.levels.levelN163Namco7; break;
		case CHANNEL_N163_8: level = param.levels.levelN163Namco8; break;
		
		case CHANNEL_VRC7_FM1: level = param.levels.levelVRC7FM1; break;
		case CHANNEL_VRC7_FM2: level = param.levels.levelVRC7FM2; break;
		case CHANNEL_VRC7_FM3: level = param.levels.levelVRC7FM3; break;
		case CHANNEL_VRC7_FM4: level = param.levels.levelVRC7FM4; break;
		case CHANNEL_VRC7_FM5: level = param.levels.levelVRC7FM5; break;
		case CHANNEL_VRC7_FM6: level = param.levels.levelVRC7FM6; break;
		
		case CHANNEL_S5B_SQUARE1: level = param.levels.levelS5BSquare1; break;
		case CHANNEL_S5B_SQUARE2: level = param.levels.levelS5BSquare2; break;
		case CHANNEL_S5B_SQUARE3: level = param.levels.levelS5BSquare3; break;
		
		default: level = 1.0f; break;
		}
		
		if (level > 1) {
			level = 1.0f;
		} else if (level < 0) {
			level = 0;
		}
		
		return level;
	}
	
	/**
	 * 计算指定 speed 影响之后, 实际每秒运行的时钟数
	 * @param speed
	 *   速度
	 * @return
	 *   实际每秒运行的时钟数
	 */
	private int countCycle(float speed) {
		int cycle = executor.cycleRate();
		if (speed != 1 && speed > 0) {
			cycle = (int) (cycle / speed);
		}
		return cycle;
	}
	
	class ChannelParam {
		/**
		 * 轨道号
		 */
		byte channelCode;
		/**
		 * Mixer 轨道标识号
		 */
		int mixerChannel;
	}
	private ChannelParam[] channels;

}
