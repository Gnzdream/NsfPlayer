package zdream.nsfplayer.nsf.device;

import static zdream.nsfplayer.core.ERegion.DENDY;
import static zdream.nsfplayer.core.ERegion.NTSC;
import static zdream.nsfplayer.core.ERegion.PAL;
import static zdream.nsfplayer.core.NsfStatic.BASE_FREQ_DENDY;
import static zdream.nsfplayer.core.NsfStatic.BASE_FREQ_NTSC;
import static zdream.nsfplayer.core.NsfStatic.BASE_FREQ_PAL;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;

import zdream.nsfplayer.core.CycleCounter;
import zdream.nsfplayer.core.ERegion;
import zdream.nsfplayer.core.IResetable;
import zdream.nsfplayer.nsf.audio.NsfAudio;
import zdream.nsfplayer.nsf.device.chip.NesAPU;
import zdream.nsfplayer.nsf.device.chip.NesDMC;
import zdream.nsfplayer.nsf.device.chip.NesFDS;
import zdream.nsfplayer.nsf.device.chip.NesMMC5;
import zdream.nsfplayer.nsf.device.chip.NesN163;
import zdream.nsfplayer.nsf.device.chip.NesS5B;
import zdream.nsfplayer.nsf.device.chip.NesVRC6;
import zdream.nsfplayer.nsf.device.chip.NesVRC7;
import zdream.nsfplayer.nsf.device.cpu.NesCPU;
import zdream.nsfplayer.nsf.executor.IN163ReattachListener;
import zdream.nsfplayer.nsf.renderer.INsfRuntimeHolder;
import zdream.nsfplayer.nsf.renderer.NsfRendererConfig;
import zdream.nsfplayer.nsf.renderer.NsfRuntime;
import zdream.nsfplayer.sound.SoundN163;

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
		fds = new NesFDS(runtime);
		n163 = new NesN163(runtime);
		vrc7 = new NesVRC7(runtime);
		s5b = new NesS5B(runtime);
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
	ERegion region = NTSC;
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
	
	/**
	 * @return
	 *   正在播放的曲目号
	 * @since v0.2.8
	 */
	public int getSong() {
		return song;
	}
	
	/**
	 * 获得当前播放采用的制式
	 * @since v0.2.8
	 */
	public ERegion getRegion() {
		return region;
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
	}
	
	// 音频芯片
	public final NesAPU apu;
	public final NesDMC dmc;
	public final NesVRC6 vrc6;
	public final NesMMC5 mmc5;
	public final NesFDS fds;
	public final NesN163 n163;
	public final NesVRC7 vrc7;
	public final NesS5B s5b;
	
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
		if (runtime.audio.useFds()) {
			fds.beforeRender();
		}
		if (runtime.audio.useN163()) {
			n163.beforeRender();
		}
		if (runtime.audio.useVrc7()) {
			vrc7.beforeRender();
		}
		if (runtime.audio.useS5b()) {
			s5b.beforeRender();
		}
		
		// 执行部分
		for (Iterator<Entry<Byte, AbstractSoundChip>> it = runtime.chips.entrySet().iterator(); it.hasNext();) {
			Entry<Byte, AbstractSoundChip> entry = it.next();
			byte channelCode = entry.getKey();
			entry.getValue().getSound(channelCode).process(freq);
		}
		
		// 结束部分
		apu.afterRender();
		dmc.afterRender();
		
		if (runtime.audio.useVrc6()) {
			vrc6.afterRender();
		}
		if (runtime.audio.useMmc5()) {
			mmc5.afterRender();
		}
		if (runtime.audio.useFds()) {
			fds.afterRender();
		}
		if (runtime.audio.useN163()) {
			n163.afterRender();
		}
		if (runtime.audio.useVrc7()) {
			vrc7.afterRender();
		}
		if (runtime.audio.useS5b()) {
			s5b.afterRender();
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
		// 原工程是 NsfPlayer.reset()
		
		// 确定制式
		region = confirmRegion();
		switch (region) {
		case NTSC:
			runtime.cpu.NES_BASECYCLES = BASE_FREQ_NTSC;
			break;
		case PAL:
			runtime.cpu.NES_BASECYCLES = BASE_FREQ_PAL;
			break;
		default:
			runtime.cpu.NES_BASECYCLES = BASE_FREQ_DENDY;
			break;
		}
		runtime.param.freqPerSec = runtime.cpu.NES_BASECYCLES;
		
		// 由于RAM空间可能在播放后被修改, 因此需要重新加载
		reload();
		// 应用所有配置
		// config.notify(-1);
		stack.reset();
		// 总线重置后, CPU 也需要重置
		runtime.cpu.reset();
		resetCPUCounter();
		
		// 这里 NSF 内部虚拟 CPU 使用的帧率是双精度浮点.
		// 默认值, NTSC: 60.0988, PAL/DENDY: 50.0070
		double speed;
		speed = 1000000.0 / ((region == NTSC) ? runtime.audio.speed_ntsc : runtime.audio.speed_pal);
		
		runtime.cpu.start(runtime.audio.init_address, runtime.audio.play_address,
				speed, this.song, (region == PAL) ? 1 : 0, 0);
		
		cycle.setParam(runtime.param.sampleRate, runtime.param.frameRate);
	}

	/**
	 * 确定制式.
	 * NsfPlayer.getRegion(int)
	 * @return
	 */
	public ERegion confirmRegion() {
		// NSF 中指定的制式
		ERegion flags = runtime.audio.getRegion();

		// 查看 flags 的数据内容来确定
		// single-mode NSF, 仅支持一种制式的, 则按这种制式渲染
		if (flags == NTSC || flags == PAL) {
			return flags;
		}
		
		// 按用户指定的制式渲染
		int pref = runtime.param.region;
		
		switch (pref) {
		case NsfRendererConfig.REGION_FORCE_NTSC:
			return NTSC;
		case NsfRendererConfig.REGION_FORCE_PAL:
			return PAL;
		case NsfRendererConfig.REGION_FORCE_DENDY:
			return DENDY;
		}
		
		if (pref == 1)
			return NTSC;
		if (pref == 2)
			return PAL;

		return NTSC;
	}
	
	/**
	 * <p>按照 Nsf 的信息, 对虚拟设备进行一次重新装配工作
	 * </p>
	 * 
	 * 这里参照 NsfPlayer 类, 见 NsfPlayer.reload()
	 */
	public void reload() {
		
		int maxBankswitch = reloadMemory();
		
		// 进行一次全拆除, 重新安装
		detachAll();
		runtime.chips.clear();
		
		// 下面开始连接设备
		
		// 这里忽略循环检测器 LoopDetector
		// 这里忽略 CPULogger
		
		if (maxBankswitch != 0) {
			layer.attach(runtime.bank);
		}
		layer.attach(runtime.mem);
		
		// 连接音频芯片
		apu_bus.attach(apu);
		apu_bus.attach(dmc);
		// 先将声卡挂到 runtime 上去
		putSoundChipToRuntime(apu);
		putSoundChipToRuntime(dmc);
		
		stack.attach(apu_bus);
		
		if (runtime.audio.useVrc6()) {
			stack.attach(vrc6);
			putSoundChipToRuntime(vrc6);
		}
		if (runtime.audio.useMmc5()) {
			stack.attach(mmc5);
			putSoundChipToRuntime(mmc5);
		}
		if (runtime.audio.useFds()) {
			stack.attach(fds);
			putSoundChipToRuntime(fds);
		}
		if (runtime.audio.useN163()) {
			n163.forceChannelCount(1);
			stack.attach(n163);
			putSoundChipToRuntime(n163);
		}
		if (runtime.audio.useVrc7()) {
			stack.attach(vrc7);
			putSoundChipToRuntime(vrc7);
		}
		if (runtime.audio.useS5b()) {
			stack.attach(s5b);
			putSoundChipToRuntime(s5b);
		}
		
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
	 * 根据重置后 N163 的轨道数, 对 N163 相关的轨道重新和 mixer 相连
	 * @param n163ChannelCount
	 */
	public void reattachN163(int n163ChannelCount) {
		for (int i = 0; i < 8; i++) {
			byte channelCode = (byte) (NesN163.CHANNEL_N163_1 + i);
			SoundN163 sound = n163.getSound(channelCode);
			boolean on = sound != null;
			
			if (on) {
				if (!runtime.chips.containsKey(channelCode)) {
					runtime.chips.put(channelCode, n163);
				}
			} else {
				if (runtime.chips.containsKey(channelCode)) {
					runtime.chips.remove(channelCode);
				}
			}
		}
		
		ArrayList<IN163ReattachListener> ls = runtime.n163Lsners;
		for (IN163ReattachListener l : ls) {
			l.onReattach(n163ChannelCount);
		}
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
	
	/* **********
	 *   执行   *
	 ********** */
	
	/**
	 * 不计播放速度影响, 计算每帧采样数
	 */
	private final CycleCounter cycle = new CycleCounter();
	
	/**
	 * CPU 剩余没有用完的时钟数.
	 * NsfPlayer.cpu_clock_rest
	 */
	int cpuFreqRemain;
	
	private void resetCPUCounter() {
		cpuFreqRemain = 0;
	}
	
	/**
	 * 让 CPU 往下走一帧
	 * (虽然说是一帧, 但是实际上是看当前帧的采样数决定的)
	 * @param needTriggleSound
	 *   是否需要让发声器工作
	 */
	public void tickCPU(boolean needTriggleSound) {
		// 这个值是, 不计播放速度时的每帧采样数
		// runtime.param.sampleInCurFrame 是将播放速度计算进去的值
		// 所以两者并不相等
		int sampleInCurFrame = cycle.tick();
		
		NesCPU cpu = runtime.cpu;
		for (int i = 0; i < sampleInCurFrame; i++) {
			runtime.clockCounter.doConvert();
			int freqInCurSample = runtime.param.cpuClockInCurSample;
			
			cpuFreqRemain += freqInCurSample;
			if (cpuFreqRemain > 0) {
				int realCpuFreq = cpu.exec(cpuFreqRemain);
				cpuFreqRemain -= realCpuFreq;

				// tick APU frame sequencer
				/*fsc.tickFrameSequence(real_cpu_clocks);
				if (nsf.useMmc5)
					mmc5.tickFrameSequence(real_cpu_clocks);*/
			}
			
			if (needTriggleSound) {
				processSounds(runtime.param.apuClockInCurSample);
			}
		}
		
		endFrame();
	}
	
}
