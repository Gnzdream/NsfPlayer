package zdream.nsfplayer.mixer;

import java.util.Arrays;

/**
 * <p>一个多声道拆分多个单声道音频轨道.
 * <p>将一个多声道输入源的数据拆分成多条单声道输出.
 * 它与 {@link SplitMixerChannel} 组合是多声道输出的解决方案
 * </p>
 * 
 * @see SplitMixerChannel
 * @author Zdream
 * @since v0.3.0
 */
public class SplitTrackChannel implements ITrackChannel {
	
	public SplitTrackChannel() {
		setTrackCount(1);
	}

	/* **********
	 * 声道轨道 *
	 ********** */
	
	private int trackCount;
	private IMixerChannel[] outs;
	
	public int getTrackCount() {
		return trackCount;
	}
	
	/**
	 * 设置声道数
	 * @param trackCount
	 *   声道数. 必须大于等于 1
	 * @throws IllegalArgumentException
	 *   当声道数小于 1 时
	 */
	public void setTrackCount(int trackCount) {
		if (trackCount < 1) {
			throw new IllegalArgumentException("声道数: " + trackCount + " 必须大于 1");
		}
		
		if (outs != null) {
			outs = Arrays.copyOf(outs, trackCount);
		} else {
			outs = new IMixerChannel[trackCount];
		}
		
		if (levels != null) {
			int oldLen = this.trackCount;
			levels = Arrays.copyOf(levels, trackCount);
			if (trackCount > oldLen) {
				Arrays.fill(levels, oldLen, trackCount, 1.0f);
			}
		} else {
			levels = new float[trackCount];
		}
		
		this.trackCount = trackCount;
	}

	/* **********
	 *   音量   *
	 ********** */
	
	/**
	 * 总轨道音量值.
	 */
	private float masterLevel;
	private float[] levels;

	@Override
	public void setLevel(float level) {
		if (level > 1) {
			level = 1;
		} else if (level < 0) {
			level = 0;
		}
		this.masterLevel = level;
	}

	@Override
	public float getLevel() {
		return masterLevel;
	}

	@Override
	public void mix(int value, int time) {
		for (int i = 0; i < outs.length; i++) {
			IMixerChannel ch = outs[i];
			if (ch != null) {
				ch.mix(value, time);
			}
		}
	}

	@Override
	public void reset() {
		for (int i = 0; i < outs.length; i++) {
			IMixerChannel ch = outs[i];
			if (ch != null) {
				ch.reset();
			}
		}
	}

	@Override
	public void setTrackLevel(float level, int track) {
		this.levels[track] = level;
	}

	@Override
	public float getTrackLevel(int track) {
		return this.levels[track];
	}

}
