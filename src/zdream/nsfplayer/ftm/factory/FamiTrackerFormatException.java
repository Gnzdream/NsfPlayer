package zdream.nsfplayer.ftm.factory;

import zdream.nsfplayer.ftm.audio.FamiTrackerException;
import zdream.nsfplayer.ftm.audio.FtmAudio;

/**
 * {@link FtmAudio} 解析时产生的错误
 * 
 * @author Zdream
 * @since v0.1
 */
public class FamiTrackerFormatException extends FamiTrackerException {

	private static final long serialVersionUID = 6874694323781455170L;

	public FamiTrackerFormatException(String message, Throwable cause) {
		super(message, cause);
	}

	public FamiTrackerFormatException(String message) {
		super(message);
	}

	public FamiTrackerFormatException(Throwable cause) {
		super(cause);
	}

}
