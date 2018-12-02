package zdream.nsfplayer.ftm.executor.hook;

import java.util.ArrayList;
import java.util.Iterator;

import zdream.nsfplayer.ftm.executor.FamiTrackerExecutorHandler;
import zdream.nsfplayer.ftm.executor.effect.IFtmEffect;

/**
 * 每帧打印帧效果的日志打印类
 * 
 * @author Zdream
 * @since v0.3.0
 */
public class EffectLogger implements IFtmExecutedListener {

	@Override
	public void onExecuteFinished(FamiTrackerExecutorHandler handler) {
		System.out.println(createText(handler));
	}
	
	protected StringBuilder createText(FamiTrackerExecutorHandler handler) {
		StringBuilder b = new StringBuilder(128);
		b.append(String.format("%02d:%03d", handler.getCurrentSection(), handler.getCurrentRow()));
		
		// 轨道效果
		ArrayList<Byte> channels = new ArrayList<>(handler.allChannelSet());
		channels.sort(null);
		for (byte channelCode : channels) {
			Iterator<IFtmEffect> it = handler.channelEffects(channelCode);
			if (!it.hasNext()) {
				continue;
			}
			
			b.append(' ').append(Integer.toHexString(channelCode)).append('=').append('[');
			
			while (it.hasNext()) {
				b.append(it.next());
				if (it.hasNext()) {
					b.append(", ");
				}
			}
			b.append(']');
		}
		
		// 全局效果
		Iterator<IFtmEffect> it = handler.globalEffects();
		if (it.hasNext()) {
			b.append(' ').append("G").append('=').append('[');
			while (it.hasNext()) {
				b.append(it.next());
				if (it.hasNext()) {
					b.append(", ");
				}
			}
			b.append(']');
		}
		
		return b;
	}

}
