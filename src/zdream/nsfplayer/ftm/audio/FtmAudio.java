package zdream.nsfplayer.ftm.audio;

import java.util.ArrayList;
import java.util.HashMap;

import zdream.nsfplayer.core.FtmChipType;
import zdream.nsfplayer.ftm.format.AbstractFtmInstrument;
import zdream.nsfplayer.ftm.format.FtmDPCMSample;
import zdream.nsfplayer.ftm.format.FtmSequence;
import zdream.nsfplayer.ftm.format.FtmSequenceType;
import zdream.nsfplayer.ftm.format.FtmTrack;

public class FtmAudio {
	
	public final FamiTrackerHandler handler;
	
	{
		handler = new FamiTrackerHandler(this);
	}
	
	/**
	 * FamiTracker 导出文本的版本号, 常见的有 0.4.2 0.4.3 ... 0.4.6
	 */
	@Deprecated
	String version;
	
	/**
	 * 标题 TITLE
	 */
	public String title;
	
	/**
	 * 作家 / 创作者 AUTHOR
	 */
	public String author;
	
	/**
	 * 版权 COPYRIGHT
	 */
	public String copyright;
	
	/**
	 * 制式
	 */
	byte machine;
	public static final byte MACHINE_NTSC = 0;
	public static final byte MACHINE_PAL = 1;
	
	/**
	 * fps, 有效值为 [0, 800].<br>
	 * 如果设置为 0, 则为制式指定默认的数值
	 */
	int frameRate;
	
	/**
	 * 各种芯片的使用情况
	 */
	boolean useVcr6, useVcr7, useFds, useMmc5, useN163, useS5b;
	
	/**
	 * 模式 mode<br>
	 * 0 for old style vibrato, 1 for new style (这是 Famitracker 文档的原话)
	 */
	byte vibrato;

	public static final int DEFAULT_SPEED_SPLIT = 21;
	/**
	 * <p>节奏与速度的分割值
	 * <p>split point where Fxx effect sets tempo instead of speed
	 * <br>'Fxx' 这个函数影响音乐播放的速度.
	 * <p>当 xx 代表的数值大于等于 {@code split} 时, 它将解析成 {@link FtmTrack#tempo};
	 * <br>否则解析成 {@link FtmTrack#speed}
	 */
	int split;
	
	/**
	 * <p>N163 的轨道数.
	 * <p>当 {@link #useN163} 为 true 时有效
	 */
	int namcoChannels;
	
	/**
	 * 制式, 包括 {@link #MACHINE_NTSC} 和 {@link #MACHINE_PAL}
	 * @return
	 */
	public byte getMachine() {
		return machine;
	}
	
	/**
	 * fps
	 * @return
	 */
	public int getFrameRate() {
		if (frameRate == 0) {
			switch (machine) {
			case MACHINE_NTSC:
				return 60;
			case MACHINE_PAL:
				return 50;
			}
		}
		return frameRate;
	}
	
	/**
	 * 是否是该制式下默认的 fps
	 * @return
	 */
	public boolean isDefaultFrameRate() {
		return frameRate == 0;
	}

	public boolean isUseVcr6() {
		return useVcr6;
	}

	public boolean isUseVcr7() {
		return useVcr7;
	}

	public boolean isUseFds() {
		return useFds;
	}

	public boolean isUseMmc5() {
		return useMmc5;
	}

	public boolean isUseN163() {
		return useN163;
	}

	public boolean isUseS5b() {
		return useS5b;
	}
	
	/**
	 * @return
	 * {@link #vibrato}
	 */
	public byte getVibrato() {
		return vibrato;
	}
	
	/**
	 * @return
	 * {@link #split}
	 */
	public int getSplit() {
		return split;
	}

	/**
	 * 曲目总数
	 * @return
	 */
	public int getTrackCount() {
		return tracks.size();
	}
	
	/**
	 * @return
	 * {@link #namcoChannels}
	 */
	public int getNamcoChannels() {
		return namcoChannels;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(100);
		
		builder.append("FTM 音频").append('\n');
		builder.append("标题").append(':').append(' ').append(title).append('\n');
		builder.append("作家").append(':').append(' ').append(author).append('\n');
		builder.append("版权").append(':').append(' ').append(copyright).append('\n');
		
		return builder.toString();
	}
	
	/* **********
	 *   乐器   *
	 ********** */
	/**
	 * 乐器部分 Instruments
	 */
	final ArrayList<AbstractFtmInstrument> insts = new ArrayList<>();
	
	/**
	 * 序列
	 * int (chip * seqtype.length + seqtype) - seq
	 */
	final HashMap<Integer, ArrayList<FtmSequence>> seqs = new HashMap<>();
	
	/**
	 * 采样列表
	 */
	final ArrayList<FtmDPCMSample> samples = new ArrayList<>();
	
	/**
	 * 获得乐器
	 * @param index
	 * @return
	 */
	public AbstractFtmInstrument getInstrument(int index) {
		return insts.get(index);
	}
	
	/**
	 * @return
	 *   乐器总数
	 */
	public int instrumentCount() {
		return insts.size();
	}
	
	public FtmSequence getSequence(FtmChipType chip, FtmSequenceType type, int index) {
		ArrayList<FtmSequence> list = seqs.get(chip.ordinal() * FtmSequenceType.values().length + type.ordinal());
		if (list == null) {
			return null;
		}
		return list.get(index);
	}
	
	/**
	 * 获得序列的个数
	 */
	public int sequenceCount(FtmChipType chip, FtmSequenceType type) {
		ArrayList<FtmSequence> list = seqs.get(chip.ordinal() * FtmSequenceType.values().length + type.ordinal());
		if (list == null) {
			return 0;
		}
		return list.size();
	}
	
	/* **********
	 * 乐曲轨道 *
	 ********** */
	/*
	 * 乐曲部分 Tracks
	 */
	
	ArrayList<FtmTrack> tracks = new ArrayList<>();
	
	public FtmTrack getTrack(int index) {
		return tracks.get(index);
	}
	
}
