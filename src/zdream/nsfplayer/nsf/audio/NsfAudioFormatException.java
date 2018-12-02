package zdream.nsfplayer.nsf.audio;

import zdream.nsfplayer.core.NsfPlayerException;

/**
 * Nsf Audio 的解析出现的异常
 * 
 * @author Zdream
 * @since v0.2.0
 */
public class NsfAudioFormatException extends NsfPlayerException {

	private static final long serialVersionUID = -3793173846691529105L;

	public NsfAudioFormatException() {
		
	}

	public NsfAudioFormatException(String message) {
		super(message);
	}

	public NsfAudioFormatException(Throwable cause) {
		super(cause);
	}

	public NsfAudioFormatException(String message, Throwable cause) {
		super(message, cause);
	}

	public NsfAudioFormatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
