package zdream.nsfplayer.ftm.renderer;

import zdream.nsfplayer.ftm.document.FamiTrackerException;
import zdream.nsfplayer.ftm.document.FtmAudio;

/**
 * <p>默认 FamiTracker 部分的音频渲染器.
 * <p>来源于原 C++ 工程的 SoundGen 类
 * </p>
 * @author Zdream
 * @since v0.2.1
 */
public class FamiTrackerRenderer {
	
	/* **********
	 * 播放参数 *
	 ********** */
	/**
	 * 播放音频数据
	 */
	FtmAudio audio;
	
	/**
	 * 正播放的曲目号
	 */
	int trackIdx;
	
	/**
	 * 正播放的段号
	 */
	int patternIdx;
	
	/* **********
	 * 公共接口 *
	 ********** */
	
	/**
	 * <p>让该渲染器读取对应的 audio 数据.
	 * <p>设置播放暂停位置为第 1 个曲目 (曲目 0) 的第一段 (段 0)
	 * </p>
	 * @param audio
	 */
	public void ready(FtmAudio audio) throws FamiTrackerException {
		ready(audio, 0, 0);
	}
	
	/**
	 * <p>让该渲染器读取对应的 audio 数据.
	 * <p>设置播放暂停位置为指定曲目的第一段 (段 0)
	 * </p>
	 * @param audio
	 * @param track
	 *   曲目号, 从 0 开始
	 */
	public void ready(FtmAudio audio, int track) throws FamiTrackerException {
		ready(audio, track, 0);
	}
	
	/**
	 * <p>让该渲染器读取对应的 audio 数据.
	 * <p>设置播放暂停位置为指定曲目的指定段
	 * </p>
	 * @param audio
	 * @param track
	 *   曲目号, 从 0 开始
	 * @param pattern
	 *   段号, 从 0 开始
	 */
	public void ready(
			FtmAudio audio,
			int track,
			int pattern)
			throws FamiTrackerException {
		this.audio = audio;
		this.trackIdx = track;
		this.patternIdx = pattern;
		
		// TODO 重置播放相关的数据
		
		// TODO SoundGen.loadMachineSettings()
	}
	
	/**
	 * 询问是否已经播放完毕
	 * @return
	 */
	public boolean isFinished() {
		// TODO
		return true;
	}
	
	/**
	 * 渲染
	 * @param bs
	 * @param offset
	 *   bs 存放数据的起始位置
	 * @param length
	 *   bs 存放的数据总量, 以 byte 为单位.
	 *   <br>一般双声道、16 位深度, 该数据需要是 4 的倍数.
	 * @return
	 */
	public int render(byte[] bs, int offset, int length) {
		// TODO
		return 0;
	}
	
	/* **********
	 * 播放部分 *
	 ********** */
	
}
