package zdream.test3;

import static zdream.nsfplayer.core.INsfChannelCode.CHANNEL_2A03_DPCM;
import static zdream.nsfplayer.core.INsfChannelCode.CHANNEL_2A03_NOISE;
import static zdream.nsfplayer.core.INsfChannelCode.CHANNEL_2A03_PULSE1;
import static zdream.nsfplayer.core.INsfChannelCode.CHANNEL_2A03_PULSE2;
import static zdream.nsfplayer.core.INsfChannelCode.CHANNEL_N163_1;
import static zdream.nsfplayer.core.INsfChannelCode.CHANNEL_N163_2;
import static zdream.nsfplayer.core.INsfChannelCode.CHANNEL_N163_3;
import static zdream.nsfplayer.core.INsfChannelCode.CHANNEL_N163_4;
import static zdream.nsfplayer.core.INsfChannelCode.CHANNEL_VRC7_FM1;
import static zdream.nsfplayer.core.INsfChannelCode.CHANNEL_VRC7_FM2;
import static zdream.nsfplayer.core.INsfChannelCode.CHANNEL_VRC7_FM3;
import static zdream.nsfplayer.core.INsfChannelCode.CHANNEL_VRC7_FM4;
import static zdream.nsfplayer.core.INsfChannelCode.CHANNEL_VRC7_FM5;
import static zdream.nsfplayer.core.INsfChannelCode.CHANNEL_VRC7_FM6;

import java.io.IOException;

import zdream.nsfplayer.ftm.audio.FtmAudio;
import zdream.nsfplayer.ftm.factory.FamiTrackerFormatException;
import zdream.nsfplayer.ftm.factory.FtmAudioFactory;
import zdream.nsfplayer.ftm.process.agreement.WaitingAgreement;
import zdream.nsfplayer.ftm.process.base.FtmPosition;
import zdream.nsfplayer.ftm.renderer.FamiTrackerConfig;
import zdream.nsfplayer.ftm.renderer.FamiTrackerSyncRenderer;
import zdream.nsfplayer.mixer.xgm.XgmMixerConfig;
import zdream.utils.common.BytesPlayer;

/**
 * 该用例展示如何使用 WaitingAgreement 来控制多个 FamiTracker 音频的同步
 * 
 * @author Zdream
 * @since v0.3.1-test
 */
public class TestWaitingAgreement {

	public static void main(String[] args) throws FamiTrackerFormatException, IOException {
		String path =
				"test\\assets\\test\\N163 - Enigma of Aqua (Sync Play Version).ftm"
				;
		String path2 =
				"test\\assets\\test\\VRC7 - Enigma of Aqua (Sync Play Version).ftm"
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
		int exeId1 = renderer.allocateAll(audio);
		int exeId2 = renderer.allocateAll(audio2, 0, 5); // 曲目 2 开始在段 5
		
		renderer.setSpeed(1f);
		renderer.free(exeId1, CHANNEL_2A03_DPCM);
		renderer.free(exeId2, CHANNEL_2A03_PULSE1);
		renderer.free(exeId2, CHANNEL_2A03_PULSE2);
		renderer.free(exeId2, CHANNEL_2A03_NOISE);

		// 设置音量
		renderer.setLevel(exeId1, CHANNEL_N163_1, 0.6f);
		renderer.setLevel(exeId1, CHANNEL_N163_2, 0.6f);
		renderer.setLevel(exeId1, CHANNEL_N163_3, 0.6f);
		renderer.setLevel(exeId1, CHANNEL_N163_4, 0.9f);
		renderer.setLevel(exeId2, CHANNEL_VRC7_FM1, 0.4f);
		renderer.setLevel(exeId2, CHANNEL_VRC7_FM2, 0.5f);
		renderer.setLevel(exeId2, CHANNEL_VRC7_FM3, 0.5f);
		renderer.setLevel(exeId2, CHANNEL_VRC7_FM4, 0.4f);
		renderer.setLevel(exeId2, CHANNEL_VRC7_FM5, 0.4f);
		renderer.setLevel(exeId2, CHANNEL_VRC7_FM6, 0.4f);
		
		// 开始时, 曲目 2 在等待,
		// 当曲目 1 执行到段 3 开头时, 曲目 2 开始播放.
		WaitingAgreement a = new WaitingAgreement(exeId2, new FtmPosition(5), exeId1, new FtmPosition(3));
		a.setTimeoutForever(); // 永远不超时
		renderer.addWaitingAgreement(a);
		
		BytesPlayer player = new BytesPlayer();
		byte[] bs = new byte[3600];
		
		for (int i = 0; i < 60000; i++) {
			int size = renderer.render(bs, 0, bs.length);
			player.writeSamples(bs, 0, size);
			if (renderer.isFinished()) {
				break;
			}
		}
	}

}
