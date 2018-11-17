package zdream.nsfplayer.ftm.renderer.channel;

import zdream.nsfplayer.ftm.format.FtmSequence;
import zdream.nsfplayer.ftm.format.FtmSequenceType;
import zdream.nsfplayer.ftm.renderer.tools.NoteLookupTable;
import zdream.nsfplayer.sound.SoundNoise;

/**
 * 2A03 噪音轨道
 * 
 * @author Zdream
 * @since v0.2.2
 */
public class NoiseChannel extends ChannelTone {

	public NoiseChannel() {
		super(CHANNEL_2A03_NOISE);
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
	
	@Override
	protected void calculateDuty() {
		super.calculateDuty();
		curDuty &= 0x1;
	}
	
	@Override
	public int periodTable(int note) {
		return NoteLookupTable.pal(note);
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
		calculateNoise();
		calculateDuty();
	}
	
	private void calculateNoise() {
		if (masterNote == 0) {
			curNote = 0;
			return;
		}
		
		int note = masterNote + curNote + seq.deltaNote;
		note += (-masterPitch + curPeriod + seq.period);
		
		if (seq.arp != 0) {
			switch (seq.arpSetting) {
			case FtmSequence.ARP_SETTING_ABSOLUTE:
				note += seq.arp;
				break;
			case FtmSequence.ARP_SETTING_FIXED: // 重置
				this.masterNote = note = seq.arp;
				break;
			case FtmSequence.ARP_SETTING_RELATIVE:
				this.masterNote += seq.arp;
				note += seq.arp;
			default:
				break;
			}
		}
		
		if (note <= 1) {
			note = 1;
		} else if (note > 16) {
			note = 16;
		}
		
		curNote = note;
	}

	/* **********
	 *  发声器  *
	 ********** */
	
	SoundNoise sound = new SoundNoise();

	@Override
	public SoundNoise getSound() {
		return sound;
	}
	
	/**
	 * <p>将轨道中的数据写到发声器中.
	 * <p>参照原工程 NoiseChan.refreshChannel()
	 * </p>
	 */
	public void writeToSound() {
//		sound.envelopeLoop = true;
//		sound.envelopeFix = true;
		
		sound.fixedVolume = curVolume / 16;
		if (curVolume == 0 || !playing || masterNote == 0) {
			sound.fixedVolume = 0;
			return;
		}
		
		int period = (curNote - 1) ^ 0x0F;
		
		sound.dutySampleRate = ((curDuty & 1) == 1) ?
				SoundNoise.DUTY_SAMPLE_RATE1 : SoundNoise.DUTY_SAMPLE_RATE0;
		sound.periodIndex = period;
		sound.lengthCounter = 0;
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
