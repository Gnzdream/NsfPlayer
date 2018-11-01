package zdream.nsfplayer.mpeg;

/**
 * Mpeg 异常
 * 
 * @author Zdream
 * @since v0.2.0
 */
public class MpegAudioException extends Exception {

	private static final long serialVersionUID = -294885338875247506L;

	public MpegAudioException() {
		
	}

	public MpegAudioException(String message) {
		super(message);
	}

	public MpegAudioException(Throwable cause) {
		super(cause);
	}

	public MpegAudioException(String message, Throwable cause) {
		super(message, cause);
	}

	public MpegAudioException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
