package zdream.nsfplayer.nsf.device.cpu;

public final class K6502Context {
	/**
	 * 寄存器 Accumulator
	 */
	public int a;
	/**
	 * 寄存器 Status register
	 */
	public int p;
	/**
	 * 寄存器 X register
	 */
	public int x;
	/**
	 * 寄存器 Y register
	 */
	public int y;
	/**
	 * 堆栈指针 Stack pointer
	 */
	public int s;
	/**
	 * 指令计数器 Program Counter
	 */
	public int pc;
	/**
	 * 中断请求 interrupt request
	 */
	public int iRequest;
	/**
	 * 中断位 interrupt mask
	 */
	public int iMask;
	/**
	 * 时钟周期计数器 (incremental) cycle counter
	 */
	public int clock;
	public int lastcode;
	
	public int illegal;
	
	@Override
	public String toString() {
		return "K6502Context [a=" + a + ", p=" + p + ", x=" + x + ", y=" + y + ", s=" + s + ", pc=" + pc + ", iRequest="
				+ iRequest + ", iMask=" + (iMask & 0x7FFFFFFF) + ", clock=" + clock + ", lastcode=" + lastcode
				+ ", illegal=" + (illegal == 1) + "]";
	}

	public interface ReadHandler {
		public int handler(int adr);
	}
	public interface WriteHandler {
		public void handler(int adr, int value);
	}
	
	public ReadHandler readByte;
	public WriteHandler writeByte;
	
	/**
	 * enum K65C02_FLAGS
	 */
	public static final int
			T_FLAG = 0,
			C_FLAG = 0x01,
			Z_FLAG = 0x02,
			I_FLAG = 0x04,
			D_FLAG = 0x08,
			B_FLAG = 0x10,
			R_FLAG = 0x20,
			V_FLAG = 0x40,
			N_FLAG = 0x80;
	
	/**
	 * enum K65C02_IRQ
	 */
	public static final int
			K6502_INIT = 1,
			K6502_RESET = 2,
			K6502_NMI = 4,
			K6502_BRK = 8,
			K6502_INT = 16;
	
	public static final int VEC_RESET = 0xFFFC,
			VEC_NMI = 0xFFFA,
			VEC_INT = 0xFFFE;
	
	public void exec() {
		if (iRequest != 0) {
			if ((iRequest & K6502_INIT) != 0) {
				a = 0;
				x = 0;
				y = 0;
				s = 0xFF;
				p = Z_FLAG | R_FLAG | I_FLAG;
				iRequest = 0;
				iMask = ~0;
				KI_ADDCLOCK(7);
				return;
			} else if ((iRequest & K6502_RESET) != 0) {
				a = 0;
				x = 0;
				y = 0;
				s = 0xFF;
				p = Z_FLAG | R_FLAG | I_FLAG;
				pc = KI_READWORD(VEC_RESET);
				iRequest = 0;
				iMask = ~0;
			} else if ((iRequest & K6502_NMI) != 0) {
				KM_PUSH(0xFF & (pc >> 8));
				KM_PUSH(0xFF & pc);
				KM_PUSH((int)(p | R_FLAG | B_FLAG));
				p = (p & ~T_FLAG) | I_FLAG;	/* 6502 bug */
				iRequest &= ~(K6502_NMI | K6502_BRK);
				pc = KI_READWORD(VEC_NMI);
				KI_ADDCLOCK(7);
			} else if ((iRequest & K6502_BRK) != 0) {
				KM_PUSH(0xFF & (pc >> 8));
				KM_PUSH(0xFF & pc);
				KM_PUSH((int)(p | R_FLAG | B_FLAG));
				p = (p & ~T_FLAG) | I_FLAG;	/* 6502 bug */
				iRequest &= ~K6502_BRK;
				pc = KI_READWORD(VEC_INT);
				KI_ADDCLOCK(7);
			} else if ((p & I_FLAG) != 0) {
				/* interrupt disabled */
			} else if ((iMask & iRequest & K6502_INT) != 0) {
				KM_PUSH(0xFF & (pc >> 8));
				KM_PUSH(0xFF & pc);
				KM_PUSH((int)((p | R_FLAG) & ~B_FLAG));
				p = (p & ~T_FLAG) | I_FLAG;	 /* 6502 bug */
				iRequest &= ~K6502_INT;
				pc = KI_READWORD(VEC_INT);
				KI_ADDCLOCK(7);
			}
		}
		K_OPEXEC();
	}
	
