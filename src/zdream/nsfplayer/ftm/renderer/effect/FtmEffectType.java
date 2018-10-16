package zdream.nsfplayer.ftm.renderer.effect;

/**
 * Ftm 效果枚举
 * 
 * @author Zdream
 * @since 0.2.1
 */
public enum FtmEffectType {
	
	/**
	 * <p>Fxx
	 * <p>调速 (调整 speed)
	 * </p>
	 * @see SpeedEffect
	 */
	SPEED,
	
	/**
	 * <p>Fxx
	 * <p>调速 (调整 tempo)
	 * </p>
	 * @see TempoEffect
	 */
	TEMPO,
	
	/**
	 * <p>Bxx
	 * <p>跳到指定的段
	 * </p>
	 * @see JumpEffect
	 */
	JUMP,
	
	/**
	 * <p>Dxx
	 * <p>跳到下一段的指定行
	 * </p>
	 * @see SkipEffect
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
	 * @see NoiseEffect
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
	 * <p>0xx 琶音
	 * <p>DPCM 无效
	 * </p>
	 * @see ArpeggioEffect
	 */
	ARPEGGIO,
	
	/**
	 * <p>4xy 颤音
	 * <p>DPCM 无效
	 * </p>
	 * @see VibratoEffect
	 */
	VIBRATO,
	
	/**
	 * <p>7xy 音量颤音
	 * <p>DPCM, Triangle 无效
	 * </p>
	 * @see TremoloEffect
	 */
	TREMOLO,
	
	/**
	 * <p>Pxx 修改音高
	 * <p>DPCM 无效
	 * </p>
	 * @see PitchEffect
	 */
	PITCH,
	
	/**
	 * <p>Gxx 延迟效果
	 * </p>
	 * @see DelayEffect
	 */
	DELAY,
	
	/**
	 * Zxx, DPCM 特有
	 * TODO
	 */
	DAC,
	
	/**
	 * <p>1xx 向上滑音 Portamento up,
	 * 2xx 向下滑音 Portamento down,
	 * <p>DPCM 无效
	 * </p>
	 * @see PortamentoEffect
	 */
	PORTA,
	
	/**
	 * <p>Vxx 修改音色
	 * <p>DPCM 无效
	 * </p>
	 * @see DutyEffect
	 */
	DUTY_CYCLE,
	
	/**
	 * Yxx, DPCM 特有
	 * TODO
	 */
	SAMPLE_OFFSET,
	
	/**
	 * <p>Qxy Slide up, 向上滑到指定的音键
	 * <p>Rxy Slide down, 向下滑到指定的音键
	 * <p>DPCM 无效
	 * </p>
	 * @see NoteSlideEffect
	 */
	SLIDE,
	
	/**
	 * <p>Axx 音量随时间线性变化
	 * <p>Triangle, DPCM 无效
	 * </p>
	 * @see VolumnSlideEffect
	 */
	VOLUME_SLIDE,
	
	/**
	 * <p>Sxx 切断效果
	 * <p>DPCM 无效
	 * </p>
	 * @see CutEffect
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
