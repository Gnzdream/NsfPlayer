package com.zdream.famitracker;

import java.nio.charset.Charset;

import com.zdream.famitracker.components.Settings;
import com.zdream.famitracker.sound.SoundGen;

public class FamiTrackerApp {
	
	private static FamiTrackerApp app;
	
	public static Charset defCharset;
	
	static {
		defCharset = Charset.forName("UTF-8");
		app = new FamiTrackerApp();
	}
	
	public static FamiTrackerApp getInstance() {
		return app;
	}
	
	public FamiTrackerApp() {
		doc = new FamiTrackerDoc();
		m_pSoundGenerator = new SoundGen();
		m_pSoundGenerator.assignDocument(doc);
		
		m_pSettings = new Settings();
		// TODO
	}
	
	// TODO
	
	/**
	 * Sound synth & player
	 */
	SoundGen m_pSoundGenerator;
	
	public final SoundGen getSoundGenerator() { 
		return m_pSoundGenerator;
	}
	
	// TODO
	
	/**
	 * 下面是我的意思
	 */
	FamiTrackerDoc doc;
	Settings m_pSettings;
	
	public static FamiTrackerDoc getDoc() {
		return getInstance().doc;
	}

	public Settings getSettings() {
		return m_pSettings;
	}

	public boolean isPlaying() {
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 * 这是我所定义的方法, 用来加载 FamiTrackerDoc 的文件.
	 * @param filename
	 */
	public boolean open(String filename) {
		return doc.onOpenDocument(filename);
	}
	
	/**
	 * <p>这是我所定义的方法, 用来播放. 调用的前提是你已经加载完 FamiTrackerDoc.
	 * <p>它毫无疑问是个阻塞方法.
	 */
	public void play(int track) {
		m_pSoundGenerator.ready(doc, track, 0);
		
		while (m_pSoundGenerator.isPlaying()) {
			m_pSoundGenerator.checkFinish();
			m_pSoundGenerator.playFrame();
		}
	}

}
