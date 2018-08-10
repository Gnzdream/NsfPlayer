package zdream.nsfplayer.ftm.renderer.channel;

import static zdream.nsfplayer.ftm.renderer.tools.FamiTrackerParameter.LENGTH_TABLE;

import zdream.nsfplayer.ftm.format.FtmSequence;
import zdream.nsfplayer.ftm.format.FtmSequenceType;
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
		processSound();
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
	
	/**
	 * 计算音量, 将序列所得出的音量合并计算, 最后将音量限定在 [0, 15] 范围内
	 */
	private void calculateVolume() {
		int volume = masterVolume * 16 + curVolume; // 精度 240

		if (volume <= 0) {
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
		System.out.println(curVolume);
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
	 * <p>将轨道中的数据写到发声器中.
	 * <p>参照原工程 Square1Chan.refreshChannel()
	 * </p>
	 */
	public void writeToSound() {
		sound.looping = true;
		sound.envelopeFix = true;
		
		if (this.curVolume == 0) {
			return;
		}
		
		sound.dutyLength = curDuty;
		sound.fixedVolume = curVolume / 16;
		
		if (sweep > 0) {
			if ((sweep & 0x80) != 0) {
				// 0x4001
				int s = sweep & 0x7F;
				sound.sweepEnabled = true;
				sound.sweepPeriod = ((s >> 4) & 0x07) + 1;
				sound.sweepMode = (s & 0x08) != 0;		
				sound.sweepShift = s & 0x07;
				sound.sweepUpdated = true;
				
				// TODO Clear sweep unit 不清楚如何清除 Sweep 部分
//				writeRegister(0x4017, (byte) 0x80);	// Clear sweep unit
//				writeRegister(0x4017, (byte) 0x00);
				
				// 0x4002 and 0x4003
				sound.period = curPeriod;
				sound.lengthCounter = LENGTH_TABLE[0];
			}
		} else {
			// 0x4001
			sound.sweepEnabled = false;
			sound.sweepPeriod = 1;
			sound.sweepMode = true;
			sound.sweepShift = 0;
			sound.sweepUpdated = true;
			
			// TODO 不清楚如何操作
//			writeRegister(0x4017, (byte) 0x80);	// Manually execute one APU frame sequence to kill the sweep unit
//			writeRegister(0x4017, (byte) 0x00);

			// 0x4002 and 0x4003
			sound.period = curPeriod;
			sound.lengthCounter = LENGTH_TABLE[0];
		}
		// sound.
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
