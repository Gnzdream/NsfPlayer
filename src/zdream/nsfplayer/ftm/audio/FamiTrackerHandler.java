package zdream.nsfplayer.ftm.audio;

import java.util.ArrayList;

import zdream.nsfplayer.core.FtmChipType;
import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.ftm.factory.FtmParseException;
import zdream.nsfplayer.ftm.format.AbstractFtmInstrument;
import zdream.nsfplayer.ftm.format.FtmDPCMSample;
import zdream.nsfplayer.ftm.format.FtmNote;
import zdream.nsfplayer.ftm.format.FtmPattern;
import zdream.nsfplayer.ftm.format.FtmSequence;
import zdream.nsfplayer.ftm.format.FtmSequenceType;
import zdream.nsfplayer.ftm.format.FtmTrack;

/**
 * FamiTracker 的文本的操作器.
 * 每一个 {@link FtmAudio} 都有且仅有一个唯一的 FamiTrackerHandler.
 * @author Zdream
 * @date 2018-04-25
 */
public class FamiTrackerHandler implements INsfChannelCode {
	
	public final FtmAudio audio;
	
	public FamiTrackerHandler(FtmAudio audio) {
		this.audio = audio;
	}
	
	/* **********
	 * 参数设置 *
	 ********** */
	
	/**
	 * 设置制式.
	 * @param m
	 */
	public void setMechine(byte m) {
		if (m != FtmAudio.MACHINE_NTSC && m != FtmAudio.MACHINE_PAL) {
			throw new FtmParseException("制式解析错误: " + m);
		}
		audio.machine = m;
	}
	
	/**
	 * 设置引擎刷新率, 默认 0.
	 * 当设置为 0 时, <b>运行时</b>系统根据制式判断刷新率, NTSC 为 60, PAL 为 50.
	 * @param fps
	 * 有效值 [0, 800]
	 */
	public void setFramerate(int fps) {
		if (fps < 0 || fps > 800) {
			throw new FtmParseException("刷新率数据错误: " + fps);
		}
		audio.frameRate = fps;
	}
	
	/**
	 * 设置芯片号码
	 * @param c
	 */
	public void setChip(byte c) {
		audio.useVcr6 = (c & 1) > 0;
		audio.useVcr7 = (c & 2) > 0;
		audio.useFds = (c & 4) > 0;
		audio.useMmc5 = (c & 8) > 0;
		audio.useN163 = (c & 16) > 0;
		audio.useS5b = (c & 32) > 0;
		
		channelDirt = true;
	}
	
	public void setVibrato(byte vibrato) {
		if (vibrato != 0 && vibrato != 1) {
			throw new FtmParseException("振动模式错误: " + vibrato);
		}
		audio.vibrato = vibrato;
	}

	/**
	 * 设置 N163 轨道数
	 * @param count
	 *   有效值 [0, 6]
	 */
	public void setNamcoChannels(int count) {
		if (count < 0 || count > 6) {
			throw new FtmParseException("N163 轨道数错误: " + count);
		}
		audio.namcoChannels = count;
		
		channelDirt = true;
	}

	/**
	 * 设置节奏与速度的分割值 {@link FtmAudio#split}
	 * @param split
	 */
	public void setSplit(int split) {
		audio.split = split;
	}

	/**
	 * 设置默认的节奏与速度的分割值 {@link FtmAudio#split}.
	 * @see FtmAudio#DEFAULT_SPEED_SPLIT
	 */
	public void setDefaultSplit() {
		setSplit(FtmAudio.DEFAULT_SPEED_SPLIT);
	}
	
	/* **********
	 *   曲目   *
	 ********** */
	
	/**
	 * 新建一个新的 track.
	 * @return 
	 *   新建的 track.
	 */
	public FtmTrack createTrack() {
		ArrayList<FtmTrack> tracks = audio.tracks;
		
		FtmTrack track = new FtmTrack();
		tracks.add(track);
		
		return track;
	}
	
