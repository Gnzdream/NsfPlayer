package zdream.nsfplayer.nsf.renderer;

import java.util.ArrayList;
import java.util.HashMap;

import zdream.nsfplayer.core.IResetable;
import zdream.nsfplayer.core.NsfRateConverter;
import zdream.nsfplayer.nsf.audio.NsfAudio;
import zdream.nsfplayer.nsf.context.NsfClockCounter;
import zdream.nsfplayer.nsf.device.AbstractSoundChip;
import zdream.nsfplayer.nsf.device.DeviceManager;
import zdream.nsfplayer.nsf.device.cpu.NesCPU;
import zdream.nsfplayer.nsf.device.memory.NesBank;
import zdream.nsfplayer.nsf.device.memory.NesMem;
import zdream.nsfplayer.nsf.executor.IN163ReattachListener;
import zdream.nsfplayer.sound.blip.BlipMixerConfig;
import zdream.nsfplayer.sound.blip.BlipSoundMixer;
import zdream.nsfplayer.sound.mixer.IMixerConfig;
import zdream.nsfplayer.sound.mixer.SoundMixer;
import zdream.nsfplayer.sound.xgm.XgmMixerConfig;
import zdream.nsfplayer.sound.xgm.XgmSoundMixer;

/**
 * Nsf 运行时状态
 * 
 * @author Zdream
 * @since v0.2.4
 */
public class NsfRuntime implements IResetable {
	
	/* **********
	 *   成员   *
	 ********** */
	
	public NsfAudio audio;
	@Deprecated
	public NsfRendererConfig config;
	public final NsfParameter param = new NsfParameter();
	public final DeviceManager manager;
	public final NsfClockCounter clockCounter;
	
	// 存储部件
	public final NesMem mem;
	public final NesBank bank;
	
	// 执行部件
	public final NesCPU cpu;
	
	// 模拟声卡
	/**
	 * 映射关系: 轨道号 - 虚拟声卡.
	 * <br>可能多个轨道号会映射到一个声卡上
	 */
	public final HashMap<Byte, AbstractSoundChip> chips = new HashMap<>();
	
	/**
	 * 音频混音器
	 */
	@Deprecated
	public SoundMixer mixer;
	
	/**
	 * N163 重连的监听器
	 */
	public final ArrayList<IN163ReattachListener> n163Lsners = new ArrayList<>();
	
	/* **********
	 *  初始化  *
	 ********** */
	
	public NsfRuntime(NsfRendererConfig config) {
		this.config = config;
		param.levels.copyFrom(config.channelLevels);
		manager = new DeviceManager(this);
		clockCounter = new NsfClockCounter(param);
		
		mem = new NesMem();
		bank = new NesBank();
		cpu = new NesCPU();
	}
	
	public NsfRuntime() {
		this(new NsfRendererConfig());
	}
	
	public void init() {
		initParam();
		initMixer();
		
		// 这个方法没干事情
		manager.init();
	}
	
	private void initParam() {
		param.sampleRate = config.sampleRate; // 默认: 48000
	}
	
	private void initMixer() {
		IMixerConfig mixerConfig = config.mixerConfig;
		if (mixerConfig == null) {
			mixerConfig = new XgmMixerConfig();
		}
		
		if (mixerConfig instanceof XgmMixerConfig) {
			// 采用 Xgm 音频混合器 (原 NsfPlayer 使用的)
			XgmSoundMixer mixer = new XgmSoundMixer();
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

	@Override
	public void reset() {
		manager.reset();
		mixer.reset();
	}
	
	/* **********
	 *   操作   *
	 ********** */
	
	/**
	 * <p>通知混音器, 当前帧的渲染开始了.
	 * <p>这个方法原本用于通知混音器, 如果本帧的渲染速度需要变化,
	 * 可以通过该方法, 让混音器提前对此做好准备, 修改存储的采样数容量, 从而调节播放速度.
	 * </p>
	 * @since v0.2.9
	 */
	void mixerReady() {
		mixer.readyBuffer();
	}
	
	/* **********
	 * 硬件部分 *
	 ********** */
	
	/**
	 * 重读 Nsf 音频
	 */
	public void reload() {
		manager.reload();
	}

}
