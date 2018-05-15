package zdream.nsfplayer.ftm.format;

import static zdream.nsfplayer.ftm.FamiTrackerSetting.MAX_INSTRUMENTS;

/**
 * <p>FTM 每一个有效 note 数据.
 * <p>里面存放了一个键的数据, 包含曲调、音量、乐器、效果等数据
 * 
 * @author Zdream
 * @date 2018-05-03
 * @version 0.1
 */
public class FtmNote {
	
	/**
	 * 该参数是 {@link NOTE_NONE} 这类
	 */
	public byte note;
	
	/**
	 * 八度音阶
	 */
	public byte octave;
	
	/**
	 * 注意, {@link MAX_VOLUMN} 为空
	 */
	public byte vol;
	
	/**
	 * 注意, {@link MAX_INSTRUMENTS} 为空
	 */
	public int instrument;
	
	public final byte[] effNumber = new byte[MAX_EFFECT_COLUMNS];
	
	public final short[] effParam = new short[MAX_EFFECT_COLUMNS];

	/**
	 * 效果列最大值
	 */
	public static final int MAX_EFFECT_COLUMNS = 4;

	/**
	 * Note 类型. NOTE_HALT 为停止符, NOTE_RELEASE 为休止符
	 */
	public static final byte
		NOTE_NONE = 0,
		NOTE_C = 1,
		NOTE_CS = 2,
		NOTE_D = 3,
		NOTE_DS = 4,
		NOTE_E = 5,
		NOTE_F = 6,
		NOTE_FS = 7,
		NOTE_G = 8,
		NOTE_GS = 9,
		NOTE_A = 10,
		NOTE_AS = 11,
		NOTE_B = 12,
		NOTE_RELEASE = 13,
		NOTE_HALT = 14;
	
	/**
	 * <p>轨道中的音乐效果
	 * <p>Channel effects
	 * <p>在 C++ 原来的文件中是记录在 effect_t 这个枚举中
	 * <p>EF_PORTAOFF (= 7) 是没有用的!<br>
	 * EF_DELAYED_VOLUME (= 25) 标记为 Unimplemented
	 */
	public static final byte
		EF_NONE = 0,
		EF_SPEED = 1,
		EF_JUMP = 2,
		EF_SKIP = 3,
		EF_HALT = 4, // Cxx cancel
		EF_VOLUME = 5,
		EF_PORTAMENTO = 6,
		EF_PORTAOFF = 7,
		EF_SWEEPUP = 8,
		EF_SWEEPDOWN = 9,
		EF_ARPEGGIO = 10,
		EF_VIBRATO = 11, // 4xx
		EF_TREMOLO = 12,
		EF_PITCH = 13, // Pxx
		EF_DELAY = 14,
		EF_DAC = 15,
		EF_PORTA_UP = 16,
		EF_PORTA_DOWN = 17,
		EF_DUTY_CYCLE = 18,
		EF_SAMPLE_OFFSET = 19,
		EF_SLIDE_UP = 20,
		EF_SLIDE_DOWN = 21,
		EF_VOLUME_SLIDE = 22,
		EF_NOTE_CUT = 23, // Sxx
		EF_RETRIGGER = 24,
		EF_DELAYED_VOLUME = 25, // Unimplemented
		EF_FDS_MOD_DEPTH = 26,
		EF_FDS_MOD_SPEED_HI = 27,
		EF_FDS_MOD_SPEED_LO = 28,
		EF_DPCM_PITCH = 29,
		EF_SUNSOFT_ENV_LO = 30,
		EF_SUNSOFT_ENV_HI = 31,
		EF_SUNSOFT_ENV_TYPE = 32,
		EF_COUNT = 33;
	
	// Channel effect letters
	public static final char EFF_CHAR[] = {
			'.',	// None 这个是我补上去的, 为了和上面的效果数组元素一一对应
			'F',	// Speed
			'B',	// Jump 
			'D',	// Skip 
			'C',	// Halt
			'E',	// Volume
			'3',	// Porta on
			 0,		// Porta off		// unused
			'H',	// Sweep up
			'I',	// Sweep down
			'0',	// Arpeggio
			'4',	// Vibrato
			'7',	// Tremolo
			'P',	// Pitch
			'G',	// Note delay
			'Z',	// DAC setting
			'1',	// Portamento up
			'2',	// Portamento down
			'V',	// Duty cycle
			'Y',	// Sample offset
			'Q',	// Slide up
			'R',	// Slide down
			'A',	// Volume slide
			'S',	// Note cut
			'X',	// DPCM retrigger						 
			 0,		// (TODO, delayed volume)
			'H',	// FDS modulation depth
			'I',	// FDS modulation speed hi
			'J',	// FDS modulation speed lo
			'W',	// DPCM Pitch
			'H',	// Sunsoft envelope low
			'I',	// Sunsoft envelope high
			'J',	// Sunsoft envelope type
			//'9'	// Targeted volume slide
			/*
			'H',	// VRC7 modulator
			'I',	// VRC7 carrier
			'J',	// VRC7 modulator/feedback level
			*/
		};
	
	/**
	 * 音量范围 [0, 15], 16 代表空
	 */
	public static final byte MAX_VOLUME = 16;
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder(30);
		String hex = "0123456789ABCDEF";
		
		if (note == NOTE_NONE) {
			b.append("...");
		} else if (note == NOTE_RELEASE) {
			b.append("===");
		} else if (note == NOTE_HALT) {
			b.append("---");
		} else {
			switch (note) {
			case NOTE_C: b.append("C-"); break;
			case NOTE_CS: b.append("C#"); break;
			case NOTE_D: b.append("D-"); break;
			case NOTE_DS: b.append("D#"); break;
			case NOTE_E: b.append("E-"); break;
			case NOTE_F: b.append("F-"); break;
			case NOTE_FS: b.append("F#"); break;
			case NOTE_G: b.append("G-"); break;
			case NOTE_GS: b.append("G#"); break;
			case NOTE_A: b.append("A-"); break;
			case NOTE_AS: b.append("A#"); break;
			case NOTE_B: b.append("B-"); break;
			}
			b.append(octave);
		}
		
		b.append(' ');
		if (instrument == MAX_INSTRUMENTS) {
			b.append("..");
		} else {
			b.append(hex.charAt(instrument / 16));
			b.append(hex.charAt(instrument % 16));
		}
		b.append(' ');
		
		if (vol == MAX_VOLUME) {
			b.append('.');
		} else {
			b.append(hex.charAt(vol));
		}
		
		for (int i = 0; i < MAX_EFFECT_COLUMNS; i++) {
			if (effNumber[i] != 0) {
				b.append(' ').append(EFF_CHAR[effNumber[i] - 1]).append('-');
				String paramStr = Integer.toHexString(effParam[i] & 0xFF);
				if (paramStr.length() == 1) {
					b.append('0');
				}
				b.append(paramStr);
			}
		}
		
		return b.toString();
	}
	
	

}
