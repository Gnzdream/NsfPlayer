package zdream.nsfplayer.ftm.executor.tools;

import zdream.nsfplayer.ftm.audio.FamiTrackerQuerier;
import zdream.nsfplayer.ftm.audio.FtmAudio;
import zdream.nsfplayer.ftm.executor.FamiTrackerParameter;
import zdream.nsfplayer.ftm.format.FtmNote;
import zdream.nsfplayer.ftm.format.FtmTrack;

/**
 * 确定 {@link FtmAudio} 已经播放的位置, 并能够获取 {@link FtmAudio} 正在播放的段和行.
 * <br>每一帧开始, 该类会计算 {@link FtmAudio} 的 tempo 和速度, 然后确定需要播放的段落和行.
 * @author Zdream
 * @since v0.2.1
 */
public class FtmRowFetcher {
	
	FamiTrackerParameter param;
	
	/**
	 * 查询器, 封装了当前播放的曲目 {@link FtmAudio}
	 */
	FamiTrackerQuerier querier;

	/* **********
	 * 播放参数 *
	 ********** */
	
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
	 * <p>下一个播放的行号, 从 0 开始.
	 * <p>当播放器播放第 0 行的音键时, 其实 row = 1, 即已经指向播放行的下一行. 因为 Famitracker 就是这样设计的.
	 * 这样做的话, 碰到跳行、跳段的效果时, 就不会出现连跳的 BUG 情况.
	 * <p>试想一下这个情况: 第 0 段第 x 行有跳行的效果 (D00 到下一段首行),
	 * 然后第 1 段、第 2 段等等第 0 行都有跳行的效果 (D00 到下一段首行), 就会出现连跳的情况.
	 * 这个在 Famitracker 中是不允许的.
	 * </p>
	 */
	int nextRow;
	
	/**
	 * <p>播放到下一行时, 段号 (pattern)
	 * </p>
	 */
	int nextSection;
	
	/**
	 * <p>重设速度值
	 * <p>原方法在 SoundGen.evaluateGlobalEffects() 中, 处理 EF_SPEED 部分的地方.
	 * </p>
	 */
	public void setSpeed(int speed) {
		this.speed = speed;
		setupSpeed();
	}
	
	/**
	 * <p>重设节奏值
	 * <p>原方法在 SoundGen.evaluateGlobalEffects() 中, 处理 EF_SPEED 部分的地方.
	 * </p>
	 */
	public void setTempo(int tempo) {
		this.tempo = tempo;
		setupSpeed();
	}
	
	/**
	 * @return
	 *   获取下一次播放位置移动之后的行号
	 * @since v0.2.9
	 */
	public int getNextRow() {
		if (skipRow == -1) {
			return nextRow;
		} else if (jumpSection == -1) {
			return 0;
		}
		
		return skipRow;
	}
	
	/**
	 * @return
	 *   获取下一次播放位置移动之后的段号
	 * @since v0.2.9
	 */
	public int getNextSection() {
		if (jumpSection == -1) {
			return nextSection;
		}
		
		return jumpSection;
	}
	
	/* **********
	 * 跳转参数 *
	 ********** */
	
	/**
	 * 跳转到的段号. 默认 -1 表示无效
	 */
	int jumpSection = -1;
	
	/**
	 * <p>跳转到的行号. 默认 -1 表示无效
	 * <p>这里做一个约定. 如果 {@link #jumpSection} 无效, 则为跳转到下一段的 skipRow 行;
	 * 如果 {@link #jumpSection} 有效, 则跳到 {@link #jumpSection} 段的 skipRow 行.
	 * </p>
	 */
	int skipRow = -1;
	
	/**
	 * 效果, 跳转至 section 段.
	 * @param section
	 *   段号
	 */
	public void jumpToSection(int section) {
		jumpSection = section;
	}
	
	/**
	 * <p>效果, 跳至下一段的 row 行.
	 * <p>此效果能和 {@link #jumpToSection(int)} 联合使用. 见 {@link #skipRow}
	 * </p>
	 * @param row
	 *   行号.
	 */
	public void skipRows(int row) {
		skipRow = row;
	}
	
	/**
	 * <p>清空跳转指令的数据.
	 * </p>
	 * @since v0.3.1
	 */
	public void clearJump() {
		jumpSection = skipRow = -1;
	}
	
	/* **********
	 * 状态参数 *
	 ********** */
	
	/**
	 * 节奏的累加器.
	 * 先加上播放一行音键所需的 tempo 值, 然后没帧减去一个 {@link #tempoDecrement}
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
	
	/**
	 * <p>修改的帧率.
	 * <p>默认为 0, 表示采用音频默认的帧率;
	 * 如果不为 0, 采用该值指示的帧率
	 * </p>
	 * @since v0.3.1
	 */
	int frameRate;
	
	public int getFrameRate() {
		return frameRate == 0 ? querier.getFrameRate() : frameRate;
	}
	
