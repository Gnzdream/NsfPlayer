package zdream.nsfplayer.ftm.document;

import java.util.ArrayList;
import java.util.HashMap;

import zdream.nsfplayer.ftm.document.format.AbstractFtmInstrument;
import zdream.nsfplayer.ftm.document.format.FtmChipType;
import zdream.nsfplayer.ftm.document.format.FtmDPCMSample;
import zdream.nsfplayer.ftm.document.format.FtmSequenceType;
import zdream.nsfplayer.ftm.document.format.FtmTrack;
import zdream.nsfplayer.ftm.document.format.IFtmSequence;

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
	String title;
	
	/**
	 * 作家 / 创作者 AUTHOR
	 */
	String author;
	
	/**
	 * 版权 COPYRIGHT
	 */
	String copyright;
	
	/**
	 * 评论, 列表形式
	 */
	String[] comments;
	
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
	int framerate;
	
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
	 * <p>当 xx 代表的数值大于 {@code split} 时, 它将解析成 {@link FtmTrack#tempo};
	 * <br>否则解析成 {@link FtmTrack#speed}
	 */
	int split;
	
	/**
	 * <p>N163 的轨道数.
	 * <p>当 {@link #useN163} 为 true 时有效
	 */
	int namcoChannels;
	
	public String getTitle() {
		return title;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public String getCopyright() {
		return copyright;
	}
	
	public String[] getComments() {
		return comments;
	}
	
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
	public int getFramerate() {
		if (framerate == 0) {
			// TODO return
		}
		return framerate;
	}
	
	/**
	 * 是否是该制式下默认的 fps
	 * @return
	 */
	public boolean isDefaultFramerate() {
		return framerate == 0;
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
		
		builder.append("FTM 文本").append('\n');
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
	ArrayList<AbstractFtmInstrument> insts = new ArrayList<>();
	
	/*
	 * 序列
	 * int (chip * seqtype.length + seqtype) - seq
	 */
	HashMap<Integer, ArrayList<IFtmSequence>> seqs = new HashMap<>();
	
	/**
	 * 采样列表
	 */
	ArrayList<FtmDPCMSample> samples = new ArrayList<>();
	
	public IFtmSequence getSequence(FtmChipType chip, FtmSequenceType type, int index) {
		ArrayList<IFtmSequence> list = seqs.get(chip.ordinal() + FtmSequenceType.values().length + type.ordinal());
		if (list == null) {
			return null;
		}
		return list.get(index);
	}
	
	/**
	 * 获得序列的个数
	 */
	public int sequenceCount(FtmChipType chip, FtmSequenceType type) {
		ArrayList<IFtmSequence> list = seqs.get(chip.ordinal() + FtmSequenceType.values().length + type.ordinal());
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
	
	/**
	 * 各个轨道的标识号
	 */
	public static final byte
			CHANNEL_2A03_PULSE1 = 1,
			CHANNEL_2A03_PULSE2 = 2,
			CHANNEL_2A03_TRIANGLE = 3,
			CHANNEL_2A03_NOISE = 4,
			CHANNEL_2A03_DPCM = 5,
			
			CHANNEL_VRC6_PULSE1 = 0x11,
			CHANNEL_VRC6_PULSE2 = 0x12,
			CHANNEL_VRC6_SAWTOOTH = 0x13,
			
			CHANNEL_VRC7_FM1 = 0x21,
			CHANNEL_VRC7_FM2 = 0x22,
			CHANNEL_VRC7_FM3 = 0x23,
			CHANNEL_VRC7_FM4 = 0x24,
			CHANNEL_VRC7_FM5 = 0x25,
			CHANNEL_VRC7_FM6 = 0x26,
			
			CHANNEL_FDS = 0x31,
			
			CHANNEL_MMC1_PULSE1 = 0x41,
			CHANNEL_MMC1_PULSE2 = 0x42,
			
			CHANNEL_N163_1 = 0x51,
			CHANNEL_N163_2 = 0x52,
			CHANNEL_N163_3 = 0x53,
			CHANNEL_N163_4 = 0x54,
			CHANNEL_N163_5 = 0x55,
			CHANNEL_N163_6 = 0x56;

}
