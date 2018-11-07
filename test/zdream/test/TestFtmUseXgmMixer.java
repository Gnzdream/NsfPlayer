package zdream.test;

import zdream.nsfplayer.ftm.audio.FtmAudio;
import zdream.nsfplayer.ftm.factory.FtmAudioFactory;
import zdream.nsfplayer.ftm.renderer.FamiTrackerConfig;
import zdream.nsfplayer.ftm.renderer.FamiTrackerRenderer;
import zdream.nsfplayer.sound.xgm.XgmMixerConfig;
import zdream.utils.common.BytesPlayer;

/**
 * <p>该用例展示如何使用 Xgm 混音器来演奏 FTM 音频
 * <p>(Xgm 是默认的混音器)
 * </p>
 * 
 * @author Zdream
 * @since v0.2.6-test
 */
public class TestFtmUseXgmMixer {

	public static void main(String[] args) throws Exception {
		String path =
				"test\\assets\\test\\mm10nsf.ftm"
				;
		
		FtmAudioFactory factory = new FtmAudioFactory();
		FtmAudio audio = factory.create(path);
		
		FamiTrackerConfig c = new FamiTrackerConfig();
		c.mixerConfig = new XgmMixerConfig();
		
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
