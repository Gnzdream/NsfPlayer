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
		return String.format("VRC6 Instrument #%d %s", seq, name);
	}

}
