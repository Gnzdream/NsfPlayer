package zdream.nsfplayer.ftm.document.format;

import java.util.HashMap;

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
	 */
	public FtmPattern[][] patterns;

	/**
	 * <p>标识每条轨道的最大效果标志数量.
	 * <p>下面数量为 0 表示最大效果数量为 1, 以此类推
	 * 
	 * <p>byte (轨道标识) - int (轨道数)
	 */
	public HashMap<Byte, Integer> channelEffCount = new HashMap<>();
	
	/* **********
	 * 曲目顺序 *
	 ********** */
	/*
	 * 顺序 ORDER
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
