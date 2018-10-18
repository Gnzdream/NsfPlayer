package zdream.nsfplayer.ftm.renderer.channel;

import zdream.nsfplayer.ftm.format.FtmSequence;
import zdream.nsfplayer.ftm.format.FtmSequenceType;
import zdream.nsfplayer.sound.TriangleSound;

/**
 * 2A03 三角轨道
 * 
 * @author Zdream
 * @since v0.2.2
 */
public class TriangleChannel extends Channel2A03Tone {

	public TriangleChannel() {
		super(CHANNEL_2A03_TRIANGLE);
	}

	@Override
	public void playNote() {
		super.playNote();
		
		// sequence
		updateSequence();
		
		// 发声器
		writeToSound();
		processSound();
	}

	@Override
	public void reset() {
		super.reset();
		seq.reset();
		sound.reset();
	}
	
	/* **********
	 *   序列   *
	 ********** */
	
	/**
	 * 更新序列, 并将序列的数据回写到轨道上
	 */
	private void updateSequence() {
		if (instrumentUpdated) {
			// 替换序列
			FtmSequence[] seqs = getRuntime().querier.getSequences(instrument);
			for (int i = 0; i < seqs.length; i++) {
				FtmSequence s = seqs[i];
				if (s != null) {
					seq.setupSequence(seqs[i]);
				} else {
					seq.clearSequence(FtmSequenceType.get(i));
				}
			}
		}
		
		seq.update();
		
		// 回写 (三角波的轨道没有音色值)
		calculateVolume();
		calculatePeriod();
	}
	
	/**
	 * 计算音量, 由于三角波轨道的特殊性, 这里最后确定的是三角波是否要发声音, 最后将音量限定在 [0, 1] 范围内
	 */
	protected void calculateVolume() {
		if (seq.volume == 0) {
			curVolume = 0;
		} else {
			curVolume = 1;
		}
	}
	
	/* **********
	 *  发声器  *
	 ********** */
	
	/**
	 * 2A03 Triangle 音频发声器
	 */
	TriangleSound sound = new TriangleSound();

	@Override
	public TriangleSound getSound() {
		return sound;
	}
	
	/**
	 * <p>将轨道中的数据写到发声器中.
	 * <p>参照原工程 TriangleChan.refreshChannel()
	 * </p>
	 */
	public void writeToSound() {
		if (this.curVolume > 0 && playing && masterNote > 0) {
			sound.looping = true;
			sound.linearLoad = 1;
			sound.period = this.curPeriod;
			sound.lengthCounter = 0;
		} else {
			sound.looping = false;
			sound.linearLoad = 0;
		}
	}
	
	/**
	 * 指导发声器工作一帧
	 */
	public void processSound() {
		// 拿到一帧对应的时钟周期数
		int freq = getRuntime().param.freqPerFrame;
		
		// TODO 暂时没有考虑 sweep 和 envelope 部分, 还有 4017 参数
		sound.process(freq);
		
		// 结束
		sound.endFrame();
	}

}
