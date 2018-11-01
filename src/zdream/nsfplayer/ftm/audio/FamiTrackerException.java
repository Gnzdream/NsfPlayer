package zdream.nsfplayer.ftm.audio;

/**
 * FamiTracker 异常
 * 
 * @author Zdream
 * @since v0.2.0
 */
public class FamiTrackerException extends RuntimeException {

	private static final long serialVersionUID = 3361046346702576108L;

	public FamiTrackerException() {
		
	}

	public FamiTrackerException(String message) {
		super(message);
	}

	public FamiTrackerException(Throwable cause) {
		super(cause);
	}

	public FamiTrackerException(String message, Throwable cause) {
		super(message, cause);
	}

	public FamiTrackerException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
