package zdream.nsfplayer.ftm.executor.channel;

import zdream.nsfplayer.ftm.format.FtmSequence;
import zdream.nsfplayer.ftm.format.FtmSequenceType;
import zdream.nsfplayer.sound.SoundVRC6Pulse;

/**
 * VRC6 一号 / 二号矩形轨道
 * 
 * @author Zdream
 * @since v0.2.3
 */
public class ChannelVRC6Pulse extends ChannelVRC6 {

	/**
	 * @param isPulse1
	 *   如果是 VRC6 一号矩形轨道, 为 true
	 *   如果是 VRC6 二号矩形轨道, 为 false
	 */
	public ChannelVRC6Pulse(boolean isPulse1) {
		super(isPulse1 ? CHANNEL_VRC6_PULSE1 : CHANNEL_VRC6_PULSE2);
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
		
		// 回写
		calculateVolume();
		calculatePeriod();
		calculateDuty();
		
	}
	
	@Override
	protected void calculateDuty() {
		super.calculateDuty();
		curDuty &= 0x7;
	}
	
	/* **********
	 *  发声器  *
	 ********** */
	
	/**
	 * VRC6 Pulse 音频发声器
	 */
	public final SoundVRC6Pulse sound = new SoundVRC6Pulse();

	@Override
	public SoundVRC6Pulse getSound() {
		return sound;
	}
	
	@Override
	public void writeToSound() {
		sound.period = this.curPeriod;
		sound.volume = this.curVolume / 16;
		sound.duty = this.curDuty;
		
		if (!playing || masterNote == 0) {
			sound.setEnable(false);
		} else {
			sound.setEnable(true);
		}
	}
	
}