	/**
	 * 获取指定索引的 track. 如果没有, 创建一个.
	 * @param index
	 * @return
	 */
	public FtmTrack getOrCreateTrack(final int index) {
		ArrayList<FtmTrack> tracks = audio.tracks;
		
		if (index <= tracks.size()) {
			FtmTrack track = tracks.get(index);
			if (track == null) {
				track = new FtmTrack();
				tracks.set(index, track);
			}
			return track;
		} else {
			FtmTrack track = new FtmTrack();
			registerT(tracks, track, index);
			return track;
		}
	}
	
	/**
	 * 获取指定索引的 pattern. 如果没有, 创建一个.
	 * @param trackIdx
	 *   曲目号
	 * @param patternIdx
	 *   pattern 序号, 即 order 值, {@link FtmTrack#orders} 里面存放的数据
	 * @param channelIdx
	 *   轨道序号
	 * @return
	 */
	public FtmPattern getOrCreatePattern(final int trackIdx, final int patternIdx, final int channelIdx) {
		if (channelIdx < 0 || channelIdx >= this.channelCount()) {
			throw new ArrayIndexOutOfBoundsException(String.format("channelIdx: %d 超出范围 [0, %d)",
					channelIdx, this.channelCount()));
		}
		
		FtmTrack track = getOrCreateTrack(trackIdx);
		return getOrCreatePattern(track, channelIdx, patternIdx);
	}

	/**
	 * 获取指定索引的 pattern. 如果没有, 创建一个.
	 * @param track
	 *   曲目实例
	 * @param patternIdx
	 *   pattern 序号, 即 order 值, {@link FtmTrack#orders} 里面存放的数据
	 * @param channelIdx
	 *   轨道序号
	 * @return
	 */
	public FtmPattern getOrCreatePattern(final FtmTrack track, final int patternIdx, final int channelIdx) {
		if (track.patterns == null) {
			int newLen = Math.max(track.orders.length, patternIdx + 1);
			track.patterns = new FtmPattern[newLen][channelCount()];
		} else if (patternIdx >= track.patterns.length) {
			// 扩大容量
			int newLen = patternIdx + 1;
			FtmPattern[][] oldps = track.patterns;
			track.patterns = new FtmPattern[newLen][];
			
			System.arraycopy(oldps, 0, track.patterns, 0, oldps.length);
			for (int i = oldps.length; i < newLen; i++) {
				track.patterns[i] = new FtmPattern[channelCount()];
			}
		}
		
		FtmPattern pattern = track.patterns[patternIdx][channelIdx];
		
		if (pattern == null) {
			track.patterns[patternIdx][channelIdx] = pattern = new FtmPattern();
		}
		return pattern;
	}
	
	/**
	 * 获取指定索引的 note. 如果没有, 创建一个.
	 * @param trackIdx
	 *   曲目号
	 * @param patternIdx
	 *   pattern 序号, 即 order 值, {@link FtmTrack#orders} 里面存放的数据
	 * @param channelIdx
	 *   轨道序号
	 * @param row
	 *   行号
	 * @return
	 */
	public FtmNote getOrCreateNote(
			final int trackIdx,
			final int patternIdx,  
			final int channelIdx, 
			final int row) {
		return getOrCreateNote(getOrCreateTrack(trackIdx), channelIdx, patternIdx, row);
	}
	
	/**
	 * 获取指定索引的 note. 如果没有, 创建一个.
	 * @param track
	 *   曲目实例
	 * @param patternIdx
	 *   pattern 序号, 即 order 值, {@link FtmTrack#orders} 里面存放的数据
	 * @param channelIdx
	 *   轨道序号
	 * @param row
	 *   行号
	 * @return
	 */
	public FtmNote getOrCreateNote(
			final FtmTrack track,
			final int patternIdx,
			final int channelIdx,
			final int row) {
		FtmPattern pattern = getOrCreatePattern(track, channelIdx, patternIdx);

		return getOrCreateNote(pattern, row, track.length);
	}
	
	private static FtmNote getOrCreateNote(final FtmPattern pattern, final int row, final int rowMax) {
		FtmNote[] notes = pattern.notes;
		if (pattern.notes == null) {
			notes = pattern.notes = new FtmNote[rowMax];
		}
		
		if (notes[row] == null) {
			notes[row] = new FtmNote();
		}
		return notes[row];
	}
	
