package zdream.test;

import java.io.IOException;

import com.zdream.famitracker.test.BytesPlayer;

import zdream.nsfplayer.nsf.audio.NsfAudio;
import zdream.nsfplayer.nsf.audio.NsfAudioFactory;
import zdream.nsfplayer.nsf.renderer.NsfRenderer;

public class TestNsf {
	
	public static void main(String[] args) {
		NsfAudioFactory factory = new NsfAudioFactory();
		NsfAudio nsf;
		
		try {
			nsf = factory.createFromFile("src/assets/test/mm10nsf.nsf");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		NsfRenderer renderer = new NsfRenderer();
		renderer.ready(nsf, 8);
		
		BytesPlayer player = new BytesPlayer();
		byte[] bs = new byte[1600];
		
		for (int i = 0; i < 3600; i++) {
			System.out.println(String.format("---- %03d ----", i));
			int len = renderer.renderOneFrame(bs, 0, bs.length);
			player.writeSamples(bs, 0, len);
		}
	}

}
