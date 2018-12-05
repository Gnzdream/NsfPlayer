package zdream.nsfplayer.ftm.executor;

import zdream.nsfplayer.core.AbstractRenderer;
import zdream.nsfplayer.ftm.audio.FtmAudio;

/**
 * <p>FamiTracker 同步音频渲染器.
 * <p>支持多个 {@link FtmAudio} 同时进行播放, 并补充部分停等协议.
 * </p>
 * 
 * @author Zdream
 * @since v0.3.1
 */
public class FamiTrackerSyncRenderer extends AbstractRenderer<FtmAudio> {

	@Override
	public void ready(FtmAudio audio) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isFinished() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected int renderFrame() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int skipFrame() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setSpeed(float speed) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public float getSpeed() {
		// TODO Auto-generated method stub
		return 0;
	}

}
