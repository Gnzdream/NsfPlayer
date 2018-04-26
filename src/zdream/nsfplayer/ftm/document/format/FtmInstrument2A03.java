package zdream.nsfplayer.ftm.document.format;

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
	
	public FtmSequence2A03 vol;
	public FtmSequence2A03 arp;
	public FtmSequence2A03 pit;
	public FtmSequence2A03 hip;
	public FtmSequence2A03 dut;
	
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
		StringBuilder builder = new StringBuilder(80);
		
		builder.append(name).append(':').append(' ').append('#').append(seq).append('\n');
		if (vol != null) {
			builder.append("vol").append(':').append(' ').append(vol).append('\n');
		}
		if (arp != null) {
			builder.append("arp").append(':').append(' ').append(arp).append('\n');
		}
		if (pit != null) {
			builder.append("pit").append(':').append(' ').append(pit).append('\n');
		}
		if (hip != null) {
			builder.append("hip").append(':').append(' ').append(hip).append('\n');
		}
		if (dut != null) {
			builder.append("dut").append(':').append(' ').append(dut).append('\n');
		}
		
		return builder.toString();
	}

}
