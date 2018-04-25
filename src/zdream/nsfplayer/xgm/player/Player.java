package zdream.nsfplayer.xgm.player;

import zdream.nsfplayer.vcm.ObserverI;
import zdream.nsfplayer.xgm.device.ITrackInfo;

public abstract class Player implements ObserverI {
	
	protected PlayerConfig config;
	
	/**
	 * getLoopCount() 的返回值
	 */
	public static final int
			NEVER_LOOP = 0,
			INFINITE_LOOP = 1;
	
	/**
     * PlayerConfig 附上一个对象
     * <P>
     * PlayerConfig 对象的观察者, 此对象将自动注册.
     * </P>
     * @param pc 要附加的 PlayerConfig 对象
     */
	public void setConfig(PlayerConfig pc) {
		config = pc;
		config.attachObserver(this);
	}
	
	public PlayerConfig getConfig() {
		return config;
	}

	@Override
	public void notify(int v) {}
	
	/**
     * 加载演奏的歌曲数据
     * <P>
     * Player 对象不会在内部存储演奏数据的副本
     * 因此，播放 Player 对象时不要让演奏数据消失
     * 性能数据的生成和消失在 Player 对象之外进行管理
     * </P>
     * @param data 演奏的歌曲数据
     * @return 成功时 true, 失败时 false
     */
	public abstract boolean load(SoundData sdat);
	
	/**
	 * 初始化
	 */
	public abstract void reset();
	
	/**
     * 设置播放速度
     */
	public abstract void setPlayFreq(double rate);

    /**
     * Number of channels to output.
     */
	public abstract void setChannels(int channels);

    /**
     * 渲染音频数据
     * <P>
     * 缓冲区大小需要 samples * sizeof(INT16) [C++], 这里依然使用 byte[]
     * </P>
     * @param buf 缓冲区用于存储渲染数据
     * @param offset buf 数组应该从哪里开始读
     * @param size 样品数量
     *      即使给出了 0，也不应该挂起
     * @return 实际产生的样本数
     */
    public abstract int render(byte[] buf, int offset, int size);

    /**
     * 淡出
     */
    public abstract void fadeOut(int fade_in_ms);

    /**
     * 音频数据的渲染跳过多少数据
     * @param samples 要跳过的样本数
     *      即使给出了 0，也不应该挂起
     * @return 实际跳过的样本数
     */
    public abstract int skip(int samples);

    /**
     * 是否演奏已经停止
     * @return 如果停止演奏, 返回 true. 如果在演奏中, 返回 false.
     */
    public abstract boolean isStopped();

    /**
     * 返回循环的演奏次数
     * <P>
     * 将第一次演奏作为第一个循环.
     * </P>
     * @return 演奏次数
     *  在 NEVER_LOOP 的情况下, 它是不循环的数据, 在 INFINITE_LOOP 的情况下, 它是无限循环数据.
     */
    public int getLoopCount() {
    	return NEVER_LOOP;
    }
    
	public String getTitleString() {
		return "UNKNOWN";
	}

	public int getLength() {
		return 5 * 60 * 1000;
	}
    
	/**
	 * 在时间 id 获取设备信息，编号为 id time == -1 返回当前设备信息
	 */
	public ITrackInfo getInfo(int time_in_ms, int device_id) {
		return null;
	}

}
