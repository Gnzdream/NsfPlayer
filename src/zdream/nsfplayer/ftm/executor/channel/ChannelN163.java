package zdream.nsfplayer.ftm.executor.channel;

import zdream.nsfplayer.ftm.executor.tools.NoteLookupTable;
import zdream.nsfplayer.ftm.format.FtmInstrumentN163;
import zdream.nsfplayer.ftm.format.FtmSequence;
import zdream.nsfplayer.ftm.format.FtmSequenceType;
import zdream.nsfplayer.sound.SoundN163;

/**
 * N163 轨道
 * 
 * @author Zdream
 * @since v0.2.6
 */
public class ChannelN163 extends ChannelTone {

	/**
	 * @param num
	 *   第几号 N163 轨道. 范围 [0, 7]
	 */
	public ChannelN163(int num) {
		super((byte) (CHANNEL_N163_1 + num));
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
		lastDuty = -1;
	}
	
	/* **********
	 * 乐器序列 *
	 ********** */
	/**
	 * 当前使用的乐器
	 */
	private FtmInstrumentN163 currentInst;
	
	/**
	 * 上一帧使用的 duty 值, 即上一帧用第几个 wave
	 */
	private int lastDuty = -1;
	
	/**
	 * 更新序列, 并将序列的数据回写到轨道上
	 */
	private void updateSequence() {
		if (instrumentUpdated) {
			lastDuty = -1;
			currentInst = getRuntime().querier.getN163Instrument(instrument);
			if (currentInst == null) {
				seq.reset();
				haltSound();
			} else {
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
		}
		
		seq.update();
		
		// 回写
		calculateVolume();
		calculatePeriod();
		calculateDuty(); // duty 用来选第几个 wave 的
	}
	
	/**
	 * 关闭 {@link #sound}, 不让其发出声音
	 */
	private void haltSound() {
		sound.setEnable(false);
		sound.volume = 0;
	}
	
	/**
	 * 计算波长, 将序列所得出的波长、音高、音键, 还有其它效果得出的音高、音键值
	 * 最后综合出波长值
	 */
	protected void calculatePeriod() {
		if (masterNote == 0) {
			// 不播放
			curNote = 0;
			curPeriod = 0;
			return;
		}
		
		int ch = getRuntime().querier.audio.getNamcoChannels();
		int note = masterNote + curNote + seq.deltaNote;
		int period = masterPitch * ch + curPeriod + seq.period;
		
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
		} else if (note > 96) {
			note = 96;
		}
		
		period += periodTable(note);
		if (period < 1) {
			period = 1;
		}
		
		curNote = note;
		curPeriod = period;
	}
	
	@Override
	public int periodTable(int note) {
		int ch = getRuntime().querier.audio.getNamcoChannels();
		return NoteLookupTable.n163(note) * ch;
	}
	
	/* **********
	 *  发声器  *
	 ********** */
	
	public final SoundN163 sound = new SoundN163();

	@Override
	public SoundN163 getSound() {
		return sound;
	}
	
	@Override
	public void writeToSound() {
		if (!playing || masterNote == 0) {
			haltSound();
			return;
		}
		
		int duty = this.curDuty;
		int waveCount = this.currentInst.waves.length;
		duty %= waveCount;
		
		if (duty != lastDuty) {
			// 写入 wave
			byte[] wave = this.currentInst.waves[duty];
			System.arraycopy(wave, 0, sound.wave, 0, wave.length);
			sound.length = wave.length;
			
			lastDuty = duty;
		}
		
		// N163 的轨道数
		int ch = getRuntime().querier.audio.getNamcoChannels();
		sound.step = 15 * ch;
		
		// 其它参数
		sound.period = this.curPeriod * 4;
		sound.volume = this.curVolume / 16;
		sound.setEnable(true);
	}
	
}
