package zdream.nsfplayer.ftm.document.format;

/**
 * VRC6 乐器部分
 * @author Zdream
 * @date 2018-04-26
 */
public class FtmInstrumentVRC6 extends AbstractFtmInstrument {

	@Override
	public FtmChipType instType() {
		return FtmChipType.VRC6;
	}
	
	public FtmSequenceVRC6 vol;
	public FtmSequenceVRC6 arp;
	public FtmSequenceVRC6 pit;
	public FtmSequenceVRC6 hip;
	public FtmSequenceVRC6 dut;
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(80);
		
		builder.append(name).append("(VRC6)").append(':').append(' ').append('#').append(seq).append('\n');
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
