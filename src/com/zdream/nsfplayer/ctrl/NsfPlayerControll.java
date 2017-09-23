package com.zdream.nsfplayer.ctrl;

import java.io.IOException;
import java.util.Properties;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import com.zdream.nsfplayer.ctrl.task.ITask;
import com.zdream.nsfplayer.ctrl.task.OpenTask;
import com.zdream.nsfplayer.vcm.Value;
import com.zdream.nsfplayer.xgm.player.nsf.NsfAudio;
import com.zdream.nsfplayer.xgm.player.nsf.NsfPlayer;
import com.zdream.nsfplayer.xgm.player.nsf.NsfPlayerConfig;

/**
 * 控制面板 (虽然用的是命令行 / 类似 Shell)
 * @author Zdream
 *
 */
public class NsfPlayerControll implements INsfPlayerEnv {
	
	NsfPlayer player;
	NsfAudio nsf;
	NsfPlayerConfig config;
	
	int rate, bps, nch;
	PlayThread thread;
	
	// javax
	private SourceDataLine dateline;
	
	// 缓存
	private byte[] samples;
	
	// 播放时检查的数据
	boolean kill = false, pause = false, reset = false;
	int decode_pos;
	
	// 补充
	/**
	 * 这个主要关注 dateline.start() 是否已经被调用
	 */
	boolean started = false;
	
	Properties conf = new Properties();
	
