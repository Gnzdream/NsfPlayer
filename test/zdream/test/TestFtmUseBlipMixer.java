package zdream.test;

import zdream.nsfplayer.ftm.audio.FtmAudio;
import zdream.nsfplayer.ftm.factory.FtmAudioFactory;
import zdream.nsfplayer.ftm.renderer.FamiTrackerConfig;
import zdream.nsfplayer.ftm.renderer.FamiTrackerRenderer;
import zdream.nsfplayer.sound.blip.BlipMixerConfig;
import zdream.utils.common.BytesPlayer;

/**
 * 该用例展示如何使用 Blip 混音器来演奏 FTM 音频
 * 
 * @author Zdream
 * @since v0.2.6-test
 */
public class TestFtmUseBlipMixer {
	
	public static void main(String[] args) throws Exception {
		String path =
				"test\\assets\\test\\mm10nsf.ftm"
				;
		
		FtmAudioFactory factory = new FtmAudioFactory();
		FtmAudio audio = factory.create(path);
		
		FamiTrackerConfig c = new FamiTrackerConfig();
		c.mixerConfig = new BlipMixerConfig();
		
		// 播放部分
		FamiTrackerRenderer renderer = new FamiTrackerRenderer(c);
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
	
}
