package zdream.nsfplayer.ftm.renderer.channel;

import zdream.nsfplayer.ftm.format.FtmDPCMSample;
import zdream.nsfplayer.ftm.format.FtmInstrument2A03;
import zdream.nsfplayer.sound.DPCMSound;

/**
 * <p>DPCM 轨道
 * 
 * <p>该轨道的 instrument, instrumentUpdated,
 * masterNote, masterPitch, curPeriod 四项参数仍然可以使用.
 * <li>masterPitch 默认值 -1, 每帧开始时重置. 允许范围 [0, 15]
 * </li>
 * </p>
 * 
 * @author Zdream
 * @since v0.2.2
 */
public class DPCMChannel extends Channel2A03 {

	public DPCMChannel() {
		super(CHANNEL_2A03_DPCM);
	}

	@Override
	public void playNote() {
		super.playNote();
		
		// 发声器
		writeToSound();
		processSound();
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
	private int offset;
	
	/**
	 * 单位: 16 bytes
	 */
	private int sampleLength;
	
	/**
	 * 单位: 64 bytes
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
	
	public void setDeltaCounter(int deltaCounter) {
		this.deltaCounter = deltaCounter & 0x7F;
	}
	
	@Override
	public void reset() {
		deltaCounter = -1;
		loop = false;
		
		super.reset();
	}
	
	@Override
	protected void startFrame() {
		masterPitch = -1;
		retrigger = 0;
		
		super.startFrame();
	}
	
	@Override
	public void doRelease() {
		this.playing = false;
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
		if (inst == null) {
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
				this.sampleLength = sampleSize / 16 - this.offset * 4;
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
	
	DPCMSound sound = new DPCMSound();

	@Override
	public DPCMSound getSound() {
		return sound;
	}
	
	/**
	 * <p>将轨道中的数据写到发声器中.
	 * <p>参照原工程 DPCMChan.refreshChannel()
	 * </p>
	 */
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
			sound.offsetAddress = offset * 64;
			sound.length = this.sampleLength * 16;
			sound.sample = this.sample;
			
			// Loop offset
			if (loopOffset > 0) {
				sound.offsetAddress = loopOffset * 64;
				sound.length = this.loopLength * 16;
			}
			
			sound.reload();

			needTrigger = false;
		}
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
