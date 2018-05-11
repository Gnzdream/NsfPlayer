package zdream.nsfplayer.ftm;

import com.zdream.famitracker.test.BytesPlayer;

import zdream.nsfplayer.ftm.document.FtmAudio;
import zdream.nsfplayer.ftm.renderer.FamiTrackerRenderer;

public class TestFamiTracker {

	public static void main(String[] args) throws Exception {
		FtmAudio audio = FamiTrackerApplication.app.open(
//				"D:\\Program\\Rockman\\FamiTracker\\Project\\Shovel Knight\\22_Of_Devious_Machinations_Clockwork_Tower.ftm"
				"D:\\Program\\Rockman\\FamiTracker\\Project\\Rockman10\\mm10nsf.ftm"
//				"D:\\Program\\Rockman\\FamiTracker\\Project\\Shovel Knight\\51_Unused_Song.ftm"
		);
		System.out.println("完成");
		System.out.println(audio);
		
		// 播放部分
		FamiTrackerRenderer renderer = new FamiTrackerRenderer();
		renderer.ready(audio);
		
		BytesPlayer player = new BytesPlayer();
		byte[] bs = new byte[2400];
		
		while (!renderer.isFinished()) {
			int size = renderer.render(bs, 0, 2400);
			player.writeSamples(bs, 0, size);
		}
	}

}
