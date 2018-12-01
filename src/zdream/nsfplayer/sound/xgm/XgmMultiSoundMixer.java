package zdream.nsfplayer.sound.xgm;

import static java.util.Objects.requireNonNull;
import static zdream.nsfplayer.core.NsfChannelCode.chipOfChannel;

import java.util.ArrayList;
import java.util.Arrays;

import zdream.nsfplayer.core.CycleCounter;
import zdream.nsfplayer.core.NsfCommonParameter;
import zdream.nsfplayer.mixer.AbstractNsfSoundMixer;
import zdream.nsfplayer.mixer.IMixerHandler;
import zdream.nsfplayer.sound.interceptor.Amplifier;
import zdream.nsfplayer.sound.interceptor.Compressor;
import zdream.nsfplayer.sound.interceptor.DCFilter;
import zdream.nsfplayer.sound.interceptor.EchoUnit;
import zdream.nsfplayer.sound.interceptor.Filter;
import zdream.nsfplayer.sound.interceptor.ISoundInterceptor;

/**
 * <p>Xgm 的混音器, 原来是 NsfPlayer 的默认使用混音器.
 * <p>自从 Mixer 渲染部分和 NSF / FTM 的执行部分分离之后,
 * 无论是 FamiTracker 还是 Nsf 部分, 均能够使用 Xgm 混音器作为输出混音器.
 * 
 * <p>与 Blip 混音器不同的是, 它渲染策略是, 对每个采样进行计算、加工, 最后输出.
 * 由于能够操作每个采样点, 因此它的灵活性和可扩展性均比 Blip 混音器高出很多,
 * 但是代价也是很明显的: 慢.
 * 
 * <p>根据测试的结果, 渲染同样的曲目, Xgm 混音器在开启所有内置效果拦截器的情况下,
 * 渲染时间是 Blip 混音器的 1.2 至 10 倍. 这个现象在渲染只有 2A03 + 2A07 轨道,
 * 而且 DPCM 轨道不发出声音的情况下尤为明显. 因此如果没有对音频数据作处理的需求, 建议使用 Blip 混音器.
 * 
 * <p>内置的效果拦截器中, 回音构造器花费的时间最长. 因此如果播放卡顿, 优先关闭回音构造器.
 * 关闭内置回音的方法: 以 NsfRenderer 为例:
 * <blockquote><pre>
 *     NsfRenderer renderer;
 *     
 *     ...
 * 
 *     XgmMixerHandler h = (XgmMixerHandler) renderer.getMixerHandler();
 *     List<ISoundInterceptor> itcs = h.getGlobalInterceptors();
 *     for (ISoundInterceptor itc: itcs) {
 *        if (itc instanceof EchoUnit) {
 *           itc.setEnable(false);
 *        }
 *     }
 * </pre></blockquote>
 * 以上方法能够成功的条件是启用 Xgm 混音器作为输出混音器,
 * 否则<code>renderer.getMixerHandler()</code> 不会返回 <code>XgmMixerHandler</code> 实例.
 * </p>
 * 
 * @version v0.2.10
 *   <br>自从大部分 Renderer 类开放了获取混音器操作类 {@link IMixerHandler} 的获取之后,
 *   音频拦截器的使用终于给用户们开放了. 这也极大地添加了 Xgm 混音器的灵活性.
 *   <br>另外, 这个版本对 Xgm 混音器作了大幅度的优化, 它的运行效率同版本 v0.2.9 相比
 *   提高了 10% - 30%.
 * 
 * @author Zdream
 * @since v0.2.1
 */
public class XgmMultiSoundMixer extends AbstractNsfSoundMixer<AbstractXgmAudioChannel> {
	
	public NsfCommonParameter param;

	public XgmMultiSoundMixer() {
		
	}
	
	@Override
	public void init() {
		initInterceptors();
	}

	/**
	 * 设置配置项
	 * @param config
	 *   配置项数据
	 * @since v0.2.5
	 */
	public void setConfig(XgmMixerConfig config) {
		
	}
	
	/* **********
	 * 轨道参数 *
	 ********** */
	
	protected class XgmMultiChannelAttr extends ChannelAttr {
		protected XgmMultiChannelAttr(byte code, AbstractXgmAudioChannel t) {
			super(code, t);
		}
		
