package zdream.nsfplayer.ftm.format;

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
	
	public FtmSequence vol;
	public FtmSequence arp;
	public FtmSequence pit;
	public FtmSequence hip;
	public FtmSequence dut;
	
	@Override
	public String toString() {
		return String.format("VRC6 Instrument #%d %s", seq, name);
	}

}
