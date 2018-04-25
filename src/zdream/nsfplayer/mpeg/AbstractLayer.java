package zdream.nsfplayer.mpeg;

/**
 * 解码音频数据的超类
 * @author Zdream
 * @since v0.1
 * @date 2018-01-17
 */
public abstract class AbstractLayer {
	
	/**
	 * 将帧头数据传入来初始化解码
	 * @param head
	 */
	public abstract void ready(MpegFrameHead head);
	
	/**
	 * 解码
	 * @param bs
	 *   数据
	 * @param offset
	 *   偏移量, 不包含帧头的数据, 希望也不包含 CRC 检验字段
	 * @param length 
	 *   数据长度
	 * @return
	 */
	public abstract byte[] decode(byte[] bs, int offset, int length);

	/**
	 * 声道数
	 */
	private int channels;
	
	/*
	 * 管理 PCM 缓冲区的5个变量
	 */
	protected byte[] pcmbuf;
	private int size;			// 一次向音频缓冲区 pcmbuf 写入的长度
	private int[] writeCursor;	// 两个声道向 pcmbuf 写入数据时使用的偏移量
	
	public AbstractLayer() {
		
	}
	
	protected void init(MpegFrameHead head) {
		size = 2 * head.getPcmSize();	//#### 几处数字不能更改
		
		channels = head.getChannels();
		filter = new Synthesis(channels);
		writeCursor = new int[2];
		writeCursor[1] = 2;				//####
		pcmbuf = new byte[size * 4];	//####
	}
	
	/*
	 * Layer1, Layer2, Layer3 都有一个滤波器。
	 */
	private Synthesis filter;

	// 测试
	int count = 0;
	
	/**
	 * 一个子带多相合成滤滤。
	 * @param samples 输入的 32 个样本值。
	 * @param ch 当前声道。0表示左声道，1表示右声道。
	 */
	protected final void synthesisSubBand (float[] samples, int ch) {
		writeCursor[ch] = filter.synthesisSubBand(samples, ch, pcmbuf, writeCursor[ch]);
	}
	
	/**
	 * 尝试输出解码结果。
	 * <p>多相合成滤波输出的 PCM 数据写入缓冲区, 当缓冲区至少填入解码 4 帧得到的 PCM 数据才产生一次输出,
	 * 但调用者并不需要知道当前缓冲区是否已经填入足够数据. 防止缓冲区溢出, 每解码最多 4 帧应调用本方法 1 次,
	 * 当然也可以每解码 1 帧就调用本方法 1 次
	 * <p>若产生音频输出, 将从缓冲区取走解码 4 帧得到的 PCM 数据。
	 * <p><b>可能产生阻塞：</b>若音频输出已经停止，对本方法的调用将被阻塞，直到开始音频输出。如果输入流解码完，阻塞被自动清除。
	 * @see #startAudio()
	 */
	protected byte[] outputPCM() {
		byte[] ret = new byte[writeCursor[0]];
		System.arraycopy(pcmbuf, 0, ret, 0, writeCursor[0]);
		
		// size = 0;
		writeCursor[0] = 0;
		writeCursor[1] = 2;
		
		return ret;
	}

}