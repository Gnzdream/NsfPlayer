package zdream.nsfplayer.nsf.device;

import zdream.nsfplayer.core.ERegion;
import zdream.nsfplayer.core.IResetable;
import zdream.nsfplayer.nsf.audio.NsfAudio;
import zdream.nsfplayer.nsf.device.chip.NesAPU;
import zdream.nsfplayer.nsf.device.chip.NesDMC;
import zdream.nsfplayer.nsf.device.chip.NesMMC5;
import zdream.nsfplayer.nsf.device.chip.NesVRC6;
import zdream.nsfplayer.nsf.device.cpu.NesCPU;
import zdream.nsfplayer.nsf.renderer.INsfRuntimeHolder;
import zdream.nsfplayer.nsf.renderer.NsfRendererConfig;
import zdream.nsfplayer.nsf.renderer.NsfRuntime;
import zdream.nsfplayer.sound.AbstractNsfSound;
import zdream.nsfplayer.sound.mixer.IMixerChannel;

import static zdream.nsfplayer.core.ERegion.*;
import static zdream.nsfplayer.core.NsfStatic.*;

import java.util.Iterator;
import java.util.Map.Entry;

/**
 * 用于管理 Nsf 运行时状态的所有硬件设备的管理者
 * 
 * @author Zdream
 * @since v0.2.4
 */
public class DeviceManager implements INsfRuntimeHolder, IResetable {
	
	NsfRuntime runtime;

	public DeviceManager(NsfRuntime runtime) {
		this.runtime = runtime;
		
		apu = new NesAPU(runtime);
		dmc = new NesDMC(runtime);
		vrc6 = new NesVRC6(runtime);
		mmc5 = new NesMMC5(runtime);
	}

	@Override
	public NsfRuntime getRuntime() {
		return runtime;
	}
	
	public void init() {
		initSoundChip();
	}

	/* **********
	 *   参数   *
	 ********** */
	
	/**
	 * 实际采用的制式
	 */
	ERegion region;
	/**
	 * 正在播放的曲目号
	 */
	int song;
	
	public void setSong(int song) {
		if (song >= runtime.audio.total_songs) {
			this.song = runtime.audio.total_songs - 1;
		} else if (song < 0) {
			this.song = 0;
		} else {
			this.song = song;
		}
	}

	/* **********
	 * 连接设备 *
	 ********** */
	
	// 总线
	public final Bus apu_bus = new Bus();
	public final Layer stack = new Layer();
	public final Layer layer = new Layer();
	
	/**
	 * 将所有连接的设备全部拆开.
	 */
	private void detachAll() {
		stack.detachAll();
		layer.detachAll();
		apu_bus.detachAll();
		runtime.mixer.detachAll();
	}
	
	// 音频芯片
	public final NesAPU apu;
	public final NesDMC dmc;
	public final NesVRC6 vrc6;
	public final NesMMC5 mmc5;
	
	private void initSoundChip() {
		// 初始化声卡的部分数据.
		// 现在可以通过 runtime.param.sampleRate 获得采样率
		// 原本下面的工作在 NsfPlayer.setPlayFreq(double) 中
		// 但是均和 mixer 相关, 这里 mixer 相关的工作全部已经不在这里
		// 所以下面的代码是空的
	}
	
	/**
	 * 将声卡放到 runtime.chips 的映射中去.
	 * @param chip
	 */
	private void putSoundChipToRuntime(AbstractSoundChip chip) {
		byte[] channelCodes = chip.getAllChannelCodes();
		for (int i = 0; i < channelCodes.length; i++) {
			byte channelCode = channelCodes[i];
			runtime.chips.put(channelCode, chip);
		}
	}
	
