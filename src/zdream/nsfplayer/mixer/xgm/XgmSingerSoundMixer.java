package zdream.nsfplayer.mixer.xgm;

import java.util.ArrayList;

import zdream.nsfplayer.core.NsfCommonParameter;
import zdream.nsfplayer.core.NsfPlayerException;
import zdream.nsfplayer.mixer.AbstractNsfSoundMixer;
import zdream.nsfplayer.mixer.NsfMixerSoundConvertor;
import zdream.nsfplayer.mixer.interceptor.Compressor;
import zdream.nsfplayer.mixer.interceptor.DCFilter;
import zdream.nsfplayer.mixer.interceptor.EchoUnit;
import zdream.nsfplayer.mixer.interceptor.Filter;
import zdream.nsfplayer.mixer.interceptor.ISoundInterceptor;

/**
 * <p>Xgm 的单轨混音器.
 * <p>与合并轨混音器 {@link XgmMultiSoundMixer} 不同的是,
 * 每个不同的轨道单独为一个单位控制.
 * </p>
 * 
 * @author Zdream
 * @since v0.3.0
 */
public class XgmSingerSoundMixer extends AbstractNsfSoundMixer<XgmSingleChannel> {
	
	public NsfCommonParameter param;
	
	public XgmSingerSoundMixer() {
		setTrackCount(1);
	}
	
	/* **********
	 * 轨道参数 *
	 ********** */
	
	/**
	 * 全局参数 : 声道数. 1 表示单声道, 2 表示立体声, 可以 3 或者更多.
	 */
	int trackCount = 1;
	
	/**
	 * @return
	 *   当前的声道数
	 */
	public int getTrackCount() {
		return trackCount;
	}

	@Override
	protected ChannelAttr createChannelAttr(byte code) {
		XgmSingleChannel c = new XgmSingleChannel();
		
		c.store = new XgmAudioChannel();
		c.exp = NsfMixerSoundConvertor.getExpression(code);
		c.setTrackCount(trackCount, param);
		
		return new ChannelAttr(code, c);
	}
	
	@Override
	public void setInSample(int id, int inSample) {
		ChannelAttr attr = attrs.get(id);
		if (attr == null) {
			return;
		}
		
		if (inSample <= 0) {
			attr.inSample = 0;
		} else {
			attr.inSample = inSample;
		}
	}
	
	/**
	 * 声道数改变之后, 声道音量、拦截器组会重置, 前面的所有修改全部被丢弃.
	 * @param trackCount
	 *   声道数
	 */
	@SuppressWarnings("unchecked")
	public void setTrackCount(int trackCount) {
		if (trackCount <= 0) {
			throw new NsfPlayerException("声道数: " + trackCount + " 为非法值");
		}
		
		this.trackCount = trackCount;
		
		samples = new short[trackCount][];
		
		// 轨道
		int len = attrs.size();
		for (int i = 0; i < len; i++) {
			ChannelAttr attr = attrs.get(i);
			if (attr == null) {
				continue;
			}
			
			attr.channel.setTrackCount(trackCount, param);
		}
		
		// 拦截器组
		interceptors = new ArrayList[trackCount];
		for (int i = 0; i < trackCount; i++) {
			interceptors[i] = initInterceptors(new ArrayList<>());
		}
		interceptorArray = new ISoundInterceptor[trackCount][];
	}
	
	/* **********
	 * 音频合成 *
	 ********** */
	
	short[][] samples;
	
	/**
	 * [声道数]
	 */
	protected ArrayList<ISoundInterceptor>[] interceptors;
	
	/**
	 * 缓存, 性能考虑
	 */
	private ISoundInterceptor[][] interceptorArray;
	
	private ArrayList<ISoundInterceptor> initInterceptors(ArrayList<ISoundInterceptor> array) {
		// 构造拦截器组
		EchoUnit echo = new EchoUnit();
		echo.setRate(param.sampleRate);
		array.add(echo); // 注意, 回音是这里产生的. 如果想去掉回音, 修改这里

		DCFilter dcf = new DCFilter();
		dcf.setRate(param.sampleRate);
		dcf.setParam(270, 164);
		array.add(dcf);

		Filter f = new Filter();
		f.setRate(param.sampleRate);
		f.setParam(4700, 112);
		array.add(f);

		Compressor cmp = new Compressor();
		cmp.setParam(1, 1, 1);
		array.add(cmp);
		
		return array;
	}
	
