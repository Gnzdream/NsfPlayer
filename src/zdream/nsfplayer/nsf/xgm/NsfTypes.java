package zdream.nsfplayer.nsf.xgm;

public class NsfTypes {
	
	private NsfTypes() {
		// can not create instance
	}
	
	/**
	 * <p>各个芯片的枚举
	 */
	public static final byte
		CHIP_UNKNOWED = 0,
		CHIP_INTERNAL = 1,
		CHIP_VRC6 = 2;

	public static final byte
		REGION_NTSC = 0,
		REGION_PAL = 1;
}