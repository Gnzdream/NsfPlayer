package zdream.nsfplayer.ftm.document.format;

public enum FtmSequenceType {
	
	VOLUME,
	
	ARPEGGIO,
	
	PITCH,
	
	HI_PITCH,
	
	/**
	 * Duty / Noise;
	 * <br>N163 里面是 Wave
	 */
	DUTY
	;
	
	public static FtmSequenceType get(int index) {
		return values()[index];
	}

}
