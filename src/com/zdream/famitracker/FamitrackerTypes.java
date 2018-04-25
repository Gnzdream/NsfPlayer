package com.zdream.famitracker;

import com.zdream.famitracker.document.Sequence;

/**
 * <p>这个类保存着 Famitracker 的常量和常用方法.
 * <p>Here are the constants that defines the limits in the tracker
 * @author Zdream
 */
public class FamitrackerTypes {
	
	public static int midiNote(final int octave, final int note) {
		return ((octave) * 12 + (note) - 1);
	}
	
	public static int getOctave(final int midi_note) {
		return ((midi_note) / 12);
	}
	
	public static int getNote(final int midi_note) {
		return ((midi_note) % 12 + 1);
	}

	/**
	 * Maximum number of instruments to use
	 */
	public static final int MAX_INSTRUMENTS = 64;

	/**
	 * Maximum number of sequence lists
	 */
	public static final int MAX_SEQUENCES = 128;

	/**
	 * <p>每个 {@link Sequence} 中可以含有的 Item 最大的数量
	 * <p>Maximum number of items in each sequence
	 * <p>TODO: need to check if this exports correctly
	 */
	public static final int MAX_SEQUENCE_ITEMS = /*128*/ 253;

	/**
	 * <p>每条轨道最大的 Pattern 数
	 * <p>Maximum number of patterns per channel
	 */
	public static final int MAX_PATTERN = 128;

	/**
	 * <p>Frame 的最大值
	 * <p>Maximum number of frames
	 */
	public static final int MAX_FRAMES = 128;

	/**
	 * <p>每个模块的最大行数. 这个值在 NSF 中也有明确定义
	 * <p>Maximum length of patterns (in rows). 256 is max in NSF
	 */
	public static final int MAX_PATTERN_LENGTH = 256;

	/**
	 * Maximum number of DPCM samples, cannot be increased unless the NSF driver is modified.
	 */
	public static final int MAX_DSAMPLES = 64;

	/**
	 * <p>Sample space available (from $C000-$FFFF), may now switch banks
	 * <p>等于 256kB
	 */
	public static final int MAX_SAMPLE_SPACE = 0x40000;

	/**
	 * Number of effect columns allowed
	 */
	public static final int MAX_EFFECT_COLUMNS = 4;

	/**
	 * Maximum numbers of tracks allowed (NSF limit is 256, but dunno if the bankswitcher can handle that)
	 */
	public static final int MAX_TRACKS = 64;

	/**
	 * Max tempo
	 */
	public static final int MAX_TEMPO = 255;

	// Min tempo
	//const int MIN_TEMPO	= 21;

	// Max speed
	//const int MAX_SPEED = 20;

	/**
	 * Min speed
	 */
	public static final int MIN_SPEED = 1;

	/**
	 * <p>Number of avaliable channels (max) TODO: should not be used anymore!
	 * <p>instead, check the channelsavailable variable and allocate dynamically
	 */
	public static final int MAX_CHANNELS	 = 5 + 3 + 2 + 6 + 1 + 8 + 3;		

	public static final int CHANNELS_DEFAULT = 5;
	public static final int CHANNELS_VRC6 = 3;
	public static final int CHANNELS_VRC7 = 6;

	public static final int OCTAVE_RANGE = 8;
	public static final int NOTE_RANGE = 12;

	public static final int INVALID_INSTRUMENT = -1;

	/**
	 * <p>当一个 note 的音量达到 MAX_VOLUME 时, 说明那个 note 实际上没有注明 note.
	 * <p>实际 note 音量的范围为 0 到 15
	 */
	public static final int MAX_VOLUME = 0x10;

	/**
	 * <p>Sequence types (shared with VRC6)
	 * <p>在 C++ 原来的文件中是记录在 sequence_t 这个枚举中
	 * <p>SEQ_HIPITCH = 3 TODO: remove this eventually
	 */
	public static final int
		SEQ_VOLUME = 0,
		SEQ_ARPEGGIO = 1,
		SEQ_PITCH = 2,
		SEQ_HIPITCH = 3,
		SEQ_DUTYCYCLE = 4,
		SEQ_COUNT = 5;

	/**
	 * <p>轨道中的音乐效果
	 * <p>Channel effects
	 * <p>在 C++ 原来的文件中是记录在 effect_t 这个枚举中
	 * <p>EF_PORTAOFF (= 7) 是没有用的!<br>
	 * EF_DELAYED_VOLUME (= 25) 标记为 Unimplemented
	 */
	public static final int
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
		EF_VIBRATO = 11,
		EF_TREMOLO = 12,
		EF_PITCH = 13,
		EF_DELAY = 14,
		EF_DAC = 15,
		EF_PORTA_UP = 16,
		EF_PORTA_DOWN = 17,
		EF_DUTY_CYCLE = 18,
		EF_SAMPLE_OFFSET = 19,
		EF_SLIDE_UP = 20,
		EF_SLIDE_DOWN = 21,
		EF_VOLUME_SLIDE = 22,
		EF_NOTE_CUT = 23,
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
	 * Note 类型. NOTE_HALT 为停止符, NOTE_RELEASE 为休止符
	 */
	public static final int NOTE_NONE = 0,
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
	 * 原来在 machine_t 枚举中
	 */
	public static final byte
		NTSC = 0,
		PAL = 1;

	/**
	 * 原来在 vibrato_t 枚举中
	 */
	public static final int
		VIBRATO_OLD = 0,
		VIBRATO_NEW = 1;
	
}
