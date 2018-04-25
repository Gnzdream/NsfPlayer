package zdream.nsfplayer.ftm;

import java.nio.charset.Charset;

import zdream.nsfplayer.ftm.document.FamiTrackerCreater;
import zdream.nsfplayer.ftm.document.FamiTrackerHandler;

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
		m_pSettings = new FamiTrackerSetting();
		creater = new FamiTrackerCreater();
	}
	
	public final FamiTrackerSetting m_pSettings;
	public final FamiTrackerCreater creater;
	
	/**
	 * 这是我所定义的方法, 用来加载 FamiTrackerDoc 的文件.
	 * @param filename
	 */
	public FamiTrackerHandler open(String filename) throws Exception {
		return creater.create(filename);
	}
	
	/**
	 * <p>这是我所定义的方法, 用来播放. 调用的前提是你已经加载完 FamiTrackerDoc.
	 * <p>它毫无疑问是个阻塞方法.
	 */
	public void play(int track) {
		
	}

}
