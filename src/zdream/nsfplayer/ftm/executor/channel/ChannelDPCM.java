package zdream.nsfplayer.ftm.executor.channel;

import zdream.nsfplayer.ftm.executor.AbstractFtmChannel;
import zdream.nsfplayer.ftm.format.FtmDPCMSample;
import zdream.nsfplayer.ftm.format.FtmInstrument2A03;
import zdream.nsfplayer.sound.SoundDPCM;

/**
 * <p>DPCM 轨道
 * 
 * <p>该轨道的 instrument, instrumentUpdated,
 * masterNote, masterPitch, curPeriod 五项参数仍然可以使用.
 * <li>masterPitch 默认值 -1, Wxx 效果, 每帧开始时重置. 允许范围 [0, 15]
 * </li>
 * </p>
 * 
 * @author Zdream
 * @since v0.2.2
 */
public class ChannelDPCM extends AbstractFtmChannel {

	public ChannelDPCM() {
		super(CHANNEL_2A03_DPCM);
	}

	@Override
	public void playNote() {
		super.playNote();
		
		/*
		 * 向外部宣布, 这个发声器是否在工作. 没有实际作用.
		 * 因为外部想知道是否在发声, 只能通过 isplaying 和音量两个方式来感知
		 * 这里修改 curVolume 只是为了让外界了解情况, 便于 debug.
		 */
		if (!sound.isFinish()) {
			curVolume = 1;
		}
	}
	
	/* **********
	 * DMA 采样 *
	 ********** */
	
	/**
	 * 采样数据
	 */
	FtmDPCMSample sample;
	
	/**
	 * m_cDAC
	 */
	private int deltaCounter = -1;
	private boolean loop;
	
	/**
	 * 读取位置的起始位, 单位 1 byte
	 */
	private int offset;
	
	/**
	 * 单位: 1 bytes
	 */
	private int sampleLength;
	
	/**
	 * 单位: 1 bytes
	 */
	private int loopOffset;
	private int loopLength;
	
	/**
	 * 标志, 是否应该重新加载 sample 以及相关数据.
	 * 当音键、乐器修改时, 那一帧的 needReload 为 true
	 */
	private boolean needReload;
	
	/**
	 * Xxx 效果暂存参数
	 */
	private int retrigger, retriggerCtrl;
	private boolean needTrigger;
	
//	private int customPitch; // 转 masterPitch
	
	/**
	 * 设置原始的 DAC 值. Zxx 效果触发
	 * @param deltaCounter
	 */
	public void setDeltaCounter(int deltaCounter) {
		this.deltaCounter = deltaCounter & 0x7F;
	}
	
	/**
	 * 设置采样的起始读取位. Yxx 效果触发.
	 * 该值使用一次后会清零.
	 * @param offset
	 */
	public void setOffset(int offset) {
		this.offset = offset;
	}
	
	/**
	 * 设置循环的时长. 调用该方法一次后循环只触发一次, Xxx 效果触发.
	 * @param retrigger
	 *   retrigger + 1 表示真实记录的时长, 单位: 帧
	 */
	public void setRetrigger(int retrigger) {
		this.retrigger = retrigger + 1;
		if (retriggerCtrl == 0)
			retriggerCtrl = this.retrigger;
	}
	
	@Override
	public void reset() {
		deltaCounter = -1;
		loop = false;
		offset = 0;
		
		super.reset();
		
		// 额外需要发声器也重置
		sound.reset();
	}
	
	@Override
	protected void startFrame() {
		masterPitch = -1;
		retrigger = 0;
		offset = 0; // 补充的
		
		super.startFrame();
	}
	
	@Override
	public void doHalt() {
		super.doHalt();
		sound.sample = null;
	}
	
	@Override
	public void doRelease() {
		this.doHalt();
	}
	
	@Override
	public void setMasterNote(int note) {
		needReload = true;
		super.setMasterNote(note);
	}
	
	@Override
	public void setInstrument(int instrument) {
		needReload = true;
		super.setInstrument(instrument);
	}
	
	public void reload() {
		FtmInstrument2A03 inst = getRuntime().querier.get2A03Instrument(instrument);
		if (inst == null || masterNote == 0) {
			return;
		}
		
		FtmDPCMSample sample = inst.getSample(masterNote);
		if (sample != null) {
			int pitch = inst.getSamplePitch(masterNote);
			this.loop = (pitch & 0x80) != 0;
			
			if (masterPitch != -1) {
				// 如果有 Wxx 效果, 以该效果为准
				pitch = masterPitch;
			}
			
			// 检查发现, 原程序对 SampleLoopOffset 也没有设置.
			// loopOffset = inst.getSampleLoopOffset(masterNote);
			this.loopOffset = 0;
		
			int sampleSize = sample.size(); // byte 总数

			if (sampleSize > 0) {
				this.sample = sample;
				this.curPeriod = pitch & 0xF;
				this.sampleLength = sampleSize - this.offset;
				this.loopLength = sampleSize - this.loopOffset;
				this.needTrigger = true;

				// Initial delta counter value
				int delta = inst.getSampleDelta(masterNote);
				
				if (delta != -1 && deltaCounter == -1)
					deltaCounter = delta & 0xFF;

				retriggerCtrl = retrigger;
			}
		} else {
			playing = false;
		}
	}
	
	/* **********
	 *  发声器  *
	 ********** */
	
	SoundDPCM sound = new SoundDPCM();

	@Override
	public SoundDPCM getSound() {
		return sound;
	}

	@Override
	public void writeToSound() {
		if (deltaCounter != -1) {
			sound.deltaCounter = deltaCounter;
			deltaCounter = -1;
		}

		// Xxx 效果
		if (retrigger != 0) {
			retriggerCtrl--;
			if (retriggerCtrl == 0) {
				retriggerCtrl = retrigger;
				needTrigger = true;
			}
		}

		if (needReload) {
			this.reload();
			needReload = false;
		}

		if (!playing) {
			return;
		}
		
		if (needTrigger) {
			// Start playing the sample
			sound.loop = loop;
			sound.periodIndex = curPeriod;
			sound.offsetAddress = offset;
			sound.length = this.sampleLength;
			sound.sample = this.sample;
			
			// Loop offset
			if (loopOffset > 0) {
				sound.offsetAddress = loopOffset;
				sound.length = this.loopLength;
			}
			
			sound.reload();

			needTrigger = false;
		}
	}
	
}
