package zdream.nsfplayer.nsf.executor;

/**
 * <p>NSF 中, 绝大部分的芯片的输出发声器个数是初始化时确定的,
 * 除了 N163 芯片. 只有 N163 芯片的发声器个数是在运行时确定的.
 * <p>因此, 当 N163 芯片确定了后面的发声器个数, 则它会向用户外
 * 报告这个信息. 这里使用监听器接口, 用于让外部的用户感知到这一个情况的发生,
 * 以此来对 N163 轨道的输出进行连接.
 * </p>
 * 
 * @author Zdream
 * @since v0.3.0
 */
public interface IN163ReattachListener {
	
	/**
	 * 根据重置后 N163 的轨道数, 对用户发出信号
	 * @param n163ChannelCount
	 *   重置后 N163 的轨道数
	 */
	public void onReattach(int n163ChannelCount);

}
