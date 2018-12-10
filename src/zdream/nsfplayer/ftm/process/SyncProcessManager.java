package zdream.nsfplayer.ftm.process;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;

import zdream.nsfplayer.ftm.process.agreement.AbstractAgreementEntry;
import zdream.nsfplayer.ftm.process.agreement.BarrierAgreementEntry;
import zdream.nsfplayer.ftm.process.agreement.WaitingAgreement;
import zdream.nsfplayer.ftm.process.agreement.WaitingAgreementEntry;
import zdream.nsfplayer.ftm.process.base.FtmPosition;
import zdream.nsfplayer.ftm.renderer.FamiTrackerSyncRenderer;

/**
 * <p>处理各个执行器依据协议内容协调执行位置、速度的管理者
 * <p>在 {@link FamiTrackerSyncRenderer} 中使用.
 * 支持信号协议. 在版本 v0.3.1 暂不支持栅栏协议
 * </p>
 * 
 * @author Zdream
 * @since v0.3.1
 */
public class SyncProcessManager {
	
	final ArrayList<ExecutorProcessState> states = new ArrayList<>();
	
	private ExecutorProcessState getState(int exeId) {
		for (ExecutorProcessState state : states) {
			if (state.id == exeId) {
				return state;
			}
		}
		throw new NullPointerException("SyncProcessManager: 不存在 " + exeId + " 对应的状态");
	}
	
	/* **********
	 *  执行器  *
	 ********** */
	
	/**
	 * 添加执行器
	 * @param exeId
	 *   执行器标识号
	 * @param pos
	 *   初始位置
	 */
	public void addExecutor(int exeId, FtmPosition pos) {
		ExecutorProcessState state = new ExecutorProcessState(exeId);
		state.pos = pos;
		states.add(state);
	}
	
	/**
	 * 删除执行器
	 * @param exeId
	 *   执行器标识号
	 */
	public void removeExecutor(int exeId) {
		clearAgreement(exeId);
		states.remove(getState(exeId));
	}
	
	/**
	 * 为执行器更新位置
	 * @param exeId
	 *   执行器标识号
	 * @param pos
	 *   更新后的位置
	 */
	public void updatePosition(int exeId, FtmPosition pos) {
		ExecutorProcessState state = getState(exeId);
		FtmPosition oldPos = state.pos;
		
		if (oldPos.equals(pos)) {
			return;
		}
		
		state.pos = pos;
		updateExecutorBound(state);
	}
	
	/**
	 * 更新束缚列表
	 * @param state
	 */
	private void updateExecutorBound(ExecutorProcessState state) {
		state.bounds.clear();
		List<AbstractAgreementEntry> agreements = state.agreements.get(state.pos);
		
		if (agreements == null) {
			return;
		}
		
		for (AbstractAgreementEntry entry : agreements) {
			state.bounds.add(entry);
		}
	}
	
	/**
	 * 每帧更新状态
	 */
	public void updateStates() {
		HashSet<BarrierAgreementEntry> barriers = null;
		
		for (ExecutorProcessState state : states) {
			if (state.bounds.isEmpty()) {
				continue;
			}
			
			ListIterator<AbstractAgreementEntry> it = state.bounds.listIterator();
			
			while (it.hasNext()) {
				AbstractAgreementEntry e0 = it.next();
				if (e0 instanceof WaitingAgreementEntry) {
					WaitingAgreementEntry e = (WaitingAgreementEntry) e0;
					
					ExecutorProcessState depend = getState(e.dependExeId);
					if (e.dependPos.equals(depend.pos)) {
						// 结束等待
						e.countdown = -1;
						it.remove();
						continue;
					}
					
					// 需要等待
					if (e.countdown == -1) {
						e.countdown = e.baseTimeout;
					} else if (e.countdown == 0) {
						// 超时了, 放行
						it.remove();
						continue;
					} else {
						e.countdown--;
					}
				} else if (e0 instanceof BarrierAgreementEntry) {
					if (barriers == null) {
						barriers = new HashSet<>();
					}
					barriers.add((BarrierAgreementEntry) e0);
				}
			}
		}
		
		// 对所有栅栏进行更新
		if (barriers != null) {
			for (BarrierAgreementEntry entry : barriers) {
				// TODO
				
				if (entry.countdown == -1) {
					entry.countdown = entry.baseTimeout;
				} else if (entry.countdown == 0) {
					// 超时了, 放行 TODO
//					state.bounds.remove(e);
					continue;
				} else {
					entry.countdown--;
				}
			}
		}
	}
	
	/**
	 * 查看执行器状态, 是否需要等待
	 * @param exeId
	 *   执行器标识号
	 */
	public boolean isWaiting(int exeId) {
		return !getState(exeId).bounds.isEmpty();
	}
	
	/* **********
	 *   协议   *
	 ********** */
	
	/**
	 * 添加等待协议
	 * @param a
	 *   协议数据
	 */
	public void addWaitingAgreement(WaitingAgreement a) {
		WaitingAgreementEntry entry = a.createEntry();
		
		int waitExeId = entry.waitExeId;
		int dependExeId = entry.dependExeId;
		
		ExecutorProcessState waitExe = getState(waitExeId);
		ExecutorProcessState dependExe = getState(dependExeId);
		
		List<AbstractAgreementEntry> list = waitExe.agreements.get(entry.waitPos);
		if (list == null) {
			list = new ArrayList<>();
			list.add(entry);
			waitExe.agreements.put(entry.waitPos, list);
		} else {
			list.add(entry);
		}
		
		dependExe.refs.add(entry);
	}
	
	/**
	 * 删除等待协议
	 * @param a
	 *   协议数据
	 */
	public void removeWaitingAgreement(WaitingAgreement a) {
		int dependExeId = a.dependExeId;
		ExecutorProcessState dependExe = getState(dependExeId);
		
		final int len = dependExe.refs.size();
		for (int i = 0; i < len; i++) {
			AbstractAgreementEntry e = dependExe.refs.get(i);
			if (e.is(a)) {
				removeWaitingAgreement((WaitingAgreementEntry) e);
				break;
			}
		}
	}
	
	private void removeWaitingAgreement(WaitingAgreementEntry entry) {
		// 删除 dependExe 中的引用
		int dependExeId = entry.dependExeId;
		ExecutorProcessState dependExe = getState(dependExeId);
		dependExe.refs.remove(entry);
		
		// 删除 waitExe 中的引用
		int waitExeId = entry.waitExeId;
		ExecutorProcessState waitExe = getState(waitExeId);
		List<AbstractAgreementEntry> list = waitExe.agreements.get(entry.waitPos);
		if (list != null) {
			list.remove(entry);
		}
		if (list.isEmpty()) {
			waitExe.agreements.remove(entry.waitPos);
		}
		
		if (entry.waitPos.equals(waitExe.pos)) {
			waitExe.bounds.remove(entry);
		}
	}
	
	/**
	 * <p>清除指定执行器的所有协议内容.
	 * <p>如果该执行器签订了等待协议, 无论是等待方还是依据方, 该协议都将取消.
	 * 另一个对象中的该协议也将删除
	 * </p>
	 * @param exeId
	 */
	private void clearAgreement(int exeId) {
		ExecutorProcessState exe = getState(exeId);
		
		ArrayList<AbstractAgreementEntry> lists =
				new ArrayList<>(exe.agreements.size() + (exe.agreements.size() << 1));
		
		for (AbstractAgreementEntry entry : lists) {
			if (entry instanceof WaitingAgreementEntry) {
				removeWaitingAgreement((WaitingAgreementEntry) entry);
			}
		}
	}

}
