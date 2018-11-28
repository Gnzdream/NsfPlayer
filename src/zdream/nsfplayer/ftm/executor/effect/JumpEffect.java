package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;

/**
 * <p>跳到指定的段, Bxx
 * <p>属全局效果
 * </p>
 * 
 * @author Zdream
 * @since 0.2.1
 */
public class JumpEffect implements IFtmEffect {
	
	/**
	 * 跳到的段号
	 */
	public final int section;

	private JumpEffect(int section) {
		this.section = section;
	}

	@Override
	public FtmEffectType type() {
		return FtmEffectType.JUMP;
	}
	
	/**
	 * 形成一个跳到指定段开头, 进行播放的效果
	 * @param section
	 *   段号. 必须是非负数
	 * @return
	 *   效果实例
	 * @throws IllegalArgumentException
	 *   当 <code>section</code> 不在指定范围内时
	 */
	public static JumpEffect of(int section) throws IllegalArgumentException {
		if (section < 0) {
			throw new IllegalArgumentException("段号必须是正整数数值");
		}
		return new JumpEffect(section);
	}
	
	@Override
	public void execute(byte channelCode, FamiTrackerRuntime rumtime) {
		rumtime.fetcher.jumpToSection(section);
	}
	
	@Override
	public String toString() {
		return "JumpTo:" + section;
	}

}
