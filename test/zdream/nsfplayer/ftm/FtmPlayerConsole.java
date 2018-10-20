package zdream.nsfplayer.ftm;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.zdream.famitracker.test.BytesPlayer;

import zdream.nsfplayer.ftm.cmd.BaseHandler;
import zdream.nsfplayer.ftm.cmd.ICommandHandler;
import zdream.nsfplayer.ftm.cmd.SongHandler;
import zdream.nsfplayer.ftm.task.ChooseSongTask;
import zdream.nsfplayer.ftm.task.OpenTask;
import zdream.nsfplayer.ftm.task.PauseTask;
import zdream.nsfplayer.ftm.task.PlayTask;
import zdream.nsfplayer.ftm.document.FtmAudio;
import zdream.nsfplayer.ftm.factory.FtmAudioFactory;
import zdream.nsfplayer.ftm.renderer.FamiTrackerRenderer;
import zdream.nsfplayer.ftm.task.IFtmTask;
import zdream.utils.common.CodeSpliter;

/**
 * <p>用于测试播放 FamiTracker 文件音频的控制面板 
 * <p>虽然用的是命令行 / 类似 Shell
 * </p>
 * 
 * @author Zdream
 * @since v0.2.3-test
 */
public class FtmPlayerConsole {
	
	FamiTrackerRenderer renderer;
	FtmAudio audio;
	BytesPlayer player;
	
	FtmAudioFactory factroy;
	PlayThreadForFtm thread;
	
	// 缓存
	private byte[] samples;

	// 指令解析
	Map<String, ICommandHandler> handlers = new HashMap<String, ICommandHandler>();

	public FtmPlayerConsole() {
		init();
		
		attachHandler(new BaseHandler());
		attachHandler(new SongHandler());
	}
	
	private void init() {
		renderer = new FamiTrackerRenderer();
		player = new BytesPlayer();
	}
	
	public FamiTrackerRenderer getRenderer() {
		return renderer;
	}
	
	public FtmAudio getAudio() {
		return audio;
	}
	
	public void setAudio(FtmAudio audio) {
		this.audio = audio;
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
		OpenTask t = new OpenTask("src\\assets\\test\\mm10nsf.ftm");
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

//	public NsfPlayer getPlayer() {
//		return player;
//	}
//
//	public NsfAudio getNsf() {
//		return nsf;
//	}
//
//	public NsfPlayerConfig getConfig() {
//		return config;
//	}
	
	public byte[] getLastSampleBytes() {
		if (samples == null) {
			samples = new byte[2400];
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
