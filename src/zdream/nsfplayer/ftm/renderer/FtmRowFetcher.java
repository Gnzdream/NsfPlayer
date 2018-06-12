package zdream.nsfplayer.ftm.renderer;

import java.util.HashMap;

import zdream.nsfplayer.ftm.document.FamiTrackerQuerier;
import zdream.nsfplayer.ftm.document.FtmAudio;
import zdream.nsfplayer.ftm.format.FtmNote;
import zdream.nsfplayer.ftm.format.FtmTrack;

/**
 * 确定 {@link FtmAudio} 已经播放的位置, 并能够获取 {@link FtmAudio} 正在播放的段和行.
 * <br>每一帧开始, 该类会计算 {@link FtmAudio} 的 tempo 和速度, 然后确定需要播放的段落和行.
 * @author Zdream
 * @since v0.2.1
 */
public class FtmRowFetcher implements IFtmRuntimeHolder {
	
	FamiTrackerRuntime runtime;
	
	/**
	 * 查询器
	 */
	FamiTrackerQuerier querier;

	@Override
	public FamiTrackerRuntime getRuntime() {
		return runtime;
	}
	
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
	 * 正在播放的节奏值, 计数单位为拍 / 分钟
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
	 * 节奏的累加器.
	 * 先加上一分钟的 tempo 值, 然后没帧减去一个 {@link #tempoDecrement}
	 * (accumulate)
	 */
	int tempoAccum;
	
	/**
	 * <p>由于节奏值和速度值并不是整数倍的关系, 所以 <code>节奏 / 速度</code> 会产生余数,
	 * 使得整数个帧之内, 不能将整个节奏解释完. 所以补充这个相当于 "余数" 的量.
	 * <p>每次确定解释器的速度之后, 这个值就被确定了.
	 * 除非解释器速度 ({@link #speed} 或 {@link #tempo}) 发生变化, 否则该值不会变.
	 * </p>
	 */
	int tempoRemainder;
	
	/**
	 * 计算的每一帧需要扣掉的节奏数
	 */
	int tempoDecrement;
	
	/**
	 * 该帧是否更新了行
	 */
	boolean updateRow;
	
	public int getFrameRate() {
		return querier.getFrameRate();
	}
	
	/* **********
	 * 其它方法 *
	 ********** */
	
	public FtmRowFetcher(FamiTrackerRuntime runtime) {
		this.runtime = runtime;
		runtime.fetcher = this;
	}
	
	public void ready(FtmAudio audio, int track, int section) {
		this.querier = new FamiTrackerQuerier(audio);
		runtime.querier = querier;
		
		ready(track, section);
	}
	
	/**
	 * 不换曲目的 ready
	 * @param track
	 * @param section
	 */
	public void ready(int track, int section) {
		this.trackIdx = track;
		this.sectionIdx = section;
		
		resetSpeed();
		runtime.notes.clear();
	}
	
	/**
	 * <p>重置速度值和节奏值
	 * <p>以 {@link FtmTrack} 里面定义的速度为准
	 * </p>
	 */
	void resetSpeed() {
		speed = querier.audio.getTrack(trackIdx).speed;
		tempo = querier.audio.getTrack(trackIdx).tempo;
		
		setupSpeed();
		tempoAccum = 0;
		updateRow = false;
	}
	
	/**
	 * 重置 {@link #tempoDecrement} 和 {@link #tempoRemainder}
	 */
	void setupSpeed() {
		int i = tempo * 24;
		tempoDecrement = i / speed;
		tempoRemainder = i % speed;
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
			updateRow = true;
			storeRow();
			
			nextRow();
		}
		
		// 第二步: (SoundGen.updatePlayer)
		if (tempoAccum <= 0) {
			int ticksPerSec = querier.getFrameRate();
			// 将拍 / 秒 -> 拍 / 分钟
			tempoAccum += (60 * ticksPerSec) - tempoRemainder;
		}
		tempoAccum -= tempoDecrement;
		
		System.out.println(String.format("%02x:%02x %c  %s", sectionIdx, row, (updateRow) ? 'T' : 'F', runtime.notes));
	}
	
	/**
	 * 确定现在正在播放的行, 放到 {@link #notes} 中
	 */
	public void storeRow() {
		final HashMap<Byte, FtmNote> map = runtime.notes;
		
		final int len = querier.channelCount();
		for (int i = 0; i < len; i++) {
			map.put(querier.channelCode(i), querier.getNote(trackIdx, sectionIdx, i, row));
		}
	}
	
	/**
	 * 按照正常的习惯, 确定下一个播放的行.
	 * <br>一般而言, 下一个播放的行是该行的下一行, 但是在以下情况下, 会有变化:
	 * <li>当到某段的结尾, 会跳转到下一段的首行;
	 * </li>
	 * 这里不处理像 Bxx Dxx 等跳着执行播放的情况
	 */
	private void nextRow() {
		row++;
		
		// 是否到段尾
		int len = querier.maxRow(trackIdx); // 段长
		if (row >= len) {
			// 跳到下一段的第 0 行
			row = 0;
			sectionIdx++;
			
			// Loop
			if (sectionIdx >= querier.trackCount(trackIdx)) {
				sectionIdx = 0;
			}
		}
	}

}
