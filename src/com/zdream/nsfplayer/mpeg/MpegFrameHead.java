package com.zdream.nsfplayer.mpeg;

/**
 * <p>Mpeg 格式的音频帧头.
 * <p>Mpeg 格式的音频是由帧并排连接成流式结构的, 每个帧的开头都有 32 位数据,
 * 用来标识该帧的相关信息.<br>该类就是用来保存帧头信息的.
 * 
 * <p>补充: 源代码中解析帧头 32 位用到的变量及含义：
 * <table border="2" bordercolor="#000000" cellpadding="8" style="border-collapse:collapse">
 * <tr><th>偏移量</th><th>长度</th><th>变量名</th><th>含义</th></tr>
 * <tr><td>0</td><td>11</td><td>帧同步时直接解析</td><td>11位全设置为'1'的帧同步字</td></tr>
 * <tr><td>11</td><td>2</td><td>verID</td><td>MPEG版本</td></tr>
 * <tr><td>13</td><td>2</td><td>layer</td><td>MPEG压缩层</td></tr>
 * <tr><td>15</td><td>1</td><td>protection_bit</td><td>是否CRC</td></tr>
 * <tr><td>16</td><td>4</td><td>bitrate_index</td><td>位率索引</td></tr>
 * <tr><td>20</td><td>2</td><td>sampling_frequency</td><td>采样率索引</td></tr>
 * <tr><td>22</td><td>1</td><td>padding</td><td>当前帧是否附加填充一槽数据</td></tr>
 * <tr><td>23</td><td>1</td><td>未解析</td><td>告知是否私有</td></tr>
 * <tr><td>24</td><td>2</td><td>mode</td><td>声道模式</td></tr>
 * <tr><td>26</td><td>2</td><td>mode_extension</td><td>声道扩展模式</td></tr>
 * <tr><td>28</td><td>1</td><td>未解析</td><td>告知是否有版权</td></tr>
 * <tr><td>29</td><td>1</td><td>未解析</td><td>告知是否为原版</td></tr>
 * <tr><td>30</td><td>2</td><td>不常用，未解析</td><td>预加重</td></tr>
 * </table>
 * 
 * @author Zdream
 * @since v0.1
 * @date 2018-01-16
 */
public class MpegFrameHead {

	/**
	 * MPEG 版本 MPEG-1
	 */
	public static final byte MPEG1 = 3;

	/**
	 * MPEG 版本 MPEG-2
	 */
	public static final byte MPEG2 = 2;

	/**
	 * MPEG 版本 MPEG-2.5（非官方版本）
	 */
	public static final byte MPEG25 = 0;

	/**
	 * <p>有效字符只有 2 bit.
	 * <p>
	 * 0 : MPEG-2.5 (非官方版本); 见 {@link #MPEG25}<br>
	 * 1 : 无效<br>
	 * 2 : MPEG-2 (ISO/IEC 13818-3); 见 {@link #MPEG2}<br>
	 * 3 : MPEG-1 (ISO/IEC 11172-3). 见 {@link #MPEG1}
	 */
	private byte verID;

	/**
	 * 音频数据 Layer 1
	 */
	public static final byte LAYER1 = 3;

	/**
	 * 音频数据 Layer 2
	 */
	public static final byte LAYER2 = 2;

	/**
	 * 音频数据 Layer 3
	 */
	public static final byte LAYER3 = 1;

	/**
	 * <p>存储的音频数据 layer 等级, 2-bit
	 * <p>
	 * 3 : Layer I; 见 {@link #LAYER1}<br>
	 * 2 : Layer II; 见 {@link #LAYER2}<br>
	 * 1 : Layer III; 见 {@link #LAYER3}<br>
	 * 0 : 无效
	 */
	private byte layer;

	/**
	 * <p>是否用 CRC 进行校验
	 * <p>原始数据中, 0 是使用 CRC 校验 (true), 1 不使用 (false)
	 */
	private boolean protection_bit;

	/**
	 * 音频比特率的对照表
	 */
	private static final int[][][] BITRATE = {
		{
			//MPEG-1
			//Layer I
			{0,32,64,96,128,160,192,224,256,288,320,352,384,416,448},
			//Layer II
			{0,32,48,56, 64, 80, 96,112,128,160,192,224,256,320,384},
			//Layer III
			{0,32,40,48, 56, 64, 80, 96,112,128,160,192,224,256,320}
		},{
			//MPEG-2/2.5
			//Layer I
			{0,32,48,56,64,80,96,112,128,144,160,176,192,224,256},
			//Layer II
			{0,8,16,24,32,40,48,56,64,80,96,112,128,144,160},
			//Layer III = Layer II
			null
		}
	};
	
