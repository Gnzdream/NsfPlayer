package com.zdream.nsfplayer.xgm.player.nsf;

import com.zdream.nsfplayer.nsf.device.Bus;
import com.zdream.nsfplayer.nsf.device.Layer;
import com.zdream.nsfplayer.vcm.Value;
import com.zdream.nsfplayer.xgm.device.ISoundChip;
import com.zdream.nsfplayer.xgm.device.ITrackInfo;
import com.zdream.nsfplayer.xgm.device.InfoBuffer;
import com.zdream.nsfplayer.xgm.device.IntHolder;
import com.zdream.nsfplayer.xgm.device.audio.Amplifier;
import com.zdream.nsfplayer.xgm.device.audio.Compressor;
import com.zdream.nsfplayer.xgm.device.audio.DCFilter;
import com.zdream.nsfplayer.xgm.device.audio.EchoUnit;
import com.zdream.nsfplayer.xgm.device.audio.Filter;
import com.zdream.nsfplayer.xgm.device.audio.Mixer;
import com.zdream.nsfplayer.xgm.device.audio.RateConverter;
import com.zdream.nsfplayer.xgm.device.cpu.NesCPU;
import com.zdream.nsfplayer.xgm.device.memory.NesBank;
import com.zdream.nsfplayer.xgm.device.memory.NesMem;
import com.zdream.nsfplayer.xgm.device.misc.CPULogger;
import com.zdream.nsfplayer.xgm.device.misc.NesDetector;
import com.zdream.nsfplayer.xgm.device.misc.NesDetectorEx;
import com.zdream.nsfplayer.xgm.device.sound.NesAPU;
import com.zdream.nsfplayer.xgm.device.sound.NesDMC;
import com.zdream.nsfplayer.xgm.device.sound.NesFDS;
import com.zdream.nsfplayer.xgm.device.sound.NesFME7;
import com.zdream.nsfplayer.xgm.device.sound.NesMMC5;
import com.zdream.nsfplayer.xgm.device.sound.NesN106;
import com.zdream.nsfplayer.xgm.device.sound.NesVRC6;
import com.zdream.nsfplayer.xgm.device.sound.NesVRC7;
import com.zdream.nsfplayer.xgm.player.MultiSongPlayer;
import com.zdream.nsfplayer.xgm.player.PlayerConfig;
import com.zdream.nsfplayer.xgm.player.SoundData;

public class NsfPlayer extends MultiSongPlayer {
	
	/**
	 * 类似于轨道编号的枚举
	 */
	public static final int
			APU1_TRK0 = 0, APU1_TRK1 = 1, APU2_TRK0 = 2, APU2_TRK1 = 3, APU2_TRK2 = 4, FDS_TRK0 = 5, MMC5_TRK0 = 6,
			MMC5_TRK1 = 7, MMC5_TRK2 = 8, FME7_TRK0 = 9, FME7_TRK1 = 10, FME7_TRK2 = 11, FME7_TRK3 = 12, FME7_TRK4 = 13,
			VRC6_TRK0 = 14, VRC6_TRK1 = 15, VRC6_TRK2 = 16, VRC7_TRK0 = 17, VRC7_TRK1 = 18, VRC7_TRK2 = 19,
			VRC7_TRK3 = 20, VRC7_TRK4 = 21, VRC7_TRK5 = 22, N106_TRK0 = 23, N106_TRK1 = 24, N106_TRK2 = 25,
			N106_TRK3 = 26, N106_TRK4 = 27, N106_TRK5 = 28, N106_TRK6 = 29, N106_TRK7 = 30, NES_TRACK_MAX = 31;
	
	protected NsfPlayerConfig config;
	
	protected double rate;
	protected int nch; // number of channels
	protected int song;

	protected int last_out;
	protected int silent_length;

	protected double cpu_clock_rest;
	protected double apu_clock_rest;

	/**
	 * 演奏的时间
	 */
	protected int time_in_ms;
	/**
	 * 如果演奏时间被查出的话, 设为 true
	 */
	protected boolean playtime_detected;
	
	public Bus apu_bus = new Bus();
	public Layer stack = new Layer();
	public Layer layer = new Layer();
	public Mixer mixer = new Mixer();