		AbstractXgmMultiMixer multi;
	}
	
	@Override
	protected XgmMultiChannelAttr createChannelAttr(final byte code) {
		
		AbstractXgmAudioChannel channel;
		for (AbstractXgmMultiMixer multi : multiList) {
			channel = multi.getRemainAudioChannel(code);
			if (channel != null) {
				// 将该轨道插入到原来已经存在的合并轨道中
				XgmMultiChannelAttr attr = new XgmMultiChannelAttr(code, channel);
				multi.setEnable(channel, true);
				attr.multi = multi;
				return attr;
			}
		}
		
		// 这里就需要创建合并轨道了
		byte chip = chipOfChannel(code);
		AbstractXgmMultiMixer multi = createMultiChannelMixer(chip);
		multiList.add(multi);
		multiArray = null;
		
		channel = multi.getRemainAudioChannel(code);
		requireNonNull(channel);
		
		XgmMultiChannelAttr attr = new XgmMultiChannelAttr(code, channel);
		multi.setEnable(channel, true);
		attr.multi = multi;
		return attr;
	}
	
	XgmMultiChannelAttr getAttr(int id) {
		if (attrs.size() <= id) {
			return null;
		}
		return (XgmMultiChannelAttr) attrs.get(id);
	}
	
	/* **********
	 * 音频管道 *
	 ********** */
	/*
	 * 连接方式是：
	 * sound (执行构件) >> XgmAudioChannel >> AbstractXgmMultiMixer >> XgmMultiSoundMixer
	 * 
	 * 其中, 一个 sound 连一个 XgmAudioChannel
	 * 多个 XgmAudioChannel 连一个 IXgmMultiChannelMixer
	 * 多个 IXgmMultiChannelMixer 连一个 XgmMultiSoundMixer
	 * XgmMultiSoundMixer 只有一个
	 */
	
	private final ArrayList<AbstractXgmMultiMixer> multiList = new ArrayList<>();
	
	/**
	 * 缓存, 性能考虑
	 */
	private AbstractXgmMultiMixer[] multiArray;
	
	/**
	 * 按照 chip 选择 IXgmMultiChannelMixer
	 */
	private AbstractXgmMultiMixer createMultiChannelMixer(byte chip) {
		AbstractXgmMultiMixer multi = null;
		
		switch (chip) {
		case CHIP_2A03:
			multi = new Xgm2A03Mixer();
			break;
		case CHIP_2A07:
			multi = new Xgm2A07Mixer();
			break;
		case CHIP_VRC6:
			multi = new XgmVRC6Mixer();
			break;
		case CHIP_MMC5:
			multi = new XgmMMC5Mixer();
			break;
		case CHIP_FDS:
			multi = new XgmFDSMixer();
			break;
		case CHIP_N163:
			multi = new XgmN163Mixer();
			break;
		case CHIP_VRC7:
			multi = new XgmVRC7Mixer();
			break;
		case CHIP_S5B:
			multi = new XgmS5BMixer();
			break;
		}
		
		if (multi != null) {
			Filter f = new Filter();
			f.setRate(param.sampleRate);
			f.setParam(4700, 0);
			multi.attachIntercept(f);

			Amplifier amp = new Amplifier();
			amp.setCompress(100, -1);
			multi.attachIntercept(amp);
		}
		
		return multi;
	}

	@Override
	public void detachAll() {
		multiList.clear();
		multiArray = null;
		super.detachAll();
	}
	
	@Override
	public void detach(int id) {
		XgmMultiChannelAttr attr = (XgmMultiChannelAttr) attrs.get(id);
		attr.multi.setEnable(attr.channel, false);
		
		super.detach(id);
	}
	
	@Override
	public void reset() {
		multiList.forEach(multi -> multi.reset());
		interceptors.forEach(i -> i.reset());
	}
	
	/* **********
	 * 音频合成 *
	 ********** */
	
	short[] samples;

	/**
	 * 拦截器组
	 */
	final ArrayList<ISoundInterceptor> interceptors = new ArrayList<>();
	
	/**
	 * 缓存, 性能考虑
	 */
	private ISoundInterceptor[] interceptorArray;
	