	{
		try {
			conf.load(getClass().getResourceAsStream("in_yansf.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		init();
	}
	
	public static void main(String[] args) throws IOException {
		NsfPlayerControll r = new NsfPlayerControll();
//		r.open("src\\assets\\test\\mm10nsf.nsf");
//		r.play();
		
		r.thread = new PlayThread(r);
		OpenTask t = new OpenTask("src\\assets\\test\\mm10nsf.nsf");
		t.setOption("s", 8);
		r.putTask(t);
		r.thread.run();
	}
	
	private void init() {
		// 播放器的创建
		player = new NsfPlayer();
		nsf = new NsfAudio();
		config = new NsfPlayerConfig();
		player.setConfig(config);
		player.nsf = nsf;

		// 扩展 Winamp 版的补充设置
		config.createValue("WRITE_TAGINFO", 0);
		config.createValue("READ_TAGINFO", 0);
		config.createValue("UPDATE_PLAYLIST", 0);
		config.createValue("MASK_INIT", 1);
		config.createValue("INFO_DELAY", 50);
		config.createValue("INFO_FREQ", 30);
		config.createValue("GRAPHIC_MODE", 1);
		config.createValue("FREQ_MODE", 1);
		config.createValue("LAST_PRESET", "Default");
		config.createValue("INI_FILE", "");
		
		// 读取配置
		config.loadProperties(conf);
		
		if (config.get("MASK_INIT").toInt() != 0) {
			config.setValue("MASK", new Value(0));
		}
		
		nsf.setDefaults(config.getIntValue("PLAY_TIME"), config.getIntValue("FADE_TIME"),
				config.getIntValue("LOOP_NUM"));
		
		// {in_yansf.dll!WA2InputModule::PlayThread(WA2InputModule *)}
		
		rate = config.getIntValue("RATE");
		bps = config.getIntValue("BPS");
		nch = config.getIntValue("NCH");
		
		player.setPlayFreq(rate);
		player.setChannels(nch);
		
		AudioFormat af = new AudioFormat(rate, bps, nch, true, false);
		try {
			dateline = AudioSystem.getSourceDataLine(af);
			dateline.open(af, rate);
		} catch (LineUnavailableException e) {
			System.err.println("初始化音频输出失败。");
		}
		
		// 我自己的补充配置
		/*
		 * 音乐播放是一个片段一个片段播放的, 这里给出的是每个片段的时间长短.
		 * 50 ms = 0.05 s
		 * 如果在 48000 Hz 的环境下播放, 每个片段要过 2400 个采样点.
		 * 如果在 16 位深度, 双声道的情况下, 每次送过去的 byte[] 数组长度为 2400 * 2 * 2 = 9600;
		 */
		config.createValue("PLAYER_FRAGMENT_IN_MS", 50);
		config.createValue("PLAYER_FRAGMENT_IN_BYTES", rate * (bps / 8) * nch / 20); // 20 = 1000 / 50
	}
	
	boolean audio_print = false;
	
	/**
	 * 打开 NSF 文件, 并进行循环播放.<br>
	 * 这个方法内部会调用 <code>play()</code> 方法.
	 * @param fn
	 * @throws IOException
	 */
	public void open(String fn) throws IOException {
		nsf.loadFile(fn);
	}
	
	/**
	 * 循环播放已经打开的 NSF 文件.<br>
	 * 如果播放前没有成功使用过 <code>open()</code> 方法, 该方法会失败;<br>
	 * 如果成功使用过 <code>open()</code> 方法, 将播放 NSF 文件, 从第一首开始循环播放.
	 * @throws IOException
	 */
	public void play() throws IOException {
		play(0);
	}
	
	/**
	 * 循环播放已经打开的 NSF 文件.<br>
	 * 如果播放前没有成功使用过 <code>open()</code> 方法, 该方法会失败;<br>
	 * 如果成功使用过 <code>open()</code> 方法, 将播放 NSF 文件, 从第 <code>beginSong</code> 首开始循环播放.
	 * @param beginSong
	 *   指定从第几首歌开始播放. 这个值的范围是从 0 开始的.
	 * @throws IOException
	 */
	public void play(int beginSong) throws IOException {
		dateline.start();
		pause = false;
		player.setSong(beginSong);
		player.reset();

		int totle_song = nsf.songs;
		while (nsf.song < totle_song) {
			while (!kill) {
				if (reset) {
					player.reset();
					decode_pos = 0;
					reset = false;
				}

				if (player.isStopped()) {
					kill = true;
				}
				
				byte[] bs = new byte[4608];
				int samples = player.render(bs, 0, bs.length);
				
				// debug
				if (audio_print) {
					String asd = "0123456789abcdef";
					System.out.println("samples: " + samples);
					StringBuilder b = new StringBuilder();
					int icount = 0;
					for (int i = 0; i < bs.length; i+=100,icount++) {
						b.append(String.format("%2d", icount)).append(':').append(' ');
						int offset = icount * 100;
						int end = (bs.length - offset) > 100 ? 100 : (bs.length - offset);
						for (int j = 0; j < end; j += 2) {
							int ii = (bs[offset + j + 1] & 0xFF);
							if (ii < 16) {
								b.append('0').append(asd.charAt(ii));
							} else {
								b.append(asd.charAt(ii / 16)).append(asd.charAt(ii % 16));
							}
							
							ii = (bs[offset + j] & 0xFF);
							if (ii < 16) {
								b.append('0').append(asd.charAt(ii));
							} else {
								b.append(asd.charAt(ii / 16)).append(asd.charAt(ii % 16));
							}
						}
						b.append('\n');
					}
					System.out.println(b);
					asd.toString();
				}
				
				long beginTime = System.currentTimeMillis();
				dateline.write(bs, 0, samples);
				System.out.println("耗时: " + (System.currentTimeMillis() - beginTime));
			}
			
			boolean result = player.nextSong(1);
			System.out.println("song: " + nsf.song);
			if (!result) {
				break;
			}
			player.reset();
			nsf.reload();
			kill = false;
		}
	}

	public NsfPlayer getPlayer() {
		return player;
	}

	public NsfAudio getNsf() {
		return nsf;
	}

	public NsfPlayerConfig getConfig() {
		return config;
	}
	
	public byte[] getLastSampleBytes() {
		if (samples == null) {
			samples = new byte[config.getIntValue("PLAYER_FRAGMENT_IN_BYTES")];
		} else {
			int bufferSize = config.getIntValue("PLAYER_FRAGMENT_IN_BYTES");
			if (samples.length != bufferSize) {
				samples = new byte[bufferSize];
			}
		}
		return samples;
	}
	
	public int writeSamples(int off, int len) {
		if (!started) {
			dateline.start();
			started = true;
		}
		return dateline.write(samples, off, len);
	}
	
	public void putTask(ITask task) {
		thread.putTask(task);
	}
	
}
