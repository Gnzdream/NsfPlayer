package com.zdream.nsfplayer.test;

import com.zdream.nsfplayer.mpeg.MpegAudio;
import com.zdream.nsfplayer.mpeg.MpegFactory;

public class MpegTest {
	
	public MpegTest() {
		factory = new MpegFactory();
	}
	
	MpegFactory factory;
	
	public void test1() throws Exception {
		String path = "E:\\Home\\Java\\RockField\\MyTest\\src\\mpeg\\assets\\test\\ZXA_Destiny.mp3";
		MpegAudio audio = factory.createFromFile(path);
		

	}

	public static void main(String[] args) throws Exception {
		new MpegTest().test1();
	}

}
