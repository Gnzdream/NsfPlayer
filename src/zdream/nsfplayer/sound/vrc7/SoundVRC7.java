package zdream.nsfplayer.sound.vrc7;

import static zdream.nsfplayer.sound.vrc7.VRC7Static.*;

import zdream.nsfplayer.sound.AbstractNsfSound;

/**
 * VRC7 轨道的发声器. 共存在六个这类轨道
 * 
 * @author Zdream
 * @since v0.2.7
 */
public class SoundVRC7 extends AbstractNsfSound {
	
	OPLL opll;
	int index;

	SoundVRC7(OPLL opll, int index) {
		this.opll = opll;
		this.index = index;
		
		this.modulatorSlot = new OPLLSlot(opll, 0);
		this.carriorSlot = new OPLLSlot(opll, 1);
	}
	
	/* **********
	 *   参数   *
	 ********** */
	
	/*
	 * (间接) 原始记录参数
	 * 
	 * 大部分数据放在 modulatorSlot 和 carriorSlot 中
	 */
	
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
	
	/*
	 * 辅助参数
	 */
	
	/**
	 * 音频的状态每 {@link #step} 个时钟变化一次, 需要向外部输出音频数值.
	 * 记录现在到下一个 step 触发点剩余的时钟数
	 */
	private int counter = 36;

	/**
	 * AM 单元 (Amp Modulator) 的相位参数
	 */
	private int am_phase;
	
	/**
	 * PM 单元 (Pitch Modulator) 的相位参数
	 */
	private int pm_phase;

	/* **********
	 * 处理写入 *
	 ********** */
	
	/**
	 * 询问当前发声器是否使用的是自定义的 patch
	 */
	public boolean useCustomPatch() {
		return patchNum == 0;
	}
	
	/**
	 * <p>在 NES 流程中, 如果 VRC7 芯片的 [0x10, 0x40) 位置被写入数据之后,
	 * 相应的 slot 需要进行一次数据的重置. 该函数就要完成这个任务
	 * <p>重新读取自定义 patch 的数据, 并用该 patch 数据重置自己的 slot.
	 * <p>如果该发声器使用的是自定义的 patch, 即 <code>patchNum == 0</code>,
	 * 则当外部修改了自定义 patch 的数据, 当前发声器就需要进行数据的同步
	 * </p>
	 */
	public void rebuildAll() {
		rebuildModDphase();
		rebuildModTll();
		rebuildModSintbl();
		recalcModDphase();
		
		rebuildCarDphase();
		rebuildCarTll();
		rebuildCarSintbl();
		recalcCarDphase();
	}
	
	public void rebuildModDphase() {
		modulatorSlot.dphase = opll.dphaseTable[modulatorSlot.fnum][modulatorSlot.block][modulatorSlot.patch.ML];
		modulatorSlot.rks = opll.rksTable[(modulatorSlot.fnum) >> 8][modulatorSlot.block][modulatorSlot.patch.KR ? 1 : 0];
	}
	
	public void rebuildCarDphase() {
		carriorSlot.dphase = opll.dphaseTable[carriorSlot.fnum][carriorSlot.block][carriorSlot.patch.ML];
		carriorSlot.rks = opll.rksTable[(carriorSlot.fnum) >> 8][carriorSlot.block][carriorSlot.patch.KR ? 1 : 0];
	}
	
	public void rebuildModTll() {
		modulatorSlot.tll = opll.tllTable[(modulatorSlot.fnum) >> 5][modulatorSlot.block][modulatorSlot.patch.TL][modulatorSlot.patch.KL];
	}
	
	public void rebuildCarTll() {
		carriorSlot.tll = opll.tllTable[(carriorSlot.fnum) >> 5][carriorSlot.block][carriorSlot.volume][carriorSlot.patch.KL];
	}
	
	public void rebuildModSintbl() {
		modulatorSlot.sintbl = opll.waveform[modulatorSlot.patch.WF];
	}
	
	public void rebuildCarSintbl() {
		carriorSlot.sintbl = opll.waveform[carriorSlot.patch.WF];
	}
	
	public void recalcModDphase() {
		modulatorSlot.eg_dphase = modulatorSlot.calc_eg_dphase();
	}
	
	public void recalcCarDphase() {
		carriorSlot.eg_dphase = carriorSlot.calc_eg_dphase();
	}
	
	/**
	 * 修改音色
	 * Change a voice
	 */
	public void setPatch(int num) {
		patchNum = num;
		modulatorSlot.patch.copyFrom(opll.patches[num * 2]);
		carriorSlot.patch.copyFrom(opll.patches[num * 2 + 1]);
	}
	
	/**
	 * 将轨道打开.
	 * Channel key on
	 */
	public void keyOn() {
		if (!modOn) {
			modulatorSlot.slotOn();
			modOn = true;
		}
		if (!carOn) {
			carriorSlot.slotOn();
			carOn = true;
		}
	}
	
	/**
	 * <p>将轨道关闭.
	 * <p>与 enable 还不一样, enable 是完全关闭, keyOff 是进入结束状态, 与 seq 的 RELEASE 状态很像
	 * <p>Channel key off
	 * </p>
	 */
	public void keyOff() {
		if (carOn) {
			carriorSlot.slotOff();
			carOn = false;
		}
		modOn = false;
	}
	
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
