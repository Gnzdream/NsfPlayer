package zdream.nsfplayer.ftm.format;

public enum FtmSequenceType {
	
	VOLUME,
	
	ARPEGGIO,
	
	PITCH,
	
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
