package zdream.nsfplayer.ftm.document.format;

public enum FtmChipType {
	
	_2A03,
	
	VRC6,
	
	VRC7,
	
	FDS,
	
	MMC5,
	
	N163
	;
	
	public static FtmChipType get(int index) {
		return values()[index];
	}
	
	public static FtmChipType ofInstrumentType(int type) {
		switch (type) {
		case 1: return _2A03;
		case 2: return VRC6;
		case 3: return VRC7;
		case 4: return FDS;
		case 5: return N163;

		default: return null;
		}
	}

}
