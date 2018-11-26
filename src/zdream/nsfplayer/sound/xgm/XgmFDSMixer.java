package zdream.nsfplayer.sound.xgm;

/**
 * FDS 的 (合并) 轨道
 * 
 * @author Zdream
 * @since v0.2.4
 */
public class XgmFDSMixer extends AbstractXgmMultiMixer {
	
	final XgmAudioChannel fds;
	
	public XgmFDSMixer() {
		fds = new XgmAudioChannel();
	}

	@Override
	public void reset() {
		super.reset();
		fds.reset();
	}

	@Override
	public AbstractXgmAudioChannel getAudioChannel(byte channelCode) {
		if (channelCode == CHANNEL_FDS) {
			return fds;
		}
		return null;
	}

	@Override
	public void beforeRender() {
		super.beforeRender();
		fds.beforeSubmit();
	}
	
	@Override
	public void checkCapacity(int size) {
		fds.checkCapacity(size);
	}

	@Override
	public int render(int index, int fromIdx, int toIdx) {
		int time = toIdx - fromIdx;
		int idx = (fromIdx + toIdx) / 2;
		
		// 最终输出部分
		// 8 bit approximation of master volume
		final double MASTER_VOL = 2935.2; // = 2.4 * 1223.0; max FDS vol vs max APU square (arbitrarily 1223)
		final double MAX_OUT = 2016; // = 32.0f * 63.0f; value that should map to master vol
		
		int value = fds.buffer[idx];
		value = (int) (value * (MASTER_VOL / MAX_OUT));
		value *= fds.getLevel();
		
		value = intercept(value, time);
		return value;
	}

}
