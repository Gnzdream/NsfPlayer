package zdream.nsfplayer.ftm.process.agreement;

import static java.util.Objects.requireNonNull;

import zdream.nsfplayer.core.NsfPlayerException;
import zdream.nsfplayer.ftm.process.base.FtmPosition;
import zdream.nsfplayer.ftm.renderer.FamiTrackerSyncRenderer;

/**
 * <p>单向等待同步协议
 * <p>在 {@link FamiTrackerSyncRenderer} 同时启动多个执行器时, 需要多个执行器同步.
 * 该协议处理这样的一种情况, 当 A 执行器执行到位置 a 时, 查看 B 执行器执行位置.
 * 当 B 执行器到达某个指定位置, A 放行;
 * 若 B 执行器未到达某个位置, A 等待 B 直到 B 到达指定位置.
 * <p>该协议是单向约束, 即 A 等待 B 到指定位置, 对 B 是透明的.
 * B 不需要等待 A 到指定位置, 该协议也不会使 B 等待.
 * <p>该协议模拟多线程并发中的信号量的工作.
 * 
 * <p>协议支持执行器中途退出协议. 如果在上面的例子中, A 或 B 执行器在执行过程中被删除,
 * 那么该协议自动取消.
 * </p>
 * 
 * @author Zdream
 * @since v0.3.1
 */
public class WaitingAgreement extends AbstractAgreement {
	
	public static final String NAME = "WAITING";
	
	/**
	 * @throws NsfPlayerException
	 *   当 <code>waitExeId == dependExeId</code> 时
	 */
	public WaitingAgreement(
			int waitExeId,
			FtmPosition waitPos,
			int dependExeId,
			FtmPosition dependPos) {
		requireNonNull(waitPos, "waitPos == null");
		requireNonNull(dependPos, "dependPos == null");
		if (waitExeId == dependExeId) {
			throw new NsfPlayerException("waitExeId == dependExeId");
		}
		this.waitExeId = waitExeId;
		this.waitPos = waitPos;
		this.dependExeId = dependExeId;
		this.dependPos = dependPos;
	}

	@Override
	public String name() {
		return NAME;
	}

	@Override
	public WaitingAgreementEntry createEntry() {
		return WaitingAgreementEntry.create(this);
	}
	
	public final int waitExeId;
	public final FtmPosition waitPos;
	public final int dependExeId;
	public final FtmPosition dependPos;

}
