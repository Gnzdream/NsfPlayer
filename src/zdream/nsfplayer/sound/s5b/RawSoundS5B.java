package zdream.nsfplayer.sound.s5b;

import zdream.nsfplayer.sound.AbstractNsfSound;

/**
 * S5B 轨道的发声器. 共存在三个这类轨道
 * (非正式版)
 * 
 * @author Zdream
 * @since v0.2.8
 */
public class RawSoundS5B extends AbstractNsfSound {

	public RawSoundS5B() {
		reset();
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
	 * <p>如果是 1 则 <code>tmask = true</code>, 表示启用噪音, 否则为 false; 表示禁用
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
	
	// TODO
	
	
	/* **********
	 * 公共方法 *
	 ********** */
	
	@Override
	public void reset() {
		super.reset();
	}

	@Override
	protected void onProcess(int time) {
		// TODO Auto-generated method stub

	}

}
