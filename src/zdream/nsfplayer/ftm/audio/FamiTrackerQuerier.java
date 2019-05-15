package zdream.nsfplayer.ftm.audio;

import static zdream.nsfplayer.core.FtmChipType.*;
import static zdream.nsfplayer.ftm.format.FtmSequenceType.*;

import zdream.nsfplayer.core.FtmChipType;
import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.ftm.format.AbstractFtmInstrument;
import zdream.nsfplayer.ftm.format.FtmInstrument2A03;
import zdream.nsfplayer.ftm.format.FtmInstrumentFDS;
import zdream.nsfplayer.ftm.format.FtmInstrumentN163;
import zdream.nsfplayer.ftm.format.FtmInstrumentVRC6;
import zdream.nsfplayer.ftm.format.FtmInstrumentVRC7;
import zdream.nsfplayer.ftm.format.FtmNote;
import zdream.nsfplayer.ftm.format.FtmPattern;
import zdream.nsfplayer.ftm.format.FtmSequence;
import zdream.nsfplayer.ftm.format.FtmTrack;

/**
 * FamiTracker 的文本数据的查询器.
 * @author Zdream
 * @date 2018-06-09
 * @since 0.2.1
 */
public class FamiTrackerQuerier implements INsfChannelCode {
	
	/**
	 * 原 Ftm 音频数据
	 */
	public final FtmAudio audio;

	/**
	 * @param audio
	 *   原 Ftm 音频数据
	 * @throws NullPointerException
	 *   当 audio = null 时
	 */
	public FamiTrackerQuerier(FtmAudio audio) {
		if (audio == null) {
			throw new NullPointerException("FTM 音频数据为 null");
		}
		this.audio = audio;
		this.frameRate = audio.getFrameRate();
	}
	
	/* **********
	 *   参数   *
	 ********** */
	int frameRate;
	
	public int getFrameRate() {
		return frameRate;
	}
	
	/* **********
	 *   轨道   *
	 ********** */
	
	/**
	 * 计算轨道数总和
	 * @return
	 */
	public int channelCount() {
		return audio.handler.channelCount();
	}
	
	/**
	 * 查看第 {@code channel} 个轨道的轨道号
	 * @return
	 */
	public byte channelCode(int channel) {
		return audio.handler.channelCode(channel);
	}
	
	/* **********
	 *   乐器   *
	 ********** */
	
	/**
	 * 返回乐器. 如果指定位置的乐器为空, 返回 null
	 * @param instrument
	 *   乐器序号
	 * @return
	 * @since v0.2.2
	 */
	public AbstractFtmInstrument getInstrument(int instrument) {
		if (instrument >= audio.instrumentCount()) {
			return null;
		}
		return audio.getInstrument(instrument);
	}
	
	/**
	 * 返回乐器所属的芯片状态. 如果指定位置的乐器为空, 返回 null
	 * @param instrument
	 *   乐器序号
	 * @return
	 * @since v0.2.5
	 */
	public FtmChipType getInstrumentType(int instrument) {
		AbstractFtmInstrument inst = getInstrument(instrument);
		if (inst != null) {
			return inst.instType();
		}
		return null;
	}
	
	/**
	 * 返回 2A03 乐器. 如果指定位置的乐器为空, 或者不是 2A03 类型的, 返回 null
	 * @param instrument
	 *   乐器号码
	 * @return
	 * @since v0.2.2
	 */
	public FtmInstrument2A03 get2A03Instrument(int instrument) {
		AbstractFtmInstrument inst = getInstrument(instrument);
		if (inst == null) {
			return null;
		}
		if (inst.instType() != _2A03) {
			return null;
		}
		return (FtmInstrument2A03) inst;
	}
	
	/**
	 * 返回 FDS 乐器. 如果指定位置的乐器为空, 或者不是 FDS 类型的, 返回 null
	 * @param instrument
	 *   乐器号码
	 * @return
	 * @since v0.2.4
	 */
	public FtmInstrumentFDS getFDSInstrument(int instrument) {
		AbstractFtmInstrument inst = getInstrument(instrument);
		if (inst == null) {
			return null;
		}
		if (inst.instType() != FDS) {
			return null;
		}
		return (FtmInstrumentFDS) inst;
	}
	
