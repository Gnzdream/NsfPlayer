package zdream.nsfplayer.xgm.device;

/**
 * @author Zdream
 * 本程序是按怎样运行效率高而编写, 因此不考虑 Java 封装规则等
 */
public class TrackInfoBasic implements ITrackInfo {
	
	public int output;
	public int volume;
	public int maxVolume;
	public int _freq;
	public double freq;
	public boolean key;
	public int tone;

	@Override
	public int getOutput() {
		return output;
	}

	@Override
	public double getFreqHz() {
		return freq;
	}

	@Override
	public int getFreq() {
		return _freq;
	}

	@Override
	public int getVolume() {
		return volume;
	}

	@Override
	public int getMaxVolumn() {
		return maxVolume;
	}

	@Override
	public boolean getKeyStatus() {
		return key;
	}

	@Override
	public int getTone() {
		return tone;
	}

	@Override
	public ITrackInfo clone() {
		TrackInfoBasic o = new TrackInfoBasic();
		o._freq = _freq;
		o.freq = freq;
		o.key = key;
		o.maxVolume = maxVolume;
		o.output = output;
		o.tone = tone;
		o.volume = volume;
		return o;
	}
	
	protected void clone(TrackInfoBasic o) {
		o._freq = _freq;
		o.freq = freq;
		o.key = key;
		o.maxVolume = maxVolume;
		o.output = output;
		o.tone = tone;
		o.volume = volume;
	}

}
