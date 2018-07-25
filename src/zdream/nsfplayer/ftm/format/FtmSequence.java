package zdream.nsfplayer.ftm.format;

/**
 * <p>序列. 每个乐器中音量、音调、音色的变化用数值来定量, 那么这个就是序列.
 * <p>所有的芯片类型的序列都是 FtmSequence.
 * @author Zdream
 * @date 2018-04-25
 */
public class FtmSequence {
	
	/**
	 * 2A03 和 VRC6 一共有 5 类 FtmSequence
	 */
	public static final int SEQUENCE_COUNT = 5;
	
	/**
	 * 设置项. {@link FtmSequenceType#ARPEGGIO} 序列的 setting 有以下三个可选值
	 */
	public static final byte
			ARP_SETTING_ABSOLUTE = 0,
			ARP_SETTING_FIXED = 1,
			ARP_SETTING_RELATIVE = 2;
	
	/**
	 * 类型
	 */
	public final FtmSequenceType type;
	
	/**
	 * 循环的点位.
	 * <br>默认 -1, 就是不循环
	 */
	public int loopPoint;
	
	/**
	 * 释放的点位.
	 * <br>默认 -1, 就是没有释放效果
	 */
	public int releasePoint;
	
	/**
	 * 其它选项数据.
	 * 只有 ARPEGGIO 会用到, 指示这个序列影响音高的方式.
	 */
	public byte settings;
	
	/**
	 * 数据
	 */
	public byte[] data;

	public FtmSequence(FtmSequenceType type) {
		this.type = type;
	}

	public	void clear() {
		loopPoint = -1;
		releasePoint = -1;
		settings = 0;
		data = null;
	}
	
	public int length() {
		return (data == null) ? 0 : data.length;
	}
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder(60);
		
		b.append("Sequence[");
		final int length = data.length - 1;
		for (int i = 0; i < length; i++) {
			if (loopPoint == i) {
				b.append("| ");
			}
			if (releasePoint == i) {
				b.append("\\ ");
			}
			b.append(data[i]).append(' ');
		}
		b.append(data[length]).append(']');
		
		return b.toString();
	}

}