	public NesCPU cpu = new NesCPU(NesCPU.DEFAULT_CLOCK);
	public NesMem mem = new NesMem();
	public NesBank bank = new NesBank();

	/** 声音芯片的实例 */
	public ISoundChip[] sc = new ISoundChip[NsfPlayerConfig.NES_DEVICE_MAX];
	/** 抽样器 */
	public RateConverter[] rconv = new RateConverter[NsfPlayerConfig.NES_DEVICE_MAX];
	/** 滤波器 */
	public Filter[] filter = new Filter[NsfPlayerConfig.NES_DEVICE_MAX];
	/** 增幅器 */
	public Amplifier[] amp = new Amplifier[NsfPlayerConfig.NES_DEVICE_MAX];
	/** 最后输出阶段直流通过滤器 */
	public DCFilter dcf = new DCFilter();
	/** 最后输出输出到滤波器 */
	public Filter lpf = new Filter();
	/** 最终输出阶段悬挂的压缩机 */
	public Compressor cmp = new Compressor();
	/** 循环检验器 */
	public NesDetector ld = new NesDetector();
	/** Logs CPU to file */
	public CPULogger logcpu = new CPULogger();
	public EchoUnit echo = new EchoUnit();
//	/** 小的噪音对策的中位数滤波器 */
//	public MedianFilter mfilter; // 在构造函数中生成
	
	/**
	 * 所有音轨数据的缓存
	 */
	public InfoBuffer[] infobuf = new InfoBuffer[NES_TRACK_MAX];
    
	/**
	 * 到现在为止生成的波形的采样数 (不考虑声道, 多声道按单声道计数)
	 */
	public int total_render;
	/**
	 * 一帧数字节数
	 */
	public int frame_render;
	/**
	 * 一帧的长度 (ms)
	 */
	public int frame_in_ms;

    /**
     * 各声音芯片的别名参照
     */
	public NesAPU apu;
	public NesDMC dmc;
	public NesVRC6 vrc6;
	public NesVRC7 vrc7;
	public NesFME7 fme7;
	public NesMMC5 mmc5;
	public NesN106 n106;
	public NesFDS fds;
	
	public NsfAudio nsf;
	
	NsfPlayerStatus status;
	
	{
		for (int i = 0; i < rconv.length; i++) {
			rconv[i] = new RateConverter();
		}
		for (int i = 0; i < filter.length; i++) {
			filter[i] = new Filter();
		}
		for (int i = 0; i < amp.length; i++) {
			amp[i] = new Amplifier();
		}
		for (int i = 0; i < infobuf.length; i++) {
			infobuf[i] = new InfoBuffer();
		}
	}
	
	public static final int REGION_NTSC = 0,
	        REGION_PAL = 1,
	        REGION_DENDY = 2;

	public NsfPlayer() {
		status = new NsfPlayerStatus(this);
		
		sc[NsfPlayerConfig.APU] = (apu = new NesAPU());
		sc[NsfPlayerConfig.DMC] = (dmc = new NesDMC());
		sc[NsfPlayerConfig.FDS] = (fds = new NesFDS());
		sc[NsfPlayerConfig.FME7] = (fme7 = new NesFME7());
		sc[NsfPlayerConfig.MMC5] = (mmc5 = new NesMMC5());
		sc[NsfPlayerConfig.N106] = (n106 = new NesN106());
		sc[NsfPlayerConfig.VRC6] = (vrc6 = new NesVRC6());
		sc[NsfPlayerConfig.VRC7] = (vrc7 = new NesVRC7());

		dmc.setApu(apu); // set APU
		mmc5.setCPU(cpu); // MMC5 PCM read action requires CPU read access

		/* 放大器 ← 滤芯速率转换器 ← 连接 */
		for (int i = 0; i < NsfPlayerConfig.NES_DEVICE_MAX; i++) {
			rconv[i].attach(sc[i]);
			filter[i].attach(rconv[i]);
			amp[i].attach(filter[i]);
		}

		nch = 1;
	}
	
	public void setConfig(PlayerConfig pc) {
		if (pc instanceof NsfPlayerConfig) {
			super.setConfig(pc);
			this.config = (NsfPlayerConfig) pc;
		} else {
			super.setConfig(null);
		}
	}

