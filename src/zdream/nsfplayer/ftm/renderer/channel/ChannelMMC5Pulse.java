package zdream.nsfplayer.ftm.renderer.channel;

import static zdream.nsfplayer.sound.Sound2A03.LENGTH_TABLE;

import zdream.nsfplayer.ftm.format.FtmSequence;
import zdream.nsfplayer.ftm.format.FtmSequenceType;
import zdream.nsfplayer.sound.PulseSound;

/**
 * MMC5 一号 / 二号矩形轨道
 * 
 * @author Zdream
 * @since v0.2.2
 */
public class ChannelMMC5Pulse extends ChannelMMC5 {

	/**
	 * @param isPulse1
	 *   如果是 MMC5 一号矩形轨道, 为 true
	 *   如果是 MMC5 二号矩形轨道, 为 false
	 */
	public ChannelMMC5Pulse(boolean isPulse1) {
		super(isPulse1 ? CHANNEL_MMC5_PULSE1 : CHANNEL_MMC5_PULSE2);
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
		curDuty &= 0x3;
	}
	
	/* **********
	 *  发声器  *
	 ********** */
	
	/**
	 * MMC5 Pulse 音频发声器
	 */
	public final PulseSound sound = new PulseSound();

	@Override
	public PulseSound getSound() {
		return sound;
	}
	
	@Override
	public void writeToSound() {
//		sound.looping = true; // 恒定
//		sound.envelopeFix = true; // 恒定
		
		if (this.curVolume == 0 || !playing || masterNote == 0) {
			sound.fixedVolume = 0;
			return;
		}
		
		sound.dutyLength = curDuty;
		sound.fixedVolume = curVolume / 16;
		
		// 0x4002 and 0x4003
		sound.period = curPeriod;
		sound.lengthCounter = LENGTH_TABLE[0];
	}
	
}
