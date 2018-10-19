package zdream.nsfplayer.sound;

import zdream.nsfplayer.ftm.format.FtmDPCMSample;

/**
 * <p>DPCM 轨道发声器
 * 
 * <p>注意, 设置数据完成后, 请调用一次 {@link #reload()} 方法.
 * </p>
 * 
 * @author Zdream
 * @since v0.2.2
 */
public class DPCMSound extends Sound2A03 {
	
	public static final short[] DMC_PERIODS_NTSC = {
			428, 380, 340, 320, 286, 254, 226, 214, 190, 160, 142, 128, 106, 84, 72, 54
	};

	public DPCMSound() {
		reset();
	}
	
	/* **********
	 *   参数   *
	 ********** */
	/*
	 * 原始记录参数
	 * 0 号位: (0x4010)
	 * 1 号位: (0x4011)
	 * 2 号位: (0x4012)
	 * 3 号位: (0x4013)
	 */
	
	/**
	 * <p>0 号位: 0x000000
	 * <p>是否循环播放
	 * <p>为 1 时为 true, 为 0 时为 false
	 * </p>
	 */
	public boolean loop;
	
	/**
	 * <p>0 号位: 0000xxxx
	 * <p>波长的索引. 调整播放的音高. 按照该索引在 {@link #DMC_PERIODS_NTSC} 里面可查询使用的波长值
	 * <p>范围 [0, 15]
	 * </p>
	 */
	public int periodIndex;
	
	/**
	 * <p>1 号位: 0xxxxxxx
	 * <p>初始声像值. 声像值在 [0, 127] 的范围内震荡, 音波在震荡时才会发出声音.
	 * 因此这个是控制音量的唯一方式, 但不是直接控制音量.
	 * <p>范围 [0, 127]
	 * </p>
	 */
	public int deltaCounter;
	
	/**
	 * <p>2 号位: (xxxxxxxx) * 64
	 * <p>起始读取位置.
	 * <p>范围 [0, 16320], 精度 64
	 * </p>
	 */
	public int offsetAddress;
	
	/**
	 * <p>3 号位: (xxxxxxxx) * 16
	 * <p>整个采样的长度, 即 byte 数组长
	 * <p>范围 [0, 4080], 精度 16
	 * </p>
	 */
	public int length;
	
	/**
	 * 采样数据
	 */
	public FtmDPCMSample sample;
	
	/*
	 * 辅助参数
	 * 
	 * 注意, 0x4015 位: (Pulse 1) 0000000x, (Pulse 2) 000000x0 是 enable, 在超类中
	 */
	
	/**
	 * 记录当前周期没放完的时钟周期数.
	 */
	private int counter;
	
	/**
	 * 新的 byte 值已经读取的标志位
	 */
	private boolean sampleFilled;
	
	/**
	 * 剩余还未读取的 byte 个数
	 */
	private int remaining;
	
	/**
	 * 暂存的读取的 byte 值. 已经转成正值.
	 */
	private int curByte;
	
	/**
	 * 在 sample 中读取的位置.
	 */
	private int address;
	
	/**
	 * 一个 byte 需要拆开来, 每个位每个位读取.
	 * 这里记录一个 byte 剩余未读的位数
	 */
	private int byteRemain;
	
	private int shiftReg;
	
	/**
	 * 是否应该静音的标志
	 */
	private boolean silenceFlag;
	
	/* **********
	 * 公共方法 *
	 ********** */
	
	@Override
	public void reset() {
		// 原始记录参数
		loop = false;
		periodIndex = 0;
		deltaCounter = 0;
		offsetAddress = 0;
		length = 0;

		// 辅助参数
		counter = 0;
		sampleFilled = false;
		remaining = 0;
		curByte = 0;
		address = 0;
		byteRemain = 0;
		shiftReg = 0;
		silenceFlag = false;
		
		super.reset();
	}

	@Override
	protected void onProcess(int time) {
		if (sample == null) {
			this.time += time;
			counter = 0;
			return;
		}
		
		final int period = DMC_PERIODS_NTSC[periodIndex];
		
		while (time >= counter) {
			time -= counter;
			this.time += counter;
			counter = period;
			
			// DMA 数据读取
			// 检查是否需要去取下一个 byte 数据
			if (!sampleFilled && (remaining > 0)) {
				curByte = Byte.toUnsignedInt(sample.read(address));
				address++;
				remaining--;
				sampleFilled = true;
				
				if (remaining == 0) {
					if (loop) {
						reload();
					} else {
						this.sample = null;
					}
				}
			}
			
			// Output unit
			if (byteRemain == 0) {
				// Begin new output cycle
				byteRemain = 8;
				if (sampleFilled) {
					shiftReg = curByte;
					sampleFilled = false;
					silenceFlag = false;
				} else {
					silenceFlag = true;
				}
			}

			if (!silenceFlag) {
				if ((shiftReg & 1) == 1) {
					if (deltaCounter < 126)
						deltaCounter += 2;
				} else {
					if (deltaCounter > 1)
						deltaCounter -= 2;
				}
			}

			shiftReg >>= 1;
			--byteRemain;

			mix(deltaCounter);
		}
		
		time -= counter;
	}
	
	/**
	 * 重置读取位置.
	 * 一般要循环的 DMA, 读取到末尾时, 需要重置, 从头开始读取.
	 */
	public void reload() {
		address = offsetAddress;
		remaining = length + 1;
		mix(deltaCounter);
	}
	
	/**
	 * 询问采样是否播放完毕
	 */
	public boolean isFinish() {
		return remaining > 0;
	}

}
