package zdream.nsfplayer.core;

/**
 * <p>Nsf Player 异常
 * <p>该异常类从原来的 FamiTrackerException 直接转换过来
 * </p>
 * 
 * @author Zdream
 * @since v0.3.0
 */
public class NsfPlayerException extends RuntimeException {

	private static final long serialVersionUID = 3361046346702576108L;

	public NsfPlayerException() {
		
	}

	public NsfPlayerException(String message) {
		super(message);
	}

	public NsfPlayerException(Throwable cause) {
		super(cause);
	}

	public NsfPlayerException(String message, Throwable cause) {
		super(message, cause);
	}

	public NsfPlayerException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
