package zdream.nsfplayer.ftm;

import com.zdream.famitracker.test.BytesPlayer;

import zdream.nsfplayer.ftm.document.FtmAudio;
import zdream.nsfplayer.ftm.factory.FtmAudioFactory;
import zdream.nsfplayer.ftm.renderer.FamiTrackerRenderer;

public class TestFamiTracker {

	public static void main(String[] args) throws Exception {
		FtmAudio audio = testReadFtm();
		
		// 播放部分
		FamiTrackerRenderer renderer = new FamiTrackerRenderer();
		renderer.ready(audio, 32);
		
		BytesPlayer player = new BytesPlayer();
		byte[] bs = new byte[2400];
		
		while (!renderer.isFinished()) {
			int size = renderer.render(bs, 0, 2400);
			player.writeSamples(bs, 0, size);
		}
	}
	
	public static FtmAudio testReadFtm() throws Exception {
		String path = 
//				"D:\\Program\\Rockman\\FamiTracker\\Project\\Shovel Knight\\22_Of_Devious_Machinations_Clockwork_Tower.ftm"
				"D:\\Program\\Rockman\\FamiTracker\\Project\\Rockman10\\mm10nsf.ftm"
//				"D:\\Program\\Rockman\\FamiTracker\\Project\\Shovel Knight\\51_Unused_Song.ftm"
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
