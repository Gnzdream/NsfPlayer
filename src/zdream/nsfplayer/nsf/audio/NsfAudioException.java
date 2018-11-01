package zdream.nsfplayer.nsf.audio;

/**
 * NsfPlayer 异常
 * 
 * @author Zdream
 * @since v0.2.0
 */
public class NsfAudioException extends RuntimeException {

	private static final long serialVersionUID = -3793173846691529105L;

	public NsfAudioException() {
		
	}

	public NsfAudioException(String message) {
		super(message);
	}

	public NsfAudioException(Throwable cause) {
		super(cause);
	}

	public NsfAudioException(String message, Throwable cause) {
		super(message, cause);
	}

	public NsfAudioException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
