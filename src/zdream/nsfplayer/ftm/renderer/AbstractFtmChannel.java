package zdream.nsfplayer.ftm.renderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import zdream.nsfplayer.ftm.document.IFtmChannelCode;
import zdream.nsfplayer.ftm.renderer.effect.IFtmEffect;
import zdream.nsfplayer.sound.AbstractNsfSound;
import zdream.nsfplayer.sound.IResetable;

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
public abstract class AbstractFtmChannel implements IFtmChannelCode, IFtmRuntimeHolder, IResetable {

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
	 * 获取音频发声器
	 * @return
	 *   音频发声器实例
	 */
	public abstract AbstractNsfSound getSound();
	
	/* **********
	 * 外部接口 *
	 ********** */
	
	/**
	 * <p>播放 note
	 * <p>由于 FtmNote 改成 IFtmEffect, 因此这里较原程序有了大幅度修改.
	 * 该方法要做的, 就是处理效果和状态的触发.
	 * <p>子类在这个基础上, 完成将数据写到发声器中, 指导发声器工作, 将数据写到音频管道中
	 * <p>原程序: ChannelHandler.playNote
	 * </p>
	 * 
	 * @param noise
	 *   键, 音符.
	 *   <br>如果 note = null, 则说明保持原来的 note 不变
	 */
	public void playNote() {
		// 初始化
		startFrame();
		
		// 效果
		forceEffect(runtime.effects.get(this.channelCode).values());
		
		// 状态
		triggleState();
	}
	
	/* **********
	 *   参数   *
	 ********** */
	/*
	 * 这里说明一下. 下面的参数, 比如 volume 等, 有两个值, curVolume 和 masterVolume.
	 * 最终使用的值是 curVolume, 但存在这样一个规定: 在 effect 和 state 进行修改时,
	 * masterVolume 表示主音量, 而 curVolume 表示偏移量, 即 curVolume 在 0 的周围浮动;
	 * 最终计算总的 curVolume 时, 会将主音量和偏移量共同进入计算, 得出的 curVolume
	 * 重写 curVolume 的值. 到这时, curVolume 的意义会从偏移量编程当前音量.
	 * 
	 * 音高 period, 音键 note 也遵循这个规则.
	 */
	
	/**
	 * 乐器
	 */
	protected int instrument;
	
	/**
	 * 当前帧中, 乐器是否更新过
	 */
	protected boolean instrumentUpdated;
	
	/**
	 * 音键, 含音符和音高
	 * <p>curNote: 当前音键
	 * <p>masterNote: 主音键
	 * </p>
	 */
	protected int curNote, masterNote;
	
	/**
	 * <p>音量
	 * <p>curVolume: 当前音量, 需要计算乐器变量.
	 * 由于我这里为了提高精度, 为原来的音量的 16 倍,
	 * 并且中途在修改的过程中允许产生负数等超过边界的情况,
	 * 最后在写入发声器前将其范围限定在 [0, 240] 范围内.
	 * <p>masterVolume: 主音量 [0, 15]
	 * </p>
	 */
	protected int curVolume, masterVolume;
	
	/**
	 * <p>音高
	 * <p>curPeriod: 当前波长, 需要计算其它比如颤音等
	 * <p>masterPitch: 主音高. 这个值在原 C++ 程序中称为 finePitch,
	 * 由 Pxx 效果控制. 默认值 0
	 * <p>波长和音高的关系是负相关, 单位相同. 波长越长, 音高越低
	 * </p>
	 */
	protected int curPeriod, masterPitch;
	
	/**
	 * <p>音色
	 * <p>curDuty: 当前音色
	 * <p>masterDuty: 主音色. 这个值由 Vxx 效果控制. 默认值 0
	 * </p>
	 */
	protected int curDuty, masterDuty;
	
	/**
	 * 是否在播放状态
	 */
	protected boolean playing = true;
	
	/**
	 * @return
	 *   {@link #instrument}
	 */
	public int getInstrument() {
		return instrument;
	}

	/**
	 * @param instrument
	 *   {@link #instrument}
	 */
	public void setInstrument(int instrument) {
		instrumentUpdated = true;
		this.instrument = instrument;
	}

	/**
	 * @return
	 *   {@link #curNote}
	 */
	public int getCurrentNote() {
		return curNote;
	}

	/**
	 * @param note
	 *   {@link #curNote}
	 */
	public void setCurrentNote(int note) {
		this.curNote = note;
	}

	/**
	 * 为 {@link #curNote} 增加一个值
	 * @param delta
	 *   增量, 可以为正数、负数或 0
	 */
	public void addCurrentNote(int delta) {
		this.curNote += delta;
	}

	/**
	 * @return
	 *   {@link #curVolume}
	 */
	public int getCurrentVolume() {
		return curVolume;
	}

	/**
	 * @param volume
	 *   {@link #curVolume}
	 */
	public void setCurrentVolume(int volume) {
		this.curVolume = volume;
	}

	/**
	 * 为 {@link #curVolume} 增加一个值
	 * @param delta
	 *   增量, 可以为正数、负数或 0
	 */
	public void addCurrentVolume(int delta) {
		this.curVolume += delta;
	}

	/**
	 * @return
	 *   {@link #curPeriod}
	 */
	public int getCurrentPeriod() {
		return curPeriod;
	}

	/**
	 * @param period
	 *   {@link #curPeriod}
	 */
	public void setCurrentPeriod(int period) {
		this.curPeriod = period;
	}

	/**
	 * 为 {@link #curPeriod} 增加一个值
	 * @param delta
	 *   增量, 可以为正数、负数或 0
	 */
	public void addCurrentPeriod(int delta) {
		this.curPeriod += delta;
	}