	static {
		BITRATE[1][3] = BITRATE[1][2];
	}

	/**
	 * samplingRate[verID][sampling_frequency]
	 */
	private static final int[][] SAMPLING_RATE = {
		{11025, 12000, 8000, 0}, // MPEG-2.5
		null, // 无
		{22050, 24000, 16000, 0}, // MPEG-2 (ISO/IEC 13818-3)
		{44100, 48000, 32000, 0} // MPEG-1 (ISO/IEC 11172-3)
	};

	private int padding;
	
	/**
	 * <p>轨道模式
	 * <p>MODE_STEREO: 立体声<br>
	 * MODE_JOINT_STEREO: 联合立体声<br>
	 * MODE_DUAL_CHANNEL: 两个单独的单声道<br>
	 * MODE_MONO: 单声道
	 */
	public static final byte
			MODE_STEREO = 0,
			MODE_JOINT_STEREO = 1,
			MODE_DUAL_CHANNEL = 2,
			MODE_MONO = 3;

	/**
	 * 轨道, 见 {@link #MODE_STEREO}, {@link #MODE_JOINT_STEREO},
	 * {@link #MODE_DUAL_CHANNEL}, {@link #MODE_MONO}
	 */
	private byte mode;
	
	/**
	 * 比特率
	 */
	private int bitrate;
	
	private int samplingRate;
	
	/**
	 * PCM 样本采样率的索引值
	 */
	private int samplingFrequency;

	private int framesize;
	
	/**
	 * 主数据长度
	 */
	private int maindatasize;
	
	/**
	 * 帧边信息长度
	 */
	private int sideinfosize;
	
	private int lsf;
	private boolean isMS, isIntensity;
	
	/**
	 * 初始化。
	 */
	protected void reset() {
		layer = 0;
		sideinfosize = framesize = 0;
		verID = 1;
	}

	/**
	 * 帧头解码. 解码的数据将直接储存在该类中
	 * @param i
	 *   帧头, 4 字节 (32位) 整数。
	 */
	protected void decode(int i) {
		verID = (byte) ((i >> 19) & 3);
		layer = (byte) ((i >> 17) & 3);
		protection_bit = ((i >> 16) & 0x1) == 0;
		int bitrate_index = (i >> 12) & 0xF;
		bitrate = BITRATE[lsf][layer - 1][bitrate_index];
		
		samplingFrequency = (i >> 10) & 3;
		padding = (i >> 9) & 0x1;
		mode = (byte) ((i >> 6) & 3);
		int mode_extension = (i >> 4) & 3;

		isMS = mode == 1 && (mode_extension & 2) != 0;
		isIntensity = mode == 1 && (mode_extension & 0x1) != 0;
		lsf = (verID == MPEG1) ? 0 : 1;
		
		samplingRate = SAMPLING_RATE[verID][samplingFrequency];

		switch (layer) {
		case 1:	
			framesize = bitrate * 12000;
			framesize /= samplingRate;
			framesize += padding;
			framesize <<= 2; // 1-slot = 4-byte
			break;
		case 2:
			framesize  = bitrate * 144000;
			framesize /= samplingRate;
			framesize += padding;
			break;
		case 3:
			framesize = bitrate * 144000;
			framesize /= samplingRate << lsf;
			framesize += padding;

			// 计算帧边信息长度
			if (verID == MPEG1)
				sideinfosize = (mode == 3) ? 17 : 32;
			else
				sideinfosize = (mode == 3) ? 9 : 17;
			break;
		}

		// 计算主数据长度
		maindatasize = framesize - 4 - sideinfosize;

		if (protection_bit)
			maindatasize -= 2;	//CRC-word
	}

	/**
	 * 是否有循环冗余校验码。
	 * @return 返回true表示有循环冗余校验码，帧头之后邻接有2字节的数据用于CRC。
	 */
	public boolean isProtected() {
		return protection_bit;
	}

	/**
	 * 获取声道模式是否为中/侧立体声（Mid/Side stereo）模式。
	 * 
	 * @return true表示是中/侧立体声模式。
	 */
	public boolean isMS() {
		return isMS;
	}