	// km6502ot.h
	void K_OPEXEC() {
		int opcode = lastcode = K_READ(KAI_IMM());
		KI_ADDCLOCK(cl_table[opcode]);
		
		switch (opcode) {
		case 0x00: Opcode00(); break;
		case 0x01: Opcode01(); break;
		case 0x02: Opcode02(); illegal = 1; break;
		case 0x03: Opcode03(); illegal = 1; break;
		case 0x04: Opcode04(); illegal = 1; break;
		case 0x05: Opcode05(); break;
		case 0x06: Opcode06(); break;
		case 0x07: Opcode07(); illegal = 1; break;
		case 0x08: Opcode08(); break;
		case 0x09: Opcode09(); break;
		case 0x0A: Opcode0A(); break;
		case 0x0B: Opcode0B(); illegal = 1; break;
		case 0x0C: Opcode0C(); illegal = 1; break;
		case 0x0D: Opcode0D(); break;
		case 0x0E: Opcode0E(); break;
		case 0x0F: Opcode0F(); illegal = 1; break;
		
		case 0x10: Opcode10(); break;
		case 0x11: Opcode11(); break;
		case 0x12: Opcode12(); illegal = 1; break;
		case 0x13: Opcode13(); illegal = 1; break;
		case 0x14: Opcode14(); illegal = 1; break;
		case 0x15: Opcode15(); break;
		case 0x16: Opcode16(); break;
		case 0x17: Opcode17(); illegal = 1; break;
		case 0x18: Opcode18(); break;
		case 0x19: Opcode19(); break;
		case 0x1A: Opcode1A(); illegal = 1; break;
		case 0x1B: Opcode1B(); illegal = 1; break;
		case 0x1C: Opcode1C(); illegal = 1; break;
		case 0x1D: Opcode1D(); break;
		case 0x1E: Opcode1E(); break;
		case 0x1F: Opcode1F(); illegal = 1; break;
		
		case 0x20: Opcode20(); break;
		case 0x21: Opcode21(); break;
		case 0x22: Opcode22(); illegal = 1; break;
		case 0x23: Opcode23(); illegal = 1; break;
		case 0x24: Opcode24(); break;
		case 0x25: Opcode25(); break;
		case 0x26: Opcode26(); break;
		case 0x27: Opcode27(); illegal = 1; break;
		case 0x28: Opcode28(); break;
		case 0x29: Opcode29(); break;
		case 0x2A: Opcode2A(); break;
		case 0x2B: Opcode2B(); illegal = 1; break;
		case 0x2C: Opcode2C(); break;
		case 0x2D: Opcode2D(); break;
		case 0x2E: Opcode2E(); break;
		case 0x2F: Opcode2F(); illegal = 1; break;
		
		case 0x30: Opcode30(); break;
		case 0x31: Opcode31(); break;
		case 0x32: Opcode32(); illegal = 1; break;
		case 0x33: Opcode33(); illegal = 1; break;
		case 0x34: Opcode34(); illegal = 1; break;
		case 0x35: Opcode35(); break;
		case 0x36: Opcode36(); break;
		case 0x37: Opcode37(); illegal = 1; break;
		case 0x38: Opcode38(); break;
		case 0x39: Opcode39(); break;
		case 0x3A: Opcode3A(); illegal = 1; break;
		case 0x3B: Opcode3B(); illegal = 1; break;
		case 0x3C: Opcode3C(); illegal = 1; break;
		case 0x3D: Opcode3D(); break;
		case 0x3E: Opcode3E(); break;
		case 0x3F: Opcode3F(); illegal = 1; break;
		
		case 0x40: Opcode40(); break;
		case 0x41: Opcode41(); break;
		case 0x42: Opcode42(); illegal = 1; break;
		case 0x43: Opcode43(); illegal = 1; break;
		case 0x44: Opcode44(); illegal = 1; break;
		case 0x45: Opcode45(); break;
		case 0x46: Opcode46(); break;
		case 0x47: Opcode47(); illegal = 1; break;
		case 0x48: Opcode48(); break;
		case 0x49: Opcode49(); break;
		case 0x4A: Opcode4A(); break;
		case 0x4B: Opcode4B(); illegal = 1; break;
		case 0x4C: Opcode4C(); break;
		case 0x4D: Opcode4D(); break;
		case 0x4E: Opcode4E(); break;
		case 0x4F: Opcode4F(); illegal = 1; break;
		
		case 0x50: Opcode50(); break;
		case 0x51: Opcode51(); break;
		case 0x52: Opcode52(); illegal = 1; break;
		case 0x53: Opcode53(); illegal = 1; break;
		case 0x54: Opcode54(); illegal = 1; break;
		case 0x55: Opcode55(); break;
		case 0x56: Opcode56(); break;
		case 0x57: Opcode57(); illegal = 1; break;
		case 0x58: Opcode58(); break;
		case 0x59: Opcode59(); break;
		case 0x5A: Opcode5A(); illegal = 1; break;
		case 0x5B: Opcode5B(); illegal = 1; break;
		case 0x5C: Opcode5C(); illegal = 1; break;
		case 0x5D: Opcode5D(); break;
		case 0x5E: Opcode5E(); break;
		case 0x5F: Opcode5F(); illegal = 1; break;
		
		case 0x60: Opcode60(); break;
		case 0x61: Opcode61(); break;
		case 0x62: Opcode62(); illegal = 1; break;
		case 0x63: Opcode63(); illegal = 1; break;
		case 0x64: Opcode64(); illegal = 1; break;
		case 0x65: Opcode65(); break;
		case 0x66: Opcode66(); break;
		case 0x67: Opcode67(); illegal = 1; break;
		case 0x68: Opcode68(); break;
		case 0x69: Opcode69(); break;
		case 0x6A: Opcode6A(); break;
		case 0x6B: Opcode6B(); illegal = 1; break;
		case 0x6C: Opcode6C(); break;
		case 0x6D: Opcode6D(); break;
		case 0x6E: Opcode6E(); break;
		case 0x6F: Opcode6F(); illegal = 1; break;
		
		case 0x70: Opcode70(); break;
		case 0x71: Opcode71(); break;
		case 0x72: Opcode72(); illegal = 1; break;
		case 0x73: Opcode73(); illegal = 1; break;
		case 0x74: Opcode74(); illegal = 1; break;
		case 0x75: Opcode75(); break;
		case 0x76: Opcode76(); break;
		case 0x77: Opcode77(); illegal = 1; break;
		case 0x78: Opcode78(); break;
		case 0x79: Opcode79(); break;
		case 0x7A: Opcode7A(); illegal = 1; break;
		case 0x7B: Opcode7B(); illegal = 1; break;
		case 0x7C: Opcode7C(); illegal = 1; break;
		case 0x7D: Opcode7D(); break;
		case 0x7E: Opcode7E(); break;
		case 0x7F: Opcode7F(); illegal = 1; break;
		
		case 0x80: Opcode80(); illegal = 1; break;
		case 0x81: Opcode81(); break;
		case 0x82: Opcode82(); illegal = 1; break;
		case 0x83: Opcode83(); illegal = 1; break;
		case 0x84: Opcode84(); break;
		case 0x85: Opcode85(); break;
		case 0x86: Opcode86(); break;
		case 0x87: Opcode87(); illegal = 1; break;
		case 0x88: Opcode88(); break;
		case 0x89: Opcode89(); illegal = 1; break;
		case 0x8A: Opcode8A(); break;
		case 0x8B: Opcode8B(); illegal = 1; break;
		case 0x8C: Opcode8C(); break;
		case 0x8D: Opcode8D(); break;
		case 0x8E: Opcode8E(); break;
		case 0x8F: Opcode8F(); illegal = 1; break;
		
		case 0x90: Opcode90(); break;
		case 0x91: Opcode91(); break;
		case 0x92: Opcode92(); illegal = 1; break;
		case 0x93: Opcode93(); illegal = 1; break;
		case 0x94: Opcode94(); break;
		case 0x95: Opcode95(); break;
		case 0x96: Opcode96(); break;
		case 0x97: Opcode97(); illegal = 1; break;
		case 0x98: Opcode98(); break;
		case 0x99: Opcode99(); break;
		case 0x9A: Opcode9A(); break;
		case 0x9B: Opcode9B(); illegal = 1; break;
		case 0x9C: Opcode9C(); illegal = 1; break;
		case 0x9D: Opcode9D(); break;
		case 0x9E: Opcode9E(); illegal = 1; break;
		case 0x9F: Opcode9F(); illegal = 1; break;
		
		case 0xA0: OpcodeA0(); break;
		case 0xA1: OpcodeA1(); break;
		case 0xA2: OpcodeA2(); break;
		case 0xA3: OpcodeA3(); illegal = 1; break;
		case 0xA4: OpcodeA4(); break;
		case 0xA5: OpcodeA5(); break;
		case 0xA6: OpcodeA6(); break;
		case 0xA7: OpcodeA7(); illegal = 1; break;
		case 0xA8: OpcodeA8(); break;
		case 0xA9: OpcodeA9(); break;
		case 0xAA: OpcodeAA(); break;
		case 0xAB: OpcodeAB(); illegal = 1; break;
		case 0xAC: OpcodeAC(); break;
		case 0xAD: OpcodeAD(); break;
		case 0xAE: OpcodeAE(); break;
		case 0xAF: OpcodeAF(); illegal = 1; break;
		
		case 0xB0: OpcodeB0(); break;
		case 0xB1: OpcodeB1(); break;
		case 0xB2: OpcodeB2(); illegal = 1; break;
		case 0xB3: OpcodeB3(); illegal = 1; break;
		case 0xB4: OpcodeB4(); break;
		case 0xB5: OpcodeB5(); break;
		case 0xB6: OpcodeB6(); break;
		case 0xB7: OpcodeB7(); illegal = 1; break;
		case 0xB8: OpcodeB8(); break;
		case 0xB9: OpcodeB9(); break;
		case 0xBA: OpcodeBA(); break;
		case 0xBB: OpcodeBB(); illegal = 1; break;
		case 0xBC: OpcodeBC(); break;
		case 0xBD: OpcodeBD(); break;
		case 0xBE: OpcodeBE(); break;
		case 0xBF: OpcodeBF(); illegal = 1; break;
		
		case 0xC0: OpcodeC0(); break;
		case 0xC1: OpcodeC1(); break;
		case 0xC2: OpcodeC2(); illegal = 1; break;
		case 0xC3: OpcodeC3(); illegal = 1; break;
		case 0xC4: OpcodeC4(); break;
		case 0xC5: OpcodeC5(); break;
		case 0xC6: OpcodeC6(); break;
		case 0xC7: OpcodeC7(); illegal = 1; break;
		case 0xC8: OpcodeC8(); break;
		case 0xC9: OpcodeC9(); break;
		case 0xCA: OpcodeCA(); break;
		case 0xCB: OpcodeCB(); illegal = 1; break;
		case 0xCC: OpcodeCC(); break;
		case 0xCD: OpcodeCD(); break;
		case 0xCE: OpcodeCE(); break;
		case 0xCF: OpcodeCF(); illegal = 1; break;
		
		case 0xD0: OpcodeD0(); break;
		case 0xD1: OpcodeD1(); break;
		case 0xD2: OpcodeD2(); illegal = 1; break;
		case 0xD3: OpcodeD3(); illegal = 1; break;
		case 0xD4: OpcodeD4(); illegal = 1; break;
		case 0xD5: OpcodeD5(); break;
		case 0xD6: OpcodeD6(); break;
		case 0xD7: OpcodeD7(); illegal = 1; break;
		case 0xD8: OpcodeD8(); break;
		case 0xD9: OpcodeD9(); break;
		case 0xDA: OpcodeDA(); illegal = 1; break;
		case 0xDB: OpcodeDB(); illegal = 1; break;
		case 0xDC: OpcodeDC(); illegal = 1; break;
		case 0xDD: OpcodeDD(); break;
		case 0xDE: OpcodeDE(); break;
		case 0xDF: OpcodeDF(); illegal = 1; break;
		
		case 0xE0: OpcodeE0(); break;
		case 0xE1: OpcodeE1(); break;
		case 0xE2: OpcodeE2(); illegal = 1; break;
		case 0xE3: OpcodeE3(); illegal = 1; break;
		case 0xE4: OpcodeE4(); break;
		case 0xE5: OpcodeE5(); break;
		case 0xE6: OpcodeE6(); break;
		case 0xE7: OpcodeE7(); illegal = 1; break;
		case 0xE8: OpcodeE8(); break;
		case 0xE9: OpcodeE9(); break;
		case 0xEA: OpcodeEA(); break;
		case 0xEB: OpcodeEB(); illegal = 1; break;
		case 0xEC: OpcodeEC(); break;
		case 0xED: OpcodeED(); break;
		case 0xEE: OpcodeEE(); break;
		case 0xEF: OpcodeEF(); illegal = 1; break;
		
		case 0xF0: OpcodeF0(); break;
		case 0xF1: OpcodeF1(); break;
		case 0xF2: OpcodeF2(); illegal = 1; break;
		case 0xF3: OpcodeF3(); illegal = 1; break;
		case 0xF4: OpcodeF4(); illegal = 1; break;
		case 0xF5: OpcodeF5(); break;
		case 0xF6: OpcodeF6(); break;
		case 0xF7: OpcodeF7(); illegal = 1; break;
		case 0xF8: OpcodeF8(); break;
		case 0xF9: OpcodeF9(); break;
		case 0xFA: OpcodeFA(); illegal = 1; break;
		case 0xFB: OpcodeFB(); illegal = 1; break;
		case 0xFC: OpcodeFC(); illegal = 1; break;
		case 0xFD: OpcodeFD(); break;
		case 0xFE: OpcodeFE(); break;
		case 0xFF: OpcodeFF(); illegal = 1; break;
		
		}
	}
	
