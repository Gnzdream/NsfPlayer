package zdream.nsfplayer.ftm.format;

/**
 * <p>序列. 每个乐器中音量、音调、音色的变化用数值来定量, 那么这个就是序列.
 * <p>所有的芯片类型的序列都是 FtmSequence.
 * 
 * @author Zdream
 * @since v0.2.0
 * @date 2018-04-25
 */
public class FtmSequence {
	
	/**
	 * <p>2A03 和 VRC6 一共有 5 类 FtmSequence
	 * </p>
	 */
	public static final int SEQUENCE_COUNT = 5;
	
	/**
	 * <p>FDS 只有音量 (VOLUME)、琶音 (ARPEGGIO)、音高 (PITCH) 3 类
	 * </p>
	 */
	public static final int SEQUENCE_COUNT_FDS = 3;
	
	/**
	 * <p>琶音设置项. {@link FtmSequenceType#ARPEGGIO} 序列的 setting 有以下三个可选值:
	 * 
	 * <li><b>ARP_SETTING_ABSOLUTE  绝对方式 (默认)</b>
	 * <br>如果设置了该值, 最后产生的音键, 为当前其它状态计算得到的音键, 加上琶音的值.
	 * <br>例如, 如果其它状态总和得到音键为 C-4, 琶音值为 3, 则最后得到的音键比 C-4 高 3 个半音, 为 D#4.
	 * </li>
	 * <p><li><b>ARP_SETTING_RELATIVE  相对方式</b>
	 * <br>如果设置了该值, 第一帧产生的值的方法与绝对方式一样,
	 * <br>但是到第二帧, 音键加上的值等于前两帧值的总和.
	 * <br>例如, 如果其它状态总和得到音键为 C-4, 第一帧琶音值为 3, 则第一帧得到的音键比 C-4 高 3 个半音, 为 D#4.
	 * 第二帧琶音值为 2, 则前二帧琶音值总和为 5, 得到的音键比 C-4 高 5 个半音, 为 F-4.
	 * </li>
	 * <p><li><b>ARP_SETTING_FIXED  修正方式</b>
	 * <br>如果设置了该值, 则系统会忽略其它状态计算得到的音键, 转而使用琶音值作为音键.
	 * </li></p>
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
		
		if (data != null) {
			b.append("Sequence").append('[');
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
		} else {
			b.append("Empty Sequence");
		}
		
		return b.toString();
	}

}