	/**
	 * 获取声道模式是否为强度立体声（Intensity Stereo）模式。
	 * 
	 * @return true表示是强度立体声模式。
	 */
	public boolean isIntensityStereo() {
		return isIntensity;
	}

	/**
	 * 获取当前帧的位率。
	 * 
	 * @return 当前帧的位率，单位为“千位每秒（Kbps）”。
	 */
	public int getBitrate() {
		return bitrate;
	}

	/**
	 * 获取声道数。
	 * 
	 * @return 声道数：1或2。
	 */
	public int getChannels() {
		return (mode == 3) ? 1 : 2;
	}

	/**
	 * 获取声道模式。
	 * 
	 * @return 声道模式，其值表示的含义：
	 * <table border="1" bordercolor="#000000" cellpadding="8" style="border-collapse:collapse">
	 * <tr><th>返回值</th><th>声道模式</th></tr>
	 * <tr><td>0</td><td>立体声（stereo）</td></tr>
	 * <tr><td>1</td><td>联合立体声（joint stereo）</td></tr>
	 * <tr><td>2</td><td>双声道（dual channel）</td></tr>
	 * <tr><td>3</td><td>单声道（mono channel）</td></tr>
	 * </table>
	 * @see #getModeExtension()
	 */
	public int getMode() {
		return mode;
	}

	/**
	 * @return
	 *   MPEG版本: {@link #MPEG1}、 {@link #MPEG2} 或 {@link #MPEG25} 。
	 */
	public byte getVersion() {
		return verID;
	}

	/**
	 * 获取MPEG编码层。
	 * 
	 * @return MPEG编码层：返回值1表示LayerⅠ，2表示LayerⅡ，3表示LayerⅢ。
	 */
	public int getLayer() {
		return layer;
	}

	/**
	 * 获取主数据长度。
	 * 
	 * @return 当前帧的主数据长度，单位“字节”。
	 */
	public int getMainDataSize() {
		return maindatasize;
	}

	/**
	 * 获取边信息长度。
	 * 
	 * @return 当前帧边信息长度，单位“字节”。
	 */
	public int getSideInfoSize() {
		return sideinfosize;
	}

	/**
	 * 获取帧长度。<p>帧的长度 = 4字节帧头 + CRC（如果有的话，2字节） + 音乐数据长度。
	 * <br>其中音乐数据长度 = 边信息长度 + 主数据长度。
	 * <p>无论是可变位率（VBR）编码的文件还是固定位率（CBR）编码的文件，每帧的长度不一定同。
	 * 
	 * @return 当前帧的长度，单位“字节”。
	 */
	public int getFrameSize() {
		return framesize;
	}

	/**
	 * 获取当前帧解码后得到的PCM样本长度。通常情况下同一文件每一帧解码后得到的PCM样本长度是相同的。
	 * 
	 * @return 当前帧解码后得到的PCM样本长度，单位“字节”。
	 */
	public int getPcmSize() {
		int pcmsize = (verID == MPEG1) ? 4608 : 2304;
		if(mode == 3) // if channels == 1
			pcmsize >>= 1;
		return pcmsize;
	}

	/**
	 * @return
	 *   当前文件一帧的播放时间长度, 单位 "秒"
	 */
	public float getFrameDuration() {
		return 1152f / (samplingRate << lsf);
	}

	public int getSamplingFrequency() {
		return samplingFrequency;
	}

	/**
	 * 获取帧头的简短信息。
	 * @return 帧头的简短信息。
	 */
	public String toString() {
		StringBuilder buf = new StringBuilder();

		if(verID == MPEG25) buf.append("MPEG-2.5");
		else if(verID == MPEG2) buf.append("MPEG-2");
		else if(verID == MPEG1) buf.append("MPEG-1");
		else return "Let me tell you gently\nThe header is unavailable";

		buf.append(", Layer "); buf.append(layer);
		buf.append(", "); buf.append(samplingRate); buf.append("Hz, ");

		if(mode == 0)      buf.append("Stereo");
		else if(mode == 1) buf.append("Joint Stereo");
		else if(mode == 2) buf.append("Dual channel");
		else if(mode == 3) buf.append("Mono");
		
		if (isMS) {
			buf.append((isIntensity) ? "(I/S & M/S)" : "(M/S)");
		} else if (isIntensity) {
			buf.append("(I/S)");
		}

		return buf.toString();
	}
	
	MpegFrameHead() {
		
	}

}
