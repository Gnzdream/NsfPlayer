package zdream.nsfplayer.ftm.renderer.effect;

/**
 * Ftm 效果枚举
 * 
 * @author Zdream
 * @since 0.2.1
 */
public enum FtmEffectType {
	
	/**
	 * Fxx
	 * 调速 (调整 speed)
	 * TODO
	 */
	SPEED,
	
	/**
	 * Fxx
	 * 调速 (调整 tempo)
	 * TODO
	 */
	TEMPO,
	
	/**
	 * Bxx
	 * 跳到下一段的指定行
	 * TODO
	 */
	JUMP,
	
	/**
	 * Dxx
	 * 跳到指定的段
	 * TODO
	 */
	SKIP,
	
	/**
	 * Cxx (对应于效果的 EF_HALT)
	 * 直接停止播放
	 * TODO
	 */
	STOP,
	
	/**
	 * <p>Exx 或直接修改 volumn
	 * 调整音量.
	 * <p>DPCM, Triangle 无效
	 * </p>
	 * @see VolumnEffect
	 */
	VOLUME,
	
	/**
	 * <p>直接修改音高或音键
	 * </p>
	 * @see NoteEffect
	 */
	NOTE,
	
	/**
	 * <p>直接停止当前音键的播放, 使该轨道静音
	 * </p>
	 * @see NoteHaltEffect
	 */
	HALT,
	
	/**
	 * <p>直接让当前音键释放. 如果该音键对应的乐器含释放部分, 则播放释放部分
	 * </p>
	 * @see NoteReleaseEffect
	 */
	RELEASE,
	
	/**
	 * <p>直接修改乐器
	 * </p>
	 * @see InstrumentEffect
	 */
	INSTRUMENT,
	
	/**
	 * 3xx
	 * TODO
	 */
	PORTAMENTO,
	
	/**
	 * Hxx, FDS 无效
	 * TODO
	 */
	SWEEPUP,
	
	/**
	 * Ixx, FDS 无效
	 * TODO
	 */
	SWEEPDOWN,
	
	/**
	 * 0xx
	 * TODO
	 */
	ARPEGGIO,
	
	/**
	 * 4xx
	 * TODO
	 */
	VIBRATO,
	
	/**
	 * 7xx
	 * TODO
	 */
	TREMOLO,
	
	/**
	 * Pxx
	 * TODO
	 */
	PITCH,
	
	/**
	 * Gxx
	 * TODO
	 */
	DELAY,
	
	/**
	 * Zxx, DPCM 特有
	 * TODO
	 */
	DAC,
	
	/**
	 * 1xx
	 * TODO
	 */
	PORTA_UP,
	
	/**
	 * 2xx
	 * TODO
	 */
	PORTA_DOWN,
	
	/**
	 * Vxx
	 * TODO
	 */
	DUTY_CYCLE,
	
	/**
	 * Yxx, DPCM 特有
	 * TODO
	 */
	SAMPLE_OFFSET,
	
	/**
	 * Qxx
	 * TODO
	 */
	SLIDE_UP,
	
	/**
	 * Rxx
	 * TODO
	 */
	SLIDE_DOWN,
	
	/**
	 * Axx
	 * TODO
	 */
	VOLUME_SLIDE,
	
	/**
	 * Sxx
	 * TODO
	 */
	NOTE_CUT,
	
	/**
	 * Xxx, DPCM 特有
	 * TODO
	 */
	RETRIGGER,
	
	/**
	 * Hxx, FDS 限定
	 * TODO
	 */
	FDS_MOD_DEPTH,
	
	/**
	 * Ixx, FDS 限定
	 * TODO
	 */
	FDS_MOD_SPEED_HI,
	
	/**
	 * Jxx, FDS 限定
	 * TODO
	 */
	FDS_MOD_SPEED_LO,
	
	/**
	 * Wxx, DPCM 特有
	 * TODO
	 */
	DPCM_PITCH,
	
	/**
	 * Hxx
	 * TODO
	 */
	SUNSOFT_ENV_LO,
	
	/**
	 * Ixx
	 * TODO
	 */
	SUNSOFT_ENV_HI,
	
	/**
	 * Jxx
	 * TODO
	 */
	SUNSOFT_ENV_TYPE;

}