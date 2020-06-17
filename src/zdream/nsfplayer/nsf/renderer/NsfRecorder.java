package zdream.nsfplayer.nsf.renderer;

import java.util.Set;

import zdream.nsfplayer.core.CycleCounter;
import zdream.nsfplayer.core.FloatCycleCounter;
import zdream.nsfplayer.core.INsfRendererHandler;
import zdream.nsfplayer.core.NsfCommonParameter;
import zdream.nsfplayer.core.NsfRateConverter;
import zdream.nsfplayer.core.NsfStatic;
import zdream.nsfplayer.nsf.audio.NsfAudio;
import zdream.nsfplayer.nsf.executor.NsfExecutor;
import zdream.nsfplayer.sound.AbstractNsfSound;
import zdream.nsfplayer.sound.SoundPulse;

/**
 * <p>NSF 记录器.
 * <p>将 NSF 的内容按照一定内容记录并输出, 以便后续播放、加工工作.
 * 该类的使用场景是, 在服务器端需要输出 NSF 播放的相关数据, 传输到前端浏览器渲染播放.
 * 这里要求传输的数据不能太高 (所以 {@link NsfRenderer} 就不能用了),
 * 速度要快 ({@link NsfRenderer} 的音频压缩成 .mp3、.ogg、.m4a 格式显然就太慢了)。
 * <p>当前计划支持: 2A03.
 * </p>
 * 
 * @author Zdream
 * @since v0.3.2
 */
public class NsfRecorder implements INsfRendererHandler<NsfAudio> {
	
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
	 * <p>N163 轨道数.
	 * <p>如果未确定, 该值为 -1.
	 * </p>
	 * @since v0.3.2
	 */
//	private int n163ChannelCount = -1;
	
	/**
	 * <p>是否正在轨道初始化中.
	 * <p>该值仅用于在 N163 轨道初始化时使用. N163 轨道数量的确定在 reset() 或者运行时确定,
	 * 如果在 reset() 时确定则会直接操作没有初始化过的轨道, 发生异常.
	 * 因此需要该值来确定该渲染器的状态.
	 * </p>
	 * @since v0.3.2
	 */
//	private boolean channelInit;
	
	public NsfRecorder() {
		this(new NsfRendererConfig());
	}
	
	public NsfRecorder(NsfRendererConfig config) {
		param.sampleRate = config.sampleRate;
		param.frameRate = frameRate;
		param.levels.copyFrom(config.channelLevels);
		
		executor.setRegion(config.region);
		executor.setRate(config.sampleRate);
		
		rate = new NsfRateConverter(param);
		exeCycle.setParam(config.sampleRate, this.frameRate);
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
//		channelInit = true;
		executor.ready(audio, track);
		
		resetCounterParam(frameRate, param.sampleRate);
		rate.onParamUpdate(frameRate, executor.cycleRate());
		apuCounter.setParam(countCycle(param.speed), param.sampleRate);
		
		connectChannels(audio.useN163());
//		clearBuffer();
		
//		channelInit = false;
	}
	
	/**
	 * <p>连接执行构件中的 sound 和渲染构件的轨道.
	 * <p>这个方法可以暂时确定所有轨道号
	 * </p>
	 * @param useN163
	 *   该音频是否使用了 N163 芯片
	 */
	private void connectChannels(boolean useN163) {
//		mixer.detachAll();
		Set<Byte> channels = executor.allChannelSet();
		
		// 计算总轨道数.
		// 当使用了 N163 轨道, 但轨道数量不确定时, 总数 + 8 为了 N163 轨道的数据能补上.
//		if (useN163 && n163ChannelCount == -1) {
//			this.channels = new ChannelParam[channels.size() + 8];
//		} else {
//			this.channels = new ChannelParam[channels.size()];
//		}
		
		for (byte channelCode: channels) {
			AbstractNsfSound sound = executor.getSound(channelCode);
			if (sound != null) {
//				mixerChannel = mixer.allocateChannel(channelCode);
//				IMixerChannel mix = mixer.getMixerChannel(mixerChannel);
//				sound.setOut(mix);
				
				sound.setMuted(true); // 否则报空指针错误
				
				// TODO 这里插入记录器
				
				// 音量
//				mix.setLevel(getInitLevel(channelCode));
				
				// TODO 告诉混音器更多的信息, 包括发声器的输出采样率 (NSF 的为 177万, mpeg 的为 44100 或 48000 等)
			}
			
			// 缓存轨道号
			ChannelParam p = new ChannelParam();
			p.channelCode = channelCode;
			p.mixerChannel = -1;
		}
	}
	
	/* **********
	 * 渲染部分 *
	 ********** */

	int ic = 0;
	boolean lastEnable = false;
	int lastPeriod = -1;
	int lastDuty = -1;
	int lastVolume = -1;
	
	public int renderFrame() {
		int ret = countNextFrame();
		param.sampleInCurFrame = ret;
		rate.doConvert();
//		mixerReady();
		
		SoundPulse p = (SoundPulse) executor.getSound(CHANNEL_2A03_PULSE1);
		final int exeCount = exeCycle.tick();
		for (int i = 0; i < exeCount; i++) {
			executor.tick();
			processSounds(apuCounter.tick());
			
			if (lastPeriod != p.period || lastDuty != p.dutyLength || lastVolume != p.fixedVolume || lastEnable != p.isEnable()) {
				System.out.println(String.format("%6x:%s,%3x,%d,%x", ic++,
						p.isEnable() ? '1' : '0', p.period, p.dutyLength, p.fixedVolume));
				lastPeriod = p.period;
				lastDuty = p.dutyLength;
				lastVolume = p.fixedVolume;
				lastEnable = p.isEnable();
			}
			ic++;
		}
		endFrame();
		
		ic = ((ic & 0x7FFFF000) + 0x1000);

		// 从 mixer 中读取数据
//		readMixer();
		
		return ret;
	}
	
