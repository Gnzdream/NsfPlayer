package zdream.nsfplayer.ftm.renderer;

import java.util.Collection;
import java.util.HashSet;

import zdream.nsfplayer.ftm.document.IFtmChannelCode;
import zdream.nsfplayer.ftm.renderer.effect.IFtmEffect;

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
	
	/* **********
	 * 外部接口 *
	 ********** */
	
	/**
	 * <p>播放 note
	 * <p>由于 FtmNote 改成 IFtmEffect, 因此这里较原程序有了大幅度修改.
	 * 该方法要做的, 就是处理效果和状态的触发
	 * <p>原程序: ChannelHandler.playNote
	 * </p>
	 * 
	 * @param note
	 *   键, 音符.
	 *   <br>如果 note = null, 则说明保持原来的 note 不变
	 */
	public void playNote() {
		// 效果
		forceEffect(runtime.effects.get(this.channelCode).values());
		
		// 状态
		triggleState();
	}
	
	/* **********
	 *   参数   *
	 ********** */
	
	/**
	 * 乐器
	 */
	public int instrument;
	
	/**
	 * 音键, 含音符和音高
	 */
	public int note;
	
	/**
	 * <p>音量
	 * <p>curVolume: 当前音量, 需要计算乐器变量
	 * <p>masterVolume: 主音量
	 * </p>
	 */
	public int curVolume, masterVolume;
	
	/**
	 * <p>音高
	 * <p>curPitch: 当前音高, 需要计算其它比如颤音等
	 * <p>masterPitch: 主音高
	 * </p>
	 */
	public int curPitch, masterPitch;
	
	/* **********
	 * 强制执行 *
	 ********** */
	
	/**
	 * <p>状态集合.
	 * <p>原本这里的延迟状态等, 都视为一个状态
	 * </p>
	 */
	HashSet<IFtmState> states = new HashSet<>();
	
	/**
	 * 添加状态
	 * @param state
	 */
	public void addState(IFtmState state) {
		states.add(state);
		state.onAttach(channelCode, runtime);
	}
	
	/**
	 * 删除状态
	 * @param state
	 */
	public void removeState(IFtmState state) {
		states.remove(state);
		state.onDetach(channelCode, runtime);
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
		for (IFtmEffect eff : effs) {
			eff.execute(channelCode, runtime);
		}
	}

	/**
	 * 所有状态的触发
	 */
	@SuppressWarnings("unchecked")
	private void triggleState() {
		((HashSet<IFtmState>) this.states.clone())
			.forEach((state) -> state.trigger(channelCode, runtime));
	}

}
