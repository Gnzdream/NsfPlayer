package zdream.test;

import java.io.IOException;

import zdream.nsfplayer.nsf.audio.NsfAudio;
import zdream.nsfplayer.nsf.audio.NsfAudioFactory;
import zdream.nsfplayer.nsf.renderer.NsfRenderer;
import zdream.utils.common.BytesPlayer;

/**
 * 测试 NSF 自动停止的方法
 * 
 * @author Zdream
 * @since v0.2.9-test
 */
public class TestNsfAutoStop {
	
	public static void main(String[] args) {
		NsfAudioFactory factory = new NsfAudioFactory();
		NsfAudio nsf;
		
		try {
			nsf = factory.createFromFile("test/assets/test/mm10nsf.nsf");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		NsfRenderer renderer = new NsfRenderer();
		renderer.ready(nsf, 9);
		
		BytesPlayer player = new BytesPlayer();
		short[] array = new short[1600];
		int silentLen = 0;
		int last = 0;
		
		while (true) {
			int len = renderer.renderOneFrame(array, 0, array.length);
			player.writeSamples(array, 0, len);
			
			// 通过检查采样判断乐曲是否播放完成
			if (silentLen == 0) {
				last = array[0];
			}
			for (int i = 1; i < array.length; i++) {
				if (array[i] != last) {
					silentLen = 0;
					continue;
				}
			}
			silentLen += len;
			
			if (silentLen >= 144000) { // 48000 * 3
				break;
			}
		}
	}

}
