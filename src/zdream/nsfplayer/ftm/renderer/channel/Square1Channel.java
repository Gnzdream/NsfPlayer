package zdream.nsfplayer.ftm.renderer.channel;

import zdream.nsfplayer.ftm.format.FtmSequence;
import zdream.nsfplayer.ftm.renderer.sequence.DefaultSequenceHandler;
import zdream.nsfplayer.ftm.renderer.tools.NoteLookupTable;
import zdream.nsfplayer.sound.PulseSound;

/**
 * 2A03 矩形轨道 1
 * 
 * @author Zdream
 * @since 0.2.1
 */
public final class Square1Channel extends Channel2A03Tone {

	public Square1Channel() {
		super(CHANNEL_2A03_PULSE1);
	}

	@Override
	public void playNote() {
		super.playNote();
		
		// sequence
		updateSequence();
		
		// 发声器
		writeToSound();
	}

	@Override
	public void reset() {
		seq.reset();
		sound.reset();
	}
	
	/* **********
	 *   序列   *
	 ********** */
	
	public final DefaultSequenceHandler seq = new DefaultSequenceHandler();
	
	/**
	 * 更新序列, 并将序列的数据回写到轨道上
	 */
	private void updateSequence() {
		seq.update();
		
		// 回写
		calculateVolume();
		calculatePeriod();
		calculateDuty();
		
	}
	
	/**
	 * 计算音量, 将序列所得出的音量合并计算, 最后将音量限定在 [0, 15] 范围内
	 */
	private void calculateVolume() {
		int volume = curVolume;

		if (curVolume <= 0) {
			curVolume = 0;
			return;
		}

		volume = (seq.volume * volume) / 15;
		if (volume > 240) {
			curVolume = 240;
		} else if (volume < 1) {
			curVolume = (seq.volume == 0) ? 0 : 1;
		} else {
			curVolume = volume;
		}
	}
	
	/**
	 * 计算波长, 将序列所得出的波长、音高、音键, 还有其它效果得出的音高、音键值
	 * 最后综合出波长值
	 */
	private void calculatePeriod() {
		int note = masterNote + curNote + seq.deltaNote;
		int period = -masterPitch + curPeriod + seq.period;
		
		if (seq.arp != 0) {
			// TODO 存在问题
			// 请注意 SequenceHandler.updateSequenceRunning 方法
			switch (seq.arpSetting) {
			case FtmSequence.ARP_SETTING_ABSOLUTE:
				note += seq.arp;
				break;
			case FtmSequence.ARP_SETTING_FIXED: // 重置
				note = seq.arp;
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
		} else if (note > 96) {
			note = 96;
		}
		
		period += NoteLookupTable.ntsc(note);
		if (period < 1) {
			period = 1;
		}
		
		curNote = note;
		curPeriod = period;
	}
	
	/**
	 * 计算音色
	 */
	private void calculateDuty() {
		if (seq.duty >= 0) {
			curDuty = seq.duty;
		} else {
			curDuty = masterDuty;
		}
	}
	
	/* **********
	 *  发声器  *
	 ********** */
	
	/**
	 * 2A03 Pulse 1 音频发声器
	 */
	public final PulseSound sound = new PulseSound();

	@Override
	public PulseSound getSound() {
		return sound;
	}
	
	/**
	 * 将轨道中的数据写到发声器中
	 */
	public void writeToSound() {
		sound.looping = true;
		sound.envelopeFix = true;
		
		if (this.curVolume == 0) {
			return;
		}
		
		sound.dutyLength = curDuty;
		sound.fixedVolume = curVolume;
		
		if (sweep > 0) {
			if ((sweep & 0x80) != 0) {
				// 0x4001
				int s = sweep & 0x7F;
				sound.sweepEnabled = true;
				sound.sweepPeriod = ((s >> 4) & 0x07) + 1;
				sound.sweepMode = (s & 0x08) != 0;		
				sound.sweepShift = s & 0x07;
				sound.sweepUpdated = true;
				
//				writeRegister(0x4017, (byte) 0x80);	// Clear sweep unit
//				writeRegister(0x4017, (byte) 0x00);
//				writeRegister(0x4002, hiFreq);
//				writeRegister(0x4003, loFreq);
//				m_iLastPeriod = 0xFFFF;
			}
		}
		// sound.
	}
	
}
