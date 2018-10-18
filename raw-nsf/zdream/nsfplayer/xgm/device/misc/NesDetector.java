package zdream.nsfplayer.xgm.device.misc;

public class NesDetector extends BasicDetector {

	public NesDetector() {
		super(16);
	}
	
	@Override
	public boolean write(int adr, int val, int id) {
		switch (adr) {
		case 0x4000: case 0x4001: case 0x4002: case 0x4003:
		case 0x4004: case 0x4005: case 0x4006: case 0x4007:
			recordAPU(adr, val, id);
			break;
		case 0x4008: case 0x4009: case 0x400A: case 0x400B:
		case 0x400C: case 0x400D: case 0x400E: case 0x400F:
		case 0x4010: case 0x4011: case 0x4012: case 0x4013:
		case 0x4017:
			recordDMC(adr, val, id);
			break;
		case 0x4015: // 这个参数特别. 这个地方的数据控制着 APU 和 DMC 一共五个通道的开闭
			recordAPU(adr, val, id);
			recordDMC(adr, val, id);
			break;
			
		case 0x9000: case 0x9001: case 0x9002:
		case 0xa000: case 0xa001: case 0xa002:
		case 0xb000: case 0xb001: case 0xb002:
			recordVRC6(adr, val, id);
			break;
			
		case 0x9003: // 原来的判断中, 也不知道为什么, 没有 0x9003
			recordVRC6(adr, val, id);
			return false;
			
		case 0x9010: case 0x9030:
			recordVRC7(adr, val, id);
			break;
			
		case 0x4800: case 0xF800: // 或者 N163 ?
			recordN106(adr, val, id);
			break;
			
		case 0x5000: case 0x5001: case 0x5002: case 0x5003:
		case 0x5004: case 0x5005: case 0x5006: case 0x5007:
		case 0x5010: case 0x5011:
			recordMMC5(adr, val, id);
			break;
			
		case 0xC000:
			recordFME7(adr, val, id);
			break;
			
		case 0xE000: // 两个设备又访问同一地址了
			recordN106(adr, val, id);
			recordFME7(adr, val, id);
			break;

		default:
			if (0x4040 <= adr && adr <= 0x4092) {
				recordFDS(adr, val, id);
			} else {
				return false;
			}
		}
		
		return super.write(adr, val, id);
	}

	private void recordAPU(int adr, int val, int id) {
		// TODO recordAPU
		
	}
	
	private void recordDMC(int adr, int val, int id) {
		// TODO recordDMC
		
	}
	
	private void recordVRC6(int adr, int val, int id) {
		// TODO recordVRC6
		
	}
	
	private void recordVRC7(int adr, int val, int id) {
		// TODO recordVRC7
		
	}
	
	private void recordFDS(int adr, int val, int id) {
		// TODO recordFDS
		
	}

	private void recordN106(int adr, int val, int id) {
		// TODO recordN106
		
	}

	private void recordMMC5(int adr, int val, int id) {
		// TODO recordMMC5
		
	}

	private void recordFME7(int adr, int val, int id) {
		// TODO recordFME7
		
	}

}
