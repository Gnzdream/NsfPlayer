package zdream.test;

import com.zdream.famitracker.test.BytesPlayer;

import zdream.nsfplayer.ftm.FamiTrackerApplication;
import zdream.nsfplayer.ftm.audio.FtmAudio;
import zdream.nsfplayer.ftm.factory.FtmAudioFactory;
import zdream.nsfplayer.ftm.renderer.FamiTrackerRenderer;

public class TestFamiTracker {

	public static void main(String[] args) throws Exception {
		FtmAudio audio = testReadFtm();
		
		// 播放部分
		FamiTrackerRenderer renderer = new FamiTrackerRenderer();
		renderer.ready(audio, 44);
		
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
	
	public static FtmAudio testReadFtm() throws Exception {
		String path =
				"src\\assets\\test\\mm10nsf.ftm"
				;
		
		FtmAudio audio = FamiTrackerApplication.app.open(path);
		System.out.println(path + " 完成");
		System.out.println(audio);
		return audio;
	}
	
	public static FtmAudio testReadFtmText() throws Exception {
		FtmAudioFactory factory = new FtmAudioFactory();
		
		FtmAudio audio = factory.createFromTextPath(
				"src\\assets\\test\\Editor_05.txt"
//				"src\\assets\\test\\mm10nsf.txt"
		);
		System.out.println("完成");
		System.out.println(audio);
		return audio;
	}

}