	public final boolean isDetected() {
		return playtime_detected;
	}

	public String getTitleString() {
		if (nsf == null)
			return "(not loaded)";

		return nsf.getTitle(this.config.get("TITLE_FORMAT").toString(), this.song);
	}
	
	@Override
	public PlayerConfig getConfig() {
		return config;
	}

	@Override
	public boolean load(SoundData sdat) {
		if (!(sdat instanceof NsfAudio)) {
			return false;
		}

		nsf = (NsfAudio) sdat;
		if (this.config.get("NSFE_PLAYLIST").toInt() != 0 && nsf.nsfe_plst != null) {
			nsf.start = 1;
			nsf.songs = nsf.nsfe_plst.length;
		} else {
			nsf.nsfe_plst = null;
		}

		System.out.println(nsf.debugOut());
		reload();
		return true;
	}
	
	protected void reload() {
		int i, bmax = 0;
		// int offset = 0; // unsigned

		for (i = 0; i < 8; i++)
			if (bmax < nsf.bankswitch[i])
				bmax = nsf.bankswitch[i];

		mem.setImage(nsf.body, nsf.load_address, nsf.body.length);

		if (bmax != 0) {
			bank.setImage(nsf.body, nsf.load_address, nsf.body.length);
			for (i = 0; i < 8; i++)
				bank.setBankDefault(i + 8, nsf.bankswitch[i]);
		}

		// virtual machine controlling memory reads and writes
		// to various devices, expansions, etc.
		stack.detachAll();
		layer.detachAll();
		mixer.detachAll();
		apu_bus.detachAll();

		// select the loop detector
		if (this.config.get("DETECT_ALT").toInt() != 0) {
			final Class<?> ti = ld.getClass();
			if (!ti.getSimpleName().equals("NESDetectorEx")) {
				ld = new NesDetectorEx();
			}
		} else {
			final Class<?> ti = ld.getClass();
			if (!ti.getSimpleName().equals("NESDetector")) {
				ld = new NesDetector();
			}
		}

		// loop detector ends up at the front of the stack
		// (will capture all writes, but does not capture write)
		stack.attach(ld);

		int log_level = this.config.get("LOG_CPU").toInt();
		logcpu.setOption(0, log_level);
		logcpu.setSoundchip(nsf.soundchip);
		logcpu.setNSF(nsf);
		if (log_level > 0) {
			logcpu.setFilename(this.config.get("LOG_CPU_FILE").toString());
			stack.attach(logcpu);
			cpu.setLogger(logcpu);
			logcpu.setCPU(cpu);
		} else {
			cpu.setLogger(null);
		}

		if (bmax != 0)
			layer.attach(bank);
		layer.attach(mem);

		dmc.setMemory(layer);

		// APU units are combined into a single bus
		apu_bus.attach(sc[NsfPlayerConfig.APU]);
		apu_bus.attach(sc[NsfPlayerConfig.DMC]);
		stack.attach(apu_bus);

		mixer.attach(amp[NsfPlayerConfig.APU]);
		mixer.attach(amp[NsfPlayerConfig.DMC]);

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
		}

		// memory layer comes last
		stack.attach(layer);

		// NOTE: each layer in the stack is given a chance to take a read or write
		// exclusively. The stack is structured like this:
		// loop detector > APU > expansions > main memory

		// main memory comes after other expansions because
		// when the FDS mode is enabled, VRC6/VRC7/5B have writable registers
		// in RAM areas of main memory. To prevent these from overwriting RAM
		// I allow the expansions above it in the stack to prevent them.

		// MMC5 comes high in the stack so that its PCM read behaviour
		// can reread from the stack below and does not get blocked by any
		// stack above.

