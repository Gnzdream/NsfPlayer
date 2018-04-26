package zdream.nsfplayer.ftm.document.format;

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
