package zdream.nsfplayer.ftm.executor.channel;

import zdream.nsfplayer.ftm.executor.AbstractFtmChannel;
import zdream.nsfplayer.ftm.executor.context.DefaultSequenceHandler;
import zdream.nsfplayer.ftm.executor.tools.NoteLookupTable;
import zdream.nsfplayer.ftm.format.FtmInstrumentFDS;
import zdream.nsfplayer.ftm.format.FtmSequence;
import zdream.nsfplayer.ftm.format.FtmSequenceType;
import zdream.nsfplayer.sound.SoundFDS;

/**
 * FDS 轨道
 * 
 * @author Zdream
 * @since v0.2.4
 */
public class ChannelFDS extends AbstractFtmChannel {

	public ChannelFDS() {
		super(CHANNEL_FDS);
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
		haltSound();
	}
	
	/* **********
	 * 乐器序列 *
	 ********** */
	
	public final DefaultSequenceHandler seq = new DefaultSequenceHandler();
	
	/**
	 * 当前使用的乐器
	 */
	private FtmInstrumentFDS currentInst;
	
	/**
	 * FDS 乐器中的一些参数
	 */
	private int modSpeed, modDepth, modDelay;
	
	/**
	 * 是否重置 sound.modPos 的标志
	 */
	private boolean resetMod = false;
	
	/**
	 * 设置 modDepth. 该方法由效果 Hxx 调用
	 */
	public void setModDepth(int modDepth) {
		this.modDepth = modDepth;
	}
	
	/**
	 * 设置 modSpeed 高 4 位. 该方法由效果 Ixx 调用
	 */
	public void setModFreqHigh(int freq) {
		this.modSpeed = (this.modSpeed & 0xFF) | (freq << 8);
	}
	
	/**
	 * 设置 modSpeed 低 8 位. 该方法由效果 Jxx 调用
	 */
	public void setModFreqLow(int freq) {
		this.modSpeed = (this.modSpeed & 0xF00) | (freq);
	}
	
	/**
	 * 更新序列, 并将序列的数据回写到轨道上
	 */
	private void updateSequence() {
		if (instrumentUpdated) {
			currentInst = getRuntime().querier.getFDSInstrument(instrument);
			if (currentInst == null) {
				seq.reset();
				haltSound();
			} else {
				// 替换序列
				FtmSequence[] seqs = new FtmSequence[]
						{currentInst.seqVolume, currentInst.seqArpeggio, currentInst.seqPitch};
				for (int i = 0; i < seqs.length; i++) {
					FtmSequence s = seqs[i];
					if (s != null) {
						seq.setupSequence(seqs[i]);
					} else {
						seq.clearSequence(FtmSequenceType.get(i));
					}
				}
				
				modSpeed = currentInst.modulationSpeed;
				modDepth = currentInst.modulationDepth;
				modDelay = currentInst.modulationDelay;
				resetMod = true;
			}
		}
		
		seq.update();
		
		// 回写
		calculateVolume();
		calculatePeriod();
	}
	
	/**
	 * 关闭 {@link #sound}, 不让其发出声音
	 */
	private void haltSound() {
		// $4090 0x00
		sound.setEnable(false);
		// $4080 0x80
		sound.wavEnvDisable = true;
		sound.wavEnvMode = false;
		sound.masterEnvSpeed = 0;
		sound.resetWavCounter();
		// $4083 0x80
		sound.wavHalt = true;
		sound.envHalt = false;
		// $408A 0xFF
		sound.masterEnvSpeed = 0xFF;
		// $4087 0x80
		sound.modHalt = true;
	}
	
	/**
	 * 计算音量, 将序列所得出的音量合并计算, 最后将音量限定在 [0, 480] 范围内.
	 * <br>masterVolume 范围 [0, 15]
	 * <br>seq.volume 范围 [0, 31]
	 */
	protected void calculateVolume() {
		int volume = masterVolume * 16 + curVolume; // 精度 240
		if (volume <= 0) {
			curVolume = 0;
			return;
		}
		
		volume = (seq.volume * volume) / 15; // 最大值可以达到 496
		
		if (volume > 480) {
			curVolume = 480;
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
	 * 根据音键查询波长值.
	 * 工具方法
	 */
	public int periodTable(int note) {
		return NoteLookupTable.fds(note);
	}
	
	/* **********
	 *  发声器  *
	 ********** */
	
	public final SoundFDS sound = new SoundFDS();

	@Override
	public SoundFDS getSound() {
		return sound;
	}
	
	@Override
	public void writeToSound() {
		if (!playing || masterNote == 0) {
			sound.setEnable(false);
			sound.wavEnvSpeed = 0;
			return;
		}
		
		if (instrumentUpdated && this.currentInst != null) {
			// 写入 wave 包络数据
			// 原本在 NSF 运行时, 还需要打开是否可写的标识, 然后再往里面写数据
			// 因为这里是用 Sound 直接写入, 所以就省去了这个步骤
			
			System.arraycopy(currentInst.samples, 0, sound.wave, 0, 64);
			sound.wavWrite = false;
			
			// 写入 mods
			sound.modHalt = true;
			sound.modPos = 0;
			for (int i = 0; i < currentInst.modulation.length; ++i)
				sound.writeMods(currentInst.modulation[i]);
		}
		
		/*
		 * ChannelHandlerFDS.refreshChannel()
		 */
		
		// 写入频率值 (波长相关)
		sound.wavFreq = this.curPeriod;
		sound.wavHalt = false;
		sound.envHalt = false;
		
		// 写入音量
		sound.wavEnvDisable = true;
		sound.wavEnvMode = false;
		sound.setEnable(true);
		sound.wavEnvSpeed = this.curVolume / 16;
		
		if (resetMod)
			sound.modPos = 0;
		resetMod = false;
		
		if (modDelay == 0) {
			sound.modHalt = false;
			sound.modFreq = this.modSpeed;
			
			sound.modEnvDisable = true;
			sound.modEnvMode = false;
			sound.modEnvSpeed = this.modDepth;
		} else {
			sound.modHalt = true;
			this.modDelay--;
		}
	}
	
}