	/* **********
	 *   轨道   *
	 ********** */
	
	/**
	 * <p>是否修改过轨道数相关的数据.
	 * <p>如果修改过, 需要重新计算相关的数据.
	 */
	boolean channelDirt = true;
	
	/**
	 * 缓存轨道总数
	 */
	int channelCount = 0;
	
	/**
	 * 缓存第 n 个轨道对应的轨道号是什么
	 */
	byte[] channelCode;
	
	/**
	 * 重新计算轨道音源相关的数据.
	 */
	private void reScanChannel() {
		// 计算轨道总数
		channelCount = 5; // 2A03
		if (audio.useVcr6) {
			channelCount += 3;
		}
		if (audio.useVcr7) {
			channelCount += 6;
		}
		if (audio.useFds) {
			channelCount += 1;
		}
		if (audio.useMmc5) {
			channelCount += 2;
		}
		if (audio.useN163) {
			channelCount += audio.namcoChannels;
		}
		/*if (audio.useS5b) { // 忽略
			count += 3;
		}*/
		
		// 补充轨道号
		channelCode = new byte[channelCount];
		int codePtr = 0;
		channelCode[codePtr++] = CHANNEL_2A03_PULSE1;
		channelCode[codePtr++] = CHANNEL_2A03_PULSE2;
		channelCode[codePtr++] = CHANNEL_2A03_TRIANGLE;
		channelCode[codePtr++] = CHANNEL_2A03_NOISE;
		channelCode[codePtr++] = CHANNEL_2A03_DPCM;
		
		if (audio.useVcr6) {
			channelCode[codePtr++] = CHANNEL_VRC6_PULSE1;
			channelCode[codePtr++] = CHANNEL_VRC6_PULSE2;
			channelCode[codePtr++] = CHANNEL_VRC6_SAWTOOTH;
		}
		if (audio.useVcr7) {
			channelCode[codePtr++] = CHANNEL_VRC7_FM1;
			channelCode[codePtr++] = CHANNEL_VRC7_FM2;
			channelCode[codePtr++] = CHANNEL_VRC7_FM3;
			channelCode[codePtr++] = CHANNEL_VRC7_FM4;
			channelCode[codePtr++] = CHANNEL_VRC7_FM5;
			channelCode[codePtr++] = CHANNEL_VRC7_FM6;
		}
		if (audio.useFds) {
			channelCode[codePtr++] = CHANNEL_FDS;
		}
		if (audio.useMmc5) {
			channelCode[codePtr++] = CHANNEL_MMC5_PULSE1;
			channelCode[codePtr++] = CHANNEL_MMC5_PULSE2;
		}
		if (audio.useN163) {
			byte[] cs = new byte[] {
					CHANNEL_N163_1,
					CHANNEL_N163_2,
					CHANNEL_N163_3,
					CHANNEL_N163_4,
					CHANNEL_N163_5,
					CHANNEL_N163_6
			};
			int length = audio.namcoChannels;
			for (int i = 0; i < length; i++) {
				channelCode[codePtr++] = cs[i];
			}
		}
		
		// 结束
		channelDirt = false;
	}
	
	/**
	 * 计算轨道数总和
	 * @return
	 */
	public int channelCount() {
		if (channelDirt) {
			reScanChannel();
		}
		
		return channelCount;
	}
	
	/**
	 * 查看第 {@code channel} 个轨道的轨道号
	 * @return
	 */
	public byte channelCode(int channel) {
		if (channelDirt) {
			reScanChannel();
		}
		
		return channelCode[channel];
	}
	
	/**
	 * <p>设置总曲目数.
	 * <p>如果 {@code size > 原曲目数} : 添加空白曲目, 直到曲目数为 size;
	 * <br>如果 {@code size <= 原曲目数} : 不做操作;
	 */
	public void allocateTrack(int size) {
		int i = size - audio.getTrackCount();
		if (i > 0) {
			for (; i > 0; i--) {
				audio.tracks.add(new FtmTrack());
			}
		}
	}
	
