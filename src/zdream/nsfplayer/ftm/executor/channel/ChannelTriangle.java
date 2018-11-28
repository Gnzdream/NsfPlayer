package zdream.nsfplayer.ftm.executor.channel;

import zdream.nsfplayer.ftm.executor.tools.NoteLookupTable;
import zdream.nsfplayer.ftm.format.FtmSequence;
import zdream.nsfplayer.ftm.format.FtmSequenceType;
import zdream.nsfplayer.sound.SoundTriangle;

/**
 * 2A03 三角轨道
 * 
 * @author Zdream
 * @since v0.2.2
 */
public class ChannelTriangle extends ChannelTone {

	public ChannelTriangle() {
		super(CHANNEL_2A03_TRIANGLE);
	}

	@Override
	public void playNote() {
		super.playNote();
		
		// sequence
		updateSequence();
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
	SoundTriangle sound = new SoundTriangle();

	@Override
	public SoundTriangle getSound() {
		return sound;
	}

	@Override
	public void writeToSound() {
		if (this.curVolume > 0 && playing && masterNote > 0) {
			sound.setEnable(true);
			sound.period = this.curPeriod;
		} else {
			sound.setEnable(false);
		}
	}
	
	/* **********
	 *   其它   *
	 ********** */
	
	/**
	 * 根据音键查询波长值.
	 * 工具方法
	 */
	public int periodTable(int note) {
		return NoteLookupTable.ntsc(note);
	}

}