	/**
	 * @param value
	 * @param time
	 *   过去的时钟周期数
	 * @param track
	 *   声道
	 * @return
	 */
	int intercept(int value, int time, int track) {
		int ret = value;
		final int length = interceptorArray.length;
		ISoundInterceptor[] array = interceptorArray[track];
		
		for (int i = 0; i < length; i++) {
			ISoundInterceptor interceptor = array[i];
			if (interceptor.isEnable()) {
				ret = interceptor.execute(ret, time);
			}
		}
		return ret;
	}
	
	/**
	 * 添加音频数据的拦截器
	 * @param interceptor
	 * @param track
	 *   声道
	 */
	public void attachIntercept(ISoundInterceptor interceptor, int track) {
		if (interceptor != null) {
			interceptors[track].add(interceptor);
		}
	}
	
	/* **********
	 * 公共方法 *
	 ********** */

	@Override
	public void reset() {
		for (ChannelAttr attr : attrs) {
			if (attr == null) {
				continue;
			}
			
			attr.channel.reset();
		}
		
		for (int i = 0; i < interceptors.length; i++) {
			interceptors[i].forEach(t -> t.reset());
		}
	}

	@Override
	public void readyBuffer() {
		allocateSampleArray();
		for (ChannelAttr attr : attrs) {
			if (attr == null) {
				continue;
			}
			
			attr.channel.checkCapacity(param.freqPerFrame, param.sampleInCurFrame);
		}
	}

	@Override
	public int finishBuffer() {
		XgmSingleChannel[] chs = new XgmSingleChannel[attrs.size()];
		int chCount = 0;
		for (XgmSingleChannel ch : chs) {
			if (ch != null) {
				chs[chCount++] = ch;
			}
		}
		
		beforeRender(chs, chCount);
		
		// 实际渲染工作
		final int length = param.sampleInCurFrame;
		int v;
		for (int track = 0; track < samples.length; track++) {
			short[] ss = samples[track];
			for (int i = 0; i < length; i++) {
				// 渲染一帧的流程
				v = 0;
				
				for (int cidx = 0; cidx < chCount; cidx++) {
					XgmSingleChannel ch = chs[cidx];
					v += ch.render(i, track);
				}
				v = intercept(v, 1, track) >> 1;
				
				if (v > Short.MAX_VALUE) {
					v = Short.MAX_VALUE;
				} else if (v < Short.MIN_VALUE) {
					v = Short.MIN_VALUE;
				}
				ss[i] = (short) v;
			}
		}
		return length;
	}
	
	private void beforeRender(XgmSingleChannel[] chs, int chCount) {
		for (int i = 0; i < chCount; i++) {
			chs[i].beforeSubmit();
		}
	}

	@Override
	public int readBuffer(short[] buf, int offset, int length) {
		int len = Math.min(length / trackCount, param.sampleInCurFrame);
		int index = 0;
		final int trackCount = this.trackCount;
		
		for (int i = 0; i < len; i++) {
			for (int track = 0; track < trackCount; track++) {
				buf[index++] = samples[track][i];
			}
		}
		
		return len * trackCount;
	}
	
	/**
	 * 为 sample 数组分配空间, 创建数组.
	 * 在创建数组的同时, 构造输出相关的拦截器.
	 * 创建数组需要知道 param.sampleInCurFrame 的值
	 */
	private void allocateSampleArray() {
		if (this.samples[0] != null) {
			int oldSize = this.samples[0].length;
			
			if (oldSize < param.sampleInCurFrame || oldSize - param.sampleInCurFrame > 32) {
				int newSize = param.sampleInCurFrame + 16;
				for (int i = 0; i < samples.length; i++) {
					samples[i] = new short[newSize];
				}
			}
			return;
		}
		
		int newSize = param.sampleInCurFrame + 16;
		for (int i = 0; i < samples.length; i++) {
			samples[i] = new short[newSize];
		}
	}

}
