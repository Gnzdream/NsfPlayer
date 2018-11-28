package zdream.nsfplayer.ftm.executor.channel;

import zdream.nsfplayer.ftm.executor.tools.NoteLookupTable;
import zdream.nsfplayer.ftm.format.FtmSequence;
import zdream.nsfplayer.ftm.format.FtmSequenceType;
import zdream.nsfplayer.sound.SoundVRC6Sawtooth;

/**
 * VRC6 锯齿形轨道. 该轨道没有控制音色的项
 * 
 * @author Zdream
 * @since v0.2.3
 */
public class ChannelVRC6Sawtooth extends ChannelVRC6 {

	public ChannelVRC6Sawtooth() {
		super(CHANNEL_VRC6_SAWTOOTH);
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
		calculateDuty(); // volume 的计算需要用到 duty
		calculateVolume();
		calculatePeriod();
	}
	
	@Override
	protected void calculateVolume() {
		int volume = masterVolume * 16 + curVolume; // 精度 240
		if (volume <= 0) {
			curVolume = 0;
			return;
		} else if ((this.curDuty & 1) == 1) {
			// VRC6 锯齿形轨道中的音色项如果为奇数 (例如 V01),
			// 那么音量将会提升一个档次.
			volume += 240;
		}
		// volume 精度 480
		
		volume = (seq.volume * volume) / 15;
		
		if (volume > 480) {
			curVolume = 480;
		} else if (volume < 1) {
			curVolume = (seq.volume == 0) ? 0 : 1;
		} else {
			curVolume = volume;
		}
	}
	
	@Override
	public int periodTable(int note) {
		return NoteLookupTable.saw(note);
	}
	
	/* **********
	 *  发声器  *
	 ********** */
	
	/**
	 * VRC6 Pulse 音频发声器
	 */
	public final SoundVRC6Sawtooth sound = new SoundVRC6Sawtooth();

	@Override
	public SoundVRC6Sawtooth getSound() {
		return sound;
	}

	@Override
	public void writeToSound() {
		if (!playing || masterNote == 0) {
			sound.period = 0;
			sound.volume = 0;
		} else {
			sound.period = this.curPeriod;
			sound.volume = this.curVolume * 64 / 480;
		}
	}
	
}