	/**
	 * 这里记录着 m6502 做每个操作所用的时钟周期
	 * m6502 clock cycle table
	 * 
	 * (n)		undefined OP-code
	 * +n		 +1 by page boundary case
	 * BRK(#$00)  +7 by interrupt
	 * 
	 * BS - corrected NOP timings for undefined opcodes
	 */
	final static byte[] cl_table = new byte[] {
	/* L 0   1   2   3   4   5   6   7   8   9   A   B   C   D   E   F	  H */
		 0 , 6 ,(2),(8),(3), 3 , 5 ,(5), 3 , 2 , 2 ,(2),(4), 4 , 6 ,(6), /* 0 */
		 2 ,+5 ,(2),(8),(4), 4 , 6 ,(6), 2 ,+4 ,(2),(7),(4),+4 , 7 ,(7), /* 1 */
		 6 , 6 ,(2),(8), 3 , 3 , 5 ,(5), 4 , 2 , 2 ,(2), 4 , 4 , 6 ,(6), /* 2 */
		 2 ,+5 ,(2),(8),(4), 4 , 6 ,(6), 2 ,+4 ,(2),(7),(4),+4 , 7 ,(7), /* 3 */
		 6 , 6 ,(2),(8),(3), 3 , 5 ,(5), 3 , 2 , 2 ,(2), 3 , 4 , 6 ,(6), /* 4 */
		 2 ,+5 ,(2),(8),(4), 4 , 6 ,(6), 2 ,+4 ,(2),(7),(4),+4 , 7 ,(7), /* 5 */
		 6 , 6 ,(2),(8),(3), 3 , 5 ,(5), 4 , 2 , 2 ,(2), 5 , 4 , 6 ,(6), /* 6 */
		 2 ,+5 ,(2),(8),(4), 4 , 6 ,(6), 2 ,+4 ,(2),(7),(4),+4 , 7 ,(7), /* 7 */
		(2), 6 ,(2),(6), 3 , 3 , 3 ,(3), 2 ,(2), 2 ,(2), 4 , 4 , 4 ,(4), /* 8 */
		 2 , 6 ,(2),(6), 4 , 4 , 4 ,(4), 2 , 5 , 2 ,(5),(5), 5 ,(5),(5), /* 9 */
		 2 , 6 , 2 ,(6), 3 , 3 , 3 ,(3), 2 , 2 , 2 ,(2), 4 , 4 , 4 ,(4), /* A */
		 2 ,+5 ,(2),(5), 4 , 4 , 4 ,(4), 2 ,+4 , 2 ,(4),+4 ,+4 ,+4 ,(4), /* B */
		 2 , 6 ,(2),(8), 3 , 3 , 5 ,(5), 2 , 2 , 2 ,(2), 4 , 4 , 6 ,(6), /* C */
		 2 ,+5 ,(2),(8),(4), 4 , 6 ,(6), 2 ,+4 ,(2),(7),(4),+4 , 7 ,(7), /* D */
		 2 , 6 ,(2),(8), 3 , 3 , 5 ,(5), 2 , 2 , 2 ,(2), 4 , 4 , 6 ,(6), /* E */
		 2 ,+5 ,(2),(8),(4), 4 , 6 ,(6), 2 ,+4 ,(2),(7),(4),+4 , 7 ,(7), /* F */
	};
	
