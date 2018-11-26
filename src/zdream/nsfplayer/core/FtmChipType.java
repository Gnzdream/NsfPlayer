package zdream.nsfplayer.core;

import static zdream.nsfplayer.core.INsfChannelCode.*;

/**
 * FamiTracker 与 NsfPlayer 使用的芯片枚举;
 * 
 * @version v0.2.5
 *   补充 S5B 芯片以及相关方法的实现
 * 
 * @author Zdream
 * @since v0.2.3
 */
public enum FtmChipType {
	
	_2A03(CHIP_2A03),
	
	_2A07(CHIP_2A07),
	
	VRC6(CHIP_VRC6),
	
	VRC7(CHIP_VRC7),
	
	FDS(CHIP_FDS),
	
	MMC5(CHIP_MMC5),
	
	N163(CHIP_N163),
	
	S5B(CHIP_S5B)
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
		case CHIP_2A07:
			return _2A07;
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
		case CHIP_S5B:
			return S5B;
		}
		return null;
	}

}
