package zdream.nsfplayer.ftm.document.format;

/**
 * VRC6 类型的序列
 * @author Zdream
 * @date 2018-04-26
 */
public class FtmSequenceVRC6 implements IFtmSequence {
	
	/**
	 * 类型
	 */
	public FtmSequenceType type;
	
	/**
	 * 序号
	 */
	public int index;

	@Override
	public FtmSequenceType getType() {
		return type;
	}

	@Override
	public FtmChipType getChip() {
		return FtmChipType.VRC6;
	}

	@Override
	public int getIndex() {
		return index;
	}
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder(24);
		b.append(FtmChipType.VRC6.name()).append(' ').append("Sequence").append(' ').append('#').append(index);
		return b.toString();
	}

}
