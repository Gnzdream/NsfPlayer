package zdream.utils.common;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class BytesPlayer {
	
	// 补充
	/**
	 * 这个主要关注 dateline.start() 是否已经被调用
	 */
	boolean started = false;
	
	// javax
	private SourceDataLine dateline;

	public BytesPlayer() {
		AudioFormat af = new AudioFormat(48000, 16, 1, true, false); // 单声道
		try {
			dateline = AudioSystem.getSourceDataLine(af);
			dateline.open(af, 48000);
		} catch (LineUnavailableException e) {
			System.err.println("初始化音频输出失败。");
		}
	}
	
	public int writeSamples(byte[] bs, int off, int len) {
		if (!started) {
			dateline.start();
			started = true;
		}
		return dateline.write(bs, off, len);
	}

}