	/**
	 * 所有的 sound 调用 sound.process(freqPerFrame);
	 */
	private void processSounds(int freq) {
		apu.beforeRender();
		dmc.beforeRender();
		
		if (runtime.audio.useVrc6()) {
			vrc6.beforeRender();
		}
		if (runtime.audio.useMmc5()) {
			mmc5.beforeRender();
		}
		
		// 其它的
		
		for (Iterator<Entry<Byte, AbstractSoundChip>> it = runtime.chips.entrySet().iterator(); it.hasNext();) {
			Entry<Byte, AbstractSoundChip> entry = it.next();
			byte channelCode = entry.getKey();
			entry.getValue().getSound(channelCode).process(freq);
		}
	}
	
	/**
	 * 所有的 sound 调用 sound.endFrame();
	 */
	private void endFrame() {
		for (Iterator<Entry<Byte, AbstractSoundChip>> it = runtime.chips.entrySet().iterator(); it.hasNext();) {
			Entry<Byte, AbstractSoundChip> entry = it.next();
			byte channelCode = entry.getKey();
			entry.getValue().getSound(channelCode).endFrame();
		}
	}

	/* **********
	 *   重置   *
	 ********** */

	@Override
	public void reset() {
		// 见 NsfPlayer.reset()
		
		region = confirmRegion(runtime.audio.pal_ntsc);
		switch (region) {
		case NTSC:
			runtime.cpu.NES_BASECYCLES = BASE_FREQ_NTSC;
			break;
		case PAL:
			runtime.cpu.NES_BASECYCLES = BASE_FREQ_PAL;
			break;
		case DENDY:
			runtime.cpu.NES_BASECYCLES = BASE_FREQ_DENDY;
			break;
		}
		
		// 由于RAM空间可能在播放后被修改, 因此需要重新加载
		reload();
		// 应用所有配置
		// config.notify(-1);
		stack.reset();
		// 总线重置后, CPU 也需要重置
		runtime.cpu.reset();
		resetCPUCounter();
		
		double speed;
		/*if (this.config.get("VSYNC_ADJUST").toInt() != 0)
			speed = ((region == REGION_NTSC) ? 60.0988 : 50.0070);
		else*/
		// NSTC 采用 speed_ntsc, PAL 和 DENDY 采用 speed_pal
		speed = 1000000.0 / ((region == NTSC) ? runtime.audio.speed_ntsc : runtime.audio.speed_pal);
		
		runtime.cpu.start(runtime.audio.init_address, runtime.audio.play_address,
				speed, this.song, (region == PAL) ? 1 : 0, 0);
		
		// 忽略 mask 部分
		// vrc7.setPatchSet(this.config.get("VRC7_PATCH").toInt());
	}

	/**
	 * 确定制式.
	 * NsfPlayer.getRegion(int)
	 * @param flags
	 * @return
	 */
	public ERegion confirmRegion(int flags) {
		// 用户指定的
		int pref = runtime.config.region;
		
		// 以用户指定的制式为准
		switch (pref) {
		case NsfRendererConfig.REGION_FORCE_NTSC:
			return ERegion.NTSC;
		case NsfRendererConfig.REGION_FORCE_PAL:
			return ERegion.PAL;
		case NsfRendererConfig.REGION_FORCE_DENDY:
			return ERegion.DENDY;
		}

		// 查看 flags 的数据内容来确定
		// single-mode NSF
		if (flags == 0)
			return ERegion.NTSC;
		if (flags == 1)
			return ERegion.PAL;

		if ((flags & 2) != 0) // dual mode
		{
			if (pref == 1)
				return ERegion.NTSC;
			if (pref == 2)
				return ERegion.PAL;
			// else pref == 0 or invalid, use auto setting based on flags bit
			return ((flags & 1) != 0) ? ERegion.PAL : ERegion.NTSC;
		}

		return ERegion.NTSC; // fallback for invalid flags
	}
	
