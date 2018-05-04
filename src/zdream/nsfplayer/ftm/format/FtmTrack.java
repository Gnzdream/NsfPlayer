package zdream.nsfplayer.ftm.format;

/**
 * FTM 乐曲
 * @author Zdream
 */
public final class FtmTrack {
	
	public static final int
			DEFAULT_NTSC_TEMPO = 150,
			DEFAULT_PAL_TEMPO = 125;
	
	/**
	 * 每个模块最大的行数
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
	 * 模式 PATTERN
	 * [段数][轨道数]
	 */
	public FtmPattern[][] patterns;
	
	/* **********
	 * 曲目顺序 *
	 ********** */
	/*
	 * 顺序 ORDER
	 * [段数][轨道数]
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