	/**
	 * 强制设置帧率
	 * @param frameRate
	 *   帧率. 0 表示采用音频默认的帧率
	 * @since v0.3.1
	 */
	public void setFrameRate(int frameRate) {
		this.frameRate = frameRate;
	}
	
	/* **********
	 * 其它方法 *
	 ********** */
	
	public FtmRowFetcher(FamiTrackerParameter param) {
		this.param = param;
	}
	
	public void ready(FamiTrackerQuerier querier, int track, int section, int row) {
		this.querier = querier;
		this.frameRate = querier.getFrameRate();
		ready(track, section, row);
	}
	
	/**
	 * 不换曲目的 ready
	 * @param track
	 *   曲目号, 从 0 开始
	 * @param section
	 *   段号, 从 0 开始
	 * @param row
	 *   行号, 从 0 开始
	 */
	public void ready(int track, int section, int row) {
		param.trackIdx = track;
		this.nextSection = section;
		this.nextRow = row;
		
		resetSpeed();
	}
	
	/**
	 * <p>重置速度值和节奏值
	 * <p>以 {@link FtmTrack} 里面定义的速度为准
	 * </p>
	 */
	public void resetSpeed() {
		speed = querier.audio.getTrack(param.trackIdx).speed;
		tempo = querier.audio.getTrack(param.trackIdx).tempo;
		
		setupSpeed();
		tempoAccum = 0;
		updateRow = false;
	}
	
	/**
	 * 重置 {@link #tempoDecrement} 和 {@link #tempoRemainder}
	 */
	private void setupSpeed() {
		int i = tempo * 24;
		tempoDecrement = i / speed;
		tempoRemainder = i % speed;
	}
	
	/**
	 * 询问当前行是否播放完毕, 需要跳到下一行 (不是询问当前帧是否播放完)
	 * @return
	 *   true, 如果当前行已经播放完毕
	 * @since v0.2.2
	 */
	public final boolean needRowUpdate() {
		return tempoAccum <= 0;
	}
	
	/**
	 * 询问当前帧是否更新了行
	 * @return
	 *   true, 如果当前帧更新了行
	 * @since v0.3.1
	 */
	public boolean isRowUpdated() {
		return updateRow;
	}
	
	/**
	 * <p>更新播放状态
	 * <p>这个调用是在 {@link FtmNote} 的效果处理完之后调用的.
	 * 这样可以确保改变 speed 的效果可以被计算进去.
	 * </p>
	 */
	public void updateState() {
		// (SoundGen.updatePlayer)
		if (tempoAccum <= 0) {
			int framePerSec = this.getFrameRate();
			// 将拍 / 秒 -> 拍 / 分钟
			tempoAccum += (60 * framePerSec) - tempoRemainder;
		}
		tempoAccum -= tempoDecrement;
	}
	
	/**
	 * 执行, 让该工具向前跑一帧.
	 * @return
	 *   该帧是否更新了行
	 * @since v0.3.0
	 */
	public boolean doFrameUpdate() {
		updateRow = false;
		
		if (this.needRowUpdate()) {
			this.updateRow = true;
			
			// 确定部分
			confirmJump();
			
			// 执行部分
			toNextRow();
		}
		
		return updateRow;
	}
	
	/**
	 * <p>确定走到下一行是否需要跳行.
	 * <p>如果上一帧有 Bxx Dxx 等跳着执行播放的效果触发,
	 * 则 {@link #jumpSection} 或 {@link #skipRow} 不等于 -1. 这时就直接进行跳转;
	 * </p>
	 */
	public void confirmJump() {
		if (skipRow >= 0) {
			if (jumpSection >= 0) {
				nextSection = jumpSection;
				jumpSection = -1;
			} else {
				confirmNextSectionBegin();
			}
			nextRow = skipRow;
			skipRow = -1;
		} else if (jumpSection >= 0) {
			nextSection = jumpSection;
			nextRow = 0;
			jumpSection = -1;
		}
		
		// 再多一次 section 段号的检查
		if (nextSection >= querier.trackCount(param.trackIdx)) {
			nextSection = 0;
		}
	}
	
	/**
	 * 执行到下一行, 并确定走到下一行之后, 再下一行需要播放的位置
	 * <p>一般而言, 下一个播放的行是该行的下一行, 但是在以下情况下, 会有变化:
	 * <p>当到某段的结尾, 会跳转到下一段的首行;
	 * </p>
	 */
	public void toNextRow() {
		param.curRow = nextRow;
		param.curSection = nextSection;
		
		nextRow++;
		
		// 是否到段尾
		int len = querier.maxRow(param.trackIdx); // 段长
		if (nextRow >= len) {
			// 跳到下一段的第 0 行
			confirmNextSectionBegin();
		}
	}
	
	/**
	 * <p>转到下一段的第一行
	 * <p>该方法是确定下一行的位置, 而不是执行
	 * </p>
	 */
	private void confirmNextSectionBegin() {
		nextRow = 0;
		nextSection = param.curSection + 1;
		
		// Loop
		if (nextSection >= querier.trackCount(param.trackIdx)) {
			nextSection = 0;
		}
	}

}
