package zdream.nsfplayer.ftm;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import zdream.nsfplayer.core.AbstractNsfAudio;
import zdream.nsfplayer.core.AbstractNsfRenderer;
import zdream.nsfplayer.ftm.audio.FtmAudio;
import zdream.nsfplayer.ftm.cmd.BaseHandler;
import zdream.nsfplayer.ftm.cmd.ChannelHandler;
import zdream.nsfplayer.ftm.cmd.ICommandHandler;
import zdream.nsfplayer.ftm.cmd.PlayHandler;
import zdream.nsfplayer.ftm.cmd.SongHandler;
import zdream.nsfplayer.ftm.renderer.FamiTrackerConfig;
import zdream.nsfplayer.ftm.renderer.FamiTrackerRenderer;
import zdream.nsfplayer.ftm.task.ChooseSongTask;
import zdream.nsfplayer.ftm.task.IFtmTask;
import zdream.nsfplayer.ftm.task.OpenTask;
import zdream.nsfplayer.ftm.task.OpenType;
import zdream.nsfplayer.ftm.task.PauseTask;
import zdream.nsfplayer.ftm.task.PlayTask;
import zdream.nsfplayer.mixer.xgm.XgmMixerConfig;
import zdream.nsfplayer.nsf.audio.NsfAudio;
import zdream.nsfplayer.nsf.renderer.NsfRenderer;
import zdream.nsfplayer.nsf.renderer.NsfRendererConfig;
import zdream.utils.common.BytesPlayer;
import zdream.utils.common.CodeSpliter;

/**
 * <p>用于测试播放 FamiTracker 文件音频的控制面板 
 * <p>虽然用的是命令行 / 类似 Shell
 * </p>
 * 
 * @version v0.2.8-test
 *   补充对 NSF 部分的支持
 * 
 * @author Zdream
 * @since v0.2.3-test
 */
public class FtmPlayerConsole {
	// FTM
	final FamiTrackerRenderer ftmRenderer;
	FtmAudio ftm;
	// NSF
	final NsfRenderer nsfRenderer;
	NsfAudio nsf;
	
	// Player
	OpenType type = OpenType.FTM;
	final BytesPlayer player;
	
	PlayThreadForFtm thread;
	
	// 缓存
	private short[] samples;

	// 指令解析
	Map<String, ICommandHandler> handlers = new HashMap<String, ICommandHandler>();

	public FtmPlayerConsole() {
		XgmMixerConfig c = new XgmMixerConfig();
		c.channelType = XgmMixerConfig.TYPE_SINGER;
		
		FamiTrackerConfig config1 = new FamiTrackerConfig();
		config1.mixerConfig = c;
		ftmRenderer = new FamiTrackerRenderer(config1);
		
		NsfRendererConfig config2 = new NsfRendererConfig();
		config2.mixerConfig = c;
		nsfRenderer = new NsfRenderer(config2);
		player = new BytesPlayer();
		
		attachHandler(new BaseHandler());
		attachHandler(new SongHandler());
		attachHandler(new ChannelHandler());
		attachHandler(new PlayHandler());
	}
	
	public FamiTrackerRenderer getFamiTrackerRenderer() {
		return ftmRenderer;
	}
	
	public NsfRenderer getNsfRenderer() {
		return nsfRenderer;
	}
	
	public AbstractNsfRenderer<?> getRenderer() {
		return (type == OpenType.FTM) ? ftmRenderer : nsfRenderer;
	}
	
	public FtmAudio getFtmAudio() {
		return ftm;
	}
	
	public NsfAudio getNsfAudio() {
		return nsf;
	}
	
	public AbstractNsfAudio getAudio() {
		return (type == OpenType.FTM) ? ftm : nsf;
	}
	
	public void setFtmAudio(FtmAudio audio) {
		this.ftm = audio;
		this.type = OpenType.FTM;
	}
	
	public void setNsfAudio(NsfAudio audio) {
		this.nsf = audio;
		this.type = OpenType.NSF;
	}
	
	public void setAudio(AbstractNsfAudio audio) {
		if (audio instanceof FtmAudio) {
			setFtmAudio((FtmAudio) audio);
		} else if (audio instanceof NsfAudio) {
			setNsfAudio((NsfAudio) audio);
		}
	}
	
	public OpenType getType() {
		return type;
	}

	public void setType(OpenType type) {
		this.type = type;
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
		this.thread = new PlayThreadForFtm(this);
		OpenTask t = new OpenTask("test\\assets\\test\\mm10nsf.ftm");
		t.setOption("beginSong", 8);
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
		IFtmTask t = PlayTask.getOne();
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
	}

	public short[] getLastSampleBytes() {
		if (samples == null) {
			samples = new short[1200];
		}
		return samples;
	}
	
	public int writeSamples(int off, int len) {
		return player.writeSamples(samples, off, len);
	}
	
	public void putTask(IFtmTask task) {
		thread.putTask(task);
	}
	
	public IFtmTask nextTask() {
		return thread.nextTask();
	}
	
	public void clearTask() {
		thread.queue.clear();
	}
	
	public void printOut(String text, Object...args) {
		if (args == null || args.length == 0) {
			System.out.println(text);
		} else {
			System.out.println(String.format(text, args));
		}
	}
	
}
