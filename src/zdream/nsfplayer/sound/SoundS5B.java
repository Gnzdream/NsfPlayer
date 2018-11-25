package zdream.nsfplayer.sound;

import zdream.nsfplayer.core.NsfStatic;

/**
 * S5B 轨道的发声器. 共存在三个这类轨道
 * 
 * @author Zdream
 * @since v0.2.8
 */
public class SoundS5B extends AbstractNsfSound {

	public SoundS5B() {
		reset();
		internalRefresh();
	}
	
	/**
	 * 音量表
	 */
	public static final int[] VOLT_BL = {
			0x00, 0x01, 0x01, 0x02, 0x02, 0x03, 0x03, 0x04, 0x05, 0x06, 0x07, 0x09, 0x0B, 0x0D, 0x0F, 0x12,
			0x16, 0x1A, 0x1F, 0x25, 0x2D, 0x35, 0x3F, 0x4C, 0x5A, 0x6A, 0x7F, 0x97, 0xB4, 0xD6, 0xEB, 0xFF 
	};
	
	/* **********
	 *   参数   *
	 ********** */
	/*
	 * 原始记录参数
	 * 
	 * 00 号位: [0x00, 0x02, 0x04] 波形频率参数的低 8 位（共 16 位）
	 * 01 号位: [0x01, 0x03, 0x05] 波形频率参数的高 8 位（共 16 位）
	 * 06 号位: 0x06 噪音频率参数
	 * 07 号位: 0x07 轨道屏蔽标识
	 * 08 号位: [0x08, 0x09, 0x0A] 音量参数
	 * 11 号位: 0x0B 包络播放速度的低 8 位（共 16 位）
	 * 12 号位: 0x0C 包络播放速度的高 8 位（共 16 位）
	 * 13 号位: 0x0D 包络控制参数
	 */
	
	/**
	 * <p>波形频率参数
	 * <p>00 号位: xxxxxxxx 作为低 8 位, 01 号位: xxxxxxxx 作为高 8 位, 共 16 位
	 * <p>范围 [0, 0xFFFF]
	 * </p>
	 */
	public int freq;
	
	/**
	 * <p>噪音频率参数
	 * <p>06 号位: 000xxxxx, 得到的数值 * 2; 如果参数为 0, 则置为 1.
	 * <p>范围 [1, 62]
	 * </p>
	 */
	public int noiseFreq;
	
	/**
	 * <p>波形启用参数
	 * <p>07 号位: 0000000x (1 号轨道), 000000x0 (2 号轨道), 00000x00 (3 号轨道)
	 * <p>如果是 1 则 <code>tmask = true</code>, 表示启用波形, 否则为 false; 表示禁用
	 * </p>
	 */
	public boolean waveEnable;
	
	/**
	 * <p>噪音启用参数
	 * <p>07 号位: 0000x000 (1 号轨道), 000x0000 (2 号轨道), 00x00000 (3 号轨道)
	 * <p>如果是 1 则为 true, 表示启用噪音, 否则为 false; 表示禁用
	 * </p>
	 */
	public boolean noiseEnable;
	
	/**
	 * <p>音量参数
	 * <p>08 号位: 0000xxxx, 得到的值记为 <code>a</code>, 则:
	 * <blockquote><pre>
	 * volume = VOLT_BL[a * 2]
	 * </pre></blockquote>
	 * <p>范围 [0, 0xFF]
	 * </p>
	 * @see #VOLT_BL
	 */
	public int volume;
	
	/**
	 * <p>包络播放速度
	 * <p>11 号位: xxxxxxxx 作为低 8 位, 12 号位: xxxxxxxx 作为高 8 位, 共 16 位
	 * <p>范围 [1, 0xFFFF]
	 * </p>
	 */
	public int envelopeSpeed;
	
	/**
	 * <p>包络继续标识
	 * <p>13 号位: 0000x000
	 * <p>如果是 1 则为 true, 表示包络继续播放, 否则为 false; 表示包络暂停播放
	 * </p>
	 */
	public boolean envelopeContinue;
	
	/**
	 * <p>包络击打标识
	 * <p>13 号位: 00000x00
	 * <p>如果是 1 则为 true, 否则为 false
	 */
	public boolean envelopeAttack;
	
