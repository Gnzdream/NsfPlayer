package zdream.nsfplayer.ftm.executor.hook;

import java.util.ArrayList;

import zdream.nsfplayer.ftm.executor.FamiTrackerExecutorHandler;

/**
 * 每帧打印帧音量值的日志打印类
 * 
 * @author Zdream
 * @since v0.3.0
 */
public class VolumeLogger implements IFtmExecutedListener {

	@Override
	public void onExecuteFinished(FamiTrackerExecutorHandler handler) {
		System.out.println(createText(handler));
	}
	
	protected StringBuilder createText(FamiTrackerExecutorHandler handler) {
		StringBuilder b = new StringBuilder(128);
		b.append(String.format("%02d:%03d", handler.getCurrentSection(), handler.getCurrentRow()));
		b.append(' ');
		
		// 轨道效果
		ArrayList<Byte> channels = new ArrayList<>(handler.allChannelSet());
		channels.sort(null);
		for (byte channelCode : channels) {
			int v = handler.currentVolume(channelCode);
			if (!handler.isChannelPlaying(channelCode)) {
				v = 0;
			}
			b.append(String.format("%3d|", v));
		}
		
		return b;
	}

}
