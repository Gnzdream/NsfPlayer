package zdream.nsfplayer.ftm.process.agreement;

import zdream.nsfplayer.core.NsfPlayerException;
import zdream.nsfplayer.ftm.process.base.AgreementCommitedException;

/**
 * <p>拥有超时时间的抽象协议
 * </p>
 * @author Zdream
 * @since v0.3.1
 */
public abstract class AbstractAgreement {
	
	public abstract String name();
	
	/**
	 * 产生内容实体
	 * @return
	 */
	public abstract AbstractAgreementEntry createEntry();
	
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
	 * @throws AgreementCommitedException
	 *   如果在调用该方法时已经提交了本协议, 将拒绝修改并抛出该异常
	 * @see #timeout
	 */
	public void setTimeout(int timeout) {
		synchronized (this) {
			if (commited) {
				throw new AgreementCommitedException("协议已经提交, 不允许修改: " + this);
			}
		}
		
		if (timeout < 1) {
			throw new NsfPlayerException("超时时间: " + timeout + " 必须为正数");
		}
		this.timeout = timeout;
	}
	
	/**
	 * 设置超时时间为永远, 即永远不超时
	 * @see #setTimeout(int)
	 */
	public void setTimeoutForever() {
		setTimeout(Integer.MAX_VALUE);
	}
	
	/* **********
	 *   提交   *
	 ********** */
	/*
	 * 协议当提交之后将不能够修改了
	 */
	private volatile boolean commited = false;
	
	public synchronized void commit() {
		setCommited(true);
	}
	
	/**
	 * <p>询问该协议是否已经被提交处理.
	 * <p>当该协议已经提交之后, 如果对它的基础属性修改, 会拒绝并抛出异常.
	 * </p>
	 */
	public synchronized boolean isCommited() {
		return commited;
	}
	
	protected synchronized void setCommited(boolean commited) {
		this.commited = commited;
	}
}
