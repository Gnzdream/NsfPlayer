package zdream.nsfplayer.sound.vrc7;

import static zdream.nsfplayer.sound.vrc7.VRC7Static.AM_DP_BITS;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.AM_DP_WIDTH;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.AM_PG_BITS;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.PM_DP_BITS;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.PM_DP_WIDTH;
import static zdream.nsfplayer.sound.vrc7.VRC7Static.PM_PG_BITS;

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
	
	/**
	 * carriorSlot, 0 号
	 * modulatorSlot, 1 号
	 */
	public final OPLLSlot carriorSlot, modulatorSlot;
	
	int divider;
	
	/* **********
	 * 公共方法 *
	 ********** */
	
	@Override
	public void reset() {
		divider = 0;
		
		// OPLL 的
		pm_phase = 0;
		am_phase = 0;
		
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
	
/* ***********************************************************
    从 OPLL 移过来的运算相关的方法, 还没有分类, 先放这里
*********************************************************** */
	
	// Pitch Modulator
	/** unsigned */
	private int pm_phase;
	private int lfo_pm;

	// Amp Modulator
	private int am_phase;
	private int lfo_am;
	
	/**
	 * Update AM, PM unit
	 * 每次渲染前需要调用
	 * @param opll
	 */
	private void update_ampm() {
		pm_phase = (pm_phase + opll.pm_dphase) & (PM_DP_WIDTH - 1);
		am_phase = (am_phase + opll.am_dphase) & (AM_DP_WIDTH - 1);
		lfo_am = opll.amtable[(am_phase) >> (AM_DP_BITS - AM_PG_BITS)];
		lfo_pm = opll.pmtable[(pm_phase) >> (PM_DP_BITS - PM_PG_BITS)];
	}
	
	private int renderStep() {
		int inst = 0;

		update_ampm();
		
		carriorSlot.calc_phase(lfo_pm);
		carriorSlot.calc_envelope(lfo_am);
		modulatorSlot.calc_phase(lfo_pm);
		modulatorSlot.calc_envelope(lfo_am);
		
		if (carriorSlot.eg_mode != VRC7Static.FINISH) {
			inst += modulatorSlot.calc_slot_car(carriorSlot.calc_slot_mod());
		}

		return inst << 3;
	}

}
