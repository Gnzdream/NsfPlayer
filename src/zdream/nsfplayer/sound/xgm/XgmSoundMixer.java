package zdream.nsfplayer.sound.xgm;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import zdream.nsfplayer.core.FamiTrackerParameter;
import zdream.nsfplayer.core.NsfChannelCode;
import zdream.nsfplayer.sound.mixer.IMixerChannel;
import zdream.nsfplayer.sound.mixer.SoundMixer;

/**
 * Nsf 的音频合成器
 * 
 * @author Zdream
 * @since 0.2.1
 */
public class XgmSoundMixer extends SoundMixer {
	
	public FamiTrackerParameter param;

	public XgmSoundMixer() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void init() {
		
	}
	
	/* **********
	 * 音频管道 *
	 ********** */
	/*
	 * 连接方式是：
	 * sound >> XgmAudioChannel >> AbstractXgmMultiMixer >> XgmSoundMixer
	 * 
	 * 其中, 一个 sound 连一个 XgmAudioChannel
	 * 多个 XgmAudioChannel 连一个 IXgmMultiChannelMixer
	 * 多个 IXgmMultiChannelMixer 连一个 XgmSoundMixer
	 * XgmSoundMixer 只有一个
	 */
	
	HashMap<Byte, AbstractXgmMultiMixer> multis = new HashMap<>();
	
	@Override
	public XgmAudioChannel allocateChannel(byte channelCode) {
		XgmAudioChannel ch = new XgmAudioChannel();
		
		ch.buffer = new short[param.freqPerFrame + 16];
		ch.reset();
		
		byte chip = chipOfChannel(channelCode);
		AbstractXgmMultiMixer multi = multis.get(chip);
		if (multi == null) {
			multi = createMultiChannelMixer(chip);
			multis.put(chip, multi);
		}
		multi.setAudioChannel(channelCode, ch);
		
		return ch;
	}
	
	private byte chipOfChannel(byte channelCode) {
		byte chip = NsfChannelCode.chipOfChannel(channelCode);
		
		if (chip == CHIP_2A03) {
			if (channelCode == CHANNEL_2A03_TRIANGLE || channelCode == CHANNEL_2A03_NOISE
					|| channelCode == CHANNEL_2A03_DPCM) {
				return 0xF; // 表示 TND
			}
		}
		
		return chip;
	}
	
	/**
	 * 按照 chip 选择 IXgmMultiChannelMixer
	 */
	private AbstractXgmMultiMixer createMultiChannelMixer(byte chip) {
		AbstractXgmMultiMixer multi = null;
		
		switch (chip) {
		case CHIP_2A03:
			multi = new Xgm2A03Mixer();
			break;
		case 0xF:
			multi = new Xgm2A07Mixer();
			break;
		}
		
		if (multi != null) {
			Filter f = new Filter();
			f.setRate(param.sampleRate);
			multi.attachIntercept(f);
			
			Amplifier amp = new Amplifier();
			multi.attachIntercept(amp);
		}
		
		return multi;
	}

	@Override
	public void detachAll() {
		multis.clear();
		
		// TODO Auto-generated method stub

	}

	@Override
	public IMixerChannel getMixerChannel(byte channelCode) {
		byte chip = chipOfChannel(channelCode);
		AbstractXgmMultiMixer multi = multis.get(chip);
		
		if (multi != null) {
			return multi.getAudioChannel(channelCode);
		}
		
		return null;
	}
	
	/* **********
	 * 音频合成 *
	 ********** */
	
	short[] samples;

	@Override
	public int finishBuffer() {
		allocateSampleArray();
		
		// 每个轨道的数据汇总到 samples 中
		for (Iterator<HashMap.Entry<Byte, AbstractXgmMultiMixer>> it = multis.entrySet().iterator(); it.hasNext();) {
			HashMap.Entry<Byte, AbstractXgmMultiMixer> entry = it.next();
			AbstractXgmMultiMixer multi = entry.getValue();
			
			multi.render(samples, param.sampleInCurFrame, param.freqPerFrame);
		}
		
		return param.sampleInCurFrame;
	}

	@Override
	public int readBuffer(short[] buf, int offset, int length) {
		int len = Math.min(length, param.sampleInCurFrame);
		System.arraycopy(samples, 0, buf, offset, len);
		
		// 重置 samples
		Arrays.fill(samples, (short) 0);
		
		return len;
	}
	
	/**
	 * 为 sample 数组分配空间, 创建数组.
	 * 创建数组需要知道 param.sampleInCurFrame 的值
	 */
	private void allocateSampleArray() {
		if (this.samples == null || this.samples.length < param.sampleInCurFrame) {
			this.samples = new short[param.sampleInCurFrame + 16];
		}
	}

}
