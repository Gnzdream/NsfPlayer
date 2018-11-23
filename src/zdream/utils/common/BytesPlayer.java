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
	
	byte[] bytes;
	
	/**
	 * 写入 short 采样数组
	 * @param bs
	 * @param off
	 * @param len
	 * @return
	 *   实际读取的采样数
	 * @since v0.2.9
	 */
	public int writeSamples(short[] bs, final int off, final int len) {
		if (bytes == null) {
			bytes = new byte[len * 2];
		} else {
			if (len > bytes.length * 2) {
				bytes = new byte[len * 2];
			}
		}
		
		convert(bs, off, len);
		return writeSamples(bytes, 0, len * 2) / 2;
	}
	
	private void convert(short[] src, int soff, int slen) {
		int sptr = soff;
		int dptr = 0;
		for (int i = 0; i < slen; i++) {
			short sample = src[sptr++];
			bytes[dptr++] = (byte) sample; // 低位
			bytes[dptr++] = (byte) ((sample & 0xFF00) >> 8); // 高位
		}
	}

}
