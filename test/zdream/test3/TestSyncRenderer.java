package zdream.test3;

import java.io.IOException;

import zdream.nsfplayer.ftm.audio.FtmAudio;
import zdream.nsfplayer.ftm.factory.FamiTrackerFormatException;
import zdream.nsfplayer.ftm.factory.FtmAudioFactory;
import zdream.nsfplayer.ftm.renderer.FamiTrackerConfig;
import zdream.nsfplayer.ftm.renderer.FamiTrackerSyncRenderer;
import zdream.nsfplayer.mixer.xgm.XgmMixerConfig;
import zdream.utils.common.BytesPlayer;

/**
 * 该用例展示如何使用 FamiTrackerSyncRenderer 来同时演奏多个 FTM 音频
 * 
 * @author Zdream
 * @since v0.3.1-test
 */
public class TestSyncRenderer {
	
	public static void main(String[] args) throws FamiTrackerFormatException, IOException {
		String path =
				"test\\assets\\test\\Hornet 2xVRC7.ftm"
				;
		String path2 =
				"test\\assets\\test\\mm9nsf.ftm"
				;
		
		FtmAudioFactory factory = new FtmAudioFactory();
		FtmAudio audio = factory.create(path);
		FtmAudio audio2 = factory.create(path2);
		
		// 这部分选择混音器, 可以删除
		FamiTrackerConfig c = new FamiTrackerConfig();
		XgmMixerConfig cc = new XgmMixerConfig();
		cc.channelType = XgmMixerConfig.TYPE_SINGER;
		c.mixerConfig = cc;
		// 选择混音器 END
		
		// 选择需要渲染的轨道
		FamiTrackerSyncRenderer renderer = new FamiTrackerSyncRenderer(c);
		renderer.allocate(audio, 0,
				FamiTrackerSyncRenderer.CHANNEL_VRC7_FM1,
				FamiTrackerSyncRenderer.CHANNEL_VRC7_FM2,
				FamiTrackerSyncRenderer.CHANNEL_VRC7_FM3,
				FamiTrackerSyncRenderer.CHANNEL_VRC7_FM4,
				FamiTrackerSyncRenderer.CHANNEL_VRC7_FM5,
				FamiTrackerSyncRenderer.CHANNEL_VRC7_FM6);
		renderer.allocate(audio, 1,
				FamiTrackerSyncRenderer.CHANNEL_VRC7_FM1,
				FamiTrackerSyncRenderer.CHANNEL_VRC7_FM2,
				FamiTrackerSyncRenderer.CHANNEL_VRC7_FM3,
				FamiTrackerSyncRenderer.CHANNEL_VRC7_FM4,
				FamiTrackerSyncRenderer.CHANNEL_VRC7_FM5,
				FamiTrackerSyncRenderer.CHANNEL_VRC7_FM6);
		renderer.allocate(audio2, 12,
				FamiTrackerSyncRenderer.CHANNEL_2A03_PULSE1,
				FamiTrackerSyncRenderer.CHANNEL_2A03_PULSE2,
				FamiTrackerSyncRenderer.CHANNEL_2A03_TRIANGLE,
				FamiTrackerSyncRenderer.CHANNEL_2A03_NOISE);
		
		BytesPlayer player = new BytesPlayer();
		byte[] bs = new byte[3200];
		
		for (int i = 0; i < 6000; i++) {
			int size = renderer.render(bs, 0, 3201);
			player.writeSamples(bs, 0, size);
			if (renderer.isFinished()) {
				break;
			}
		}
	}

}
