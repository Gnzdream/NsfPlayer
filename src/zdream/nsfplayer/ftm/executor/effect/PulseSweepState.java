package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.ftm.executor.AbstractFtmChannel;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.executor.IFtmState;
import zdream.nsfplayer.ftm.executor.channel.Channel2A03Pulse;

/**
 * <p>扫音效果状态
 * <p>当某个轨道有该状态时, 音高会随着时间变化.
 * 在这段时间之内, 轨道将不会对任何其它的修改音高、音键的效果作出反应.
 * </p>
 * 
 * @see PulseSweepEffect
 * 
 * @author Zdream
 * @since 0.2.9
 */
public class PulseSweepState implements IFtmState {
	
	public static final String NAME = "Sweep";

	@Override
	public String name() {
		return "Sweep";
	}

	@Override
	public void trigger(byte channelCode, FamiTrackerRuntime runtime) {
		Channel2A03Pulse ch = (Channel2A03Pulse) runtime.channels.get(channelCode);
		
		if (ch.isNoteUpdated()) {
			ch.clearSweep();
			
			// 删除自己
			ch.removeState(this);
		}
	}
	
	/**
	 * 低优先度. 需要绝大部分主音键 {@link AbstractFtmChannel#setMasterNote(int)} 的调用完后再触发
	 */
	public int priority() {
		return -5;
	}

}
