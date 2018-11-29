package zdream.nsfplayer.nsf.renderer;

import java.util.ArrayList;
import java.util.HashMap;

import zdream.nsfplayer.core.IResetable;
import zdream.nsfplayer.nsf.audio.NsfAudio;
import zdream.nsfplayer.nsf.context.NsfClockCounter;
import zdream.nsfplayer.nsf.device.AbstractSoundChip;
import zdream.nsfplayer.nsf.device.DeviceManager;
import zdream.nsfplayer.nsf.device.cpu.NesCPU;
import zdream.nsfplayer.nsf.device.memory.NesBank;
import zdream.nsfplayer.nsf.device.memory.NesMem;
import zdream.nsfplayer.nsf.executor.IN163ReattachListener;

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
		// 这个方法没干事情
		manager.init();
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
