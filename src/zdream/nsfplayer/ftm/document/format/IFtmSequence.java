package zdream.nsfplayer.ftm.document.format;

/**
 * FTM 序列
 * @author Zdream
 * @date 2018-04-26
 */
public interface IFtmSequence {
	
	/**
	 * 标识这个序列的类型. 它是控制音量变化的, 还是音调等等
	 * @return
	 */
	FtmSequenceType getType();
	
	/**
	 * 芯片类型. 注意, MMC5 的 chip 类型是 2A03
	 * @return
	 */
	FtmChipType getChip();
	
	int getIndex();

}
