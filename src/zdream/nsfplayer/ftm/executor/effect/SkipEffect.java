package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;

/**
 * <p>跳到下一段的指定行, Dxx
 * <p>属全局效果
 * </p>
 * 
 * @author Zdream
 * @since 0.2.1
 */
public class SkipEffect implements IFtmEffect {
	
	/**
	 * 跳到的段号
	 */
	int row;

	private SkipEffect(int row) {
		this.row = row;
	}

	@Override
	public FtmEffectType type() {
		return FtmEffectType.SKIP;
	}
	
	/**
	 * 形成一个跳到下一段指定行, 进行播放的效果
	 * @param row
	 *   行号. 必须是非负数
	 * @return
	 *   效果实例
	 * @throws IllegalArgumentException
	 *   当 <code>row</code> 不在指定范围内时
	 */
	public static SkipEffect of(int row) throws IllegalArgumentException {
		if (row < 0) {
			throw new IllegalArgumentException("行号必须是非负数数值");
		}
		return new SkipEffect(row);
	}
	
	@Override
	public void execute(byte channelCode, FamiTrackerRuntime runtime) {
		runtime.fetcher.skipRows(row);
	}
	
	@Override
	public String toString() {
		return "SkipTo:" + row;
	}

}
