package zdream.test;

import zdream.nsfplayer.ftm.audio.FtmAudio;
import zdream.nsfplayer.ftm.factory.FtmAudioFactory;
import zdream.nsfplayer.ftm.renderer.FamiTrackerRenderer;
import zdream.utils.common.BytesPlayer;

/**
 * <p>该用例展示如何读取 txt 文件来播放音频
 * </p>
 * 
 * @author Zdream
 * @since v0.2.6-test
 */
public class TestFtmByTXTFile {

	public static void main(String[] args) throws Exception {
		String path =
				"test\\assets\\test\\Editor_05.txt"
//				"test\\assets\\test\\mm10nsf.txt"
				;
		
		FtmAudioFactory factory = new FtmAudioFactory();
		FtmAudio audio = factory.createFromTextPath(path);
		
		// 播放部分
		FamiTrackerRenderer renderer = new FamiTrackerRenderer();
		renderer.ready(audio);
		
		BytesPlayer player = new BytesPlayer();
		byte[] bs = new byte[2400];
		
		for (int i = 0; i < 3600; i++) {
			int size = renderer.render(bs, 0, 2400);
			player.writeSamples(bs, 0, size);
			if (renderer.isFinished()) {
				break;
			}
		}
	}

}
