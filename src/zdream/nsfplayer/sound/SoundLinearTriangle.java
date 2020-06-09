package zdream.nsfplayer.sound;

import zdream.nsfplayer.core.IFrameSequence;

/**
 * 拥有 linear 声部的噪音发声器
 * 
 * @author Zdream
 * @since v0.2.9
 */
public class SoundLinearTriangle extends SoundTriangle implements IFrameSequence {
	
	/* **********
	 *   参数   *
	 ********** */
	/*
	 * 原始记录参数
	 */

	/**
	 * <p>0 号位: x0000000
	 * <p>为 1 时为 true, 为 0 时为 false
	 * </p>
	 */
	public boolean looping;
	
	/**
	 * <p>0 号位: 0xxxxxxx
	 * <p>unsigned, 值域 [0, 127]
	 * </p>
	 */
	public int linearLoad;
	
	
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
	 * linear 部分
	 */
	private boolean linearCounterHalt;
	private int linearCounter;
	
	
	/* **********
	 *   设置   *
	 ********** */

	@Override
	public void setSequenceStep(int clock) {
		this.sequenceStep = clock;
	}
	
	/**
	 * 如果刚刚更新了 3 号位, 请调用该方法
	 */
	public void onEnvelopeUpdated() {
		linearCounterHalt = true;
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
		looping = false;
		linearLoad = 0;
		
		// 辅助参数
		linearCounterHalt = false;
		linearCounter = 0;
		
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
			if (linearCounterHalt) {
				linearCounter = linearLoad;
			} else {
				if (linearCounter > 0)
					--linearCounter;
			}
			if (!looping) {
				linearCounterHalt = false;
			}
		}
		
		// 120hz clock
		if ((sequenceCount & 1) == 1) {
			if (!looping && (lengthCounter > 0))
				--lengthCounter;
		}
	}
	
	@Override
	protected int processStep(int period) {
		// Frame Sequence 更新部分
		sequenceRemain -= period;
		while (sequenceRemain < 0) {
			sequenceRemain += sequenceStep;
			sequenceUpdate();
		}
		
		return super.processStep(period);
	}
	
	protected boolean isStatusValid() {
		return super.isStatusValid() && linearCounter > 0;
	}

}
