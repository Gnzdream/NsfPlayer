package zdream.nsfplayer.ftm.format;

/**
 * 序列的类型
 * @author Zdream
 * @since 0.2.0
 */
public enum FtmSequenceType {
	
	VOLUME,
	
	ARPEGGIO,
	
	PITCH,
	
	/**
	 * Hi-pitch 变化 1 等于 pitch 变化 16
	 */
	HI_PITCH,
	
	/**
	 * Duty / Noise;
	 * <br>VRC6 里面是 Pulse Width
	 * <br>N163 里面是 Wave
	 */
	DUTY
	;
	
	public static FtmSequenceType get(int index) {
		return values()[index];
	}

}