	/**
	 * 从 Mixer 中读取音频数据
	 */
//	private void readMixer() {
//		mixer.finishBuffer();
//		mixer.readBuffer(data, 0, data.length);
//	}
	
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
	
//	/**
//	 * <p>通知混音器, 当前帧的渲染开始了.
//	 * <p>这个方法原本用于通知混音器, 如果本帧的渲染速度需要变化,
//	 * 可以通过该方法, 让混音器提前对此做好准备, 修改存储的采样数容量, 从而调节播放速度.
//	 * </p>
//	 * @since v0.2.9
//	 */
//	private void mixerReady() {
//		mixer.readyBuffer();
//	}

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
			executor.getSound(p.channelCode).process(freq); // 不知道是否需要
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
	 * 不支持设置某个轨道的音量
	 */
	public void setLevel(byte channelCode, float level) {
		// 没用的
	}
	
	/**
	 * 不支持获得某个轨道的音量
	 */
	public float getLevel(byte channelCode) throws NullPointerException {
		return 1f;
	}
	
	/**
	 * 设置轨道是否发出声音
	 * @param channelCode
	 *   轨道号
	 * @param mask
	 *   false, 使该轨道发声; true, 则静音
	 * @since v0.2.4
	 */
	public void setChannelMuted(byte channelCode, boolean mask) {
		AbstractNsfSound sound = executor.getSound(channelCode);
		if (sound != null) {
			sound.setMuted(mask);
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
	public boolean isChannelMuted(byte channelCode) throws NullPointerException {
		return executor.getSound(channelCode).isMuted();
	}
	
	public void setSpeed(float speed) {
		if (speed > 10) {
			speed = 10;
		} else if (speed < 0.1f) {
			speed = 0.1f;
		}
		
		param.speed = speed;

		resetCounterParam(frameRate, param.sampleRate);
		apuCounter.setParam(countCycle(speed), param.sampleRate);
		rate.onParamUpdate();
	}
	
	public float getSpeed() {
		return param.speed;
	}
	
//	class N163ReattachListener implements IN163ReattachListener {
//
//		@Override
//		public void onReattach(int n163ChannelCount) {
//			NsfRenderer.this.n163ChannelCount = n163ChannelCount;
//			if (channelInit) {
//				return;
//			}
//			
//			for (int i = 0; i < 8; i++) {
//				byte channelCode = (byte) (NesN163.CHANNEL_N163_1 + i);
//				AbstractNsfSound sound = executor.getSound(channelCode);
//				if (sound != null) {
//					ChannelParam p = searchParam(channelCode);
//					if (p == null) {
//						// 创建连接
//						int mixerChannel = mixer.allocateChannel(channelCode);
//						IMixerChannel mix = mixer.getMixerChannel(mixerChannel);
//						sound.setOut(mix);
//						mix.setLevel(getInitLevel(channelCode));
//						
//						p = new ChannelParam();
//						p.channelCode = channelCode;
//						p.mixerChannel = mixerChannel;
//						
//						putChannelParam(p);
//					}
//				} else {
//					ChannelParam p = searchParam(channelCode);
//					if (p != null) {
//						// 删除连接
//						
//						int mixerChannel = p.mixerChannel;
//						mixer.detach(mixerChannel);
//						
//						removeChannelParam(p);
//					}
//				}
//			}
//		}
//		
//		private ChannelParam searchParam(byte code) {
//			for (ChannelParam p : channels) {
//				if (p == null) {
//					continue;
//				}
//				if (p.channelCode == code) {
//					return p;
//				}
//			}
//			
//			return null;
//		}
//		
//		private void putChannelParam(ChannelParam p) {
//			for (int i = 0; i < channels.length; i++) {
//				if (channels[i] == null) {
//					channels[i] = p;
//					return;
//				}
//			}
//			
//			// 数组需要扩充
//		}
//		
//		private void removeChannelParam(ChannelParam p) {
//			for (int i = 0; i < channels.length; i++) {
//				if (channels[i] == p) {
//					channels[i] = null;
//					return;
//				}
//			}
//		}
//		
//	}
//	private final N163ReattachListener n163lsner = new N163ReattachListener();
	
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
	
	/* **********
	 *   补充   *
	 ********** */
	
	/**
	 * 采样率计数器.
	 * @since v0.2.5
	 */
	protected final CycleCounter counter = new CycleCounter();
	
	private void resetCounterParam(int maxFrameCount, int maxSampleCount) {
		float speed = getSpeed();
		int cycle = maxSampleCount;
		if (speed != 1) {
			cycle = (int) (cycle / speed);
		}
		
		// 重置计数器
		counter.setParam(cycle, maxFrameCount);
	}
	
	/**
	 * 计算下一帧需要的采样数 (每个声道), 已经将播放速度造成的影响计入
	 * @return
	 *   下一帧需要的采样数
	 */
	protected int countNextFrame() {
		return counter.tick();
	}

}