	/**
	 * @return
	 *   {@link #curDuty}
	 */
	public int getCurrentDuty() {
		return curDuty;
	}

	/**
	 * 设置并重置现在的音键
	 * @param note
	 *   {@link #masterNote}
	 */
	public void setMasterNote(int note) {
		this.masterNote = note;
	}
	
	/**
	 * @return
	 *   {@link #masterNote}
	 */
	public int getMasterNote() {
		return masterNote;
	}
	
	/**
	 * 设置并重置现在的音量
	 * @param masterVolume
	 *   {@link #masterVolume}
	 */
	public void setMasterVolume(int masterVolume) {
		this.masterVolume = masterVolume;
	}
	
	/**
	 * @return
	 *   {@link #masterVolume}
	 */
	public int getMasterVolume() {
		return masterVolume;
	}

	/**
	 * 设置并重置现在的音高
	 * @param masterPitch
	 *   {@link #masterPitch}
	 */
	public void setMasterPitch(int masterPitch) {
		this.masterPitch = masterPitch;
	}
	
	/**
	 * @return
	 *   {@link #masterPitch}
	 */
	public int getMasterPitch() {
		return masterPitch;
	}

	/**
	 * 设置并重置现在的音色
	 * @param masterDuty
	 *   {@link #masterDuty}
	 */
	public void setMasterDuty(int masterDuty) {
		this.masterDuty = masterDuty;
	}
	
	/**
	 * @return
	 *   {@link #masterDuty}
	 */
	public int getMasterDuty() {
		return masterDuty;
	}
	
	/**
	 * 打开, 让轨道播放.
	 * 调用它的情况是, 当这个轨道接收一个新的 note 后, 它就要开始播放新的 note.
	 */
	public void turnOn() {
		playing = true;
	}

	/**
	 * <p>询问当前轨道是否在播放状态.
	 * <p>注意, 音量为 0 不等于不在播放.
	 * 只有 halt 效果、Sxx 效果等才能设置 <code>playing = false</code>.
	 * </p>
	 * @return
	 *   当前轨道是否在播放状态
	 */
	public boolean isPlaying() {
		return playing;
	}
	
	@Override
	public void reset() {
		playing = true;
		
		schedules.clear();
		states.clear();
	}
	
	/**
	 * 每帧开始时调用
	 */
	protected void startFrame() {
		curNote = 0;
		curDuty = 0;
		curPeriod = 0;
		curVolume = 0;
		instrumentUpdated = false;
		
		for (IFtmSchedule s : schedules) {
			s.trigger(channelCode, runtime);
		}
		schedules.clear();
	}
	
	/* **********
	 * 强制执行 *
	 ********** */

	/**
	 * <p>状态集合.
	 * <p>原本这里的延迟状态等, 都视为一个状态.
	 * 状态的触发在效果发生之后. 如果想要在状态发生之前, 需要将状态放在 schedules 中.
	 * </p>
	 */
	HashSet<IFtmState> states = new HashSet<>();
	
	/**
	 * 准备阶段触发的状态集合. 比如 delay 状态触发比效果触发的时间还要早, 就放在这里.
	 * 准备阶段的状态只调用一次, 调用完自动删除
	 */
	HashSet<IFtmSchedule> schedules = new HashSet<>();
	
	/**
	 * 添加状态
	 * @param state
	 */
	public void addState(IFtmState state) {
		states.add(state);
		state.onAttach(channelCode, runtime);
	}
	
	/**
	 * 添加准备阶段触发的状态
	 * @param state
	 */
	public void addSchedule(IFtmSchedule schedule) {
		schedules.add(schedule);
	}
	
	/**
	 * 删除状态
	 * @param state
	 */
	public void removeState(IFtmState state) {
		state.onDetach(channelCode, runtime);
		states.remove(state);
	}
	
	/**
	 * 删除名称匹配的所有状态
	 * @param state
	 */
	public void removeStates(String name) {
		states.removeIf((s) -> {
			boolean b = s.name().equals(name);
			if (b) {
				s.onDetach(channelCode, runtime);
			}
			return b;
		});
	}
	
	/**
	 * 过滤出所有名称匹配的状态集合
	 * @param name
	 * @return
	 */
	public HashSet<IFtmState> filterStates(String name) {
		HashSet<IFtmState> set = new HashSet<>();
		for (IFtmState s : states) {
			if (s.name().equals(name)) {
				set.add(s);
			}
		}
		return set;
	}
	
	/**
	 * 强制、立即执行效果集合
	 * @param effs
	 *   效果集合（非全局效果有效）
	 */
	public void forceEffect(Collection<IFtmEffect> effs) {
		ArrayList<IFtmEffect> list = new ArrayList<>(effs);
		list.sort(null); // 效果类有自然的优先度排序
		
		for (IFtmEffect eff : list) {
			eff.execute(channelCode, runtime);
		}
	}

	/**
	 * 强制按照状态的优先度, 触发现有的状态
	 */
	private void triggleState() {
		ArrayList<IFtmState> list = new ArrayList<>(this.states);
		list.sort(null); // 状态类有自然的优先度排序
		
		list.forEach((state) -> state.trigger(channelCode, runtime));
	}
	
	/**
	 * 暂停声音播放. ftm 里面显示为 "---" 的 note 的效果
	 */
	public void doHalt() {
		playing = false;
	}
	
	/**
	 * 播放乐器的释放效果. ftm 里面显示为 "===" 的 note 的效果
	 */
	public void doRelease() {
		// TODO
	}
	
	
	/**
	 * <p>根据音键查询波长值.
	 * <p>工具方法, 需要子类按照需要来重写
	 * </p>
	 * @param note
	 * @return
	 */
	public int periodTable(int note) {
		throw new IllegalStateException("该轨道不支持查询音键波长的功能");
	}

}
