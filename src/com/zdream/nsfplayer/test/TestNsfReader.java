package com.zdream.nsfplayer.test;

import java.io.IOException;
import java.util.Properties;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import com.zdream.nsfplayer.vcm.Value;
import com.zdream.nsfplayer.xgm.player.nsf.NsfAudio;
import com.zdream.nsfplayer.xgm.player.nsf.NsfPlayer;
import com.zdream.nsfplayer.xgm.player.nsf.NsfPlayerConfig;

public class TestNsfReader {
	
	NsfPlayer player;
	NsfAudio nsf;
	NsfPlayerConfig config;
	
	int rate, bps, nch;
	
	// javax
	private SourceDataLine dateline;
	
	// 播放时检查的数据
	boolean kill = false, pause = false, reset = false;
	int decode_pos;
	
	Properties conf = new Properties();
	
	public static void main(String[] args) throws IOException {
		
		TestNsfReader r = new TestNsfReader();
		r.init();
		r.play("src\\assets\\test\\mm10nsf.nsf");
	}
	
	{
		try {
			conf.load(getClass().getResourceAsStream("in_yansf.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void init() {
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
		
		// 负载配置
		config.loadProperties(conf);
		
		if (config.get("MASK_INIT").toInt() != 0) {
			config.setValue("MASK", new Value(0));
		}
		
		// {in_yansf.dll!Init(void)}
	}
	
	boolean audio_print = false;
	
	public void play(String fn) throws IOException {
		
		// {in_yansf.dll!WA2NSF::Play(char *)}
		nsf.setDefaults(config.getIntValue("PLAY_TIME"), config.getIntValue("FADE_TIME"),
				config.getIntValue("LOOP_NUM"));
		
		// {in_yansf.dll!WA2InputModule::PlayThread(WA2InputModule *)}
		
		rate = config.getIntValue("RATE");
		bps = config.getIntValue("BPS");
		nch = config.getIntValue("NCH");
		
		nsf.loadFile(fn);
		player.setPlayFreq(rate);
		player.setChannels(nch);
		
		int bufferSize = rate / 2;
		
		AudioFormat af = new AudioFormat(rate, bps, nch, true, false);
		try {
			dateline = AudioSystem.getSourceDataLine(af);
			dateline.open(af, bufferSize);
			// dateline.open(af);
		} catch (LineUnavailableException e) {
			System.out.println("初始化音频输出失败。");
		}

		dateline.start();
		pause = false;
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
				
				dateline.write(bs, 0, samples);
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
	
	public static final void debugOut(String text) {
		
	}
	
}
