package zdream.nsfplayer.mixer.xgm;

import java.util.ArrayList;

import zdream.nsfplayer.core.IExpression;
import zdream.nsfplayer.core.NsfCommonParameter;
import zdream.nsfplayer.mixer.ITrackChannel;
import zdream.nsfplayer.mixer.interceptor.Amplifier;
import zdream.nsfplayer.mixer.interceptor.Filter;
import zdream.nsfplayer.mixer.interceptor.ISoundInterceptor;

/**
 * <p>Xgm 的单轨道.
 * </p>
 * 
 * @author Zdream
 * @since v0.3.0
 */
public class XgmSingleChannel extends AbstractXgmAudioChannel implements ITrackChannel {
	
	/**
	 * 起到暂存的作用. store.level 数据将被忽略.
	 */
	AbstractXgmAudioChannel store;
	IExpression exp;
	/**
	 * 入采样数
	 */
	int inSample;
	
	@Override
	public void mix(int value, int time) {
		// 采样转化, 主音量
		int v = (int) (exp.f(value) * level);
		
		// 存储轨道
		store.mix(v, time);
	}

	@Override
	public void reset() {
		store.reset();
		for (int i = 0; i < interceptors.length; i++) {
			interceptors[i].forEach(t -> t.reset());
		}
	}

	@Override
	protected void beforeSubmit() {
		// 暂存部分
		store.beforeSubmit();
		
		// 拦截器部分
		for (int i = 0; i < interceptorArray.length; i++) {
			ISoundInterceptor[] array = interceptorArray[i];
			if (array == null || array.length != interceptors[i].size()) {
				interceptorArray[i] = new ISoundInterceptor[interceptors[i].size()];
			}
			interceptors[i].toArray(interceptorArray[i]);
		}
	}

	@Override
	protected void checkCapacity(int inSample, int outSample) {
		if (this.inSample != 0) {
			inSample = this.inSample;
		}
		store.checkCapacity(inSample, outSample);
	}

	@Override
	protected float read(int index) {
		return store.read(index);
	}
	
	/**
	 * 采样数据提交
	 * @param index
	 * @param track
	 *   声道号
	 * @return
	 *   该采样的值
	 */
	public int render(int index, int track) {
		float lv = trackLevel[track];
		float v = (lv == 0) ? 0 : read(index) * lv * 12;
		return intercept((int) v, 1, track);
	}

	/* **********
	 * 输出声道 *
	 ********** */
	
	/**
	 * 各个轨道的音量
	 */
	private float[] trackLevel;
	
	/**
	 * 设置声道数.
	 * 声道数改变之后, 声道音量、拦截器组会重置, 前面的所有修改全部被丢弃.
	 * @param count
	 *   声道数
	 * @param param
	 */
	@SuppressWarnings("unchecked")
	void setTrackCount(int count, NsfCommonParameter param) {
		interceptors = new ArrayList[count];
		for (int i = 0; i < interceptors.length; i++) {
			interceptors[i] = initInterceptor(param, new ArrayList<>());
		}
		interceptorArray = new ISoundInterceptor[count][];
		
		trackLevel = new float[count];
		for (int i = 0; i < trackLevel.length; i++) {
			trackLevel[i] = 1.0f;
		}
	}
	
	public void setTrackLevel(float level, int track) {
		trackLevel[track] = level;
	}
	
	public float getTrackLevel(int track) {
		return trackLevel[track];
	}

	/* **********
	 * 拦截器组 *
	 ********** */
	
	/**
	 * [声道数]
	 */
	protected ArrayList<ISoundInterceptor>[] interceptors;
	
	/**
	 * 缓存, 性能考虑
	 */
	private ISoundInterceptor[][] interceptorArray;
	
	private ArrayList<ISoundInterceptor> initInterceptor(
			NsfCommonParameter param, ArrayList<ISoundInterceptor> array) {
		Filter f = new Filter();
		f.setRate(param.sampleRate);
		f.setParam(4700, 0);
		array.add(f);

		Amplifier amp = new Amplifier();
		amp.setCompress(100, -1);
		array.add(amp);
		
		return array;
	}
	
	/**
	 * @param value
	 * @param time
	 *   过去的时间数, 一般为 1. 单位为一个出采样率的时间间隔
	 * @param track
	 *   声道号
	 * @return
	 */
	protected int intercept(int value, int time, int track) {
		int ret = value;
		ISoundInterceptor[] array = interceptorArray[track];
		final int length = array.length;
		
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
	 *   声道号
	 */
	public void attachIntercept(ISoundInterceptor interceptor, int track) {
		if (interceptor != null) {
			interceptors[track].add(interceptor);
		}
	}
	
}