	/**
	 * <p>包络修正标识
	 * <p>13 号位: 000000x0
	 * <p>如果是 1 则为 true, 否则为 false
	 */
	public boolean envelopeAlternate;
	
	/**
	 * <p>包络 HOLD 标识
	 * <p>13 号位: 0000000x
	 * <p>如果是 1 则为 true, 否则为 false
	 */
	public boolean envelopeHold;
	
	/*
	 * 辅助参数
	 */
	
	private boolean envFace;
	private boolean envPause;
	private int envCount;
	private int envPtr;
	
	private int noiseCount;
	private int noiseSeed;
	private int waveCount;
	private boolean waveEdge;
	
	/**
	 * 音频的状态每 {@link #step} 个时钟变化一次, 需要向外部输出音频数值.
	 * 记录现在到下一个 step 触发点剩余的时钟数
	 */
	private int counter = 8;
	
	/* **********
	 * 输入方法 *
	 ********** */
	
	public void envelopeReset() {
		envFace = envelopeAttack;
		envPause = false;
		envCount = 0x10000 - envelopeSpeed;
		envPtr = (envFace) ? 0 : 0x1f;
	}
	
	/* **********
	 * 私有方法 *
	 ********** */
	
	private static final int GETA_BITS = 24;
	
	/**
	 * 每 8 个时钟 baseCount 的增量
	 */
	private int baseDelta;
	private int baseCount;
	
	private void internalRefresh() {
		int clk = NsfStatic.BASE_FREQ_NTSC;
		int rate = clk / 8;
		baseDelta = (int) ((double) clk * (1 << GETA_BITS) / (16 * rate));
	}
	
	
	/* **********
	 * 公共方法 *
	 ********** */
	
	@Override
	public void reset() {
		// TODO S5B 参数重置
		
		
		
		noiseSeed = 0xffff;
		
		super.reset();
	}

	@Override
	protected void onProcess(int time) {
		int value;
		
		while (time >= counter) {
			time -= counter;
			this.time += counter;
			counter	= 8;
			
			value = this.renderStep();
			mix(value);
		}
		
		this.time += time;
		counter -= time;
	}
	
	private int renderStep() {
		int noise;
		int delta; // unsigned

		baseCount += baseDelta;
		delta = (baseCount >> GETA_BITS);
		baseCount &= (1 << GETA_BITS) - 1;
		
		/* Envelope */
		envCount += delta;
		if (envelopeSpeed > 0) {
			while (envCount >= 0x10000) {
				if (!envPause) {
					if (envFace)
						envPtr = (envPtr + 1) & 0x3f;
					else
						envPtr = (envPtr + 0x3f) & 0x3f;
				}

				if ((envPtr & 0x20) != 0) /* if carry or borrow */
				{
					if (envelopeContinue) {
						if (envelopeAlternate && envelopeHold)
							envFace &= true;
						if (envelopeHold)
							envPause = true;
						envPtr = (envFace) ? 0 : 0x1f;
					} else {
						envPause = true;
						envPtr = 0;
					}
				}

				envCount -= envelopeSpeed;
			}
		}
		
		/* Noise */
		noiseCount += delta;
		if ((noiseCount & 0x40) != 0) {
			if ((noiseSeed & 1) != 0)
				noiseSeed ^= 0x24000;
			noiseSeed >>= 1;
			noiseCount -= noiseFreq;
		}
		noise = noiseSeed & 1;
		
		/* Tone / Wave */
		waveCount += delta;
		if ((waveCount & 0x1000) != 0) {
			if (freq > 1) {
				waveEdge = !waveEdge; // ?
				waveCount -= freq;
			} else {
				waveEdge = true;
			}
		}
		
		/* Out */
		int out = 0; // maintaining cout for stereo mix

		if (!isEnable())
			return 0;

		if ((waveEnable || waveEdge) && (noiseEnable || noise != 0)) {
			if ((volume & 32) == 0) {
				out = VOLT_BL[volume & 31];
			} else {
				System.out.println(Integer.toHexString(volume));
				out = VOLT_BL[envPtr];
			}
		}
		
		return out;
	}

}
