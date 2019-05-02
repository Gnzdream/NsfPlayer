package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.ftm.executor.AbstractFtmChannel;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;

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
			if (baseNote == 0) {
				// 该效果出现时, 原来的轨道的 note 还没有设置,
				// 此时先不产生滑音效果
				baseNote = masterNote;
				delta = 0;
			} else if (speed != 0) {
				delta += ch.periodTable(baseNote) - ch.periodTable(masterNote);
				baseNote = masterNote;
			} else {
				delta = 0; // delta = 0, speed = 0, 等待删除
			}
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
