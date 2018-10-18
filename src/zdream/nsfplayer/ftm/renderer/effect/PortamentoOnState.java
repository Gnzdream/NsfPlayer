package zdream.nsfplayer.ftm.renderer.effect;

import zdream.nsfplayer.ftm.renderer.AbstractFtmChannel;
import zdream.nsfplayer.ftm.renderer.FamiTrackerRuntime;

/**
 * <p>持续滑音的状态, 3xx
 * </p>
 * 
 * @see NoteSlideEffect
 * 
 * @author Zdream
 * @since 0.2.2
 */
public class PortamentoOnState extends NoteSlideState {
	
	public int baseNote;

	public PortamentoOnState(int speed, int baseNote) {
		super(speed, 0);
		this.baseNote = baseNote;
	}

	public PortamentoOnState(int speed, int delta, int baseNote) {
		super(speed, delta);
		this.baseNote = baseNote;
	}
	
	@Override
	public void trigger(byte channelCode, FamiTrackerRuntime runtime) {
		AbstractFtmChannel ch = runtime.channels.get(channelCode);
		
		int masterNote = ch.getMasterNote();
		if (masterNote != baseNote) {
			delta += ch.periodTable(baseNote) - ch.periodTable(masterNote);
			baseNote = masterNote;
		}
		
		if (delta > 0) {
			delta -= speed;
			if (delta < 0) {
				delta = 0;
			}
		} else {
			delta += speed;
			if (delta > 0) {
				delta = 0;
			}
		}
		
		if (delta == 0 && speed == 0) {
			// 删除该状态
			ch.removeState(this);
			return;
		}
		
		ch.addCurrentPeriod(delta);
	}
	
	@Override
	public int priority() {
		return 5;
	}

}
