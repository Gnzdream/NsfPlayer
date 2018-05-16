package zdream.nsfplayer.ftm.renderer;

import zdream.nsfplayer.ftm.document.FtmAudio;
import zdream.nsfplayer.ftm.format.FtmTrack;

/**
 * 确定 {@link FtmAudio} 已经播放的位置, 并能够获取 {@link FtmAudio} 正在播放的段和行.
 * <br>每一帧开始, 该类会计算 {@link FtmAudio} 的 tempo 和速度, 然后确定需要播放的段落和行.
 * @author Zdream
 * @since v0.2.1
 */
public class FtmRowFetcher {
	
	FamiTrackerRenderer parent;
	
	/**
	 * 播放音频数据
	 */
	FtmAudio audio;
	
	/* **********
	 * 播放参数 *
	 ********** */
	
	/**
	 * 正播放的曲目号
	 */
	int trackIdx;
	
	/**
	 * 正播放的段号 (pattern)
	 */
	int sectionIdx;
	
	/**
	 * 正在播放的 tempo 值
	 * @see FtmTrack#tempo
	 */
	int tempo;
	
	/**
	 * 正在播放的速度值
	 * @see FtmTrack#speed
	 */
	int speed;
	
	/**
	 * 正播放的行数
	 */
	int row;
	
	/* **********
	 * 状态参数 *
	 ********** */
	
	/**
	 * tempo 的累加器
	 * (accumulate)
	 */
	int tempoAccum;
	
	/**
	 * 该帧是否更新了行
	 */
	boolean updateRow;
	
	/* **********
	 * 其它方法 *
	 ********** */
	
	public FtmRowFetcher(FamiTrackerRenderer parent) {
		this.parent = parent;
	}
	
	public void ready(FtmAudio audio, int track, int section) {
		this.audio = audio;
		this.trackIdx = track;
		this.sectionIdx = section;
	}
	
	/**
	 * 音乐向前跑一帧. 看看现在跑到 Ftm 的哪一行上
	 */
	public void runFrame() {
		// 重置
		updateRow = false;
		
		// 第一步: (SoundGen.runFrame)
		if (tempoAccum <= 0) {
			// Enable this to skip rows on high tempos
			row++;
			
			updateRow = true;
			nextRow();
		}
		
		// 第二步: (SoundGen.updatePlayer)
		
	}
	
	/**
	 * 确定下一个播放的行.
	 * <br>一般而言, 下一个播放的行是该行的下一行, 但是在以下情况下, 会有变化:
	 * <li>当到某段的结尾, 会跳转到下一段的首行;
	 * <li>出现跳转指令 (Bxx, Dxx), 将跳转到指定位置播放
	 * </li>
	 */
	public void nextRow() {
		// TODO
	}

}
