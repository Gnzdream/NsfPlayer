package zdream.nsfplayer.ftm.document;

import java.util.ArrayList;

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
	}

	/**
	 * 设置 N163 轨道数
	 * @param count
	 */
	public void setNamcoChannels(int count) {
		// TODO Auto-generated method stub
		
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

}