	/**
	 * 用于计算周期和采样关系的模型
	 */
	private final CycleCounter counter = new CycleCounter();
	
	private void initInterceptors() {
		// 构造拦截器组
		EchoUnit echo = new EchoUnit();
		echo.setRate(param.sampleRate);
		attachIntercept(echo); // 注意, 回音是这里产生的. 如果想去掉回音, 修改这里

		DCFilter dcf = new DCFilter();
		dcf.setRate(param.sampleRate);
		dcf.setParam(270, 164);
		attachIntercept(dcf);

		Filter f = new Filter();
		f.setRate(param.sampleRate);
		f.setParam(4700, 112);
		attachIntercept(f);

		Compressor cmp = new Compressor();
		cmp.setParam(1, 1, 1);
		attachIntercept(cmp);
	}
	
	/**
	 * @param value
	 * @param time
	 *   过去的时钟周期数
	 * @return
	 */
	int intercept(int value, int time) {
		int ret = value;
		final int length = interceptorArray.length;
		for (int i = 0; i < length; i++) {
			ISoundInterceptor interceptor = interceptorArray[i];
			if (interceptor.isEnable()) {
				ret = interceptor.execute(ret, time);
			}
		}
		return ret;
	}
	
	/**
	 * 添加音频数据的拦截器
	 * @param interceptor
	 */
	public void attachIntercept(ISoundInterceptor interceptor) {
		if (interceptor != null) {
			interceptors.add(interceptor);
		}
	}
	
	@Override
	public void readyBuffer() {
		allocateSampleArray();
		final int len = multiList.size();
		for (int i = 0; i < len; i++) {
			AbstractXgmMultiMixer multi = multiList.get(i);
			multi.checkCapacity(param.freqPerFrame, param.sampleInCurFrame);
		}
	}

	@Override
	public int finishBuffer() {
		beforeRender();
		final int length = param.sampleInCurFrame;
		int v, fromTime, delta, toTime = 0;
		for (int i = 0; i < length; i++) {
			
			// 渲染一帧的流程
			fromTime = toTime;
			delta = counter.tick();
			toTime = counter.getCycleCount();
			// fromTime 和 toTime 是时钟数
			v = 0;
			
			final int mlen = multiArray.length;
			for (int midx = 0; midx < mlen; midx++) {
				AbstractXgmMultiMixer multi = multiArray[midx];
				v += multi.render(i, fromTime, toTime);
			}
			v = intercept(v, delta) >> 1;
			
			if (v > Short.MAX_VALUE) {
				v = Short.MAX_VALUE;
			} else if (v < Short.MIN_VALUE) {
				v = Short.MIN_VALUE;
			}
			samples[i] = (short) v;
		}
		
		return length;
	}
	
	private void beforeRender() {
		final int len = multiList.size();
		for (int i = 0; i < len; i++) {
			AbstractXgmMultiMixer multi = multiList.get(i);
			multi.beforeRender();
		}
		
		counter.setParam(param.freqPerFrame, param.sampleInCurFrame);
		
		// 以下是性能考虑
		if (interceptorArray == null || interceptorArray.length != interceptors.size()) {
			interceptorArray = new ISoundInterceptor[interceptors.size()];
		}
		interceptors.toArray(interceptorArray);
		
		if (multiArray == null) {
			multiArray = new AbstractXgmMultiMixer[multiList.size()];
			multiList.toArray(multiArray);
		}
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
	 * 在创建数组的同时, 构造输出相关的拦截器.
	 * 创建数组需要知道 param.sampleInCurFrame 的值
	 */
	private void allocateSampleArray() {
		if (this.samples != null) {
			if (this.samples.length < param.sampleInCurFrame) {
				this.samples = new short[param.sampleInCurFrame + 16];
			}
			return;
		}
		
		this.samples = new short[param.sampleInCurFrame + 16];
	}
	
	/* **********
	 * 用户操作 *
	 ********** */
	
	XgmMixerHandler handler;
	
	@Override
	public XgmMixerHandler getHandler() {
		if (handler == null) {
			handler = new XgmMixerHandler(this);
		}
		return handler;
	}

}
