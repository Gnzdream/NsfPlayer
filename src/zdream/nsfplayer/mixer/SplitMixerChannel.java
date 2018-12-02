package zdream.nsfplayer.mixer;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>拆分音频轨道.
 * <p>将一个输入源的数据拆分成多条路输出. 这个是多声道输出的解决方案
 * </p>
 * 
 * @author Zdream
 * @since v0.3.0
 */
public class SplitMixerChannel implements IMixerChannel {
	
	/**
	 * 总轨道音量值.
	 */
	private float masterLevel;

	/* **********
	 * 下级轨道 *
	 ********** */
	
	/**
	 * 所有下级轨道的集合
	 */
	private final ArrayList<IMixerChannel> channels = new ArrayList<>();
	
	/**
	 * 添加下级轨道
	 * @param channel
	 *   下级轨道
	 * @throws NullPointerException
	 *   当下级轨道 channel = null 时
	 */
	public void addOutputChannel(IMixerChannel channel) {
		requireNonNull(channel, "channel = null");
		channels.add(channel);
	}
	
	/**
	 * 删除下级轨道
	 * @param channel
	 * @return
	 *   {@link List#remove(Object)}
	 */
	public boolean removeOutputChannel(IMixerChannel channel) {
		return channels.remove(channel);
	}
	
	/**
	 * 清空下级轨道
	 */
	public void clearOutputChannel() {
		channels.clear();
	}

	/* **********
	 * 公共接口 *
	 ********** */

	@Override
	public void reset() {
		channels.forEach(c -> c.reset());
	}

	@Override
	public void setLevel(float level) {
		this.masterLevel = level;
	}

	@Override
	public float getLevel() {
		return masterLevel;
	}

	@Override
	public void mix(int value, int time) {
		channels.forEach(c -> c.mix(value, time));
	}

}
