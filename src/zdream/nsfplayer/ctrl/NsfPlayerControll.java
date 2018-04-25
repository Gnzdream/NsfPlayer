package zdream.nsfplayer.ctrl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import zdream.nsfplayer.ctrl.cmd.BaseHandler;
import zdream.nsfplayer.ctrl.cmd.ICommandHandler;
import zdream.nsfplayer.ctrl.cmd.SongHandler;
import zdream.nsfplayer.ctrl.task.ChooseSongTask;
import zdream.nsfplayer.ctrl.task.ITask;
import zdream.nsfplayer.ctrl.task.OpenTask;
import zdream.nsfplayer.ctrl.task.PauseTask;
import zdream.nsfplayer.ctrl.task.PlayTask;
import zdream.nsfplayer.vcm.Value;
import zdream.nsfplayer.xgm.player.nsf.NsfAudio;
import zdream.nsfplayer.xgm.player.nsf.NsfPlayer;
import zdream.nsfplayer.xgm.player.nsf.NsfPlayerConfig;
import zdream.utils.common.CodeSpliter;

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
	
	// 补充
	/**
	 * 这个主要关注 dateline.start() 是否已经被调用
	 */
	boolean started = false;
	
	Properties conf = new Properties();
	
	// 指令解析
	Map<String, ICommandHandler> handlers = new HashMap<String, ICommandHandler>();
	
	{
		try {
			conf.load(getClass().getResourceAsStream("in_yansf.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		init();
		
		attachHandler(new BaseHandler());
		attachHandler(new SongHandler());
	}
	
	public static void main(String[] args) throws IOException {
		NsfPlayerControll r = new NsfPlayerControll();
		r.go();
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
	
	public void attachHandler(ICommandHandler h) {
		String[] cmds = h.canHandle();
		for (int i = 0; i < cmds.length; i++) {
			handlers.put(cmds[i], h);
		}
	}
	
	/**
	 * 启动播放器线程, 而该线程监听输入事件
	 */
	public void go() {
		this.thread = new PlayThread(this);
		OpenTask t = new OpenTask("src\\assets\\test\\mm10nsf.nsf");
		t.setOption("s", 8);
		putTask(t);
		
		Thread thread = new Thread(this.thread, "player");
		thread.setDaemon(true); // 播放进程是守护进程
		thread.start();
		
		Scanner scan = new Scanner(System.in);
		String text;
		boolean exits = false;
		
		while (!exits) {
			text = scan.nextLine();
			
			String[] args = CodeSpliter.split(text);
			try {
				String cmd = args[0] = args[0].toLowerCase();
				ICommandHandler h = handlers.get(cmd);
				if (h != null) {
					h.handle(args, this);
				}
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		}
		// 一般而言不会到这里
		scan.close();
	}
	
	boolean audio_print = false;
	
	/**
	 * 打开 NSF 文件, 但不进行播放.<br>
	 * @param fn
	 * @throws IOException
	 */
	public void open(String fn) throws IOException {
		OpenTask t = new OpenTask(fn);
		putTask(t);
	}
	
	/**
	 * 循环播放已经打开的 NSF 文件.<br>
	 * 如果播放前没有成功使用过 <code>open()</code> 方法, 该方法会失败;<br>
	 * @throws IOException
	 */
	public void play() {
		ITask t = PlayTask.getOne();
		putTask(t);
	}
	
	/**
	 * 暂停播放
	 */
	public void pause() {
		putTask(new PauseTask());
	}
	
	/**
	 * 切换到曲目
	 */
	public void chooseSong(int song) {
		ChooseSongTask t = new ChooseSongTask(song);
		t.setOption("needReset", true);
		putTask(t);
		player.getStatus().replace = true;
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
	
	@Override
	public ITask nextTask() {
		return thread.nextTask();
	}
	
	public void printOut(String text, Object...args) {
		if (args == null || args.length == 0) {
			System.out.println(text);
		} else {
			System.out.println(String.format(text, args));
		}
	}
	
}
