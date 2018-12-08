package zdream.nsfplayer.ftm.agreement;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;

import zdream.nsfplayer.ftm.renderer.FamiTrackerSyncRenderer;

/**
 * <p>栅栏同步协议
 * <p>在 {@link FamiTrackerSyncRenderer} 同时启动多个执行器时, 需要多个执行器同步.
 * 该协议处理这样的一种情况, 当 A 执行器执行到位置 a, 或者 B 执行器执行到位置 b,
 * 两个或多个执行器执行到指定位置时, 都必须等待其它执行器执行到指定位置后, 
 * 才能够同时放行、向下执行.
 * <p>该协议模拟多线程并发中的栅栏的工作.
 * 
 * <p>协议支持某个执行器中途退出协议. 如果 A 执行器在执行过程中被删除,
 * 那么 A 执行器以及相关的数据从将从协议中剔除.
 * 如果协议中剩余的执行器个数少于等于 1, 该协议自动取消.
 * </p>
 * 
 * @author Zdream
 * @since v0.3.1
 */
public class BarrierAgreement extends TimeoutAgreement {
	
	/* **********
	 * 协议内容 *
	 ********** */
	
	/**
	 * exeId - pos
	 */
	private final HashMap<Integer, FtmPosition> poses = new HashMap<>();
	
	public HashMap<Integer, FtmPosition> getPoses() {
		return new HashMap<>(poses);
	}
	
	public void put(int exeId, FtmPosition pos) {
		requireNonNull(pos, "pos == null");
		poses.put(exeId, pos);
	}
}
