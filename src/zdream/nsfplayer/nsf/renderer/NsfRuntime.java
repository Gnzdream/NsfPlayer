package zdream.nsfplayer.nsf.renderer;

import zdream.nsfplayer.nsf.audio.NsfAudio;
import zdream.nsfplayer.nsf.device.DeviceManager;
import zdream.nsfplayer.nsf.device.cpu.NesCPU;
import zdream.nsfplayer.nsf.device.memory.NesBank;
import zdream.nsfplayer.nsf.device.memory.NesMem;
import zdream.nsfplayer.sound.mixer.SoundMixer;

/**
 * Nsf 运行时状态
 * 
 * @author Zdream
 * @since v0.2.4
 */
public class NsfRuntime {
	
	/* **********
	 *   成员   *
	 ********** */
	
	public NsfAudio audio;
	public NsfRendererConfig config;
	public DeviceManager manager;
	
	// 存储部件
	public NesMem mem;
	public NesBank bank;
	
	// 执行部件
	public NesCPU cpu;
	
	/**
	 * 音频合成器（混音器）
	 */
	public SoundMixer mixer;
	
	{
		config = new NsfRendererConfig();
		manager = new DeviceManager(this);
		
		mem = new NesMem();
		//cpu = new NesCPU(clock)
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
