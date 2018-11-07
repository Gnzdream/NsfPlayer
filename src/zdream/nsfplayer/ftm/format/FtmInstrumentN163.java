package zdream.nsfplayer.ftm.format;

import zdream.nsfplayer.core.FtmChipType;

/**
 * N163 芯片乐器
 * 
 * @author Zdream
 * @since v0.2.6
 */
public class FtmInstrumentN163 extends AbstractFtmInstrument {
	
	public static final int SEQUENCE_COUNT = 5;
	public static final int MAX_WAVE_SIZE = 32 /*128*/;		// Wave size (32 samples)
	public static final int MAX_WAVE_COUNT = 16;		// Number of waves

	// 序列选择
	public int vol = -1;
	public int arp = -1;
	public int pit = -1;
	public int hip = -1;
	
	/**
	 * 这个是选择 wave 的
	 */
	public int dut = -1;
	
	/**
	 * 原: samples, [序号][位置号].
	 * 生成时是, new byte[inst.waveCount][inst.waveSize].
	 */
	public byte[][] waves;
	
	public int getWaveSize() {
		return waves[0].length;
	}
	
	public int getWaveCount() {
		return waves.length;
	}
	
	public int wavePos;
	
	@Override
	public FtmChipType instType() {
		return FtmChipType.N163;
	}
	
	@Override
	public String toString() {
		return String.format("N163 Instrument #%d %s", seq, name);
	}

}
