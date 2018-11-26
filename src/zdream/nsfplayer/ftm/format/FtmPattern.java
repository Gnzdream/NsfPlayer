package zdream.nsfplayer.ftm.format;

/**
 * <p>FTM 每一个有效模式 (pattern) 数据.
 * <p>里面存放了一个模式 (或段落) 的、指定轨道的所有 note 数据, 也就是 FamiTracker 的一列数据
 * 
 * @author Zdream
 * @since v0.1
 */
public class FtmPattern {
	
	public FtmNote[] notes;
	
	/**
	 * 段长
	 * @return
	 * @since v0.2.10
	 */
	public int length() {
		return (notes == null) ? 0 : notes.length;
	}
	
}
