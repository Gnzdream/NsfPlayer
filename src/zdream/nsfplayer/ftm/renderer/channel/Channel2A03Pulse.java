package zdream.nsfplayer.ftm.renderer.channel;

import static zdream.nsfplayer.sound.Sound2A03.LENGTH_TABLE;

import zdream.nsfplayer.ftm.format.FtmSequence;
import zdream.nsfplayer.ftm.format.FtmSequenceType;
import zdream.nsfplayer.ftm.renderer.tools.NoteLookupTable;
import zdream.nsfplayer.sound.PulseSound;
import zdream.nsfplayer.sound.SweepSoundPulse;

/**
 * 2A03 矩形轨道 1 / 2
 * 
 * @version v0.2.5
 *   将两个 2A03 矩形轨道进行了合并
 * 
 * @author Zdream
 * @since v0.2.1
 */
public final class Channel2A03Pulse extends ChannelTone {

	/**
	 * @param isPulse1
	 *   如果是 2A03 一号矩形轨道, 为 true
	 *   如果是 2A03 二号矩形轨道, 为 false
	 */
	public Channel2A03Pulse(boolean isPulse1) {
		super(isPulse1 ? CHANNEL_2A03_PULSE1 : CHANNEL_2A03_PULSE2);
		sound = new SweepSoundPulse(isPulse1);
	}

	@Override
	public void playNote() {
		// 每帧开始时重置 sweep 参数
		this.sweepUpdated = false;
		
		super.playNote();
		
		// sequence
		updateSequence();
	}
	
	@Override
	public void reset() {
		sweepPeriod = 0;
		sweepMode = false;
		sweepShift = 0;
		sweepEnable = false;
		this.sweepUpdated = false;
		
		super.reset();
		seq.reset();
		sound.reset();
	}
	
	/* **********
	 *  sweep   *
	 ********** */
	
	/**
	 * <p>范围 [0, 0xFF]
	 * </p>
	 * @since v0.2.9
	 */
	protected int sweepPeriod;
	protected boolean sweepMode;
	protected int sweepShift;
	protected boolean sweepUpdated;
	private boolean sweepEnable;
	
	/**
	 * 设置扫音 sweep 的参数
	 * @param sweepPeriod
	 *   范围 [0, 7]
	 * @param sweepMode
	 *   true 表示音高向上滑, false 表示音高向下滑
	 * @param sweepShift
	 *   范围 [0, 7]
	 * @since v0.2.9
	 */
	public void setSweep(int sweepPeriod, boolean sweepMode, int sweepShift) {
		this.sweepPeriod = sweepPeriod;
		this.sweepMode = sweepMode;
		this.sweepShift = sweepShift;
		this.sweepUpdated = true;
		
		sweepEnable = (sweepShift > 0);
	}
	
	/**
	 * 清除扫音 sweep 的参数
	 * @since v0.2.9
	 */
	public void clearSweep() {
		this.sweepPeriod = 0;
		this.sweepMode = false;
		this.sweepShift = 0;
		this.sweepUpdated = true;
		
		sweepEnable = false;
	}
	
	/**
	 * 查询该轨道是否正在运行 sweep 效果
	 * @return
	 */
	public boolean isSweepEnable() {
		return sweepEnable;
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
	 * 2A03 Pulse 1 音频发声器
	 */
	public final SweepSoundPulse sound;

	@Override
	public PulseSound getSound() {
		return sound;
	}
	
	@Override
	public void writeToSound() {
		sound.envelopeLoop = true;
		sound.envelopeFix = true;
		
		if (this.curVolume == 0 || !playing || masterNote == 0) {
			sound.fixedVolume = 0;
			return;
		}
		
		sound.dutyLength = curDuty;
		sound.fixedVolume = curVolume / 16;
		
		if (sweepEnable) {
			if (sweepUpdated) {
				// 仅 sweep 触发的第一帧, 可以向发声器内写入数据
				// 否则 period 会被轨道重写, 那么 sweep 的效果会消失
				// 0x4001
				sound.sweepEnabled = true;
				sound.sweepPeriod = (sweepPeriod) + 1;
				sound.sweepMode = sweepMode;		
				sound.sweepShift = sweepShift;
				sound.onSweepUpdated();
				
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
			sound.onSweepUpdated();
			
			// 0x4002 and 0x4003
			sound.period = curPeriod;
			sound.lengthCounter = LENGTH_TABLE[0];
		}
		
		sound.onEnvelopeUpdated();
	}
	
	/* **********
	 *   其它   *
	 ********** */
	
	/**
	 * 根据音键查询波长值.
	 * 工具方法
	 */
	public int periodTable(int note) {
		return NoteLookupTable.ntsc(note);
	}
	
}
