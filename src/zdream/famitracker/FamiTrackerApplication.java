package zdream.famitracker;

import java.nio.charset.Charset;

import zdream.famitracker.document.FamiTrackerCreater;
import zdream.famitracker.document.FamiTrackerDocument;

/**
 * 应用的实体
 * @author Zdream
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
	public FamiTrackerDocument open(String filename) throws Exception {
		return creater.create(filename);
	}
	
	/**
	 * <p>这是我所定义的方法, 用来播放. 调用的前提是你已经加载完 FamiTrackerDoc.
	 * <p>它毫无疑问是个阻塞方法.
	 */
	public void play(int track) {
		
	}

}
