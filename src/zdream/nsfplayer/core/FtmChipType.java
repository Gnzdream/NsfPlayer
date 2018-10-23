package zdream.nsfplayer.core;

import static zdream.nsfplayer.core.INsfChannelCode.*;

public enum FtmChipType {
	
	_2A03(CHIP_2A03),
	
	VRC6(CHIP_VRC6),
	
	VRC7(CHIP_VRC7),
	
	FDS(CHIP_FDS),
	
	MMC5(CHIP_MMC5),
	
	N163(CHIP_N163)
	;
	
	public final byte chipCode;
	private FtmChipType(byte chipCode) {
		this.chipCode = chipCode;
	}
	
	public static FtmChipType get(int index) {
		return values()[index];
	}
	
	/**
	 * code 和枚举间进行转换
	 * @param code
	 *   见 {@link INsfChannelCode} 的常量
	 * @return
	 * @since v0.2.4
	 */
	public static FtmChipType ofChipCode(byte code) {
		switch (code) {
		case CHIP_2A03:
			return _2A03;
		case CHIP_VRC6:
			return VRC6;
		case CHIP_VRC7:
			return VRC7;
		case CHIP_FDS:
			return FDS;
		case CHIP_MMC5:
			return MMC5;
		case CHIP_N163:
			return N163;
		}
		return null;
	}

}
