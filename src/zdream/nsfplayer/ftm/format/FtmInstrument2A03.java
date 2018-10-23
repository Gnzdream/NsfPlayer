package zdream.nsfplayer.ftm.format;

import zdream.nsfplayer.ftm.renderer.FamiTrackerConfig;

/**
 * 2A03 乐器部分
 * @author Zdream
 */
public final class FtmInstrument2A03 extends AbstractFtmInstrument {

	@Override
	public FtmChipType instType() {
		return FtmChipType._2A03;
	}
	
	public int vol = -1;
	public int arp = -1;
	public int pit = -1;
	public int hip = -1;
	public int dut = -1;
	
	// 采样相关的数据
	public final FtmDPCMSample[][] samples = new FtmDPCMSample[FamiTrackerConfig.OCTAVE_RANGE][12];
	public final byte[][] samplePitches = new byte[FamiTrackerConfig.OCTAVE_RANGE][12];
	public final byte[][] sampleDeltas = new byte[FamiTrackerConfig.OCTAVE_RANGE][12];
	
	/**
	 * 设置采样数据
	 */
	public void setSample(int octave, int pitchOfOctave, FtmDPCMSample sample, byte pitch, byte delta) {
		samples[octave][pitchOfOctave] = sample;
		samplePitches[octave][pitchOfOctave] = pitch;
		sampleDeltas[octave][pitchOfOctave] = delta;
	}
	
	/**
	 * 设置空采样
	 */
	public void setEmptySample(int octave, int pitchOfOctave) {
		setSample(octave, pitchOfOctave, null, (byte) 0, (byte) -1);
	}
	
	/**
	 * 获取 DMA 采样实例
	 * @param octave
	 * @param pitchOfOctave
	 * @return
	 */
	public FtmDPCMSample getSample(int octave, int pitchOfOctave) {
		return samples[octave][pitchOfOctave];
	}
	
	/**
	 * 获取 DMA 采样实例
	 * @param pitch
	 *   范围 [1, 96]
	 * @return
	 */
	public FtmDPCMSample getSample(int pitch) {
		int i = pitch - 1;
		int octave = i / 12;
		int pitchOfOctave = i % 12;
		return getSample(octave, pitchOfOctave);
	}
	
	@Override
	public String toString() {
		return String.format("2A03 Instrument #%d %s", seq, name);
	}

	public int getSamplePitch(int pitch) {
		int i = pitch - 1;
		int octave = i / 12;
		int pitchOfOctave = i % 12;
		return samplePitches[octave][pitchOfOctave];
	}

	public int getSampleDelta(int pitch) {
		int i = pitch - 1;
		int octave = i / 12;
		int pitchOfOctave = i % 12;
		return sampleDeltas[octave][pitchOfOctave];
	}

}
