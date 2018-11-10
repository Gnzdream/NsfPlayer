package zdream.nsfplayer.sound.vrc7;

import static zdream.nsfplayer.sound.vrc7.VRC7Static.*;

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
		
		this.modulatorSlot = new OPLLSlot(opll, 0);
		this.carriorSlot = new OPLLSlot(opll, 1);
	}
	
	/* **********
	 *   参数   *
	 ********** */
	
	/**
	 * modulatorSlot, 0 号
	 * carriorSlot, 1 号
	 */
	public final OPLLSlot modulatorSlot, carriorSlot;
	
	/**
	 * modulatorSlot 与 carriorSlot 是否打开的标志
	 */
	public boolean modOn, carOn;
	
	/**
	 * 采用的 patch 号码. 0 为自定义
	 */
	public int patchNum;
	
	/**
	 * 音频的状态每 {@link #step} 个时钟变化一次, 需要向外部输出音频数值.
	 * 记录现在到下一个 step 触发点剩余的时钟数
	 */
	private int counter = 36;
	
	/* **********
	 * 公共方法 *
	 ********** */
	
	@Override
	public void reset() {
		counter = 36;
		
		// OPLL 的
		pm_phase = 0;
		am_phase = 0;
		
		modulatorSlot.reset();
		carriorSlot.reset();
		
		super.reset();
	}

	@Override
	protected void onProcess(int time) {
		int value;
		
		while (time >= counter) {
			time -= counter;
			this.time += counter;
			counter	= 36;
			
			value = this.renderStep();
			mix(value);
		}
		
		this.time += time;
		counter -= time;
	}
	
/* ***********************************************************
    从 OPLL 移过来的运算相关的方法, 还没有分类, 先放这里
*********************************************************** */
	
	// Pitch Modulator
	private int pm_phase;

	// Amp Modulator
	private int am_phase;
	
	private int renderStep() {
		// 原工程的 opll.update_ampm();
		// Update AM, PM unit
		pm_phase = (pm_phase + opll.pm_dphase) & (PM_DP_WIDTH - 1);
		am_phase = (am_phase + opll.am_dphase) & (AM_DP_WIDTH - 1);
		int lfo_am = opll.amtable[(am_phase) >> (AM_DP_BITS - AM_PG_BITS)];
		int lfo_pm = opll.pmtable[(pm_phase) >> (PM_DP_BITS - PM_PG_BITS)];
		
		carriorSlot.calc_phase(lfo_pm);
		carriorSlot.calc_envelope(lfo_am);
		modulatorSlot.calc_phase(lfo_pm);
		modulatorSlot.calc_envelope(lfo_am);
		
		if (carriorSlot.eg_mode != FINISH) {
			carriorSlot.calc_slot_car(modulatorSlot.calc_slot_mod());
		}

		return carriorSlot.output[1];
	}

}
