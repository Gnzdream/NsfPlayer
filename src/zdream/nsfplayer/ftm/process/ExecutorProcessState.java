package zdream.nsfplayer.ftm.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import zdream.nsfplayer.ftm.process.agreement.AbstractAgreementEntry;
import zdream.nsfplayer.ftm.process.base.FtmPosition;

/**
 * <p>执行器执行状态
 * </p>
 * @author Zdream
 * @since v0.3.1
 */
class ExecutorProcessState {
	
	/**
	 * 执行器 ID
	 */
	final int id;
	
	ExecutorProcessState(int id) {
		this.id = id;
	}

	/**
	 * 该执行器所有的同步协议, 即会导致该执行器发生等待的协议
	 */
	final HashMap<FtmPosition, List<AbstractAgreementEntry>> agreements = new HashMap<>();
	
	/**
	 * 依靠该执行器判断的同步协议. 如果该执行器删除或发生改变将导致变动的协议
	 */
	final ArrayList<AbstractAgreementEntry> refs = new ArrayList<>();
	
	/**
	 * 现在的位置
	 */
	FtmPosition pos;
	
	/**
	 * 正在束缚该执行器、让该执行器等待的协议
	 */
	final ArrayList<AbstractAgreementEntry> bounds = new ArrayList<>();
	
}
