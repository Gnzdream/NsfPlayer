package zdream.nsfplayer.ftm.renderer;

import static zdream.nsfplayer.ftm.format.FtmNote.EF_DELAY;
import static zdream.nsfplayer.ftm.format.FtmNote.EF_JUMP;
import static zdream.nsfplayer.ftm.format.FtmNote.EF_NONE;
import static zdream.nsfplayer.ftm.format.FtmNote.EF_SKIP;

import zdream.nsfplayer.ftm.document.IFtmChannelCode;
import zdream.nsfplayer.ftm.format.FtmNote;

/**
 * 抽象的 Famitracker 轨道, 用于存储各个轨道的播放局部参数, 比如局部 pitch 等
 * 
 * <p>原工程里, 它相当于 TrackerChannel 和 ChannelHandler 的结合体
 * </p>
 * 
 * @author Zdream
 * @data 2018-06-09
 * @since 0.2.1
 */
public abstract class AbstractFtmChannel implements IFtmChannelCode, IFtmRuntimeHolder {

	/**
	 * 轨道号
	 */
	public final byte channelCode;
	
	/**
	 * 运行时数据
	 */
	private FamiTrackerRuntime runtime;

	@Override
	public FamiTrackerRuntime getRuntime() {
		return runtime;
	}
	
	/**
	 * 设置环境
	 * @param runtime
	 */
	void setRuntime(FamiTrackerRuntime runtime) {
		this.runtime = runtime;
	}
	
	public AbstractFtmChannel(byte channelCode) {
		this.channelCode = channelCode;
	}
	
	/**
	 * 播放 note
	 * 
	 * <p>这里说明一下. 下面是将缓存上一次需要播放的键, 但是这个方法的功能远不止如此.
	 * 所以后面的方法要继承的类来实现.
	 * <p>覆盖的方法要做的有如下几件事:
	 * <li>处理延迟触发的键
	 * <li>处理键的全局效果
	 * <li>处理键的局部效果
	 * </li>
	 * <p>原程序: ChannelHandler.playNote
	 * </p>
	 * 
	 * @param note
	 *   键, 音符.
	 *   <br>如果 note = null, 则说明保持原来的 note 不变
	 */
	public void playNote() {
		// TODO 由于 FtmNote 改成 IFtmEffect, 因此这里会大幅度修改.
		
		/*if (note != null) {
			HANDLE: {
				if (handleDelay(note)) {
					break HANDLE;
				}
				
				
			}
			
			previousNote = note;
		}*/
	}
	
	/* **********
	 *   延迟   *
	 ********** */
	/*
	 * 延迟触发效果 Gxx.
	 * 局部效果, 即当前轨道有效
	 */
	
	/**
	 * 是否处在延迟状态. 如果上一个键含 Gxx 效果, 该开关会打开, 并持续到延迟结束
	 */
	private boolean delaying;
	
	/**
	 * 延迟计数器. 一般是 0, 但如果上面 {@link #delaying} = true 时会大于零,
	 * 记录还有多少帧将播放延迟的键 {@link #delayNote}.
	 * 
	 * 每一帧扣 1, 扣到 0 时延迟的键播放.
	 */
	private int delayCounter;
	
	/**
	 * 延迟的那个键. 在 {@link #delaying} = true 的时候有效
	 */
	private FtmNote delayNote;
	
	/**
	 * @return
	 *   {@link #delaying}
	 */
	public boolean isDelaying() {
		return delaying;
	}
	
	/**
	 * 处理延迟触发的效果
	 * 
	 * <p>将延迟的相关数据写入到上面的成员变量中
	 * </p>
	 * 
	 * @param note
	 * @return
	 */
	protected boolean handleDelay(FtmNote note) {
		// 如果 delaying = true 说明上一个有效的键 (note) 有延迟效果,
		// 但是还没等延迟触发, 新的键已经到来了, 这时, 上一个延迟触发的数据将立即执行
		if (delaying) {
			delaying = false;
			handleNote(delayNote);
		}
		
		// Check delay
		for (int i = 0; i < FtmNote.MAX_EFFECT_COLUMNS; ++i) {
			if (note.effNumber[i] == EF_DELAY && note.effParam[i] > 0) {
				delaying = true;
				delayCounter = note.effParam[i];
				delayNote = note.clone();

				// Only one delay/row is allowed. Remove global effects
				// 延迟效果只有一列有效. 出现了多余的延迟列、或者跳转相关效果, 都直接删除该延迟效果
				for (int j = 0; j < FtmNote.MAX_EFFECT_COLUMNS; ++j) {
					switch (delayNote.effNumber[j]) {
						case EF_DELAY:
							delayNote.effNumber[j] = EF_NONE;
							delayNote.effParam[j] = 0;
							break;
						case EF_JUMP:
							handleJump(delayNote.effParam[j]);
							delayNote.effNumber[j] = EF_NONE;
							delayNote.effParam[j] = 0;
							break;
						case EF_SKIP:
							handleSkip(delayNote.effParam[j]);
							delayNote.effNumber[j] = EF_NONE;
							delayNote.effParam[j] = 0;
							break;
					}
				}
				return true;
			}
		}

		return false;
	}
	
	/* **********
	 * 全局参数 *
	 ********** */
	/*
	 * 比如 Dxx Bxx Cxx 等影响全局播放进程的
	 */
	
	/**
	 * 跳到对应的段号
	 * @param section
	 *   段号, 从 0 开始
	 */
	protected void handleJump(int section) {
		// TODO
	}
	
	/**
	 * 跳到下一段的第 <code>row</code>行
	 * @param row
	 *   行号, 从 0 开始
	 */
	protected void handleSkip(int row) {
		// TODO
	}
	
	/* **********
	 * 数据处理 *
	 ********** */
	
	/**
	 * 上一个缓存的 note
	 */
	protected FtmNote previousNote;
	
	protected void handleNote(FtmNote note) {
		// TODO
	}

}
