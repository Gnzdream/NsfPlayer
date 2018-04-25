package zdream.nsfplayer.ftm;

import zdream.nsfplayer.ftm.document.FtmAudio;

public class TestFamiTracker {

	public static void main(String[] args) throws Exception {
		FtmAudio audio = FamiTrackerApplication.app.open(
				"D:\\Program\\Rockman\\FamiTracker\\Project\\Shovel Knight\\22_Of_Devious_Machinations_Clockwork_Tower.ftm"
		);
		System.out.println("完成");
		System.out.println(audio);
	}

}
