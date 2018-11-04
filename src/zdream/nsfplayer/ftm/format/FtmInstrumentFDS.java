package zdream.nsfplayer.ftm.format;

import zdream.nsfplayer.core.FtmChipType;

/**
 * FDS 芯片乐器
 * 
 * @author Zdream
 * @since v0.2.4
 */
public class FtmInstrumentFDS extends AbstractFtmInstrument {
	
	public static final int SAMPLE_LENGTH = 64;
	public static final int MODULATION_LENGTH = 32;
	
	/**
	 * 一个周期内音量包络的变动情况数据
	 */
	public final byte[] samples = new byte[SAMPLE_LENGTH];
	
	/**
	 * 调制参数
	 */
	public final byte[] modulation = new byte[MODULATION_LENGTH];
	public int modulationSpeed;
	public int modulationDepth;
	public int modulationDelay;

	/**
	 * 音量序列
	 */
	public FtmSequence seqVolume;
	/**
	 * 琶音序列
	 */
	public FtmSequence seqArpeggio;
	/**
	 * 音高序列
	 */
	public FtmSequence seqPitch;

	@Override
	public FtmChipType instType() {
		return FtmChipType.FDS;
	}
	
	@Override
	public String toString() {
		return String.format("FDS Instrument #%d %s", seq, name);
	}

}