	/**
	 * 重读 Nsf 音频
	 * 
	 * 这里参照 NsfPlayer 类, 见 NsfPlayer.reload()
	 */
	public void reload() {
		
		int maxBankswitch = reloadMemory();
		
		// 以下是原工程 NsfPlayer 的注释:
		// virtual machine controlling memory reads and writes
		// to various devices, expansions, etc.
		// 应用 read() 和 write() 方法来控制虚拟设备, 以及它们的拓展类（子类）.
		detachAll();
		runtime.chips.clear();
		
		// 下面开始连接设备
		
		// 这里忽略循环检测器 LoopDetector
		// 这里忽略 CPULogger
		
		if (maxBankswitch != 0) {
			layer.attach(runtime.bank);
		}
		layer.attach(runtime.mem);
		// dmc.setMemory(layer);
		
		// 连接音频芯片
		apu_bus.attach(apu);
		apu_bus.attach(dmc);
		// 先将声卡挂到 runtime 上去
		putSoundChipToRuntime(apu);
		putSoundChipToRuntime(dmc);
		// 混音器
		attachSoundChipAndMixer(apu); // mixer.attach(apu);
		attachSoundChipAndMixer(dmc); // mixer.attach(dmc);
		
		// apu_bus.attach(fsc); // FrameSequenceCounter
		stack.attach(apu_bus);
		
		if (runtime.audio.useVrc6()) {
			stack.attach(vrc6);
			putSoundChipToRuntime(vrc6);
			attachSoundChipAndMixer(vrc6);
		}
		if (runtime.audio.useMmc5()) {
			stack.attach(mmc5);
			putSoundChipToRuntime(mmc5);
			attachSoundChipAndMixer(mmc5);
		}
		// TODO 其它的芯片
		
		/*
		if (nsf.useMmc5) {
			stack.attach(sc[NsfPlayerConfig.MMC5]);
			mixer.attach(amp[NsfPlayerConfig.MMC5]);
		}
		if (nsf.useVrc6) {
			stack.attach(sc[NsfPlayerConfig.VRC6]);
			mixer.attach(amp[NsfPlayerConfig.VRC6]);
		}
		if (nsf.useVrc7) {
			stack.attach(sc[NsfPlayerConfig.VRC7]);
			mixer.attach(amp[NsfPlayerConfig.VRC7]);
		}
		if (nsf.useFme7) {
			stack.attach(sc[NsfPlayerConfig.FME7]);
			mixer.attach(amp[NsfPlayerConfig.FME7]);
		}
		if (nsf.useN106) {
			stack.attach(sc[NsfPlayerConfig.N106]);
			mixer.attach(amp[NsfPlayerConfig.N106]);
		}
		if (nsf.useFds) {
			boolean write_enable = (config.getDeviceOption(NsfPlayerConfig.FDS, NesFDS.OPT_WRITE_PROTECT).toInt() == 0);

			stack.attach(sc[NsfPlayerConfig.FDS]); // last before memory layer
			mixer.attach(amp[NsfPlayerConfig.FDS]);
			mem.setFDSMode(write_enable);
			bank.setFDSMode(write_enable);

			bank.setBankDefault(6, nsf.bankswitch[6]);
			bank.setBankDefault(7, nsf.bankswitch[7]);
		} else {
			mem.setFDSMode(false);
			bank.setFDSMode(false);
		}*/
		
		
		// 最后是 layer
		stack.attach(layer);

		// NOTE: each layer in the stack is given a chance to take a read or write
		// exclusively. The stack is structured like this:
		// loop detector > APU > expansions > main memory
		// 注意, 这里已经忽略了 LoopDetector, 因此不存在 loop detector.

		// main memory comes after other expansions because
		// when the FDS mode is enabled, VRC6/VRC7/5B have writable registers
		// in RAM areas of main memory. To prevent these from overwriting RAM
		// I allow the expansions above it in the stack to prevent them.

		// MMC5 comes high in the stack so that its PCM read behaviour
		// can reread from the stack below and does not get blocked by any
		// stack above.

		runtime.cpu.setMemory(stack);
		
	}
	
