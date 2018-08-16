package zdream.nsfplayer.ftm.renderer.mixer;

import java.util.HashMap;

import zdream.nsfplayer.ftm.renderer.FamiTrackerRuntime;
import zdream.nsfplayer.ftm.renderer.IFtmRuntimeHolder;
import zdream.nsfplayer.ftm.renderer.tools.ChannalDeviceSelector;
import zdream.nsfplayer.sound.buffer.BlipBuffer;
import zdream.nsfplayer.sound.buffer.BlipEQ;
import zdream.nsfplayer.sound.mixer.SoundMixer;

/**
 * Ftm 的音频合成器
 * @author Zdream
 * @since 0.2.1
 */
public class FtmSoundMixer extends SoundMixer implements IFtmRuntimeHolder {
	
	FamiTrackerRuntime runtime;

	public FtmSoundMixer(FamiTrackerRuntime runtime) {
		this.runtime = runtime;
	}

	@Override
	public FamiTrackerRuntime getRuntime() {
		return runtime;
	}
	
	public void init() {
		final int sampleRate = runtime.setting.sampleRate;
		int size = sampleRate / runtime.setting.frameRate;
		
		buffer.setSampleRate(sampleRate, (size * 1000 * 2) / sampleRate);
		buffer.bassFreq(runtime.setting.bassFilter);
	}
	
	@Override
	public void reset() {
		super.reset();

		buffer.clockRate(runtime.param.freqPerSec);
	}
	
	/* **********
	 * 音频管道 *
	 ********** */
	
	/**
	 * 轨道号 - 音频管道
	 */
	HashMap<Byte, BlipMixerChannel> mixers = new HashMap<>();
	
	@Override
	public void detachAll() {
		mixers.clear();
	}
	
	/**
	 * 分配一个新的音频管道
	 * @param code
	 *   轨道号
	 * @return
	 */
	public BlipMixerChannel allocateChannel(byte code) {
		BlipMixerChannel c = new BlipMixerChannel(this);
		mixers.put(code, c);
		
		ChannalDeviceSelector.configMixChannel(code, c);
		c.synth.output(buffer);
		
		// EQ
		BlipEQ eq = new BlipEQ(-runtime.setting.trebleDamping, runtime.setting.trebleFilter,
				runtime.setting.sampleRate, 0);
		c.synth.trebleEq(eq);
		c.synth.volume(1.0);
		
		return c;
	}
	
	public BlipMixerChannel getMixerChannel(byte code) {
		return mixers.get(code);
	}
	
	/* **********
	 * 音频合成 *
	 ********** */
	
	/**
	 * 音频缓存
	 */
	BlipBuffer buffer = new BlipBuffer();

	/**
	 * @return
	 *   返回有多少音频采样数
	 */
	public int finishBuffer() {
		int freq = runtime.param.freqPerFrame;
		buffer.endFrame(freq);
		
		return buffer.samplesAvail();
	}
	
	@Override
	public int readBuffer(short[] buf, int offset, int length) {
		int ret = buffer.readSamples(buf, offset, length, false);
		
		// TODO 这里为了避免 mixer 缓冲区的移出, 用了一些方法
		buffer.removeSamples(buffer.samplesAvail());
		
		return ret;
	}

}
