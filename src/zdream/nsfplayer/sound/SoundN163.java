package zdream.nsfplayer.sound;

import java.util.Arrays;

/**
 * N163 轨道的发声器, 在 NSF 中最多有 8 个轨道
 * 
 * @author Zdream
 * @since v0.2.6
 */
public class SoundN163 extends AbstractNsfSound {

	public SoundN163() {
		reset();
	}
	
	/* **********
	 *   参数   *
	 ********** */
	/*
	 * 原始记录参数
	 * 
	 * 波形包络表部分:
	 * 变长数组、不定起始位置
	 * 
	 * 其它参数部分:
	 * 如果这个是第 n 个轨道 (n 的范围是 [0, 7]), 则:
	 * 
	 * 00 号位: 0x40+(n*8) 频率参数的低 8 位（0 - 7 位, 共 18 位）
	 * 01 号位: 0x41+(n*8) 相位的低 8 位（0 - 7 位, 共 24 位）
	 * 02 号位: 0x42+(n*8) 频率参数的中 8 位（8 - 15 位, 共 18 位）
	 * 03 号位: 0x43+(n*8) 相位的中 8 位（8 - 15 位, 共 24 位）
	 * 04 号位: 0x44+(n*8) 频率参数的高 2 位（16 - 17 位, 共 18 位）; 音量包络长度参数
	 * 05 号位: 0x45+(n*8) 相位的高 8 位（16 - 23 位, 共 24 位）
	 * 06 号位: 0x46+(n*8) 包络起点参数 (不记录)
	 * 07 号位: 0x47+(n*8) 音量
	 */
	
	/**
	 * <p>波形包络表
	 * <p>每个单位范围 [0, 15]
	 * </p>
	 */
	public final byte[] wave = new byte[240];
	
	/**
	 * <p>00 号位: xxxxxxxx 作为低 8 位, 02 号位: xxxxxxxx 作为中 8 位,
	 * 04 号位: 000000xx 作为高 2 位, 共 18 位
	 * <p>频率参数 (虽说实际上理解成波长更准确), 控制音高
	 * <p>范围 [0, 0x3FFFF]
	 * </p>
	 */
	public int period;
	
	/**
	 * <p>01 号位: xxxxxxxx 作为低 8 位, 03 号位: xxxxxxxx 作为中 8 位,
	 * 05 号位: xxxxxxxx 作为高 8 位, 共 24 位
	 * <p>相位
	 * <p>范围 [0, 0xFFFFFF]
	 * </p>
	 */
	public int phase;
	
	/**
	 * <p>04 号位: xxxxxx00, 得到的值记为 <code>a</code>, 则:
	 * <blockquote><pre>
	 * length = 256 - (4 * a)
	 * </pre></blockquote>
	 * <p>音量包络长度参数. 即 {@link #wave} 的有效长度
	 * <p>范围 [4, 256], 且该值能被 4 整除
	 * </p>
	 */
	public int length;
	
//	/**
//	 * <p>06 号位: xxxxxxxx
//	 * <p>音量包络起点参数
//	 * <p>范围 [0, 255]
//	 * </p>
//	 */
//	public int offset;
	
	/**
	 * <p>07 号位: 0000xxxx
	 * <p>音量
	 * <p>范围 [0, 15]
	 * </p>
	 */
	public int volume;
	
	/*
	 * 辅助参数
	 */
	
	/**
	 * <p>每次音频的状态变化相隔的时钟数. 该值需要外部输入.
	 * 该数值有这样的计算方式:
	 * <blockquote><pre>
	 * step = 15 * channelCount
	 * </pre></blockquote>
	 * 其中 channelCount 为 N163 的轨道个数.
	 * <p>step 将不会被 {@link #reset()} 重置.
	 * </p>
	 */
	public int step;
	
	/**
	 * 音频的状态每 {@link #step} 个时钟变化一次, 需要向外部输出音频数值.
	 * 记录现在到下一个 step 触发点剩余的时钟数
	 */
	private int counter;
	
	/* **********
	 * 公共方法 *
	 ********** */
	
	@Override
	public void reset() {
		// 原始记录参数
		period = 0;
		phase = (int) (Math.random() * 0xFFFF); // TODO 这个不是长久之计. 现在处理共振暂时用该方法
		length = 0;
		volume = 0;
		Arrays.fill(wave, (byte) 0);
		
		// 辅助参数
		counter = step;
		// step 不进行初始化
		
		super.reset();
	}

	@Override
	protected void onProcess(int time) {
		// 请务必设置 step 的值
		if (step <= 0) {
			this.counter = 0;
			this.time += time;
			return;
		}
		
		while (time >= counter) {
			time -= counter;
			this.time += counter;
			counter	= step;
			
			phase = (phase + period) & 0x00FFFFFF;

			// 相位边界值
			int hilen = length << 16;
			// 相位控制在相位边界值内
			if (hilen > 0) {
				while (phase >= hilen)
					phase -= hilen;
			}
			
			// NsfPlayer 工程原话:
			// fetch sample (note: N163 output is centred at 8, and inverted w.r.t 2A03)
			// 意思是说, N163 输出以 8 为中心, 这个与 2A03 有本质不同
			int index = (phase >> 16);
			int sample = 8 - wave[index];
			
			mix(sample * volume);
		}
		
		this.time += time;
		counter -= time;
	}

}
