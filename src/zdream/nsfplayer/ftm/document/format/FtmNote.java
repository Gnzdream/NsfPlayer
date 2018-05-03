package zdream.nsfplayer.ftm.document.format;

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
	
	public byte vol;
	
	public int instrument;
	
	public final int[] effNumber = new int[MAX_EFFECT_COLUMNS];
	
	public final int[] effParam = new int[MAX_EFFECT_COLUMNS];

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
	
	/**
	 * 音量范围 [0, 15], 16 代表空
	 */
	public static final byte MAX_VOLUME = 16;

}
