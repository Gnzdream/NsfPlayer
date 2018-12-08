package zdream.nsfplayer.ftm.agreement;

import zdream.nsfplayer.core.NsfPlayerException;

/**
 * <p>拥有超时时间的抽象协议
 * </p>
 * @author Zdream
 * @since v0.3.1
 */
public abstract class TimeoutAgreement {
	
	/* **********
	 * 超时时间 *
	 ********** */
	
	/**
	 * <p>超时时间. 若一部分执行器到达指定位置, 记录最先到达的执行器等待时间.
	 * 如果超过了超时时间, 还有协议中指定的执行器没有到指定位置,
	 * 所有该协议等待中的执行器同时放行.
	 * <p>时间单位为一帧, 为正数.
	 * </p>
	 */
	private int timeout = 60;
	
	/**
	 * @return
	 *   超时时间
	 * @see #timeout
	 */
	public int getTimeout() {
		return timeout;
	}
	
	/**
	 * 设置超时时间
	 * @param timeout
	 *   超时时间, 正数.
	 * @see #timeout
	 */
	public void setTimeout(int timeout) {
		if (timeout < 1) {
			throw new NsfPlayerException("超时时间: " + timeout + " 必须为正数");
		}
		this.timeout = timeout;
	}

}
