package zdream.nsfplayer.ftm.format;

/**
 * <p>FTM 乐曲
 * <p>{@link FtmAudio} 是多个 FTM 乐曲的集合.
 * </p>
 * 
 * @author Zdream
 * @since v0.1
 */
public final class FtmTrack {
	
	public static final int
			DEFAULT_NTSC_TEMPO = 150,
			DEFAULT_PAL_TEMPO = 125;
	
	/**
	 * 每个模式 / 段的最大行数
	 */
	public int length;
	
	/**
	 * 播放速度
	 */
	public int speed;
	
	/**
	 * 节奏值
	 */
	public int tempo;
	
	/**
	 * 名称
	 */
	public String name;
	
	/* **********
	 *   模式   *
	 ********** */
	/*
	 * 模式 PATTERN (段是 FRAME)
	 * [模式号][轨道序号]
	 */
	public FtmPattern[][] patterns;
	
	/* **********
	 * 曲目顺序 *
	 ********** */
	/*
	 * 顺序 ORDER
	 * [段数][轨道序号]
	 */
	public int[][] orders;
	
	/* **********
	 *   其它   *
	 ********** */
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder(24);
		b.append("Track").append(' ').append(name);
		return b.toString();
	}

}
