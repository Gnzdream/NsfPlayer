package zdream.nsfplayer.ftm;

import java.io.IOException;
import java.nio.charset.Charset;

import zdream.nsfplayer.ftm.audio.FtmAudio;
import zdream.nsfplayer.ftm.factory.FtmAudioFactory;
import zdream.nsfplayer.ftm.factory.FtmParseException;

/**
 * 应用的实体. 用于打开 FamiTracker 的文件等操作
 * @author Zdream
 * @date 2018-04-25
 */
public class FamiTrackerApplication {
	
	public static final FamiTrackerApplication app;
	
	public static Charset defCharset;
	
	static {
		defCharset = Charset.forName("UTF-8");
		app = new FamiTrackerApplication();
	}
	
	public FamiTrackerApplication() {
		factory = new FtmAudioFactory();
	}
	
	public final FtmAudioFactory factory;
	
	/**
	 * 加载 FamiTracker (.ftm) 的文件, 形成 {@link FtmAudio} 实例
	 * @param filename
	 */
	public FtmAudio open(String filename) throws IOException, FtmParseException {
		return factory.create(filename);
	}
	
}