	/**
	 * 用 audio 的数据内容, 重设、覆盖 memory 里面的数据内容
	 * @return
	 *   bankswitch 的最大值
	 */
	private int reloadMemory() {
		NsfAudio audio = runtime.audio;
		
		int i, bmax = 0;

		for (i = 0; i < 8; i++)
			if (bmax < audio.bankswitch[i])
				bmax = audio.bankswitch[i];

		runtime.mem.setImage(audio.body, audio.load_address, audio.body.length);

		if (bmax != 0) {
			runtime.bank.setImage(audio.body, audio.load_address, audio.body.length);
			for (i = 0; i < 8; i++)
				runtime.bank.setBankDefault(i + 8, audio.bankswitch[i]);
		}
		
		return bmax;
	}
	
	/**
	 * 连接 Mixer 和虚拟声卡
	 * @param chip
	 */
	private void attachSoundChipAndMixer(AbstractSoundChip chip) {
		byte[] channels = chip.getAllChannelCodes();
		
		for (int i = 0; i < channels.length; i++) {
			byte code = channels[i];
			AbstractNsfSound sound = chip.getSound(code);
			
			IMixerChannel mix = runtime.mixer.allocateChannel(code);
			sound.setOut(mix);
			
			// 音量
			mix.setLevel(getInitLevel(code));
		}
	}
	
	/**
	 * 获取每个轨道的音量. 这个值应该是从参数 param / 配置 config 中去取.
	 * @param channelCode
	 * @return
	 */
	private float getInitLevel(byte channelCode) {
		// TODO 原本从参数 param / 配置 config 中去取, 现在先这样
		return 1.0f;
	}

	/* **********
	 *   执行   *
	 ********** */
	
	/**
	 * 在当前这一秒内, 已经执行的时钟数.
	 * 范围 [0, CPU 每秒的时钟数)
	 */
	int freqConsumed;
	/**
	 * 在当前这一秒内, 已经输出的采样数.
	 * 范围 [0, 采样率)
	 */
	int sampleConsumed;
	
	/**
	 * CPU 剩余没有用完的时钟数.
	 * NsfPlayer.cpu_clock_rest
	 */
	int cpuFreqRemain;
	
	private void resetCPUCounter() {
		freqConsumed = 0;
		sampleConsumed = 0;
		cpuFreqRemain = 0; // apuFreqRemain = 0
	}
	
	/**
	 * 让 CPU 往下走一帧
	 * (虽然说是一帧, 但是实际上是看当前帧的采样数决定的)
	 */
	public void tickCPU() {
		int sampleInCurFrame = runtime.param.sampleInCurFrame;
		
		NesCPU cpu = runtime.cpu;
		for (int i = 0; i < sampleInCurFrame; i++) {
			int freqInCurSample = countFreqInCurSample();
			
			cpuFreqRemain += freqInCurSample;
			if (cpuFreqRemain > 0) {
				int realCpuFreq = cpu.exec(cpuFreqRemain);
				cpuFreqRemain -= realCpuFreq;

				// tick APU frame sequencer
				/*fsc.tickFrameSequence(real_cpu_clocks);
				if (nsf.useMmc5)
					mmc5.tickFrameSequence(real_cpu_clocks);*/
			}
			
			processSounds(freqInCurSample);
		}
		
		endFrame();
	}
	
	/**
	 * 计算输出当前采样, 需要的时钟周期数, 并返回
	 */
	private int countFreqInCurSample() {
		int sampleRate = runtime.param.sampleRate; // 默认 48000
		int freqPerSec = runtime.param.freqPerSec; // 每秒时钟周期数
		
		int ret = 0;
		
		sampleConsumed += 1;
		int to = (int) ((long) sampleConsumed * freqPerSec / sampleRate);
		ret = to - freqConsumed;
		freqConsumed = to;
		
		if (sampleConsumed >= sampleRate) {
			if (sampleConsumed > sampleRate || freqConsumed != freqPerSec) {
				// 出现了问题
				throw new IllegalStateException("时钟计算出现了错误");
			}
			
			freqConsumed = 0;
			sampleConsumed = 0;
		}
		
		return ret;
	}

}
