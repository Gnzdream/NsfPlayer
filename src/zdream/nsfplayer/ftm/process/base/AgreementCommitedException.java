package zdream.nsfplayer.ftm.process.base;

import zdream.nsfplayer.core.NsfPlayerException;

/**
 * <p>当协议已经提交后再次尝试修改时, 将抛出该错误
 * </p>
 * 
 * @author Zdream
 * @since v0.3.1
 */
public class AgreementCommitedException extends NsfPlayerException {

	private static final long serialVersionUID = 7173441668934880803L;

	public AgreementCommitedException() {
		
	}

	public AgreementCommitedException(String message) {
		super(message);
	}

	public AgreementCommitedException(Throwable cause) {
		super(cause);
	}

	public AgreementCommitedException(String message, Throwable cause) {
		super(message, cause);
	}

	public AgreementCommitedException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
