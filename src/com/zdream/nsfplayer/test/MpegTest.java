package com.zdream.nsfplayer.test;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

import com.zdream.nsfplayer.mpeg.MpegAudio;
import com.zdream.nsfplayer.mpeg.MpegDecoder;
import com.zdream.nsfplayer.mpeg.MpegFactory;

public class MpegTest {
	
	public MpegTest() {
		factory = new MpegFactory();
	}
	
	MpegFactory factory;
	
	public void test1() throws Exception {
		String path = "D:\\Program\\Netease\\CloudMusic\\downloads\\GMRemix - FC 特救指令B.mp3";
		MpegAudio audio = factory.createFromFile(path);
		
		// javax
		AudioFormat af = new AudioFormat(48000, 16, 2, true, false);
		SourceDataLine dateline = AudioSystem.getSourceDataLine(af);
		dateline.open(af, 8192);
		// javax end
		
		MpegDecoder decoder = new MpegDecoder();
		decoder.ready(audio);
		
		dateline.start();
		
		while (!decoder.isEnd()) {
			byte[] bs = decoder.decode();
			
			if (bs != null) {
				dateline.write(bs, 0, bs.length);
			} else {
				System.out.println("null");
			}
		}
		
		dateline.drain();
		dateline.close();
	}

	public static void main(String[] args) throws Exception {
		new MpegTest().test1();
	}

}
