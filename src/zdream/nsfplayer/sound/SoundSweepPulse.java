package zdream.nsfplayer.sound;

import zdream.nsfplayer.core.IFrameSequence;

/**
 * 拥有 sweep 和 envelope 声部的矩形波发声器
 * 
 * @author Zdream
 * @since v0.2.9
 */
public class SoundSweepPulse extends SoundPulse implements IFrameSequence {
	
	public SoundSweepPulse() {
		super();
		this.isFirstChannel = true;
	}
	
	/**
	 * @param isFirstChannel
	 *   2A03 有两个轨道. 如果是第一个轨道, 请输入 true; 如果是第二个轨道, 请输入 false
	 */
	public SoundSweepPulse(boolean isFirstChannel) {
		super();
		this.isFirstChannel = isFirstChannel;
	}
	
	/* **********
	 *   参数   *
	 ********** */
	
	/**
	 * <p>2A03 有两个轨道. 如果是第一个轨道, 为 true; 如果是第二个轨道, 为 false.
	 * <p>该参数不会被 {@link #reset()} 方法重置.
	 * </p>
	 */
	public boolean isFirstChannel;
	
	/*
	 * 原始记录参数
	 */

	/**
	 * <p>0 号位: 00x00000
	 * <p>为 1 时为 true, 为 0 时为 false
	 * </p>
	 */
	public boolean envelopeLoop;
	
	/**
	 * <p>0 号位: 000x0000
	 * <p>为 1 时为 true, 为 0 时为 false
	 * </p>
	 */
	public boolean envelopeFix;
	
	/**
	 * <p>1 号位: x0000000
	 * <p>为 1 时为 true, 为 0 时为 false
	 * </p>
	 */
	public boolean sweepEnabled;

	/**
	 * <p>1 号位: 0xxx0000, 取得数值之后加 1,
	 * 表示每隔多少单位时间段扫音频率变化
	 * <p>unsigned, 值域 [1, 8]
	 * </p>
	 */
	public int sweepPeriod;

	/**
	 * <p>1 号位: 0000x000
	 * <p>为 1 时为 true 升音, 为 0 时为 false 降音
	 * </p>
	 */
	public boolean sweepMode;

	/**
	 * <p>1 号位: 00000xxx, 偏移位,
	 * 表示每个时间段扫音频率的变化量参数
	 * <p>unsigned, 值域 [0, 7]
	 * </p>
	 */
	public int sweepShift;
	
	/*
	 * 辅助参数
	 */
	
	/**
	 * 该帧有多少 Frame Sequence 已经过去了
	 */
	private int sequenceCount;
	
	/**
	 * 每个 Frame Sequence 需要的时钟数
	 */
	private int sequenceStep = SEQUENCE_STEP_NTSC;
	
	/**
	 * 到达下一个 Frame Sequence 触发时间还需要多少时钟数
	 */
	private int sequenceRemain;
	
	/*
	 * sweep 部分数据
	 */
	
	/**
	 * 记录 sweep 相关参数是否被修改
	 */
	public boolean sweepUpdated;
	
	/**
	 * Sweep 部分计算的波长修正值
	 */
	private int sweepResult;
	private int sweepDiv;
	
	/*
	 * envelope 部分数据
	 */
	private int envelopeCounter; // 即 envelope 部分计算的音量修正值
	private int envelopeDiv;
	private boolean envelopeUpdated;
	
	/* **********
	 *   设置   *
	 ********** */

	@Override
	public void setSequenceStep(int clock) {
		this.sequenceStep = clock;
	}
	
	/**
	 * 如果刚刚更新了 sweep 部分, 请调用该方法
	 */
	public void onSweepUpdated() {
		sweepUpdated = true;
		calcSweepPeriod();
	}
	
	/**
	 * 如果刚刚更新了 envelope 部分 (3 号位) 请调用该方法
	 */
	public void onEnvelopeUpdated() {
		envelopeUpdated = true;
	}
	
	/* **********
	 * 公共方法 *
	 ********** */
	
	@Override
	public void endFrame() {
		sequenceCount = 0;
		sequenceRemain = 0;
		super.endFrame();
	}
	
	@Override
	public void reset() {
		// 原始记录参数
		sweepEnabled = false;
		sweepPeriod = 1;
		sweepMode = false;		
		sweepShift = 0;
		
		envelopeFix = false;

		// 辅助参数
		sweepUpdated = false;
		sweepResult = 0;
		sweepDiv = 1;
		
		envelopeCounter = 0;
		envelopeDiv = 0;
		envelopeUpdated = false;
		
		super.reset();
	}
	
	/* **********
	 *   渲染   *
	 ********** */
	
	private void sequenceUpdate() {
		sequenceCount++;
		// 规定一帧最多触发 4 次
		if (sequenceCount > 4) {
			return;
		}
		
		// 240hz clock, envelope 部分
		{
			boolean divider = false;
			if (envelopeUpdated) {
				envelopeUpdated = false;
				envelopeCounter = 15;
				envelopeDiv = 0;
			} else {
				++envelopeDiv;
				if (envelopeDiv > fixedVolume) {
					divider = true;
					envelopeDiv = 0;
				}
			}
			
			if (divider) {
				if (envelopeLoop && envelopeCounter == 0)
					envelopeCounter = 15;
				else if (envelopeCounter > 0)
					--envelopeCounter;
			}
		}
		
		// 120hz clock
		if ((sequenceCount & 1) == 1) {
			// envelope 部分
			if (!envelopeLoop && (lengthCounter > 0))
				--lengthCounter;
			
			// sweep 部分
			if (sweepEnabled) {
				--sweepDiv;
				if (sweepDiv <= 0) {
					calcSweepPeriod(); // 重新计算 sweep 后的频率数据

					if (period >= 8 && sweepResult < 0x800 && sweepShift > 0) {
						// 如果频率数据合适, 才会更新
						period = sweepResult < 0 ? 0 : sweepResult;
					}
					sweepDiv = sweepPeriod + 1;
				}

				if (sweepUpdated) {
					sweepDiv = sweepPeriod + 1;
					sweepUpdated = false;
				}
			}
		}
	}
	
	/**
	 * <p>计算目标扫音效果导致的真实频率值.
	 * <p>真实频率数将放在变量 {@link #sweepResult} 中, 使用时替换 {@link SoundPulse#period}.
	 * </p>
	 */
	private void calcSweepPeriod() {
		 int shifted = this.period >> sweepShift;
		 if (isFirstChannel && sweepMode)
			 shifted += 1;
		 sweepResult = period + (sweepMode ? -shifted : shifted);
	}
	
	@Override
	protected int processStep(int period) {
		// Frame Sequence 更新部分
		sequenceRemain -= period;
		if (sequenceRemain < 0) {
			sequenceRemain += sequenceStep;
			sequenceUpdate();
		}
		
		// 渲染部分 volume
		int volume = envelopeFix ? fixedVolume : envelopeCounter;
		if (DUTY_TABLE[dutyLength][dutyCycle]) {
			return volume;
		} else {
			return 0;
		}
	}
	
	@Override
	protected boolean isStatusValid() {
		return super.isStatusValid() && (sweepResult < 0x800);
	}

}