	final static byte[] fl_table = new byte[] {
		(byte)0x02,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00, // 00
		(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
		(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00, // 01
		(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
		(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00, // 02
		(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
		(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00, // 03
		(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
		(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00, // 04
		(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
		(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00, // 05
		(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
		(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00, // 06
		(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
		(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00, // 07
		(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,

		(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80, // 08
		(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,
		(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80, // 09
		(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,
		(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80, // 0A
		(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,
		(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80, // 0B
		(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,
		(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80, // 0C
		(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,
		(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80, // 0D
		(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,
		(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80, // 0E
		(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,
		(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80, // 0F
		(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0x80,

		(byte)0x03,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01, // 10
		(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
		(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01, // 11
		(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
		(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01, // 12
		(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
		(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01, // 13
		(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
		(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01, // 14
		(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
		(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01, // 15
		(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
		(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01, // 16
		(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,
		(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01, // 17
		(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,

		(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81, // 18
		(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,
		(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81, // 19
		(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,
		(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81, // 1A
		(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,
		(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81, // 1B
		(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,
		(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81, // 1C
		(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,
		(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81, // 1D
		(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,
		(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81, // 1E
		(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,
		(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81, // 1F
		(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,(byte)0x81,
	};
	
	// km6502m.h
	/**
	 * #define K_READ K_READ
	 * @param adr
	 * @return
	 */
	int K_READ(int adr) {
		return readByte.handler(adr);
	}
	
	/**
	 * #define K_WRITENP K_WRITE
	 * @param adr
	 * @param value
	 */
	void K_WRITE(int adr, int value) {
		writeByte.handler(adr, value);
	}
	
	// km6502cd.h
	
	public static final int BASE_OF_ZERO = 0x0000;
	
	void KI_ADDCLOCK(int cycle) {
		this.clock += cycle;
	}
	
	int KI_READWORD(int adr) {
		int ret = K_READ(adr);
		return (ret + (K_READ((int)((adr + 1) & 0xFFFF)) << 8)) & 0xFFFF;
	}
	
	int KI_READWORDZP(int adr)	{
		int ret = K_READ((int)(BASE_OF_ZERO + adr));
		return (ret + (K_READ((int)(BASE_OF_ZERO + ((adr + 1) & 0xFF))) << 8)) & 0xFFFF;
	}

	int KAI_IMM() {
		int ret = pc;
		pc = (pc + 1) & 0xFFFF;
		return ret;
	}

	int KAI_IMM16() {
		int ret = pc;
		pc = (pc + 2) & 0xFFFF;
		return ret;
	}

	int KAI_ABS() {
		return KI_READWORD(KAI_IMM16());
	}
	
	int KAI_ABSX() {
		return (KAI_ABS() + x) & 0xFFFF;
	}
	
	int KAI_ABSY() {
		return (KAI_ABS() + y) & 0xFFFF;
	}
	
	int KAI_ZP() {
		return K_READ(KAI_IMM());
	}
	
	int KAI_ZPX() {
		return (KAI_ZP() + x) & 0xFF;
	}
	
	int KAI_INDY() {
		return (KI_READWORDZP(KAI_ZP()) + y) & 0xFFFF;
	}

	int KA_IMM() {
		int ret = pc;
		pc = (pc + 1) & 0xFFFF;
		return ret;
	}

	int KA_IMM16() {
		int ret = pc;
		pc = (pc + 2) & 0xFFFF;
		return ret;
	}

	int KA_ABS() {
		return KI_READWORD(KAI_IMM16());
	}

	int KA_ABSX() {
		return (KAI_ABS() + x) & 0xFFFF;
	}
	
	int KA_ABSY() {
		return (KAI_ABS() + y) & 0xFFFF;
	}

	int KA_ZP() {
		return BASE_OF_ZERO + K_READ(KAI_IMM());
	}

	int KA_ZPX() {
		return BASE_OF_ZERO + ((KAI_ZP() + x) & 0xFF);
	}

	int KA_ZPY() {
		return BASE_OF_ZERO + ((KAI_ZP() + y) & 0xFF);
	}

	int KA_INDX() {
		return KI_READWORDZP(KAI_ZPX());
	}

	int KA_INDY() {
		return (KI_READWORDZP(KAI_ZP()) + y) & 0xFFFF;
	}
	
	int KI_READWORDBUG(int adr) {
		int ret = K_READ(adr);
		return ret + (K_READ((int)((adr & 0xFF00) + ((adr + 1) & 0xFF))) << 8);
	}
	
	int KA_ABSX_() {
		if ((pc & 0xFF) == 0xFF) KI_ADDCLOCK(1);	/* page break */
		return KAI_ABSX();
	}
	
	int KA_ABSY_() {
		if ((pc & 0xFF) == 0xFF) KI_ADDCLOCK(1);	/* page break */
		return KAI_ABSY();
	}
	
	int KA_INDY_()	{
		int adr = KAI_INDY();
		if ((adr & 0xFF) == 0xFF) KI_ADDCLOCK(1);	/* page break */
		return adr;
	}
	
	void KM_ALUADDER(int src) {
		int w = a + src + (p & C_FLAG);
		p &= ~(N_FLAG | V_FLAG | Z_FLAG | C_FLAG);
		p += fl_table[w & 0x01ff] + ((((~a ^ src) & (a ^ w)) >> 1) & V_FLAG);
		p &= 0xFF;
		a = (w & 0xFF);
	}
	
	void KM_ALUADDER_D(int src)	{
		int wl = (a & 0x0F) + (src & 0x0F) + (p & C_FLAG);
		int w = a + src + (p & C_FLAG);
		p &= ~C_FLAG;
		if (wl > 0x9) w += 0x6;
		if (w > 0x9F) {
			p += C_FLAG;
			w += 0x60;
		}
		p &= 0xFF;
		a = (w & 0xFF);
		KI_ADDCLOCK(1);
	}
	
	void KMI_ADC(int src) {
		KM_ALUADDER(src);
	}

	void KMI_ADC_D(int src) {
		KM_ALUADDER_D(src);
	}

	void KMI_SBC(int src) {
		KM_ALUADDER(src ^ 0xFF);
	}

	void KMI_SBC_D(int src) {
		KM_ALUADDER_D(0xFF & ((src ^ 0xFF) + (0x100 - 0x66)));
	}
	
	void KM_CMP(int src) {
		int w = a + (src ^ 0xFF) + 1;
		p &= ~(N_FLAG | Z_FLAG | C_FLAG);
		p += fl_table[w & 0x01ff];
		p &= 0xFF;
	}

	void KM_CPX(int src) {
		int w = x + (src ^ 0xFF) + 1;
		p &= ~(N_FLAG | Z_FLAG | C_FLAG);
		p += fl_table[w & 0x01ff];
		p &= 0xFF;
	}

	void KM_CPY(int src) {
		int w = y + (src ^ 0xFF) + 1;
		p &= ~(N_FLAG | Z_FLAG | C_FLAG);
		p += fl_table[w & 0x01ff];
		p &= 0xFF;
	}

	void KM_BIT(int src) {
		int w = a & src;
		p &= ~(N_FLAG | V_FLAG | Z_FLAG);
		p += (src & (N_FLAG | V_FLAG)) + (w != 0 ? 0 : Z_FLAG);
		p &= 0xFF;
	}

	void KM_AND(int src) {
		a &= src;
		p &= ~(N_FLAG | Z_FLAG);
		p += fl_table[a & 0xFF];
		p &= 0xFF;
	}

	void KM_ORA(int src) {
		a |= src;
		p &= ~(N_FLAG | Z_FLAG);
		p += fl_table[a & 0xFF];
		p &= 0xFF;
	}

	void KM_EOR(int src) {
		a ^= src;
		p &= ~(N_FLAG | Z_FLAG);
		p += fl_table[a & 0xFF];
		p &= 0xFF;
	}

	int KM_DEC(int des) {
		int w = des - 1;
		p &= ~(N_FLAG | Z_FLAG);
		p += fl_table[w & 0xFF];
		p &= 0xFF;
		return (w & 0xFF);
	}

	int KM_INC(int des) {
		int w = des + 1;
		p &= ~(N_FLAG | Z_FLAG);
		p += fl_table[w & 0xFF];
		p &= 0xFF;
		return (w & 0xFF);
	}

	int KM_ASL(int des) {
		int w = des << 1;
		p &= ~(N_FLAG | Z_FLAG | C_FLAG);
		p += fl_table[w & 0xFF] + ((des >> 7)/* & C_FLAG */);
		p &= 0xFF;
		return (w & 0xFF);
	}

	int KM_LSR(int des) {
		int w = des >> 1;
		p &= ~(N_FLAG | Z_FLAG | C_FLAG);
		p += fl_table[w & 0xFF] + (des & C_FLAG);
		p &= 0xFF;
		return w;
	}

	int KM_LD(int src) {
		p &= ~(N_FLAG | Z_FLAG);
		p += fl_table[src & 0xFF];
		p &= 0xFF;
		return src;
	}

	int KM_ROL(int des) {
		int w = (des << 1) + (p & C_FLAG);
		p &= ~(N_FLAG | Z_FLAG | C_FLAG);
		p += fl_table[w & 0xFF] + ((des >> 7)/* & C_FLAG */);
		p &= 0xFF;
		return (w & 0xFF);
	}

	int KM_ROR(int des) {
		int w = (des >> 1) + ((p & C_FLAG) << 7);
		p &= ~(N_FLAG | Z_FLAG | C_FLAG);
		p += fl_table[w & 0xFF] + (des & C_FLAG);
		p &= 0xFF;
		return (w & 0xFF);
	}

	void KM_BRA(int rel) {
		int oldPage = pc & 0xFF00;
		pc = (pc + (rel ^ 0x80) - 0x80) & 0xFFFF;
		KI_ADDCLOCK((int) (1 + ((oldPage != (pc & 0xFF00)) ? 1 : 0)));
	}
	
	void KM_PUSH(int src) {
		K_WRITE((int) (BASE_OF_ZERO + 0x100 + s), src);
		s = (s - 1) & 0xFF;
	}

	int KM_POP() {
		s = (s + 1) & 0xFF;
		return K_READ((int) (BASE_OF_ZERO + 0x100 + s));
	}
	
	// ADC 部分
	/**
	 * 61 - ADC - (Indirect,X)
	 */
	void Opcode61() {
		KMI_ADC(K_READ(KA_INDX()));
	}

	void D_Opco61() {
		KMI_ADC_D(K_READ(KA_INDX()));
	}
	
	/**
	 * 65 - ADC - Zero Page
	 */
	void Opcode65() {
		KMI_ADC(K_READ(KA_ZP()));
	}

	void D_Opco65() {
		KMI_ADC_D(K_READ(KA_ZP()));
	}
	
	/**
	 * 69 - ADC - Immediate
	 */
	void Opcode69() {
		KMI_ADC(K_READ(KA_IMM()));
	}

	void D_Opco69() {
		KMI_ADC_D(K_READ(KA_IMM()));
	}
	
	/**
	 * 6D - ADC - Absolute
	 */
	void Opcode6D() {
		KMI_ADC(K_READ(KA_ABS()));
	}
	
	void D_Opco6D() {
		KMI_ADC_D(K_READ(KA_ABS()));
	}
	
	/**
	 * 71 - ADC - (Indirect),Y
	 */
	void Opcode71() {
		KMI_ADC(K_READ(KA_INDY_()));
	}
	
	void D_Opco71() {
		KMI_ADC_D(K_READ(KA_INDY_()));
	}
	
	/**
	 * 75 - ADC - Zero Page,X
	 */
	void Opcode75() {
		KMI_ADC(K_READ(KA_ZPX()));
	}
	
	void D_Opco75() {
		KMI_ADC_D(K_READ(KA_ZPX()));
	}
	
	/**
	 * 79 - ADC - Absolute,Y
	 */
	void Opcode79() {
		KMI_ADC(K_READ(KA_ABSY_()));
	}
	
	void D_Opco79() {
		KMI_ADC_D(K_READ(KA_ABSY_()));
	}
	
	/**
	 * 7D - ADC - Absolute,X
	 */
	void Opcode7D() {
		KMI_ADC(K_READ(KA_ABSX_()));
	}
	
	void D_Opco7D() {
		KMI_ADC_D(K_READ(KA_ABSX_()));
	}
	
	// AND 部分
	/**
	 * 21 - AND - (Indirect,X)
	 */
	void Opcode21() {
		KM_AND(K_READ(KA_INDX()));
	}

	/**
	 * 25 - AND - Zero Page
	 */
	void Opcode25() {
		KM_AND(K_READ(KA_ZP()));
	}

	/**
	 * 29 - AND - Immediate
	 */
	void Opcode29() {
		KM_AND(K_READ(KA_IMM()));
	}

	/**
	 * 2D - AND - Absolute
	 */
	void Opcode2D() {
		KM_AND(K_READ(KA_ABS()));
	}

	/**
	 * 31 - AND - (Indirect),Y
	 */
	void Opcode31() {
		KM_AND(K_READ(KA_INDY_()));
	}

	/**
	 * 35 - AND - Zero Page,X
	 */
	void Opcode35() {
		KM_AND(K_READ(KA_ZPX()));
	}

	/**
	 * 39 - AND - Absolute,Y
	 */
	void Opcode39() {
		KM_AND(K_READ(KA_ABSY_()));
	}

	/**
	 * 3D - AND - Absolute,X
	 */
	void Opcode3D() {
		KM_AND(K_READ(KA_ABSX_()));
	}

	// ASL 部分
	/**
	 * 06 - ASL - Zero Page
	 */
	void Opcode06() {
		int adr = KA_ZP();
		K_WRITE(adr, KM_ASL(K_READ(adr)));
	}

	/**
	 * 0E - ASL - Absolute
	 */
	void Opcode0E() {
		int adr = KA_ABS();
		K_WRITE(adr, KM_ASL(K_READ(adr)));
	}

	/**
	 * 16 - ASL - Zero Page,X
	 */
	void Opcode16() {
		int adr = KA_ZPX();
		K_WRITE(adr, KM_ASL(K_READ(adr)));
	}

	/**
	 * 1E - ASL - Absolute,X
	 */
	void Opcode1E() {
		int adr = KA_ABSX();
		K_WRITE(adr, KM_ASL(K_READ(adr)));
	}
	
	/**
	 * 0A - ASL - Accumulator 
	 */
	void Opcode0A() {
		a = KM_ASL(a);
	}
	
	// BIT 部分
	
	/**
	 * 24 - BIT - Zero Page
	 */
	void Opcode24() {
		KM_BIT(K_READ(KA_ZP()));
	}

	/**
	 * 2C - BIT - Absolute
	 */
	void Opcode2C() {
		KM_BIT(K_READ(KA_ABS()));
	}
	
	// Bcc 部分
	/** 10 - BPL */
	void Opcode10() { int rel = K_READ(KA_IMM()); if ((p & N_FLAG) == 0) KM_BRA(rel);}
	/** 30 - BMI */
	void Opcode30() { int rel = K_READ(KA_IMM()); if ((p & N_FLAG) != 0) KM_BRA(rel);}
	/** 50 - BVC */
	void Opcode50() { int rel = K_READ(KA_IMM()); if ((p & V_FLAG) == 0) KM_BRA(rel);}
	/** 70 - BVS */
	void Opcode70() { int rel = K_READ(KA_IMM()); if ((p & V_FLAG) != 0) KM_BRA(rel);}
	/** 90 - BCC */
	void Opcode90() { int rel = K_READ(KA_IMM()); if ((p & C_FLAG) == 0) KM_BRA(rel);}
	/** B0 - BCS */
	void OpcodeB0() { int rel = K_READ(KA_IMM()); if ((p & C_FLAG) != 0) KM_BRA(rel);}
	/** D0 - BNE */
	void OpcodeD0() { int rel = K_READ(KA_IMM()); if ((p & Z_FLAG) == 0) KM_BRA(rel);}
	/** F0 - BEQ */
	void OpcodeF0() { int rel = K_READ(KA_IMM()); if ((p & Z_FLAG) != 0) KM_BRA(rel);}
	
	// BRK 部分
	/** 00 - BRK */
	void Opcode00() {
		pc = (pc + 1) & 0xFFFF;
		iRequest |= K6502_BRK;
	}
	
	/** 18 - CLC */
	void Opcode18() { p &= ~C_FLAG; }
	/** D8 - CLD */
	void OpcodeD8() { p &= ~D_FLAG; }
	/** 58 - CLI */
	void Opcode58() { p &= ~I_FLAG; }
	/** B8 - CLV */
	void OpcodeB8() { p &= ~V_FLAG; }
	
	// CMP 部分
	/** C1 - CMP - (Indirect,X) */
	void OpcodeC1() { KM_CMP(K_READ(KA_INDX())); }
	/** C5 - CMP - Zero Page */
	void OpcodeC5() { KM_CMP(K_READ(KA_ZP())); }
	/** C9 - CMP - Immediate */
	void OpcodeC9() { KM_CMP(K_READ(KA_IMM())); }
	/** CD - CMP - Absolute */
	void OpcodeCD() { KM_CMP(K_READ(KA_ABS())); }
	/** D1 - CMP - (Indirect),Y */
	void OpcodeD1() { KM_CMP(K_READ(KA_INDY_())); }
	/** D5 - CMP - Zero Page,X */
	void OpcodeD5() { KM_CMP(K_READ(KA_ZPX())); }
	/** D9 - CMP - Absolute,Y */
	void OpcodeD9() { KM_CMP(K_READ(KA_ABSY_())); }
	/** DD - CMP - Absolute,X */
	void OpcodeDD() { KM_CMP(K_READ(KA_ABSX_())); }
	
	// CPX 部分
	/** E0 - CPX - Immediate */
	void OpcodeE0() { KM_CPX(K_READ(KA_IMM())); }
	/** E4 - CPX - Zero Page */
	void OpcodeE4() { KM_CPX(K_READ(KA_ZP())); }
	/** EC - CPX - Absolute */
	void OpcodeEC() { KM_CPX(K_READ(KA_ABS())); }
	
	// CPY 部分
	/** C0 - CPY - Immediate */
	void OpcodeC0() { KM_CPY( K_READ( KA_IMM())); }
	/** C4 - CPY - Zero Page */
	void OpcodeC4() { KM_CPY( K_READ( KA_ZP())); }
	/** CC - CPY - Absolute */
	void OpcodeCC() { KM_CPY( K_READ( KA_ABS())); }
	
	// DEC 部分
	/** C6 - DEC - Zero Page */
	void OpcodeC6() { int adr = KA_ZP(); K_WRITE(adr, KM_DEC(K_READ(adr))); }
	/** CE - DEC - Absolute */
	void OpcodeCE() { int adr = KA_ABS(); K_WRITE(adr, KM_DEC(K_READ(adr))); }
	/** D6 - DEC - Zero Page,X */
	void OpcodeD6() { int adr = KA_ZPX(); K_WRITE(adr, KM_DEC(K_READ(adr))); }
	/** DE - DEC - Absolute,X */
	void OpcodeDE() { int adr = KA_ABSX(); K_WRITE(adr, KM_DEC(K_READ(adr))); }
	/** CA - DEX */
	void OpcodeCA() { x = KM_DEC(x); }
	/** 88 - DEY */
	void Opcode88() { y = KM_DEC(y); }
	
	// EOR 部分
	/** 41 - EOR - (Indirect,X) */
	void Opcode41() { KM_EOR( K_READ( KA_INDX())); }
	/** 45 - EOR - Zero Page */
	void Opcode45() { KM_EOR( K_READ( KA_ZP())); }
	/** 49 - EOR - Immediate */
	void Opcode49() { KM_EOR( K_READ( KA_IMM())); }
	/** 4D - EOR - Absolute */
	void Opcode4D() { KM_EOR( K_READ( KA_ABS())); }
	/** 51 - EOR - (Indirect),Y */
	void Opcode51() { KM_EOR( K_READ( KA_INDY_())); }
	/** 55 - EOR - Zero Page,X */
	void Opcode55() { KM_EOR( K_READ( KA_ZPX())); }
	/** 59 - EOR - Absolute,Y */
	void Opcode59() { KM_EOR( K_READ( KA_ABSY_())); }
	/** 5D - EOR - Absolute,X */
	void Opcode5D() { KM_EOR( K_READ( KA_ABSX_())); }
	
	// INC 部分
	/** E6 - INC - Zero Page */
	void OpcodeE6() { int adr = KA_ZP(); K_WRITE(adr, KM_INC(K_READ(adr))); }
	/** EE - INC - Absolute */
	void OpcodeEE() { int adr = KA_ABS(); K_WRITE(adr, KM_INC(K_READ(adr))); }
	/** F6 - INC - Zero Page,X */
	void OpcodeF6() { int adr = KA_ZPX(); K_WRITE(adr, KM_INC(K_READ(adr))); }
	/** FE - INC - Absolute,X */
	void OpcodeFE() { int adr = KA_ABSX(); K_WRITE(adr, KM_INC(K_READ(adr))); }
	/** E8 - INX */
	void OpcodeE8() { x = KM_INC(x); }
	/** C8 - INY */
	void OpcodeC8() { y = KM_INC(y); }
	
	// JMP 部分
	/** 4C - JMP - Immediate */
	void Opcode4C() { pc = KI_READWORD(KA_IMM16()); }
	/** 6C - JMP - Absolute */
	void Opcode6C() { pc = KI_READWORDBUG(KA_ABS()); }
	
	// JSR 部分
	/** 20 - JSR */
	void Opcode20() {
		int adr = KA_IMM();
		KM_PUSH((pc >> 8) & 0xFF);	/* !!! PC = NEXT - 1; !!! */
		KM_PUSH((pc) & 0xFF);
		pc = KI_READWORD(adr);
	}
	
	// LDA 部分
	/** A1 - LDA - (Indirect,X) */
	void OpcodeA1() { a = KM_LD(K_READ( KA_INDX())); }
	/** A5 - LDA - Zero Page */
	void OpcodeA5() { a = KM_LD(K_READ( KA_ZP())); }
	/** A9 - LDA - Immediate */
	void OpcodeA9() { a = KM_LD(K_READ( KA_IMM())); }
	/** AD - LDA - Absolute */
	void OpcodeAD() { a = KM_LD(K_READ( KA_ABS())); }
	/** B1 - LDA - (Indirect),Y */
	void OpcodeB1() { a = KM_LD(K_READ( KA_INDY_())); }
	/** B5 - LDA - Zero Page,X */
	void OpcodeB5() { a = KM_LD(K_READ( KA_ZPX())); }
	/** B9 - LDA - Absolute,Y */
	void OpcodeB9() { a = KM_LD(K_READ( KA_ABSY_())); }
	/** BD - LDA - Absolute,X */
	void OpcodeBD() { a = KM_LD(K_READ( KA_ABSX_())); }
	
	// LDX 部分
	/** A2 - LDX - Immediate */
	void OpcodeA2() { x = KM_LD(K_READ(KA_IMM())); }
	/** A6 - LDX - Zero Page */
	void OpcodeA6() { x = KM_LD(K_READ(KA_ZP())); }
	/** AE - LDX - Absolute */
	void OpcodeAE() { x = KM_LD(K_READ(KA_ABS())); }
	/** B6 - LDX - Zero Page,Y */
	void OpcodeB6() { x = KM_LD(K_READ(KA_ZPY())); }
	/** BE - LDX - Absolute,Y */
	void OpcodeBE() { x = KM_LD(K_READ(KA_ABSY_())); }
	
	// LDY 部分
	/** A0 - LDY - Immediate */
	void OpcodeA0() { y = KM_LD(K_READ(KA_IMM())); }
	/** A4 - LDY - Zero Page */
	void OpcodeA4() { y = KM_LD(K_READ(KA_ZP())); }
	/** AC - LDY - Absolute */
	void OpcodeAC() { y = KM_LD(K_READ(KA_ABS())); }
	/** B4 - LDY - Zero Page,X */
	void OpcodeB4() { y = KM_LD(K_READ(KA_ZPX())); }
	/** BC - LDY - Absolute,X */
	void OpcodeBC() { y = KM_LD(K_READ(KA_ABSX_())); }
	
	// LSR 部分
	/** 46 - LSR - Zero Page */
	void Opcode46() { int adr = KA_ZP(); K_WRITE( adr, KM_LSR(K_READ(adr))); }
	/** 4E - LSR - Absolute */
	void Opcode4E() { int adr = KA_ABS(); K_WRITE( adr, KM_LSR(K_READ(adr))); }
	/** 56 - LSR - Zero Page,X */
	void Opcode56() { int adr = KA_ZPX(); K_WRITE( adr, KM_LSR(K_READ(adr))); }
	/** 5E - LSR - Absolute,X */
	void Opcode5E() { int adr = KA_ABSX(); K_WRITE( adr, KM_LSR(K_READ(adr))); }
	/** 4A - LSR - Accumulator */
	void Opcode4A() { a = KM_LSR(a); }
	
	// NOP 部分
	/** EA - NOP */
	void OpcodeEA()	{}
	
	// ORA 部分
	/** 01 - ORA - (Indirect,X) */
	void Opcode01() { KM_ORA(K_READ( KA_INDX())); }
	/** 05 - ORA - Zero Page */
	void Opcode05() { KM_ORA(K_READ( KA_ZP())); }
	/** 09 - ORA - Immediate */
	void Opcode09() { KM_ORA(K_READ( KA_IMM())); }
	/** 0D - ORA - Absolute */
	void Opcode0D() { KM_ORA(K_READ( KA_ABS())); }
	/** 11 - ORA - (Indirect),Y */
	void Opcode11() { KM_ORA(K_READ( KA_INDY_())); }
	/** 15 - ORA - Zero Page,X */
	void Opcode15() { KM_ORA(K_READ( KA_ZPX())); }
	/** 19 - ORA - Absolute,Y */
	void Opcode19() { KM_ORA(K_READ( KA_ABSY_())); }
	/** 1D - ORA - Absolute,X */
	void Opcode1D() { KM_ORA(K_READ( KA_ABSX_())); }
	
	// PHr PLr 部分
	/** 48 - PHA */
	void Opcode48() { KM_PUSH(a); }
	/** 08 - PHP */
	void Opcode08() { KM_PUSH((int)((p | B_FLAG | R_FLAG) & ~T_FLAG)); }
	/** 68 - PLA */
	void Opcode68() { a = KM_LD(KM_POP()); }
	/** 28 - PLP */
	void Opcode28() { p = KM_POP() & ~T_FLAG; }
	
	// ROL 部分
	/** 26 - ROL - Zero Page */
	void Opcode26() { int adr = KA_ZP(); K_WRITE( adr, KM_ROL(K_READ(adr))); }
	/** 2E - ROL - Absolute */
	void Opcode2E() { int adr = KA_ABS(); K_WRITE( adr, KM_ROL(K_READ(adr))); }
	/** 36 - ROL - Zero Page,X */
	void Opcode36() { int adr = KA_ZPX(); K_WRITE( adr, KM_ROL(K_READ(adr))); }
	/** 3E - ROL - Absolute,X */
	void Opcode3E() { int adr = KA_ABSX(); K_WRITE( adr, KM_ROL(K_READ(adr))); }
	/** 2A - ROL - Accumulator */
	void Opcode2A() { a = KM_ROL(a); }
	
	// ROR 部分
	/** 66 - ROR - Zero Page */
	void Opcode66() { int adr = KA_ZP(); K_WRITE( adr, KM_ROR( K_READ(adr))); }
	/** 6E - ROR - Absolute */
	void Opcode6E() { int adr = KA_ABS(); K_WRITE( adr, KM_ROR( K_READ(adr))); }
	/** 76 - ROR - Zero Page,X */
	void Opcode76() { int adr = KA_ZPX(); K_WRITE( adr, KM_ROR( K_READ(adr))); }
	/** 7E - ROR - Absolute,X */
	void Opcode7E() { int adr = KA_ABSX(); K_WRITE( adr, KM_ROR( K_READ(adr))); }
	/** 6A - ROR - Accumulator */
	void Opcode6A() { a = KM_ROR(a); }
	
	/** 40 - RTI */
	void Opcode40()
	{
		p = KM_POP();
		pc  = KM_POP();
		pc += KM_POP() << 8;
	}
	/** 60 - RTS */
	void Opcode60()
	{
		pc  = KM_POP();
		pc += KM_POP() << 8;
		pc  = (pc + 1) & 0xFFFF;
	}
	
	// SBC 部分
	/** E1 - SBC - (Indirect,X) */
	void OpcodeE1() { KMI_SBC( K_READ( KA_INDX())); }
	void D_OpcoE1() { KMI_SBC_D( K_READ( KA_INDX())); }
	/** E5 - SBC - Zero Page */
	void OpcodeE5() { KMI_SBC( K_READ( KA_ZP())); }
	void D_OpcoE5() { KMI_SBC_D( K_READ( KA_ZP())); }
	/** E9 - SBC - Immediate */
	void OpcodeE9() { KMI_SBC( K_READ( KA_IMM())); }
	void D_OpcoE9() { KMI_SBC_D( K_READ( KA_IMM())); }
	/** ED - SBC - Absolute */
	void OpcodeED() { KMI_SBC( K_READ( KA_ABS())); }
	void D_OpcoED() { KMI_SBC_D( K_READ( KA_ABS())); }
	/** F1 - SBC - (Indirect),Y */
	void OpcodeF1() { KMI_SBC( K_READ( KA_INDY_())); }
	void D_OpcoF1() { KMI_SBC_D( K_READ( KA_INDY_())); }
	/** F5 - SBC - Zero Page,X */
	void OpcodeF5() { KMI_SBC( K_READ( KA_ZPX())); }
	void D_OpcoF5() { KMI_SBC_D( K_READ( KA_ZPX())); }
	/** F9 - SBC - Absolute,Y */
	void OpcodeF9() { KMI_SBC( K_READ( KA_ABSY_())); }
	void D_OpcoF9() { KMI_SBC_D( K_READ( KA_ABSY_())); }
	/** FD - SBC - Absolute,X */
	void OpcodeFD() { KMI_SBC( K_READ( KA_ABSX_())); }
	void D_OpcoFD() { KMI_SBC_D( K_READ( KA_ABSX_())); }
	
	/** 38 - SEC */
	void Opcode38() { p |= C_FLAG; }
	/** F8 - SED */
	void OpcodeF8() { p |= D_FLAG; }
	/** 78 - SEI */
	void Opcode78() { p |= I_FLAG; }
	
	// STA 部分
	/** 81 - STA - (Indirect,X) */
	void Opcode81() { K_WRITE( KA_INDX(), a); }
	/** 85 - STA - Zero Page */
	void Opcode85() { K_WRITE( KA_ZP(), a); }
	/** 8D - STA - Absolute */
	void Opcode8D() { K_WRITE( KA_ABS(), a); }
	/** 91 - STA - (Indirect),Y */
	void Opcode91() { K_WRITE( KA_INDY(), a); }
	/** 95 - STA - Zero Page,X */
	void Opcode95() { K_WRITE( KA_ZPX(), a); }
	/** 99 - STA - Absolute,Y */
	void Opcode99() { K_WRITE( KA_ABSY(), a); }
	/** 9D - STA - Absolute,X */
	void Opcode9D() { K_WRITE( KA_ABSX(), a); }
	
	// STX 部分
	/** 86 - STX - Zero Page */
	void Opcode86() { K_WRITE( KA_ZP(), x); }
	/** 8E - STX - Absolute */
	void Opcode8E() { K_WRITE( KA_ABS(), x); }
	/** 96 - STX - Zero Page,Y */
	void Opcode96() { K_WRITE( KA_ZPY(), x); }
	
	// STY 部分
	/** 84 - STY - Zero Page */
	void Opcode84() { K_WRITE( KA_ZP(), y); }
	/** 8C - STY - Absolute */
	void Opcode8C() { K_WRITE( KA_ABS(), y); }
	/** 94 - STY - Zero Page,X */
	void Opcode94() { K_WRITE( KA_ZPX(), y); }
	
	/** AA - TAX */
	void OpcodeAA() { x = KM_LD(a); }
	/** A8 - TAY */
	void OpcodeA8() { y = KM_LD(a); }
	/** BA - TSX */
	void OpcodeBA() { x = KM_LD(s); }
	/** 8A - TXA */
	void Opcode8A() { a = KM_LD(x); }
	/** 9A - TXS */
	void Opcode9A() { s = x; }
	/** 98 - TYA */
	void Opcode98() { a = KM_LD(y); }
	
	// KIL 部分
	/** 02 - KIL - halts CPU */
	void Opcode02() { pc = (pc - 1) & 0xFFFF; p |= I_FLAG; /* disable interrupt */ }
	/** 12 - KIL - halts CPU */
	void Opcode12() { pc = (pc - 1) & 0xFFFF; p |= I_FLAG; /* disable interrupt */ }
	/** 22 - KIL - halts CPU */
	void Opcode22() { pc = (pc - 1) & 0xFFFF; p |= I_FLAG; /* disable interrupt */ }
	/** 32 - KIL - halts CPU */
	void Opcode32() { pc = (pc - 1) & 0xFFFF; p |= I_FLAG; /* disable interrupt */ }
	/** 42 - KIL - halts CPU */
	void Opcode42() { pc = (pc - 1) & 0xFFFF; p |= I_FLAG; /* disable interrupt */ }
	/** 52 - KIL - halts CPU */
	void Opcode52() { pc = (pc - 1) & 0xFFFF; p |= I_FLAG; /* disable interrupt */ }
	/** 62 - KIL - halts CPU */
	void Opcode62() { pc = (pc - 1) & 0xFFFF; p |= I_FLAG; /* disable interrupt */ }
	/** 72 - KIL - halts CPU */
	void Opcode72() { pc = (pc - 1) & 0xFFFF; p |= I_FLAG; /* disable interrupt */ }
	/** 92 - KIL - halts CPU */
	void Opcode92() { pc = (pc - 1) & 0xFFFF; p |= I_FLAG; /* disable interrupt */ }
	/** B2 - KIL - halts CPU */
	void OpcodeB2() { pc = (pc - 1) & 0xFFFF; p |= I_FLAG; /* disable interrupt */ }
	/** D2 - KIL - halts CPU */
	void OpcodeD2() { pc = (pc - 1) & 0xFFFF; p |= I_FLAG; /* disable interrupt */ }
	/** F2 - KIL - halts CPU */
	void OpcodeF2() { pc = (pc - 1) & 0xFFFF; p |= I_FLAG; /* disable interrupt */ }
	
	// NOP 部分
	/** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
	void Opcode80() { KAI_IMM(); }
	/** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
	void Opcode82() { KAI_IMM(); }
	/** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
	void OpcodeC2() { KAI_IMM(); }
	/** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
	void OpcodeE2() { KAI_IMM(); }
	/** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
	void Opcode04() { KAI_ZP(); }
	/** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
	void Opcode14() { KAI_ZPX(); }
	/** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
	void Opcode34() { KAI_ZPX(); }
	/** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
	void Opcode44() { KAI_ZP(); }
	/** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
	void Opcode54() { KAI_ZPX(); }
	/** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
	void Opcode64() { KAI_ZP(); }
	/** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
	void Opcode74() { KAI_ZPX(); }
	/** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
	void OpcodeD4() { KAI_ZPX(); }
	/** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
	void OpcodeF4() { KAI_ZPX(); }
	/** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
	void Opcode89() { KAI_IMM(); }
	/** does nothing */
	void Opcode1A() {}
	/** does nothing */
	void Opcode3A() {}
	/** does nothing */
	void Opcode5A() {}
	/** does nothing */
	void Opcode7A() {}
	/** does nothing */
	void OpcodeDA() {}
	/** does nothing */
	void OpcodeFA() {}
	/** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
	void Opcode0C() { KAI_ABS(); }
	/** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
	void Opcode1C() { KA_ABSX_(); }
	/** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
	void Opcode3C() { KA_ABSX_(); }
	/** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
	void Opcode5C() { KA_ABSX_(); }
	/** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
	void Opcode7C() { KA_ABSX_(); }
	/** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
	void OpcodeDC() { KA_ABSX_(); }
	/** fetches operands but does not use them, issues dummy reads (may have page boundary cycle penalty) */
	void OpcodeFC() { KA_ABSX_(); }
	
	// SLO 部分
	/** shift left, OR result */
	int KM_SLO(int src) {
		int w = (src << 1) & 0xFF;
		a |= w;
		p &= ~(N_FLAG | Z_FLAG | C_FLAG);
		p |= (fl_table[a & 0xFF]);
		p |= (src >> 7) & C_FLAG;
		return w;
	}
	
	/** macro - opcodes */
	void Opcode03() { int adr = KA_INDX(); int src = K_READ(adr); K_WRITE(adr, KM_SLO(src)); }
	/** macro - opcodes */
	void Opcode13() { int adr = KA_INDY(); int src = K_READ(adr); K_WRITE(adr, KM_SLO(src)); }
	/** macro - opcodes */
	void Opcode07() { int adr = KA_ZP(); int src = K_READ(adr); K_WRITE(adr, KM_SLO(src)); }
	/** macro - opcodes */
	void Opcode17() { int adr = KA_ZPX(); int src = K_READ(adr); K_WRITE(adr, KM_SLO(src)); }
	/** macro - opcodes */
	void Opcode1B() { int adr = KA_ABSY(); int src = K_READ(adr); K_WRITE(adr, KM_SLO(src)); }
	/** macro - opcodes */
	void Opcode0F() { int adr = KA_ABS(); int src = K_READ(adr); K_WRITE(adr, KM_SLO(src)); }
	/** macro - opcodes */
	void Opcode1F() { int adr = KA_ABSX(); int src = K_READ(adr); K_WRITE(adr, KM_SLO(src)); }
	
	// RLA 部分
	/** rotate left, AND result */
	int KM_RLA(int src) {
		int w = ((src << 1) | (p & C_FLAG)) & 0xFF;
		a &= w;
		p &= ~(N_FLAG | Z_FLAG | C_FLAG);
		p |= fl_table[a & 0xFF];
		p |= (src >> 7) & C_FLAG;
		return w;
	}
	
	/** macro - opcodes */
	void Opcode23() { int adr = KA_INDX(); int src = K_READ(adr); K_WRITE(adr, KM_RLA(src)); }
	/** macro - opcodes */
	void Opcode33() { int adr = KA_INDY(); int src = K_READ(adr); K_WRITE(adr, KM_RLA(src)); }
	/** macro - opcodes */
	void Opcode27() { int adr = KA_ZP(); int src = K_READ(adr); K_WRITE(adr, KM_RLA(src)); }
	/** macro - opcodes */
	void Opcode37() { int adr = KA_ZPX(); int src = K_READ(adr); K_WRITE(adr, KM_RLA(src)); }
	/** macro - opcodes */
	void Opcode3B() { int adr = KA_ABSY(); int src = K_READ(adr); K_WRITE(adr, KM_RLA(src)); }
	/** macro - opcodes */
	void Opcode2F() { int adr = KA_ABS(); int src = K_READ(adr); K_WRITE(adr, KM_RLA(src)); }
	/** macro - opcodes */
	void Opcode3F() { int adr = KA_ABSX(); int src = K_READ(adr); K_WRITE(adr, KM_RLA(src)); }
	
	// SRE 部分
	/** shift right, EOR result */
	int KM_SRE(int src) {
		int w = (src >> 1) & 0xFF;
		a ^= w;
		p &= ~(N_FLAG | Z_FLAG | C_FLAG);
		p |= fl_table[a & 0xFF];
		p |= src & C_FLAG;
		return w;
	}
	
	/** macro - opcodes SRE */
	void Opcode43() { int adr = KA_INDX(); int src = K_READ(adr); K_WRITE(adr, KM_SRE(src)); }
	/** macro - opcodes SRE */
	void Opcode53() { int adr = KA_INDY(); int src = K_READ(adr); K_WRITE(adr, KM_SRE(src)); }
	/** macro - opcodes SRE */
	void Opcode47() { int adr = KA_ZP(); int src = K_READ(adr); K_WRITE(adr, KM_SRE(src)); }
	/** macro - opcodes SRE */
	void Opcode57() { int adr = KA_ZPX(); int src = K_READ(adr); K_WRITE(adr, KM_SRE(src)); }
	/** macro - opcodes SRE */
	void Opcode5B() { int adr = KA_ABSY(); int src = K_READ(adr); K_WRITE(adr, KM_SRE(src)); }
	/** macro - opcodes SRE */
	void Opcode4F() { int adr = KA_ABS(); int src = K_READ(adr); K_WRITE(adr, KM_SRE(src)); }
	/** macro - opcodes SRE */
	void Opcode5F() { int adr = KA_ABSX(); int src = K_READ(adr); K_WRITE(adr, KM_SRE(src)); }
	
	// RRA 部分
	int KM_RRA(int src) {
		int w = ((src >> 1) | ((p & C_FLAG) << 7)) & 0xFF;
		p &= ~(C_FLAG);
		p |= src & C_FLAG;
		KMI_ADC(w);
		return w;
	}
	
	/** macro - opcodes RRA */
	void Opcode63() { int adr = KA_INDX(); int src = K_READ(adr); K_WRITE(adr, KM_RRA(src)); }
	/** macro - opcodes RRA */
	void Opcode73() { int adr = KA_INDY(); int src = K_READ(adr); K_WRITE(adr, KM_RRA(src)); }
	/** macro - opcodes RRA */
	void Opcode67() { int adr = KA_ZP(); int src = K_READ(adr); K_WRITE(adr, KM_RRA(src)); }
	/** macro - opcodes RRA */
	void Opcode77() { int adr = KA_ZPX(); int src = K_READ(adr); K_WRITE(adr, KM_RRA(src)); }
	/** macro - opcodes RRA */
	void Opcode7B() { int adr = KA_ABSY(); int src = K_READ(adr); K_WRITE(adr, KM_RRA(src)); }
	/** macro - opcodes RRA */
	void Opcode6F() { int adr = KA_ABS(); int src = K_READ(adr); K_WRITE(adr, KM_RRA(src)); }
	/** macro - opcodes RRA */
	void Opcode7F() { int adr = KA_ABSX(); int src = K_READ(adr); K_WRITE(adr, KM_RRA(src)); }
	
	// DCP 部分
	/** decrement, CMP */
	int KM_DCP(int src) { int w = (src - 1) & 0xFF; KM_CMP(w); return w; }
	/** macro - opcodes DCP */
	void OpcodeC3() { int adr = KA_INDX(); int src = K_READ(adr); K_WRITE(adr, KM_DCP(src)); }
	/** macro - opcodes DCP */
	void OpcodeD3() { int adr = KA_INDY(); int src = K_READ(adr); K_WRITE(adr, KM_DCP(src)); }
	/** macro - opcodes DCP */
	void OpcodeC7() { int adr = KA_ZP(); int src = K_READ(adr); K_WRITE(adr, KM_DCP(src)); }
	/** macro - opcodes DCP */
	void OpcodeD7() { int adr = KA_ZPX(); int src = K_READ(adr); K_WRITE(adr, KM_DCP(src)); }
	/** macro - opcodes DCP */
	void OpcodeDB() { int adr = KA_ABSY(); int src = K_READ(adr); K_WRITE(adr, KM_DCP(src)); }
	/** macro - opcodes DCP */
	void OpcodeCF() { int adr = KA_ABS(); int src = K_READ(adr); K_WRITE(adr, KM_DCP(src)); }
	/** macro - opcodes DCP */
	void OpcodeDF() { int adr = KA_ABSX(); int src = K_READ(adr); K_WRITE(adr, KM_DCP(src)); }
	
	// ISC 部分
	/** increment, SBC */
	int KM_ISC(int src) { int w = (src + 1) & 0xFF; KMI_SBC(w); return w; }
	/** macro - opcodes ISC */
	void OpcodeE3() { int adr = KA_INDX(); int src = K_READ(adr); K_WRITE(adr, KM_ISC(src)); }
	/** macro - opcodes ISC */
	void OpcodeF3() { int adr = KA_INDY(); int src = K_READ(adr); K_WRITE(adr, KM_ISC(src)); }
	/** macro - opcodes ISC */
	void OpcodeE7() { int adr = KA_ZP(); int src = K_READ(adr); K_WRITE(adr, KM_ISC(src)); }
	/** macro - opcodes ISC */
	void OpcodeF7() { int adr = KA_ZPX(); int src = K_READ(adr); K_WRITE(adr, KM_ISC(src)); }
	/** macro - opcodes ISC */
	void OpcodeFB() { int adr = KA_ABSY(); int src = K_READ(adr); K_WRITE(adr, KM_ISC(src)); }
	/** macro - opcodes ISC */
	void OpcodeEF() { int adr = KA_ABS(); int src = K_READ(adr); K_WRITE(adr, KM_ISC(src)); }
	/** macro - opcodes ISC */
	void OpcodeFF() { int adr = KA_ABSX(); int src = K_READ(adr); K_WRITE(adr, KM_ISC(src)); }
	
	// LAX 部分
	/** load A and X */
	void KM_LAX(int src) {
		a = src;
		x = src;
		p &= ~(N_FLAG | Z_FLAG);
		p |= fl_table[src & 0xFF];
	}
	/** macro - opcodes LAX */
	void OpcodeA3() { int adr = KA_INDX(); int src = K_READ(adr); KM_LAX(src); }
	/** macro - opcodes LAX */
	void OpcodeB3() { int adr = KA_INDY_(); int src = K_READ(adr); KM_LAX(src); }
	/** macro - opcodes LAX */
	void OpcodeA7() { int adr = KA_ZP(); int src = K_READ(adr); KM_LAX(src); }
	/** macro - opcodes LAX */
	void OpcodeB7() { int adr = KA_ZPY(); int src = K_READ(adr); KM_LAX(src); }
	/** macro - opcodes LAX | this one is unstable on hardware */
	void OpcodeAB() { int adr = KA_IMM(); int src = K_READ(adr); KM_LAX(src); }
	/** macro - opcodes LAX */
	void OpcodeAF() { int adr = KA_ABS(); int src = K_READ(adr); KM_LAX(src); }
	/** macro - opcodes LAX */
	void OpcodeBF() { int adr = KA_ABSY_(); int src = K_READ(adr); KM_LAX(src); }
	
	// SAX 部分
	/** SAX - store A AND X */
	void Opcode83() { K_WRITE( KA_INDX(), (a & x) ); }
	/** SAX - store A AND X */
	void Opcode87() { K_WRITE( KA_ZP(), (a & x) ); }
	/** SAX - store A AND X */
	void Opcode97() { K_WRITE( KA_ZPY(), (a & x) ); }
	/** SAX - store A AND X */
	void Opcode8F() { K_WRITE( KA_ABS(), (a & x) ); }
	
	// AHX 部分
	/** AHX - store A AND X AND high address (somewhat unstable) */
	void Opcode93() {
		int adr = KA_ZPY();
		K_WRITE(adr, 0xFF & (a & x & ((adr >> 8) + 1)) );
	}
	/** AHX - store A AND X AND high address (somewhat unstable) */
	void Opcode9F() {
		int adr = KA_ABSY();
		K_WRITE(adr, 0xFF & (a & x & ((adr >> 8) + 1)) );
	}
	
	// TAS 部分
	/** transfer A AND X to S, store A AND X AND high address */
	void Opcode9B() {
		int adr = KA_ABSY();
		s = a & x;
		K_WRITE(adr, 0xFF & (s & ((adr >> 8) + 1)) );
	}
	
	// SHY 部分
	/** store Y AND high address (somewhat unstable) */
	void Opcode9C() {
		int adr = KA_ABSX();
		K_WRITE(adr, 0xFF & (y & ((adr >> 8) + 1)) );
	}
	
	// SHX 部分
	/** store X AND high address (somewhat unstable) */
	void Opcode9E() {
		int adr = KA_ABSY();
		K_WRITE(adr, 0xFF & (x & ((adr >> 8) + 1)) );
	}
	
	// ANC 部分
	/** a = A AND immediate */
	void Opcode0B() {
		int adr = KA_IMM();
		a = 0xFF & (a & K_READ(adr));
		p &= ~(N_FLAG | Z_FLAG | C_FLAG);
		p |= fl_table[a & 0xFF];
		p |= (a >> 7); /* C_FLAG */
	}
	void Opcode2B() {
		int adr = KA_IMM();
		a = 0xFF & (a & K_READ(adr));
		p &= ~(N_FLAG | Z_FLAG | C_FLAG);
		p |= fl_table[a & 0xFF];
		p |= (a >> 7) & C_FLAG;
	}
	
	// XAA 部分
	/** a = X AND immediate (unstable) */
	void Opcode8B() {
		int adr = KA_IMM();
		a = 0xFF & (x & K_READ(adr));
		p &= ~(N_FLAG | Z_FLAG);
		p |= fl_table[a & 0xFF];
	}

	// ALR 部分
	/** A AND immediate (unstable), shift right */
	void Opcode4B() {
		int adr = KA_IMM();
		int res = 0xFF & (a & K_READ(adr));
		a = res >> 1;
		p &= ~(N_FLAG | Z_FLAG | C_FLAG);
		p |= fl_table[a & 0xFF];
		p |= (res & C_FLAG);
	}

	// ARR 部分
	/** A AND immediate (unstable), rotate right, weird carry */
	void Opcode6B() {
		int adr = KA_IMM();
		int res = 0xFF & (a & K_READ(adr));
		a = (res >> 1) + ((p & C_FLAG) << 7);
		p &= ~(N_FLAG | V_FLAG | Z_FLAG | C_FLAG);
		p |= fl_table[a & 0xFF];
		p |= (res ^ (res >> 1)) & V_FLAG;
		p |= (res >> 7) & C_FLAG;
	}

	// LAS 部分
	/** stack AND immediate, copy to A and X */
	void OpcodeBB() {
		int adr = KA_ABSY_();
		s &= 0xFF & (K_READ(adr));
		a = s;
		x = s;
		p &= ~(N_FLAG | Z_FLAG);
		p |= fl_table[a & 0xFF];
	}

	// AXS 部分
	/** (A & X) - immediate, result in X */
	void OpcodeCB() {
		int adr = KA_IMM();
		int res = (a & x) - 0xFF & (K_READ(adr));
		x = 0xFF & (res);
		p &= ~(N_FLAG | Z_FLAG | C_FLAG);
		p |= fl_table[x & 0xFF];
		p |= (res <= 0xFF) ? C_FLAG : 0;
	}

	// SBC 部分
	/** EB is alternate opcode for SBC E9 */
	void OpcodeEB() {
		OpcodeE9();
	}
	
}
