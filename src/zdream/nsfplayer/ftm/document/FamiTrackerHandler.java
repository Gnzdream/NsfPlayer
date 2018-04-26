package zdream.nsfplayer.ftm.document;

import java.util.ArrayList;

import zdream.nsfplayer.ftm.document.format.AbstractFtmInstrument;
import zdream.nsfplayer.ftm.document.format.FtmChipType;
import zdream.nsfplayer.ftm.document.format.FtmDPCMSample;
import zdream.nsfplayer.ftm.document.format.FtmSequence;
import zdream.nsfplayer.ftm.document.format.FtmSequenceType;
import zdream.nsfplayer.ftm.document.format.FtmTrack;

/**
 * FamiTracker 的文本的操作器.
 * 每一个 {@link FtmAudio} 都有且仅有一个唯一的 FamiTrackerHandler.
 * @author Zdream
 * @date 2018-04-25
 */
public class FamiTrackerHandler {
	
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
		audio.framerate = fps;
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
		channelCode[codePtr++] = FtmAudio.CHANNEL_2A03_PULSE1;
		channelCode[codePtr++] = FtmAudio.CHANNEL_2A03_PULSE2;
		channelCode[codePtr++] = FtmAudio.CHANNEL_2A03_TRIANGLE;
		channelCode[codePtr++] = FtmAudio.CHANNEL_2A03_NOISE;
		channelCode[codePtr++] = FtmAudio.CHANNEL_2A03_DPCM;
		
		if (audio.useVcr6) {
			channelCode[codePtr++] = FtmAudio.CHANNEL_VRC6_PULSE1;
			channelCode[codePtr++] = FtmAudio.CHANNEL_VRC6_PULSE2;
			channelCode[codePtr++] = FtmAudio.CHANNEL_VRC6_SAWTOOTH;
		}
		if (audio.useVcr7) {
			channelCode[codePtr++] = FtmAudio.CHANNEL_VRC7_FM1;
			channelCode[codePtr++] = FtmAudio.CHANNEL_VRC7_FM2;
			channelCode[codePtr++] = FtmAudio.CHANNEL_VRC7_FM3;
			channelCode[codePtr++] = FtmAudio.CHANNEL_VRC7_FM4;
			channelCode[codePtr++] = FtmAudio.CHANNEL_VRC7_FM5;
			channelCode[codePtr++] = FtmAudio.CHANNEL_VRC7_FM6;
		}
		if (audio.useFds) {
			channelCode[codePtr++] = FtmAudio.CHANNEL_FDS;
		}
		if (audio.useMmc5) {
			channelCode[codePtr++] = FtmAudio.CHANNEL_MMC1_PULSE1;
			channelCode[codePtr++] = FtmAudio.CHANNEL_MMC1_PULSE2;
		}
		if (audio.useN163) {
			byte[] cs = new byte[] {
					FtmAudio.CHANNEL_N163_1,
					FtmAudio.CHANNEL_N163_2,
					FtmAudio.CHANNEL_N163_3,
					FtmAudio.CHANNEL_N163_4,
					FtmAudio.CHANNEL_N163_5,
					FtmAudio.CHANNEL_N163_6
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
	public byte channelCount(int channel) {
		if (channelDirt) {
			reScanChannel();
		}
		
		return channelCode[channel];
	}
	
	/**
	 * 设置第 {@code channel} 个轨道的效果列数
	 * @param track
	 * @param channelNo
	 *   轨道的序号 (不是轨道号)
	 * @param column
	 *   原效果列数 - 1. (0 代表 1 列)
	 *   有效值 [0, 3]
	 */
	public void setEffectColumn(int track, int channelNo, int column) {
		if (column < 0 || column > 3) {
			throw new FtmParseException("效果列数错误: " + column);
		}
		byte channelCode = this.channelCode[channelNo];
		audio.getTrack(track).channelEffCount.put(channelCode, column);
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
	 * 获得序列数据
	 */
	public FtmSequence getSequence2A03(FtmSequenceType type, int index) {
		int key = FtmChipType._2A03.ordinal() + FtmSequenceType.values().length + type.ordinal();
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
	 * 获得序列数据. 如果没有, 就创建一个
	 */
	public FtmSequence getOrCreateSequence2A03(FtmSequenceType type, int index) {
		int key = FtmChipType._2A03.ordinal() + FtmSequenceType.values().length + type.ordinal();
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
	public void registerSequence(FtmSequence seq, FtmChipType chip) {
		int key = chip.ordinal() + FtmSequenceType.values().length + seq.type.ordinal();
		ArrayList<FtmSequence> list = audio.seqs.get(key);
		if (list == null) {
			list = new ArrayList<>();
			audio.seqs.put(key, list);
		}
		
		int index = seq.getIndex();
		registerT(list, seq, index);
	}

	/**
	 * 注册采样
	 * @param sample
	 */
	public void registerDPCMSample(FtmDPCMSample sample) {
		int index = sample.index;
		ArrayList<FtmDPCMSample> list = audio.samples;
		registerT(list, sample, index);
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
