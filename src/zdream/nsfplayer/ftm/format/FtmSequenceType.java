package zdream.nsfplayer.ftm.format;

/**
 * 序列的类型
 * @author Zdream
 * @since v0.2.0
 */
public enum FtmSequenceType {
	
	/**
	 * 音量
	 */
	VOLUME,
	
	/**
	 * 琶音
	 */
	ARPEGGIO,
	
	/**
	 * 音高变化
	 */
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
	 * <br>N163 里面是 Wave (波形包络) 号
	 */
	DUTY
	;
	
	public static FtmSequenceType get(int index) {
		return values()[index];
	}

}
