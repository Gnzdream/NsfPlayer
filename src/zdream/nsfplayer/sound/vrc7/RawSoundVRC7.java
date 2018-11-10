package zdream.nsfplayer.sound.vrc7;

import zdream.nsfplayer.sound.AbstractNsfSound;

/**
 * VRC7 轨道的发声器. 共存在六个这类轨道
 * (非正式版)
 * 
 * @author Zdream
 * @since v0.2.7
 */
public class RawSoundVRC7 extends AbstractNsfSound {
	
	OPLL opll;
	int index;

	RawSoundVRC7(OPLL opll, int index) {
		this.opll = opll;
		this.index = index;
		
		this.carriorSlot = new OPLLSlot(opll);
		this.modulatorSlot = new OPLLSlot(opll);
	}
	
	/* **********
	 *   参数   *
	 ********** */
	
	public final OPLLSlot carriorSlot, modulatorSlot;
	
	int divider;
	
	/* **********
	 * 公共方法 *
	 ********** */
	
	@Override
	public void reset() {
		divider = 0;
		
		super.reset();
	}

	@Override
	protected void onProcess(int time) {
		// TODO 下面是过渡, 需要修改
		if (index == 0) {
			divider += time;
			while (divider >= 36) {
				divider -= 36;
				opll.calc0();
			}
		}
		
		this.time += time;
		mix(opll.slots[(index << 1) | 1].output[1]);
	}

}
