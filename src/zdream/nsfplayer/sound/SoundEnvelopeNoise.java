package zdream.nsfplayer.sound;

import zdream.nsfplayer.core.IFrameSequence;

/**
 * 拥有 envelope 声部的噪音发声器
 * 
 * @author Zdream
 * @since v0.2.8
 */
public class SoundEnvelopeNoise extends SoundNoise implements IFrameSequence {
	
	/* **********
	 *   参数   *
	 ********** */
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
	public boolean envelopeDisable;
	
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
	 * envelope 部分数据
	 */
	private int envelopeCounter;
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
	 * 如果刚刚更新了 envelope 部分, 请调用该方法
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
		envelopeLoop = false;
		envelopeDisable = false;
		
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
		
		// 240hz clock
		{
			boolean divider = false;
			if (envelopeUpdated) {
				envelopeUpdated = false;
				envelopeCounter = 15;
				envelopeDiv = 0;
			} else {
				++envelopeDiv;
				// volume = envelopeDivPeriod
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
			// noise length counter
			if (!envelopeLoop && (lengthCounter > 0))
				--lengthCounter;
		}
	}
	
	protected int processStep(int period) {
		// Frame Sequence 更新部分
		sequenceRemain -= period;
		if (sequenceRemain < 0) {
			sequenceRemain += sequenceStep;
			sequenceUpdate();
		}
		
		// 渲染部分
		int volume;
		if (envelopeDisable) {
			volume = fixedVolume;
		} else {
			volume = envelopeCounter;
		}
		if (lengthCounter <= 0) {
			volume = 0;
		}
		
		int ret = (shiftReg & 1) != 0 ? volume : 0;
		shiftReg = (((shiftReg << 14) ^ (shiftReg << dutySampleRate)) & 0x4000) | (shiftReg >> 1);
		return ret;
	}
	
	@Override
	protected void processRemainTime(int period) {
		sequenceRemain -= period;
		if (sequenceRemain < 0) {
			sequenceRemain += sequenceStep;
			sequenceUpdate();
		}
	}

}
