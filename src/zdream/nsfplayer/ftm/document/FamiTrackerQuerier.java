package zdream.nsfplayer.ftm.document;

import zdream.nsfplayer.ftm.format.FtmNote;
import zdream.nsfplayer.ftm.format.FtmPattern;
import zdream.nsfplayer.ftm.format.FtmTrack;

/**
 * FamiTracker 的文本数据的查询器.
 * @author Zdream
 * @date 2018-06-09
 * @since 0.2.0
 */
public class FamiTrackerQuerier {
	
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
		
		reScanChannel();
	}
	
	/* **********
	 *   轨道   *
	 ********** */
	/*
	 * 该区域的数据在查询器生成时就确定了
	 * 
	 * 这里和 Handler 的方法重合了, TODO 需要删掉多余的.
	 */
	
	/**
	 * 缓存第 n 个轨道对应的轨道号是什么
	 */
	private byte[] channelCode;
	
	/**
	 * 重新计算轨道音源相关的数据.
	 */
	private void reScanChannel() {
		// 计算轨道总数
		int channelCount = 5; // 2A03
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
		channelCode[codePtr++] = FtmChannels.CHANNEL_2A03_PULSE1;
		channelCode[codePtr++] = FtmChannels.CHANNEL_2A03_PULSE2;
		channelCode[codePtr++] = FtmChannels.CHANNEL_2A03_TRIANGLE;
		channelCode[codePtr++] = FtmChannels.CHANNEL_2A03_NOISE;
		channelCode[codePtr++] = FtmChannels.CHANNEL_2A03_DPCM;
		
		if (audio.useVcr6) {
			channelCode[codePtr++] = FtmChannels.CHANNEL_VRC6_PULSE1;
			channelCode[codePtr++] = FtmChannels.CHANNEL_VRC6_PULSE2;
			channelCode[codePtr++] = FtmChannels.CHANNEL_VRC6_SAWTOOTH;
		}
		if (audio.useVcr7) {
			channelCode[codePtr++] = FtmChannels.CHANNEL_VRC7_FM1;
			channelCode[codePtr++] = FtmChannels.CHANNEL_VRC7_FM2;
			channelCode[codePtr++] = FtmChannels.CHANNEL_VRC7_FM3;
			channelCode[codePtr++] = FtmChannels.CHANNEL_VRC7_FM4;
			channelCode[codePtr++] = FtmChannels.CHANNEL_VRC7_FM5;
			channelCode[codePtr++] = FtmChannels.CHANNEL_VRC7_FM6;
		}
		if (audio.useFds) {
			channelCode[codePtr++] = FtmChannels.CHANNEL_FDS;
		}
		if (audio.useMmc5) {
			channelCode[codePtr++] = FtmChannels.CHANNEL_MMC1_PULSE1;
			channelCode[codePtr++] = FtmChannels.CHANNEL_MMC1_PULSE2;
		}
		if (audio.useN163) {
			byte[] cs = new byte[] {
					FtmChannels.CHANNEL_N163_1,
					FtmChannels.CHANNEL_N163_2,
					FtmChannels.CHANNEL_N163_3,
					FtmChannels.CHANNEL_N163_4,
					FtmChannels.CHANNEL_N163_5,
					FtmChannels.CHANNEL_N163_6
			};
			int length = audio.namcoChannels;
			for (int i = 0; i < length; i++) {
				channelCode[codePtr++] = cs[i];
			}
		}
	}
	
	/**
	 * 计算轨道数总和
	 * @return
	 */
	public int channelCount() {
		return this.channelCode.length;
	}
	
	/**
	 * 查看第 {@code channel} 个轨道的轨道号
	 * @return
	 */
	public byte channelCode(int channel) {
		return channelCode[channel];
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
		FtmPattern p = t.patterns[order][channel];
		if (p == null) {
			return null;
		}
		return p.notes[row];
	}

}
