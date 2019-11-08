package zdream.test;

import java.io.IOException;
import java.util.Properties;

import zdream.nsfplayer.ftm.FtmPlayerConsole;

/**
 * @author Zdream
 * @since v0.2.3-test
 */
public class TestFtmPlayer {

	/**
	 * @param args
	 *   比如: -mixer:blip
	 * 
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Properties prop = new Properties();
		
		if (args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				if (args[i].startsWith("-mixer:") ) {
					prop.setProperty("mixer", args[i].substring("-mixer:".length()));
				}
			}
		}
		
		FtmPlayerConsole r = new FtmPlayerConsole(prop);
		r.go();
	}

}
