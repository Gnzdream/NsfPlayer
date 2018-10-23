package zdream.nsfplayer.nsf.device;

import zdream.nsfplayer.core.ERegion;
import zdream.nsfplayer.core.IResetable;
import zdream.nsfplayer.nsf.audio.NsfAudio;
import zdream.nsfplayer.nsf.renderer.INsfRuntimeHolder;
import zdream.nsfplayer.nsf.renderer.NsfRendererConfig;
import zdream.nsfplayer.nsf.renderer.NsfRuntime;

/**
 * 用于管理 Nsf 运行时状态的所有硬件设备的管理者
 * 
 * @author Zdream
 * @since v0.2.4
 */
public class DeviceManager implements INsfRuntimeHolder, IResetable {
	
	NsfRuntime runtime = new NsfRuntime();

	public DeviceManager(NsfRuntime runtime) {
		this.runtime = runtime;
	}

	@Override
	public NsfRuntime getRuntime() {
		return runtime;
	}

	/* **********
	 *   参数   *
	 ********** */
	
	/**
	 * CPU 在三种不同的制式下的每秒时钟周期数
	 */
	public static final int
			BASECYCLES_NTSC = 1789773,
			BASECYCLES_PAL = 1662607,
			BASECYCLES_DENDY = 1773448;
	
	/**
	 * 可能是每帧 CPU, APU 剩余周期数
	 * NsfPlayer.cpu_clock_rest 和 NsfPlayer.apu_clock_rest
	 */
	double cpu_clock_rest, apu_clock_rest;
	/**
	 * 实际采用的制式
	 */
	ERegion region;

	/* **********
	 *   重置   *
	 ********** */

	@Override
	public void reset() {
		// 见 NsfPlayer.reset()
		
		cpu_clock_rest = apu_clock_rest = 0;
		region = confirmRegion(runtime.audio.pal_ntsc);
		switch (region) {
		case NTSC:
			runtime.cpu.NES_BASECYCLES = BASECYCLES_NTSC;
			break;
		case PAL:
			runtime.cpu.NES_BASECYCLES = BASECYCLES_PAL;
			break;
		case DENDY:
			runtime.cpu.NES_BASECYCLES = BASECYCLES_DENDY;
			break;
		}
		
		// 由于RAM空间可能在播放后被修改, 因此需要重新加载
		reload();
		
		/*
				// 速率设置应在复位前完成
				setPlayFreq(rate);
				// 应用所有配置
				config.notify(-1);
				// 复位总线
				stack.reset();
				// 总线复位后总是重启（重要）
				cpu.reset();

				double speed;
				if (this.config.get("VSYNC_ADJUST").toInt() != 0)
					speed = ((region == REGION_NTSC) ? 60.0988 : 50.0070);
				else
					speed = 1000000.0 / ((region == REGION_NTSC) ? nsf.speed_ntsc : nsf.speed_pal);
				debugOut("Playback mode: %s\n", (region == REGION_PAL) ? "PAL" : (region == REGION_DENDY) ? "DENDY" : "NTSC");
				debugOut("Playback speed: %f\n", speed);

				int song = nsf.song;
				if (nsf.nsfe_plst != null) {
					song = nsf.nsfe_plst[song];
				}

				cpu.start(nsf.init_address, nsf.play_address, speed, song, (region == REGION_PAL) ? 1 : 0, 0);

				// mask 更新
				int mask = this.config.get("MASK").toInt();
				apu.setMask(mask);
				dmc.setMask(mask);
				fds.setMask(mask);
				mmc5.setMask(mask);
				fme7.setMask(mask);
				vrc6.setMask(mask);
				vrc7.setMask(mask);
				n106.setMask(mask);

				vrc7.setPatchSet(this.config.get("VRC7_PATCH").toInt());

				for (int i = 0; i < NES_TRACK_MAX; i++)
					infobuf[i].clear();

				for (int i = 0; i < NsfPlayerConfig.NES_DEVICE_MAX; ++i)
					notifyPan(i);*/
		
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
	 */
	public void reload() {
		reloadMemory();
		
		// TODO 这里参照 NsfPlayer 类
		// 见 NsfPlayer.reload()
	}
	
	/**
	 * 用 audio 的数据内容, 重设、覆盖 memory 里面的数据内容
	 */
	public void reloadMemory() {
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
	}

}
