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
	
	public int vol = -1;
	public int arp = -1;
	public int pit = -1;
	public int hip = -1;
	public int dut = -1;
	
	@Override
	public String toString() {
		return String.format("VRC6 Instrument #%d %s", seq, name);
	}

}
