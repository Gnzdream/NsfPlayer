package zdream.nsfplayer.ftm.process.agreement;

import static java.util.Objects.requireNonNull;

import java.lang.ref.WeakReference;

/**
 * <p>协议包含的内容数据
 * </p>
 * 
 * @author Zdream
 * @since v0.3.1
 */
public abstract class AbstractAgreementEntry {
	
	/**
	 * 对原来实例的弱引用.
	 */
	private final WeakReference<AbstractAgreement> ref;
	
	/**
	 * 超时时间
	 */
	public final int baseTimeout;

	/**
	 * 若正在触发中, 大于等于 0, 值为剩余的超时时间; 不触发时为 -1
	 */
	public int countdown = -1;

	public AbstractAgreementEntry(AbstractAgreement ref) {
		requireNonNull(ref, "agreement == null");
		this.ref = new WeakReference<AbstractAgreement>(ref);
		this.baseTimeout = ref.getTimeout();
	}
	
	public boolean is(AbstractAgreement a) {
		return a == ref.get();
	}

}