	/* **********
	 * 乐器序列  *
	 ********** */
	
	/**
	 * 获得 2A03 序列数据
	 */
	public FtmSequence getSequence2A03(FtmSequenceType type, int index) {
		int key = FtmChipType._2A03.ordinal() * FtmSequenceType.values().length + type.ordinal();
		ArrayList<FtmSequence> list = audio.seqs.get(key);
		if (list == null) {
			return null;
		}
		
		if (index >= list.size()) {
			return null;
		}
		return list.get(index);
	}
	
	/**
	 * 获得 VRC6 序列数据
	 */
	public FtmSequence getSequenceVRC6(FtmSequenceType type, int index) {
		int key = FtmChipType.VRC6.ordinal() * FtmSequenceType.values().length + type.ordinal();
		ArrayList<FtmSequence> list = audio.seqs.get(key);
		if (list == null) {
			return null;
		}
		
		if (index >= list.size()) {
			return null;
		}
		return list.get(index);
	}
	
	/**
	 * 获得 2A03 序列数据. 如果没有, 就创建一个
	 */
	public FtmSequence getOrCreateSequence2A03(FtmSequenceType type, int index) {
		int key = FtmChipType._2A03.ordinal() * FtmSequenceType.values().length + type.ordinal();
		ArrayList<FtmSequence> list = audio.seqs.get(key);
		if (list == null) {
			list = new ArrayList<>();
			audio.seqs.put(key, list);
		}
		
		FtmSequence seq = null;
		if (index < list.size()) {
			seq = list.get(index);
		}
		
		if (seq == null) {
			seq = new FtmSequence(type);
			registerT(list, seq, index);
		}
		return seq;
	}
	
	/**
	 * 获得 VRC6 序列数据. 如果没有, 就创建一个
	 */
	public FtmSequence getOrCreateSequenceVRC6(FtmSequenceType type, int index) {
		int key = FtmChipType.VRC6.ordinal() * FtmSequenceType.values().length + type.ordinal();
		ArrayList<FtmSequence> list = audio.seqs.get(key);
		if (list == null) {
			list = new ArrayList<>();
			audio.seqs.put(key, list);
		}
		
		FtmSequence seq = null;
		if (index < list.size()) {
			seq = list.get(index);
		}
		
		if (seq == null) {
			seq = new FtmSequence(type);
			registerT(list, seq, index);
		}
		return seq;
	}
	
	/**
	 * 注册乐器
	 */
	public void registerInstrument(AbstractFtmInstrument inst) {
		int index = inst.seq;
		ArrayList<AbstractFtmInstrument> list = audio.insts;
		registerT(list, inst, index);
	}
	
	/**
	 * 注册序列
	 */
	public FtmSequence getOrCreateSequence(FtmChipType chip, FtmSequenceType type, int index) {
		FtmSequence seq = new FtmSequence(type);
		
		int key = chip.ordinal() * FtmSequenceType.values().length + seq.type.ordinal();
		ArrayList<FtmSequence> list = audio.seqs.get(key);
		if (list == null) {
			list = new ArrayList<>();
			audio.seqs.put(key, list);
		}
		
		registerT(list, seq, index);
		return seq;
	}

	/**
	 * 注册采样
	 * @param sample
	 * @return 
	 */
	public FtmDPCMSample getOrCreateDPCMSample(int index) {
		ArrayList<FtmDPCMSample> list = audio.samples;
		FtmDPCMSample sample = null;
		
		if (list.size() > index) {
			sample = list.get(index);
		}
		
		if (sample == null) {
			sample = new FtmDPCMSample();
			registerT(list, sample, index);
		}
		
		return sample;
	}
	
	private <T> void registerT(ArrayList<T> list, T t, int index) {
		int size = list.size();
		int d = index - size;
		if (d < 0) {
			list.set(index, t);
			return;
		}

		// 将序列摆到 index 指定的位置, 中间填充 null
		while (d > 0) {
			list.add(null);
			d--;
		}
		list.add(t);
	}

}
