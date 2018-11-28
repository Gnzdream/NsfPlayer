package zdream.nsfplayer.ftm.executor.effect;

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
	 * <p>Cxx (对应于效果的 EF_HALT)
	 * <p>直接停止播放
	 * </p>
	 * @see StopEffect
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
	 * <p>3xx 持续滑音
	 * <p>DPCM 无效
	 * </p>
	 * @see PortamentoOnEffect
	 */
	PORTAMENTO,
	
	/**
	 * <p>Hxy (升音), Ixy (降音), 扫音
	 * <p>2A03 Pulse 特有
	 * </p>
	 * @see PulseSweepEffect
	 */
	SWEEP,
	
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
	 * @see VRC7VolumeSlideEffect
	 */
	VOLUME_SLIDE,
	
	/**
	 * <p>Sxx 切断效果
	 * </p>
	 * @see CutEffect
	 */
	NOTE_CUT,
	
	/**
	 * <p>Zxx, 设置 DAC 值
	 * <p>DPCM 特有
	 * </p>
	 * @see DPCM_DACSettingEffect
	 */
	DAC,
	
	/**
	 * <p>Yxx, 设置起始读取位 offset.
	 * <p>DPCM 特有
	 * </p>
	 * @see DPCMSampleOffsetEffect
	 */
	SAMPLE_OFFSET,
	
	/**
	 * <p>Xxx, 重新触发采样, 即循环一次.
	 * <p>DPCM 特有
	 * </p>
	 * @see DPCMRetriggerEffect
	 */
	RETRIGGER,
	
	/**
	 * Wxx, 设置 DPCM 采样的音高
	 * <p>DPCM 特有
	 * </p>
	 * @see DPCMPitchEffect
	 */
	DPCM_PITCH,
	
	/**
	 * Hxx, 设置 FDS 频率调制器的音源深度
	 * <p>FDS 特有
	 * </p>
	 * @see FDSModDepthEffect
	 */
	FDS_MOD_DEPTH,
	
	/**
	 * Ixx, 设置 FDS 调制器频率高 4 位
	 * <p>FDS 特有
	 * </p>
	 * @see FDSModSpeedHighEffect
	 */
	FDS_MOD_SPEED_HIGH,
	
	/**
	 * Jxx, 设置 FDS 调制器频率低 8 位
	 * <p>FDS 特有
	 * </p>
	 * @see FDSModSpeedLowEffect
	 */
	FDS_MOD_SPEED_LOW,
	
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
