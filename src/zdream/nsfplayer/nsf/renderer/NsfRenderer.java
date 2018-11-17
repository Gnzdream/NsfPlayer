package zdream.nsfplayer.nsf.renderer;

import java.util.HashSet;
import java.util.Set;

import zdream.nsfplayer.core.AbstractNsfRenderer;
import zdream.nsfplayer.core.NsfStatic;
import zdream.nsfplayer.nsf.audio.NsfAudio;
import zdream.nsfplayer.nsf.device.AbstractSoundChip;

/**
 * <p>NSF 渲染器.
 * <p>该类在 v0.2.3 版本以前基本处于不可用的状态, 直到 v0.2.4 版本进行了大量的改造.
 * </p>
 * 
 * @author Zdream
 * @since v0.1
 */
public class NsfRenderer extends AbstractNsfRenderer<NsfAudio> {
	
	private NsfRuntime runtime;
	
	public NsfRenderer() {
		this.runtime = new NsfRuntime();
		runtime.init();
	}
	
	public NsfRenderer(NsfRendererConfig config) {
		this.runtime = new NsfRuntime(config);
		runtime.init();
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
		if (audio == null) {
			throw new NullPointerException("audio = null");
		}
		ready(audio, audio.start);
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
		if (audio == null) {
			throw new NullPointerException("audio = null");
		}
		if (track < 0 || track >= audio.total_songs) {
			throw new IllegalArgumentException(
					"曲目号 track 需要在范围 [0, " + audio.total_songs + ") 内");
		}
		
		runtime.param.sampleRate = this.runtime.config.sampleRate;
		runtime.param.calcFreq(frameRate);
		
		runtime.audio = audio;
		runtime.manager.setSong(track);
		runtime.reset();
		
		super.resetCounterParam(frameRate, runtime.config.sampleRate);
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
		NsfAudio audio = runtime.audio;
		
		if (track < 0 || track >= audio.total_songs) {
			throw new IllegalArgumentException(
					"曲目号 track 需要在范围 [0, " + audio.total_songs + ") 内");
		}
		
		runtime.manager.setSong(track);
		runtime.reset();
	}
	
	/* **********
	 * 渲染部分 *
	 ********** */

	@Override
	protected int renderFrame() {
		int ret = countNextFrame();
		runtime.param.sampleInCurFrame = ret;
		
		runtime.manager.tickCPU();

		// 从 mixer 中读取数据
		readMixer();
		
		/*
		int[] buf = new int[2], out = new int[2];
		int outm, i;
		int master_volume = intConfig("MASTER_VOLUME");
		int ptr = offset; // 指向 b 的索引指针

		double apu_clock_per_sample = cpu.NES_BASECYCLES / rate;
		// MULT_SPEED 起到变速的作用
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
				fsc.tickFrameSequence(real_cpu_clocks);
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
			if (outm == last_out) // 这里用两段时间里面输出采样没变来判定 silent
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
		 */
		
		return ret;
	}
	
	/**
	 * 从 Mixer 中读取音频数据
	 */
	private void readMixer() {
		runtime.mixer.finishBuffer();
		runtime.mixer.readBuffer(data, 0, data.length);
	}
	
	@Override
	public boolean isFinished() {
		return false;
	}

	/* **********
	 * 仪表盘区 *
	 ********** */
	/*
	 * 用于控制实际播放数据的部分.
	 * 其中有: 控制音量、控制是否播放
	 */
	
	/**
	 * @return
	 *   当前正在播放的曲目号
	 * @since v0.2.8
	 */
	public int getCurrentTrack() {
		return runtime.manager.getSong();
	}
	
	/**
	 * @since v0.2.8
	 */
	@Override
	public Set<Byte> allChannelSet() {
		return new HashSet<>(runtime.chips.keySet());
	}
	
	/**
	 * 设置某个轨道的音量
	 * @param channelCode
	 *   轨道号
	 * @param level
	 *   音量. 范围 [0, 1]
	 * @since v0.2.4
	 */
	public void setLevel(byte channelCode, float level) {
		if (level < 0) {
			level = 0;
		} else if (level > 1) {
			level = 1;
		}
		runtime.mixer.setLevel(channelCode, level);
	}
	
	/**
	 * 获得某个轨道的音量
	 * @param channelCode
	 *   轨道号
	 * @return
	 *   音量. 范围 [0, 1]
	 * @throws NullPointerException
	 *   当不存在 <code>channelCode</code> 对应的轨道时
	 * @since v0.2.4
	 */
	public float getLevel(byte channelCode) throws NullPointerException {
		return runtime.mixer.getLevel(channelCode);
	}
	
	/**
	 * 设置轨道是否发出声音
	 * @param channelCode
	 *   轨道号
	 * @param mask
	 *   false, 使该轨道发声; true, 则静音
	 * @since v0.2.4
	 */
	public void setChannelMask(byte channelCode, boolean mask) {
		AbstractSoundChip chip = runtime.chips.get(channelCode);
		if (chip != null) {
			chip.getSound(channelCode).setMask(mask);
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
	public boolean isChannelMask(byte channelCode) throws NullPointerException {
		return runtime.chips.get(channelCode).getSound(channelCode).isMask();
	}

}
