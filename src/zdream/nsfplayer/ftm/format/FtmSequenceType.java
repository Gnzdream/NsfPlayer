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
	 * Hi-pitch 变化 1 等于 pitch 变化 16.
	 * <br>FDS 不存在该序列类型
	 */
	HI_PITCH,
	
	/**
	 * Duty / Noise;
	 * <br>VRC6 里面是 Pulse Width
	 * <br>FDS 不存在该序列类型
	 */
	DUTY
	;
	
	public static FtmSequenceType get(int index) {
		return values()[index];
	}

}