	/**
	 * 返回 N163 乐器. 如果指定位置的乐器为空, 或者不是 N163 类型的, 返回 null
	 * @param instrument
	 *   乐器号码
	 * @return
	 * @since v0.2.6
	 */
	public FtmInstrumentN163 getN163Instrument(int instrument) {
		AbstractFtmInstrument inst = getInstrument(instrument);
		if (inst == null) {
			return null;
		}
		if (inst.instType() != N163) {
			return null;
		}
		return (FtmInstrumentN163) inst;
	}
	
	/**
	 * 返回 N163 乐器. 如果指定位置的乐器为空, 或者不是 N163 类型的, 返回 null
	 * @param instrument
	 *   乐器号码
	 * @return
	 * @since v0.2.6
	 */
	public FtmInstrumentVRC7 getVRC7Instrument(int instrument) {
		AbstractFtmInstrument inst = getInstrument(instrument);
		if (inst == null) {
			return null;
		}
		if (inst.instType() != VRC7) {
			return null;
		}
		return (FtmInstrumentVRC7) inst;
	}
	
	public FtmSequence[] getSequences(int instrument) {
		AbstractFtmInstrument inst = getInstrument(instrument);
		if (inst == null) {
			return new FtmSequence[5];
		}
		
		switch (inst.instType()) {
		case _2A03: {
			FtmInstrument2A03 i2 = (FtmInstrument2A03) inst;
			return new FtmSequence[] {
					i2.vol == -1 ? null : audio.getSequence(_2A03, VOLUME, i2.vol),
					i2.arp == -1 ? null : audio.getSequence(_2A03, ARPEGGIO, i2.arp),
					i2.pit == -1 ? null : audio.getSequence(_2A03, PITCH, i2.pit),
					i2.hip == -1 ? null : audio.getSequence(_2A03, HI_PITCH, i2.hip),
					i2.dut == -1 ? null : audio.getSequence(_2A03, DUTY, i2.dut),
			};
		}
		
		case VRC6: {
			FtmInstrumentVRC6 i2 = (FtmInstrumentVRC6) inst;
			return new FtmSequence[] {
					i2.vol == -1 ? null : audio.getSequence(VRC6, VOLUME, i2.vol),
					i2.arp == -1 ? null : audio.getSequence(VRC6, ARPEGGIO, i2.arp),
					i2.pit == -1 ? null : audio.getSequence(VRC6, PITCH, i2.pit),
					i2.hip == -1 ? null : audio.getSequence(VRC6, HI_PITCH, i2.hip),
					i2.dut == -1 ? null : audio.getSequence(VRC6, DUTY, i2.dut),
			};
		}
		
		case FDS: {
			FtmInstrumentFDS i2 = (FtmInstrumentFDS) inst;
			return new FtmSequence[] {i2.seqVolume, i2.seqArpeggio, i2.seqPitch};
		}
		
		case N163: {
			FtmInstrumentN163 i2 = (FtmInstrumentN163) inst;
			return new FtmSequence[] {
					i2.vol == -1 ? null : audio.getSequence(N163, VOLUME, i2.vol),
					i2.arp == -1 ? null : audio.getSequence(N163, ARPEGGIO, i2.arp),
					i2.pit == -1 ? null : audio.getSequence(N163, PITCH, i2.pit),
					i2.hip == -1 ? null : audio.getSequence(N163, HI_PITCH, i2.hip),
					i2.dut == -1 ? null : audio.getSequence(N163, DUTY, i2.dut),
			};
		}

		default:
			break;
		}
		
		return new FtmSequence[5];
	}
	
	/* **********
	 *  段  键  *
	 ********** */
	/*
	 * section, note
	 */
	
	/**
	 * 确定指定曲目的段数
	 * @param track
	 *   曲目号
	 */
	public int trackCount(int track) {
		return audio.getTrack(track).orders.length;
	}
	
	/**
	 * 确定指定曲目一段的最大行数
	 * @param track
	 *   曲目号
	 */
	public int maxRow(int track) {
		return audio.getTrack(track).length;
	}
	
	/**
	 * 获取键数据
	 * @param track
	 *   曲目号
	 * @param section
	 *   段号
	 * @param channel
	 *   轨道号, 从 0 开始
	 * @param row
	 *   行号
	 * @return
	 */
	public FtmNote getNote(int track, int section, int channel, int row) {
		FtmTrack t = audio.getTrack(track);
		int order = t.orders[section][channel];
		if (order >= t.patterns.length) {
			return null;
		}
		FtmPattern p = t.patterns[order][channel];
		if (p == null) {
			return null;
		}
		return p.notes[row];
	}

}
