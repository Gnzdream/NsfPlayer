package zdream.nsfplayer.ftm.format;

import zdream.nsfplayer.ftm.FamiTrackerSetting;

/**
 * 2A03 乐器部分
 * @author Zdream
 */
public final class FtmInstrument2A03 extends AbstractFtmInstrument {

	@Override
	public FtmChipType instType() {
		return FtmChipType._2A03;
	}
	
	public FtmSequence vol;
	public FtmSequence arp;
	public FtmSequence pit;
	public FtmSequence hip;
	public FtmSequence dut;
	
	// 采样相关的数据
	public final FtmDPCMSample[][] samples = new FtmDPCMSample[FamiTrackerSetting.OCTAVE_RANGE][12];
	public final byte[][] samplePitches = new byte[FamiTrackerSetting.OCTAVE_RANGE][12];
	public final byte[][] sampleDeltas = new byte[FamiTrackerSetting.OCTAVE_RANGE][12];
	
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
	
	@Override
	public String toString() {
		return String.format("2A03 Instrument #%d %s", seq, name);
	}

}
