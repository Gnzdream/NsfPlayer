package zdream.recorder;

import java.io.IOException;

import zdream.nsfplayer.nsf.audio.NsfAudio;
import zdream.nsfplayer.nsf.audio.NsfAudioFactory;
import zdream.nsfplayer.nsf.renderer.NsfRecorder;

public class TestNsfRecorder {

	public static void main(String[] args) {
		NsfAudioFactory factory = new NsfAudioFactory();
		NsfAudio nsf;
		
		try {
			nsf = factory.createFromFile("test/assets/test/mm10nsf.nsf");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		NsfRecorder recorder = new NsfRecorder();
		recorder.ready(nsf, 8);
		
		for (int i = 0; i < 3600; i++) {
			recorder.renderFrame();
		}
	}

}
