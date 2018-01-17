package com.zdream.nsfplayer.nsf.xgm;

import java.util.ArrayList;

import com.zdream.nsfplayer.nsf.audio.NsfAudio;
import com.zdream.nsfplayer.nsf.audio.NsfAudioException;
import com.zdream.nsfplayer.xgm.device.Bus;
import com.zdream.nsfplayer.xgm.device.IDevice;
import com.zdream.nsfplayer.xgm.device.Layer;
import com.zdream.nsfplayer.xgm.device.audio.Mixer;
import com.zdream.nsfplayer.xgm.device.cpu.NesCPU;
import com.zdream.nsfplayer.xgm.device.memory.NesBank;
import com.zdream.nsfplayer.xgm.device.memory.NesMem;

/**
 * <p>任务是将 NSF 格式的数据 {@link NsfAudio} 输出为 byte[]
 * @author Zdream
 * @version v0.1
 * @date 2018-01-16
 */
public class NsfConverter {
	
	/* ***********
	 *	输出数据  *
	 *********** */
	
	/**
	 * 每秒采样数
	 */
	public final int sampleRate;
	
	/**
	 * 每个采样的数据大小, 按位计数
	 */
	public final int sampleSizeInBits;
	
	/**
	 * 轨道数, 1 是单声道, 2 是立体声, 等等
	 */
	public final int channels;
	
	public static final byte
		NTSC = 0,
		PAL = 1;
	
	/**
	 * 机器类型. 只能为 {@link #NTSC} 或 {@link #PAL}. 默认 {@link #NTSC}
	 */
	byte machine = NTSC;
	
	/**
	 * @param sampleRate
	 *   每秒采样数, 默认 48000
	 * @param sampleSizeInBits
	 *   每个采样的数据大小, 按位计数, 默认 16
	 * @param channels
	 *   轨道数, 1 是单声道, 2 是立体声, 等等, 默认 2
	 */
	public NsfConverter(int sampleRate, int sampleSizeInBits, int channels) {
		this.sampleRate = sampleRate;
		this.sampleSizeInBits = sampleSizeInBits;
		this.channels = channels;
		
		initDevices();
	}
	
	/**
	 * @return
	 * 机器类型. {@link #NTSC} 或 {@link #PAL}
	 */
	public byte getMachine() {
		return machine;
	}
	
	/**
	 * @param machine
	 * 机器类型. 只能为 {@link #NTSC} 或 {@link #PAL}
	 */
	public void setMachine(byte machine) throws NsfAudioException {
		if (machine != NTSC || machine != PAL) {
			throw new NsfAudioException("machine: " + machine + " 数值非法");
		}
		this.machine = machine;
	}
	
	/* ***********
	 *	NSF 部分  *
	 *********** */
	
	/**
	 * 存储, 等待播放的 NSF 音频
	 */
	NsfAudio audio;
	
	NsfConfig config;
	
	/**
	 * 模拟内存
	 */
	NesMem mem;
	
	/**
	 * 模拟处理器
	 */
	NesCPU cpu;
	
	/**
	 * 
	 */
	NesBank bank;
	
	Bus apu_bus;
	Layer stack;
	Layer layer;
	Mixer mixer;
	
	/**
	 * 虚拟音频设备, 比如 APU 部分, DMC 部分, VRC6 部分等
	 */
	ArrayList<IDevice> soundDevices;
	
	
	
/*FDS_TRK0 = 5, MMC5_TRK0 = 6,
		MMC5_TRK1 = 7, MMC5_TRK2 = 8, FME7_TRK0 = 9, FME7_TRK1 = 10, FME7_TRK2 = 11, FME7_TRK3 = 12, FME7_TRK4 = 13,
		VRC6_TRK0 = 14, VRC6_TRK1 = 15, VRC6_TRK2 = 16, VRC7_TRK0 = 17, VRC7_TRK1 = 18, VRC7_TRK2 = 19,
		VRC7_TRK3 = 20, VRC7_TRK4 = 21, VRC7_TRK5 = 22, N106_TRK0 = 23, N106_TRK1 = 24, N106_TRK2 = 25,
		N106_TRK3 = 26, N106_TRK4 = 27, N106_TRK5 = 28, N106_TRK6 = 29, N106_TRK7 = 30, NES_TRACK_MAX = 31;*/

	/**
	 * 初始化 NSF 部分
	 */
	void initDevices() {
		config = new NsfConfig();
		
		mem = new NesMem();
		cpu = new NesCPU(config.getInt("cpu_clock", NesCPU.DEFAULT_CLOCK));
		cpu.setMemory(mem);
		
		bank = new NesBank();
		
		apu_bus = new Bus();
		stack = new Layer();
		layer = new Layer();
		mixer = new Mixer();
		
		soundDevices = new ArrayList<>();
	}
	
	/**
	 * 让该转化器作准备, 在转换前先读取 audio 中的参数来重置自己
	 * @param audio
	 *   NSF 音频数据
	 */
	public void ready(NsfAudio audio) {
		System.out.println(Thread.currentThread().getStackTrace()[1]);
		System.out.println(audio);
		
		this.audio = audio;
		
		// 1. 处理 bankswitch 部分
		
		int i, bmax = 0;
		for (i = 0; i < 8; i++)
			if (bmax < audio.bankswitch[i])
				bmax = audio.bankswitch[i];
		
		mem.setImage(audio.body, audio.load_address, audio.body.length);
		if (bmax != 0) {
			bank.setImage(audio.body, audio.load_address, audio.body.length);
			for (i = 0; i < 8; i++)
				bank.setBankDefault(i + 8, audio.bankswitch[i]);
		}
		
		// 2. 重置所有关键设备, 这里将放弃循环检测器
		stack.detachAll();
		layer.detachAll();
		mixer.detachAll();
		apu_bus.detachAll();
		
		if (bmax != 0)
			layer.attach(bank);
		layer.attach(mem);
		
		// 3. 按照
		
	}

}
