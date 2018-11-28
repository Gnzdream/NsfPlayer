package zdream.nsfplayer.ftm.executor.channel;

import zdream.nsfplayer.ftm.executor.AbstractFtmChannel;
import zdream.nsfplayer.ftm.executor.context.DefaultSequenceHandler;
import zdream.nsfplayer.ftm.format.FtmSequence;

/**
 * <p>含音键处理的轨道, 包含:
 * <li>2A03 Pulse, Triangle, Noise 轨道;
 * <li>VRC6 Pulse, Sawtooth 轨道; (还未移动过来)
 * <li>MMC5 Pulse 轨道; (还未移动过来)
 * <li>FDS 轨道; (还未移动过来)
 * <li>N163 轨道;
 * <li>TODO 其它支持音键处理的轨道
 * </li></p>
 * 
 * @author Zdream
 * @since 0.2.5
 */
public abstract class ChannelTone extends AbstractFtmChannel {

	public ChannelTone(byte channelCode) {
		super(channelCode);
	}
	
	/* **********
	 *   序列   *
	 ********** */
	
	public final DefaultSequenceHandler seq = new DefaultSequenceHandler();
	
	/**
	 * 计算音量, 将序列所得出的音量合并计算, 最后将音量限定在 [0, 240] 范围内
	 */
	protected void calculateVolume() {
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
		
		int note = masterNote + curNote + seq.deltaNote;
		int period = -masterPitch + curPeriod + seq.period;
		
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
	
	/**
	 * 计算音色
	 */
	protected void calculateDuty() {
		if (seq.duty >= 0) {
			curDuty = seq.duty;
		} else {
			curDuty = masterDuty;
		}
	}
	
	@Override
	public void doRelease() {
		seq.setRelease(true);
	}

}
