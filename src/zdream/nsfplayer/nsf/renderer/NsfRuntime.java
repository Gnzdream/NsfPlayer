package zdream.nsfplayer.nsf.renderer;

import java.util.HashMap;

import zdream.nsfplayer.core.IResetable;
import zdream.nsfplayer.ftm.renderer.FamiTrackerParameter;
import zdream.nsfplayer.nsf.audio.NsfAudio;
import zdream.nsfplayer.nsf.device.AbstractSoundChip;
import zdream.nsfplayer.nsf.device.DeviceManager;
import zdream.nsfplayer.nsf.device.cpu.NesCPU;
import zdream.nsfplayer.nsf.device.memory.NesBank;
import zdream.nsfplayer.nsf.device.memory.NesMem;
import zdream.nsfplayer.sound.mixer.SoundMixer;
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
	public NsfRendererConfig config;
	public FamiTrackerParameter param = new FamiTrackerParameter();
	public DeviceManager manager;
	
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
	 * 音频合成器（混音器）
	 */
	public SoundMixer mixer;
	
	/* **********
	 *  初始化  *
	 ********** */
	
	{
		config = new NsfRendererConfig();
		manager = new DeviceManager(this);
		
		mem = new NesMem();
		bank = new NesBank();
		cpu = new NesCPU();
	}
	
	void init() {
		initParam();
		initMixer();
		manager.init();
	}
	
	private void initParam() {
		param.sampleRate = config.sampleRate; // 默认: 48000
	}
	
	private void initMixer() {
		
		// 可以采用 Blip 音频混合器 (FamiTracker 使用的)
//		BlipSoundMixer mixer = new BlipSoundMixer();
//		mixer.sampleRate = setting.sampleRate;
//		mixer.frameRate = setting.frameRate;
//		mixer.bassFilter = setting.bassFilter;
//		mixer.trebleDamping = setting.trebleDamping;
//		mixer.trebleFilter = setting.trebleFilter;
//		
//		mixer.param = param;
//		this.mixer = mixer;

		// 也可以采用 Xgm 音频混合器 (NsfPlayer 使用的)
		XgmSoundMixer mixer = new XgmSoundMixer();
		mixer.param = param;
		this.mixer = mixer;

		mixer.init();
	}

	@Override
	public void reset() {
		manager.reset();
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
