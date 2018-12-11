package zdream.nsfplayer.mixer.blip;

import zdream.nsfplayer.core.IExpression;
import zdream.nsfplayer.core.NsfCommonParameter;
import zdream.nsfplayer.mixer.AbstractNsfSoundMixer;
import zdream.nsfplayer.mixer.NsfMixerSoundConvertor;

/**
 * <p>Blip 的音频合成器, 原 FamiTracker 专用
 * <p>没有很多的功能, 但是处理速度非常快.
 * 如果在实时场景, 且不需要其它混音效果需求时, 推荐使用该混音器
 * </p>
 * 
 * @author Zdream
 * @since 0.2.1
 */
public class BlipSoundMixer extends AbstractNsfSoundMixer<BlipMixerChannel> {
	
	public int sampleRate;
	public int bassFilter, trebleDamping, trebleFilter;
	
	public NsfCommonParameter param;
	
	/**
	 * 上一帧 buffer 的大小
	 */
	private int oldSize;
	
	@Override
	public void init() {
		int size = sampleRate / 50; // 帧率在最低值 50, 这样可以保证高帧率 (比如 60) 也能兼容
		oldSize = (size * 1000 * 2) / sampleRate;
		
		buffer.setSampleRate(sampleRate, oldSize);
		buffer.bassFreq(bassFilter);
	}
	
	@Override
	public void reset() {
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
	 * 轨道参数 *
	 ********** */
	
	@Override
	protected ChannelAttr createChannelAttr(byte type) {
		BlipMixerChannel c = new BlipMixerChannel(this);
		
		configMixChannel(type, c);
		c.synth.output(buffer);
		
		// EQ
		BlipEQ eq = new BlipEQ(-trebleDamping, trebleFilter, sampleRate, 0);
		c.synth.trebleEq(eq);
		c.synth.volume(1.0);
		
		return new ChannelAttr(type, c);
	}
	
	@Override
	public void setInSample(int id, int inSample) {
		ChannelAttr a = attrs.get(id);
		if (a == null) {
			return;
		}
		
		BlipMixerChannel c = a.channel;
		c.synth.in_sample_rate(inSample * param.frameRate);
	}
	
	/* **********
	 * 音频管道 *
	 ********** */
	
	/**
	 * 配置音频轨道
	 * @param type
	 *   轨道类型
	 */
	private static void configMixChannel(byte type, BlipMixerChannel mixer) {
		IExpression exp = NsfMixerSoundConvertor.getExpression(type);
		mixer.setExpression(exp);
		
		switch (type) {
		case CHANNEL_TYPE_PULSE:
		case CHANNEL_TYPE_TRIANGLE:
		case CHANNEL_TYPE_NOISE:
		case CHANNEL_TYPE_DPCM:
		case CHANNEL_TYPE_MMC5_PULSE:
		case CHANNEL_TYPE_VRC6_PULSE:
		case CHANNEL_TYPE_SAWTOOTH:
		{
			mixer.updateSetting(12, -500);
		} break;
		
		case CHANNEL_TYPE_FDS:
		{
			mixer.updateSetting(12, -420);
		} break;
		
		case CHANNEL_TYPE_N163:
		case CHANNEL_TYPE_VRC7:
		{
			mixer.updateSetting(12, -600);
		} break;
		
		case CHANNEL_TYPE_S5B:
		{
			mixer.updateSetting(12, -800);
		} break;
		
		default:
		{
			mixer.updateSetting(12, -1);
		} break;
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
	public void readyBuffer() {
		int size = param.sampleInCurFrame;
		this.sampleRate = param.sampleRate;
		int newSize = (size * 1000 * 2) / sampleRate;
		
		// 合理的振幅为 4
		if (newSize > oldSize + 4 || newSize < oldSize - 4) {
			buffer.setSampleRate(sampleRate, newSize);
			oldSize = newSize;
		}
	}

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
	
	/* **********
	 * 用户操作 *
	 ********** */
	
	BlipMixerHandler handler;
	
	@Override
	public BlipMixerHandler getHandler() {
		if (handler == null) {
			handler = new BlipMixerHandler(this);
		}
		return handler;
	}

}
