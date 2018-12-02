package zdream.nsfplayer.core;

import java.io.IOException;
import java.nio.charset.Charset;

import zdream.nsfplayer.ftm.audio.FtmAudio;
import zdream.nsfplayer.ftm.factory.FamiTrackerFormatException;
import zdream.nsfplayer.ftm.factory.FtmAudioFactory;
import zdream.nsfplayer.mixer.factory.NsfSoundMixerFactory;

/**
 * <p>NsfPlayer 应用
 * <p>替换原来的 FamiTrackerApplication
 * </p>
 * 
 * @author Zdream
 * @since v0.3.0
 */
public class NsfPlayerApplication {
	
	public static final NsfPlayerApplication app;
	
	public static Charset defCharset;
	
	static {
		defCharset = Charset.forName("UTF-8");
		app = new NsfPlayerApplication();
	}
	
	public NsfPlayerApplication() {
		ftmFactory = new FtmAudioFactory();
		mixerFactory = new NsfSoundMixerFactory();
	}
	
	/* **********
	 *   FTM    *
	 ********** */
	
	public final FtmAudioFactory ftmFactory;
	
	/**
	 * 加载 FamiTracker (.ftm) 的文件, 生成 {@link FtmAudio} 实例
	 * @param filePath
	 *   文件路径
	 */
	public FtmAudio open(String filePath) throws IOException, FamiTrackerFormatException {
		return ftmFactory.create(filePath);
	}
	
	/**
	 * 加载 FamiTracker 导出的文本文件 (.txt), 生成 {@link FtmAudio} 实例
	 * @param filePath
	 *   文件路径
	 * @since v0.2.5
	 */
	public FtmAudio openWithTxt(String filePath) throws IOException, FamiTrackerFormatException {
		return ftmFactory.createFromTextPath(filePath);
	}
	
	/* **********
	 *   NSF    *
	 ********** */
	
	/* **********
	 *  Mixer   *
	 ********** */
	
	public final NsfSoundMixerFactory mixerFactory;
	
}