		cpu.setMemory(stack);
	}
	
	public void debugOut(String text, Object...args) {
		System.out.print(String.format(text, args));
	}

	@Override
	public void setPlayFreq(double r) {
		rate = r;
		this.status.rate = r;

		int region = getRegion(nsf.pal_ntsc);
		boolean pal = (region == REGION_PAL);
		dmc.setPal(pal);

		// 使用 rate 转换器
		int[][] MULT = {
				{ 1, 5, 8, 20 }, // APU1
				{ 1, 5, 8, 20 }, // DMC
				{ 1, 5, 8, 8 }, // FME7
				{ 1, 5, 8, 20 }, // MMC5
				{ 1, 5, 8, 20 }, // N106
				{ 1, 5, 8, 20 }, // VRC6
				{ 1, 3, 3, 3 }, // VRC7
				{ 1, 5, 8, 20 } // FDS
		};

		for (int i = 0; i < NsfPlayerConfig.NES_DEVICE_MAX; i++) {
			int quality = config.getDeviceConfig(i, "QUALITY").toInt();

			double clock;
			switch (region) {
			case REGION_PAL:
				clock = intConfig("PAL_BASECYCLES");
				break;
			case REGION_DENDY:
				clock = intConfig("DENDY_BASECYCLES");
				break;
			case REGION_NTSC:
			default:
				clock = intConfig("NTSC_BASECYCLES");
			}
			sc[i].setClock(clock);

			int mult = config.getDeviceConfig(i, "QUALITY").toInt() & 3;

			sc[i].setRate(rate * MULT[i][mult]);

			rconv[i].setClock(rate * MULT[i][mult]);
			rconv[i].setRate(rate);
			rconv[i].reset();

			if (quality != 0) {
				filter[i].attach(rconv[i]);
			} else {
				// 不要使用速率转换器
				filter[i].attach(sc[i]);
			}
			// 设置滤波器的工作频率
			filter[i].setRate(rate);
			filter[i].reset();
		}
		mixer.reset();
		echo.setRate(rate);
		echo.reset();
		lpf.setRate(rate);
		lpf.reset();
		dcf.setRate(rate);
		dcf.reset();
	}

	public void setChannels(int channels) {
		if (channels > 2)
			channels = 2;
		if (channels < 1)
			channels = 1;
		nch = channels;
	}

	@Override
	public void reset() {
//		mfilter.reset();
		time_in_ms = 0;
		silent_length = 0;
		playtime_detected = false;
		total_render = 0;
		frame_render = (int) (rate) / 60; // 演奏数据更新的周期
		apu_clock_rest = 0.0;
		cpu_clock_rest = 0.0;

		int region = getRegion(nsf.pal_ntsc);
		switch (region) {
		default:
		case REGION_NTSC:
			cpu.NES_BASECYCLES = intConfig("NTSC_BASECYCLES");
			break;
		case REGION_PAL:
			cpu.NES_BASECYCLES = intConfig("PAL_BASECYCLES");
			break;
		case REGION_DENDY:
			cpu.NES_BASECYCLES = intConfig("DENDY_BASECYCLES");
			break;
		}

		if (logcpu.getLogLevel() > 0)
			logcpu.begin(getTitleString());

		// 由于RAM空间可能在播放后被修改, 因此需要重新加载
		reload();
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
			notifyPan(i);
	}
	
	private int intConfig(String str) {
		return this.config.getIntValue(str);
	}
	
	private int intConfig(String str, int defaultValue) {
		Value v = config.get(str);
		return (v == null) ? defaultValue : v.toInt();
	}

	/**
	 * 检查到一个歌曲已经放完
	 */
	protected void detectSilent() {
		if (mixer.isFading() || playtime_detected || !nsf.playtime_unknown || nsf.useNSFePlaytime())
			return;

		if (intConfig("MASK") == 0 && intConfig("AUTO_STOP") != 0 && (silent_length > rate * intConfig("STOP_SEC"))) {
			playtime_detected = true;
			nsf.time_in_ms = time_in_ms - intConfig("STOP_SEC") * 1000 + 1000;
			nsf.loop_in_ms = 0;
			nsf.fade_in_ms = 0;
		}
	}

	/**
	 * 检查到一个歌曲已经放完一个循环, 准备开始下一个循环
	 */
	protected void detectLoop() {
		if (mixer.isFading() || playtime_detected || !nsf.playtime_unknown || nsf.useNSFePlaytime())
			return;
		
		Value v = config.get("AUTO_DETECT");
		
		if (v != null) {
			/*
			 * 检查, 如果出现 AUTO_DETECT 被关闭 (为 0) 时,
			 * 如果碰到能够循环的曲目将会一直循环, 且不会停下并切换曲目
			 */
			if (v.toInt() == 0) {
				return;
			}
			
			if (ld.isLooped(time_in_ms, intConfig("DETECT_TIME"), intConfig("DETECT_INT"))) {
				playtime_detected = true;
				nsf.time_in_ms = ld.getLoopEnd();
				nsf.loop_in_ms = ld.getLoopEnd() - ld.getLoopStart();
				nsf.fade_in_ms = -1;
			}
		}
	}
	
	/**
	 * <p>询问该播放器是否检查循环.
	 * @return
	 * @since v0.0.2
	 */
	public boolean isDetectLoop() {
		Value v = config.get("AUTO_DETECT");
		if (v == null || v.toInt() == 0) {
			return false;
		}
		return true;
	}
	
	/**
	 * <p>设置播放器是否自动侦测循环.
	 * <p>设置为 false 时, 播放器遇到会循环的曲目时会一直循环播放下去
	 * @since v0.0.2
	 */
	public void setDetectLoop(boolean b) {
		config.setValue("AUTO_DETECT", new Value((b) ? 1 : 0));
	}
	
	/**
	 * <p>询问播放器在播放不循环的歌曲时, 如果播放完毕后, 是否自动跳到下一曲进行播放.
	 * @return
	 *   true 会自动跳转到下一曲 (默认)<br>
	 *   false 会直接停住, 什么都不做
	 * @since v0.0.2
	 */
	public boolean isAutoStop() {
		return (intConfig("AUTO_STOP", 1) == 0);
	}
	
	/**
	 * <p>设置播放器在播放不循环的歌曲时, 如果播放完毕后, 是否自动跳到下一曲进行播放.
	 * @param b
	 *   true 会自动跳转到下一曲 (默认)<br>
	 *   false 会直接停住, 什么都不做
	 * @since v0.0.2
	 */
	public void setAutoStop(boolean b) {
		config.setValue("AUTO_STOP", new Value((b) ? 1 : 0));
	}
	
	protected void checkTerminal() {
		if (mixer.isFading())
			return;
		
		int pos = time_in_ms + nsf.getFadeTime();
		int len = nsf.getLength();
		if (pos >= len) {
			mixer.fadeStart(rate, nsf.getFadeTime());
		}
	}

	/**
	 * @param length
	 *   unsigned
	 * @return
	 *   unsigned
	 */
	public int skip(int length) {
		if (length > 0) {
			double apu_clock_per_sample = cpu.NES_BASECYCLES / rate;
			double cpu_clock_per_sample = apu_clock_per_sample * ((double) (intConfig("MULT_SPEED")) / 256.0);

			for (int i = 0; i < length; i++) {
				total_render++;

				// tick CPU
				cpu_clock_rest += cpu_clock_per_sample;
				int cpu_clocks = (int) (cpu_clock_rest);
				if (cpu_clocks > 0) {
					// TODO 注意这里 ！！！ CPU 机器码的操作
					int real_cpu_clocks = cpu.exec(cpu_clocks);
					cpu_clock_rest -= (double) (real_cpu_clocks);

					// tick APU frame sequencer
					dmc.tickFrameSequence(real_cpu_clocks);
					if (nsf.useMmc5)
						mmc5.tickFrameSequence(real_cpu_clocks);
				}

				// skip APU / expansions
				apu_clock_rest += apu_clock_per_sample;
				int apu_clocks = (int) (apu_clock_rest);
				if (apu_clocks > 0) {
					// mixer.Tick(apu_clocks);
					apu_clock_rest -= (double) (apu_clocks);
				}
			}

			mixer.skip(length);
			time_in_ms += (int) (1000 * length / rate * (intConfig("MULT_SPEED")) / 256);
			checkTerminal();
			detectLoop();
		}
		return length;
	}

	@Override
	public void fadeOut(int fade_in_ms) {
		if (fade_in_ms < 0)
			mixer.fadeStart(rate, intConfig("FADE_TIME"));
		else
			mixer.fadeStart(rate, fade_in_ms);
	}

	protected void updateInfo() {
		if (frame_render == 0) {
			return;
		}
		if (total_render % frame_render == 0) {
			int i;

			for (i = 0; i < 2; i++)
				infobuf[APU1_TRK0 + i].addInfo(total_render, apu.getTrackInfo(i));

			for (i = 0; i < 3; i++)
				infobuf[APU2_TRK0 + i].addInfo(total_render, dmc.getTrackInfo(i));

			if (nsf.useFds)
				infobuf[FDS_TRK0].addInfo(total_render, fds.getTrackInfo(0));

			if (nsf.useN106) {
				for (i = 0; i < 3; i++)
					infobuf[VRC6_TRK0 + i].addInfo(total_render, vrc6.getTrackInfo(i));
			}

			if (nsf.useN106) {
				for (i = 0; i < 8; i++)
					infobuf[N106_TRK0 + i].addInfo(total_render, n106.getTrackInfo(i));
			}

			if (nsf.useVrc7) {
				for (i = 0; i < 6; i++)
					infobuf[VRC7_TRK0 + i].addInfo(total_render, vrc7.getTrackInfo(i));
			}

			if (nsf.useMmc5) {
				for (i = 0; i < 3; i++)
					infobuf[MMC5_TRK0 + i].addInfo(total_render, mmc5.getTrackInfo(i));
			}

			if (nsf.useFme7) {
				for (i = 0; i < 5; i++)
					infobuf[FME7_TRK0 + i].addInfo(total_render, fme7.getTrackInfo(i));
			}

		}
	}
	  
	public ITrackInfo getInfo(int time_in_ms, int id) {
		if (time_in_ms >= 0) {
			int pos = (int) (rate * time_in_ms / 1000);
			return infobuf[id].getInfo(pos);
		} else {
			return infobuf[id].getInfo(-1);
		}
	}

	@Override
	public int render(byte[] b, int offset, int size) {
		int[] buf = new int[2], out = new int[2];
		int outm, i;
		int master_volume = intConfig("MASTER_VOLUME");
		int ptr = offset; // 指向 b 的索引指针

		double apu_clock_per_sample = cpu.NES_BASECYCLES / rate;
		double cpu_clock_per_sample = apu_clock_per_sample * (double) (intConfig("MULT_SPEED") / 256.0);

		int length = size / 4; // 2 = 16 / 8, 每个音频采样需要 2 byte (16 bit)
		for (i = 0; i < length; i ++) {
			total_render ++;

			// tick CPU
			cpu_clock_rest += cpu_clock_per_sample;
			int cpu_clocks = (int) (cpu_clock_rest);
			if (cpu_clocks > 0) {
				int real_cpu_clocks = cpu.exec(cpu_clocks);
				cpu_clock_rest -= (double) (real_cpu_clocks);

				// tick APU frame sequencer
				dmc.tickFrameSequence(real_cpu_clocks);
				if (nsf.useMmc5)
					mmc5.tickFrameSequence(real_cpu_clocks);
			}

			updateInfo();

			// tick APU / expansions
			apu_clock_rest += apu_clock_per_sample;
			int apu_clocks = (int) (apu_clock_rest);
			if (apu_clocks > 0) {
				mixer.tick(apu_clocks);
				apu_clock_rest -= (double) (apu_clocks);
			}

			// render output
			mixer.render(buf);
			outm = (buf[0] + buf[1]) >> 1; // mono mix
			if (outm == last_out)
				silent_length++;
			else
				silent_length = 0;
			last_out = outm;

			// echo.FastRender(buf);
			dcf.fastRender(buf);
			lpf.fastRender(buf);
			cmp.fastRender(buf);

			// mfilter.Put(buf[0]);
			// out = mfilter.get();

			out[0] = (buf[0] * master_volume) >> 8;
			out[1] = (buf[1] * master_volume) >> 8;

			if (out[0] < -32767)
				out[0] = -32767;
			else if (32767 < out[0])
				out[0] = 32767;

			if (out[1] < -32767)
				out[1] = -32767;
			else if (32767 < out[1])
				out[1] = 32767;

			if (nch == 2) {
				b[ptr++] = (byte) out[0]; // 低位 (一声道)
				b[ptr++] = (byte) ((out[0] & 0xFF00) >> 8); // 高位 (一声道)
				b[ptr++] = (byte) out[1]; // 低位 (二声道)
				b[ptr++] = (byte) ((out[1] & 0xFF00) >> 8); // 高位 (二声道)
			} else // if not 2 channels, presume mono
			{
				outm = (out[0] + out[1]) >> 1;
				for (int ii = 0; ii < nch; ++ii) {
					b[ptr++] = (byte) outm; // 低位
					b[ptr++] = (byte) ((outm & 0xFF00) >> 8); // 高位
				}
			}
		}

		time_in_ms += (int) (1000 * size / rate * (intConfig("MULT_SPEED")) / 256);

		checkTerminal();
		detectLoop();
		detectSilent();

		return size;
	}

	@Override
	public int getLength() {
		if (nsf == null)
			return -1;
		return nsf.getLength();
	}

	@Override
	public boolean isStopped() {
		if (status.replace) {
			return true;
		}
		return mixer.isFadeEnd();
	}
	
	public boolean isPaused() {
		return status.pause;
	}

	@Override
	public boolean setSong(int s) {
		nsf.song = s % nsf.songs;
		return true;
	}

	@Override
	public boolean nextSong(int s) {
		nsf.song += s;
		boolean result = true;
		while (nsf.song >= nsf.songs) {
			nsf.song -= nsf.songs;
			result = false;
		}
		return result;
	}

	@Override
	public boolean prevSong(int s) {
		nsf.song -= s;
		boolean result = true;
		while (nsf.song < 0) {
			nsf.song += nsf.songs;
			result = false;
		}
		return result;
	}

	@Override
	public int getSong() {
		return nsf.song;
	}

	public void getMemory(int[] buf) {
		int i;
		IntHolder val = new IntHolder();
		int len = (buf.length < 65536) ? buf.length : 65536;
		for (i = 0; i < len; i++) {
			cpu.read(i, val, 0);
			buf[i] = val.val;
		}
	}

	public void getMemoryString(StringBuilder buf) {
		final char[] itoa = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		IntHolder val = new IntHolder();
		int i;

		for (i = 0; i < 65536; i++) {
			cpu.read(i, val, 0);

			if ((i & 0xF) == 0) {
				if (i != 0) {
					buf.append('\r').append('\n');
				}
				buf.append(itoa[(i >> 12) & 0xF]).append(itoa[(i >> 8) & 0xF]).append(itoa[(i >> 4) & 0xF])
						.append(itoa[i & 0xF]).append(':');
			}

			buf.append(itoa[(val.val >> 4) & 0xF]).append(itoa[val.val & 0xF]).append(' ');
		}
	}

	/**
	 * Update Configuration
	 */
	public void notify(int id) {
		int i;

		if (id == -1) {
			for (i = 0; i < NsfPlayerConfig.NES_DEVICE_MAX; i++)
				notify(i);

			cmp.setParam(0.01 * intConfig("COMP_LIMIT"), 0.01 * intConfig("COMP_THRESHOLD"),
					0.01 * intConfig("COMP_VELOCITY"));

			dcf.setParam(270, intConfig("HPF"));
			lpf.setParam(4700, intConfig("LPF"));

			// DEBUG_OUT("dcf: %3d > %f\n", (*config)["HPF"].getInt(), dcf.getFactor());
			// DEBUG_OUT("lpf: %3d > %f\n", (*config)["LPF"].getInt(), lpf.getFactor());

			return;
		}

		filter[id].setParam(4700, config.getDeviceConfig(id, "FILTER").toInt());

		amp[id].setVolume(config.getDeviceConfig(id, "VOLUME").toInt());
		amp[id].setMute(config.getDeviceConfig(id, "MUTE").toInt());
		// amp[id].setCompress (config.getDeviceConfig(id,"THRESHOLD"),
		// config.getDeviceConfig(id,"TWEIGHT"));
		amp[id].setCompress(config.getDeviceConfig(id, "THRESHOLD").toInt(), -1);

		switch (id) {
		case NsfPlayerConfig.APU:
			for (i = 0; i < NesAPU.OPT_END; i++)
				apu.setOption(i, config.getDeviceOption(id, i).toInt());
			apu.setMask(intConfig("MASK"));
			break;
		case NsfPlayerConfig.DMC:
			for (i = 0; i < NesDMC.OPT_END; i++)
				dmc.setOption(i, config.getDeviceOption(id, i).toInt());
			dmc.setMask(intConfig("MASK") >> 2);
			break;
		case NsfPlayerConfig.FDS:
			for (i = 0; i < NesFDS.OPT_END; i++)
				fds.setOption(i, config.getDeviceOption(id, i).toInt());
			fds.setMask(intConfig("MASK") >> 5);
			break;
		case NsfPlayerConfig.MMC5:
			for (i = 0; i < NesMMC5.OPT_END; i++)
				mmc5.setOption(i, config.getDeviceOption(id, i).toInt());
			mmc5.setMask(intConfig("MASK") >> 6);
			break;
		case NsfPlayerConfig.FME7:
			fme7.setMask(intConfig("MASK") >> 9);
			break;
		case NsfPlayerConfig.VRC6:
			vrc6.setMask(intConfig("MASK") >> 12);
			break;
		case NsfPlayerConfig.VRC7:
			vrc7.setMask(intConfig("MASK") >> 15);
			break;
		case NsfPlayerConfig.N106:
			for (i = 0; i < NesN106.OPT_END; i++)
				n106.setOption(i, config.getDeviceOption(id, i).toInt());
			n106.setMask(intConfig("MASK") >> 21);
			break;
		default:
			break;
		}

		notifyPan(id);
	}

	public void notifyPan(int id) {
		if (id == -1) {
			for (int i = 0; i < NsfPlayerConfig.NES_DEVICE_MAX; i++)
				notifyPan(i);
			return;
		}

		for (int i = 0; i < NsfPlayerConfig.NES_CHANNEL_MAX; ++i) {
			int d = NsfPlayerConfig.channel_device[i];
			if (d != id)
				continue;

			int pan = config.getChannelConfig(i, "PAN").toInt(); // 0 = full left, 255 = full right, 128 = centre
			int vol = config.getChannelConfig(i, "VOL").toInt(); // 128 = full volume
			int r = (pan + 1) >> 1; // +1 and truncation is intentional
			// r: 0 . 0 (left), 128 . 64 (mid), 255 . 128 (right)
			if (r < 0)
				r = 0;
			if (r > 128)
				r = 128;
			int l = 128 - r;
			l = (l * vol) / 128;
			r = (r * vol) / 128;
			l <<= 1; // undo truncation
			r <<= 1;
			int ci = NsfPlayerConfig.channel_device_index[i];
			switch (d) {
			case NsfPlayerConfig.APU:
				apu.setStereoMix(ci, l, r);
				break;
			case NsfPlayerConfig.DMC:
				dmc.setStereoMix(ci, l, r);
				break;
			case NsfPlayerConfig.FDS:
				fds.setStereoMix(ci, l, r);
				break;
			case NsfPlayerConfig.MMC5:
				mmc5.setStereoMix(ci, l, r);
				break;
			case NsfPlayerConfig.FME7:
				fme7.setStereoMix(ci, l, r);
				break;
			case NsfPlayerConfig.VRC6:
				vrc6.setStereoMix(ci, l, r);
				break;
			case NsfPlayerConfig.VRC7:
				vrc7.setStereoMix(ci, l, r);
				break;
			case NsfPlayerConfig.N106:
				n106.setStereoMix(ci, l, r);
				break;
			}
		}
	}

	public int getRegion(int flags) {
		int pref = intConfig("REGION");

		// user forced region
		if (pref == 3)
			return REGION_NTSC;
		if (pref == 4)
			return REGION_PAL;
		if (pref == 5)
			return REGION_DENDY;

		// single-mode NSF
		if (flags == 0)
			return REGION_NTSC;
		if (flags == 1)
			return REGION_PAL;

		if ((flags & 2) != 0) // dual mode
		{
			if (pref == 1)
				return REGION_NTSC;
			if (pref == 2)
				return REGION_PAL;
			// else pref == 0 or invalid, use auto setting based on flags bit
			return ((flags & 1) != 0) ? REGION_PAL : REGION_NTSC;
		}

		return REGION_NTSC; // fallback for invalid flags
	}
	
	public NsfPlayerStatus getStatus() {
		return status;
	}

}
