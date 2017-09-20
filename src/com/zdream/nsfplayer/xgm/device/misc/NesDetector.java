package com.zdream.nsfplayer.xgm.device.misc;

public class NesDetector extends BasicDetector {

	public NesDetector() {
		super(16);
	}
	
	@Override
	public boolean write(int adr, int val, int id) {
		if ((0x4000 <= adr && adr <= 0x4013) // APU / DMC
				|| (0x4015 == adr)
				|| (0x4017 == adr)
				|| (0x9000 <= adr && adr <= 0x9002) // VRC6
				|| (0xA000 <= adr && adr <= 0xA002)
				|| (0xB000 <= adr && adr <= 0xB002)
				|| (0x9010 == adr) // VRC7
				|| (0x9030 == adr)
				|| (0x4040 <= adr && adr <= 0x4092) // FDS
				|| (0x4800 == adr) // N163
				|| (0xF800 == adr)
				|| (0x5000 <= adr && adr <= 0x5007) // MMC5
				|| (0x5010 == adr)
				|| (0x5011 == adr)
				|| (0xC000 == adr) // 5B
				|| (0xE000 == adr)) {
			return super.write(adr, val, id);
		}

		return false;
	}

}
