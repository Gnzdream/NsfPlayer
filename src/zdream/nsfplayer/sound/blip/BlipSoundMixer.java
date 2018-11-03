package zdream.nsfplayer.sound.blip;

import java.util.HashMap;

import zdream.nsfplayer.ftm.renderer.FamiTrackerParameter;
import zdream.nsfplayer.sound.mixer.SoundMixer;

/**
 * Blip 的音频合成器, FamiTracker 专用
 * @author Zdream
 * @since 0.2.1
 */
public class BlipSoundMixer extends SoundMixer {
	
	public int sampleRate;
	public int frameRate;
	public int bassFilter, trebleDamping, trebleFilter;
	
	public FamiTrackerParameter param;

	@Override
	public void init() {
		int size = sampleRate / frameRate;
		
		buffer.setSampleRate(sampleRate, (size * 1000 * 2) / sampleRate);
		buffer.bassFreq(bassFilter);
	}
	
	@Override
	public void reset() {
		super.reset();

		buffer.clockRate(param.freqPerSec);
	}
	
	/**
	 * 根据配置项重置参数
	 * @param config
	 *   配置项数据
	 * @since v0.2.5
	 */
	public void setConfig(BlipMixerConfig config) {
		this.bassFilter = config.bassFilter;
		this.trebleDamping = config.trebleDamping;
		this.trebleFilter = config.trebleFilter;
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
	
	@Override
	public BlipMixerChannel allocateChannel(byte code) {
		BlipMixerChannel c = new BlipMixerChannel(this);
		mixers.put(code, c);
		
		configMixChannel(code, c);
		c.synth.output(buffer);
		
		// EQ
		BlipEQ eq = new BlipEQ(-trebleDamping, trebleFilter, sampleRate, 0);
		c.synth.trebleEq(eq);
		c.synth.volume(1.0);
		
		return c;
	}

	@Override
	public BlipMixerChannel getMixerChannel(byte code) {
		return mixers.get(code);
	}
	
	/**
	 * 配置音频轨道
	 * @param code
	 *   轨道号
	 */
	private static void configMixChannel(byte code, BlipMixerChannel mixer) {
		switch (code) {
		case CHANNEL_2A03_PULSE1: case CHANNEL_2A03_PULSE2:
		{
			mixer.updateSetting(12, -500);
			mixer.setExpression((x) -> (x > 0) ? (int) (95.88 * 400 / ((8128.0 / x) + 156.0)) : 0);
		} break;
		
		case CHANNEL_2A03_TRIANGLE:
		{
			mixer.updateSetting(12, -500);
			mixer.setExpression((x) -> (x > 0) ? (int) (46159.29 / (1 / (x / 8227.0) + 30.0)) : 0);
		} break;
		
		case CHANNEL_2A03_NOISE:
		{
			mixer.updateSetting(12, -500);
			mixer.setExpression((x) -> (x > 0) ? (int) (41543.36 / (1 / (x / 12241.0) + 30.0)) : 0);
		} break;
		
		case CHANNEL_2A03_DPCM:
		{
			mixer.updateSetting(12, -500);
			mixer.setExpression((x) -> (x > 0) ? (int) (41543.36 / (1 / (x / 22638.0) + 30.0)) : 0);
		} break;
		
		case CHANNEL_MMC5_PULSE1: case CHANNEL_MMC5_PULSE2:
		case CHANNEL_VRC6_PULSE1: case CHANNEL_VRC6_PULSE2:
		case CHANNEL_VRC6_SAWTOOTH:
		{
			mixer.updateSetting(12, -500);
			mixer.setExpression((x) -> (x > 0) ? (int) (96 * 360 / ((8000.0 / x) + 180)) : 0);
		} break;
		
		case CHANNEL_FDS:
		{
			mixer.updateSetting(12, -3500);
			mixer.setExpression((x) -> (x > 0) ? (int) (x / 1.9) : 0);
		} break;
		
		default:
			break;
		}
		
	}
	
	/* **********
	 * 音频合成 *
	 ********** */
	
	/**
	 * 音频缓存
	 */
	BlipBuffer buffer = new BlipBuffer();

	@Override
	public int finishBuffer() {
		int freq = param.freqPerFrame;
		buffer.endFrame(freq);
		
		return buffer.samplesAvail();
	}
	
	@Override
	public int readBuffer(short[] buf, int offset, int length) {
		int ret = buffer.readSamples(buf, offset, length, false);
		
		// 这里为了避免 mixer 缓冲区的溢出, 用了一些方法
		buffer.removeSamples(buffer.samplesAvail());
		
		return ret;
	}

}
