package zdream.nsfplayer.sound;

import java.util.Arrays;

/**
 * FDS 轨道的发声器.
 * 
 * @author Zdream
 * @since v0.2.4
 */
public class SoundFDS extends AbstractNsfSound {

	public SoundFDS() {
		reset();
	}
	
	/* **********
	 *   参数   *
	 ********** */
	/*
	 * 原始记录参数
	 * 
	 * 波形包络表部分:
	 *   [0x4040, 0x407F]
	 * 其它参数部分:
	 * 00 号位: 0x4080 波形包络控制
	 * 01 号位: 0x4081 无
	 * 02 号位: 0x4082 波形频率参数的低 8 位（共 12 位）
	 * 03 号位: 0x4083 波形频率参数的高 4 位（共 12 位）, 禁用标识
	 * 04 号位: 0x4084 调制包络控制
	 * 05 号位: 0x4085 调制相位
	 * 06 号位: 0x4086 调制频率参数的低 8 位（共 12 位）
	 * 07 号位: 0x4087 调制频率参数的高 4 位（共 12 位）, 禁用标识
	 * 08 号位: 0x4088 调制包络表输入
	 * 09 号位: 0x4089 波形写入标识, 总音量
	 * 10 号位: 0x408A 包络播放速度
	 */
	
	/**
	 * <p>[0x4040, 0x407F]
	 * <p>波形包络表, 每个单元的值的范围 [0, 63]
	 * </p>
	 */
	public final byte[] wave = new byte[64];
	
	/**
	 * <p>08 号位: 00000xxx 累积值的表数据
	 * <p>调制包络表, 每个单元的值的范围 [0, 7].
	 * <p>推荐使用 {@link #writeMods(byte)} 方法来写入该值
	 * </p>
	 */
	public final byte[] mods = new byte[64];
	
	/**
	 * <p>00 号位: x0000000
	 * <p>波形包络禁用标志.
	 * <p>为 1 时为 true, 此时波形包络被禁用; 为 0 时为 false, 此时启用波形包络
	 * </p>
	 */
	public boolean wavEnvDisable;
	
	/**
	 * <p>00 号位: 0x000000
	 * <p>波形包络模式
	 * <p>为 1 时为 true, 为 0 时为 false
	 * </p>
	 */
	public boolean wavEnvMode;
	
	/**
	 * <p>00 号位: 00xxxxxx
	 * <p>波形包络播放速度 / 波形总体音量. 它取哪个意思以 wavEnvDisable 的值而定.
	 * 当 <code>wavEnvDisable == true</code> 时, 该值表示总体音量.
	 * <p>范围 [0, 63]
	 * </p>
	 */
	public int wavEnvSpeed;
	
	/**
	 * <p>02 号位: xxxxxxxx 作为低 8 位, 03 号位: 0000xxxx 作为高 4 位, 共 12 位
	 * <p>波形频率参数, 可视为每个时钟, 波形相位向前走的步长数值.
	 * <p>范围 [0, 0xFFF]
	 * </p>
	 */
	public int wavFreq;
	
	/**
	 * <p>03 号位: x0000000
	 * <p>波形静音标志
	 * <p>为 1 时为 true, 波形静音; 为 0 时为 false
	 * </p>
	 */
	public boolean wavHalt;
	
	/**
	 * <p>03 号位: 0x000000
	 * <p>包络暂停标志
	 * <p>为 1 时为 true, 包络暂停; 为 0 时为 false
	 * </p>
	 */
	public boolean envHalt;
	
	/**
	 * <p>04 号位: x0000000
	 * <p>调制包络禁用标志.
	 * <p>为 1 时为 true, 此时调制包络被禁用; 为 0 时为 false, 此时启用调制包络
	 * </p>
	 */
	public boolean modEnvDisable;
	
	/**
	 * <p>04 号位: 0x000000
	 * <p>调制包络模式
	 * <p>为 1 时为 true, 为 0 时为 false
	 * </p>
	 */
	public boolean modEnvMode;
	
	/**
	 * <p>04 号位: 00xxxxxx
	 * <p>调制包络播放速度
	 * <p>范围 [0, 63]
	 * </p>
	 */
	public int modEnvSpeed;
	
	/**
	 * <p>05 号位: 0xxxxxxx
	 * <p>调制包络相位
	 * <p>范围 [0, 127]
	 * </p>
	 */
	public int modPos;
	
	/**
	 * <p>06 号位: xxxxxxxx 作为低 8 位, 07 号位: 0000xxxx 作为高 4 位, 共 12 位
	 * <p>调制频率参数, 可视为每个时钟, 调制相位向前走的步长数值.
	 * <p>范围 [0, 0xFFF]
	 * </p>
	 */
	public int modFreq;
	
	/**
	 * <p>07 号位: x0000000
	 * <p>调制暂停标志
	 * <p>为 1 时为 true, 调制暂停; 为 0 时为 false
	 * </p>
	 */
	public boolean modHalt;
	
	/**
	 * <p>09 号位: x0000000
	 * <p>波形可以写入标志
	 * <p>为 1 时为 true; 为 0 时为 false
	 * </p>
	 */
	public boolean wavWrite;
	
	/**
	 * <p>09 号位: 000000xx
	 * <p>总音量选择参数
	 * <p>范围 [0, 3], 4 个档位的音量比值分别为 30 : 20 : 15 : 12
	 * </p>
	 */
	public int masterVolume;
	
	/**
	 * <p>10 号位: xxxxxxxx
	 * <p>总包络速度
	 * <p>范围 [0, 255]
	 * </p>
	 */
	public int masterEnvSpeed;
	
	/*
	 * 辅助参数
	 */
	
	/**
	 * <p>波形相位
	 * 相位的值为了提高精度, 实际记录的值比原值做了 << 16 位的处理.
	 * <p>该值每增加 0x10000, 波形往前进一格, {@link #wave} 的索引添加 1.
	 * </p>
	 */
	private int wavPhase;
	
	/**
	 * 调制相位
	 * 相位的值为了提高精度, 实际记录的值比原值做了 << 16 位的处理.
	 */
	private int modPhase;
	
	/**
	 * 波形时钟计数器
	 */
	private int wavEnvCounter;
	
	/**
	 * 调制时钟计数器
	 */
	private int modEnvCounter;
	
	/**
	 * 波形输出
	 */
	private int wavEnvOut;
	
	/**
	 * 调制输出
	 */
	private int modEnvOut;
	
	/**
	 * 现在整体的输出值
	 */
	private int curOut;
	
	/* **********
	 * 输入方法 *
	 ********** */
	
	/**
	 * <p>写入 mod 值到调制包络表中.
	 * <p>在原 NSF 程序中, 通过写入 0x4088 单元来控制该表, 而该方法就模拟这个行为.
	 * </p>
	 * @param mod
	 *   范围 [0, 7]
	 */
	public void writeMods(int mod) {
		if (modHalt) {
			// 下面是原话
			// writes to current playback position (there is no direct way to set phase)
			mods[(modPhase >> 16) & 0x3F] = (byte) (mod & 0x07);
			modPhase = (modPhase + 0x010000) & 0x3FFFFF;
			mods[(modPhase >> 16) & 0x3F] = (byte) (mod & 0x07);
			modPhase = (modPhase + 0x010000) & 0x3FFFFF;
		}
	}
	
	/* **********
	 * 查询方法 *
	 ********** */
	
	public int getWavEnvOut() {
		return wavEnvOut;
	}
	
	public int getModEnvOut() {
		return modEnvOut;
	}
	
	/* **********
	 * 公共方法 *
	 ********** */
	
	@Override
	public void reset() {
		// 原始记录参数
		Arrays.fill(wave, (byte) 0);
		Arrays.fill(mods, (byte) 0);
		wavEnvDisable = true;
		wavEnvMode = false;
		wavEnvSpeed = 0;
		wavFreq = 0;
		wavHalt = true;
		envHalt = false;
		modEnvDisable = true;
		modEnvMode = false;
		modEnvSpeed = 0;
		modPos = 0;
		modFreq = 0;
		modHalt = true;
		wavWrite = false;
		masterVolume = 0;
		masterEnvSpeed = 0xE8;
		
		// 辅助参数
		wavPhase = modPhase = 0;
		wavEnvCounter = modEnvCounter = 0;
		wavEnvOut = modEnvOut = 0;
		curOut = 0;
		
		super.reset();
		
		// 还要补充的
		// write(0x408A, 0xE8, 0);
		// 0x4080
		// 0x4083
		// 0x4084
		// 0x4085
		// 0x4087
		
	}
	
	public void resetCounter() {
		wavEnvCounter = modEnvCounter = 0;
	}
	
	public void resetWavCounter() {
		wavEnvCounter = 0;
	}
	
	public void resetModCounter() {
		modEnvCounter = 0;
	}

	@Override
	protected void onProcess(int time) {
		// 前置操作
		if (wavHalt) {
			wavPhase = 0;
		}
		
		if (modHalt) {
			modPhase &= 0x3F0000; // 重置累积的相位
		}
		
		if (envHalt) {
			resetCounter();
		}
		
		if (wavEnvDisable) {
			wavEnvOut = wavEnvSpeed;
		}
		
		// 上面是开胃菜, 下面才是主菜
		/*
		 * 我在这里做了调整, 因为不确定上面 time 的值,
		 * NSF 部分调用时 time 为一采样的时间, FTM 部分调用时 time 为一帧的时间, 这两个 time 相差很大.
		 * 因此我决定以 wavPhase 代表的相位作为统一的时间步长.
		 * 
		 * 当 wavPhase 代表的相位向前挪了一格, 相应的所有部分,
		 * 包括 wavCounter, modCounter, modTable 都执行这部分的时间.
		 * 
		 * 这样虽然会和真实的音频输出会有非常细微的差别（人感觉不到的）,
		 * 但是这样的 CPU 和代码可读性都会有显著提高.
		 */
		int clockLeft = time;
		
		if (!wavHalt) {
			int wavePhaseDelta = countWavePhaseDelta();
			int waveLeft = (((wavPhase >> 16) + 1) << 16) - wavPhase;
			int clockAccum = 0;
			
			while (clockLeft > 0) {
				waveLeft -= wavePhaseDelta;
				wavPhase += wavePhaseDelta;
				if (waveLeft < 0) {
					wavPhase &= 0x3FFFFF;
					// 上面这个条件满足时, wavPhase 指向的 wave 数组的索引向前挪了一格.
					stepAll(clockAccum);
					putOut(wavEnvOut);
					clockAccum = 0;
					waveLeft = (((wavPhase >> 16) + 1) << 16) - wavPhase;
				}
				
				clockAccum ++;
				clockLeft --;
				this.time ++;
			}
			
			if (clockAccum > 0) {
				stepAll(clockAccum);
			}
		} else {
			if (!modHalt) {
				modTableStep(time);
			}
			this.time += time;
		}
	}
	
	private void stepAll(int time) {
		if (!envHalt && !wavHalt && (masterEnvSpeed != 0)) {
			if (!wavEnvDisable) {
				wavCounterStep(time);
			}
			
			if (!modEnvDisable) {
				modCounterStep(time);
			}
		}
		if (!modHalt) {
			modTableStep(time);
		}
	}
	
	private void wavCounterStep(int time) {
		wavEnvCounter += time;
		int period = ((wavEnvSpeed + 1) * masterEnvSpeed) << 3;
		while (wavEnvCounter >= period) {
			// 包络按时钟向前走
			// clock the envelope
			if (wavEnvMode) {
				if (wavEnvOut < 32)
					++wavEnvOut;
			} else {
				if (wavEnvOut > 0)
					--wavEnvOut;
			}
			this.time += period;
			wavEnvCounter -= period;
		}
	}
	
	/**
	 * mod 计数器向前走
	 */
	private void modCounterStep(int time) {
		modEnvCounter += time;
		int modPeriod = ((modEnvSpeed + 1) * masterEnvSpeed) << 3;
		while (modEnvCounter >= modPeriod) {
			// clock the envelope
			if (modEnvMode) {
				if (modEnvOut < 32)
					++modEnvOut;
			} else {
				if (modEnvOut > 0)
					--modEnvOut;
			}
			modEnvCounter -= modPeriod;
		}
	}
	
	/**
	 * mod 调制表向前走
	 * @param time
	 */
	private void modTableStep(int time) {
		if (!modHalt) {
			// 分别计算前进前和前进后的相位
			// advance phase, adjust for modulator | unsigned
			int start_pos = modPhase >> 16;
			modPhase += (time * modFreq);
			int end_pos = modPhase >> 16;

			// modPhase 含低 24 位累积值位, 和高 6 位表示真正相位的数据, 范围 [0, 64 * 0xFFFF - 1]
			// wrap the phase to the 64-step table (+ 16 bit accumulator)
			modPhase = modPhase & 0x3FFFFF;

			// execute all clocked steps
			for (int p = start_pos; p < end_pos; ++p) {
				int wv = mods[p & 0x3F];
				if (wv == 4) // 4 意味着重置 mod position
					modPos = 0;
				else {
					final int BIAS[] = { 0, 1, 2, 4, 0, -4, -2, -1 };
					modPos += BIAS[wv];
					modPos &= 0x7F; // 7-bit 数值, 无符号位
				}
			}
		}
	}
	
	/**
	 * 计算 wavFreq 每个时钟会往上加多少值
	 */
	private int countWavePhaseDelta() {
		int mod = 0;
		if (modEnvOut != 0) { // skip if modulator off
			// convert mod_pos to 7-bit signed
			int pos = (modPos < 64) ? modPos : (modPos - 128);

			// multiply pos by gain,
			// shift off 4 bits but with odd "rounding" behaviour
			int temp = pos * modEnvOut;
			int rem = temp & 0x0F;
			temp >>= 4;
			if ((rem > 0) && ((temp & 0x80) == 0)) {
				if (pos < 0)
					temp -= 1;
				else
					temp += 2;
			}

			// temp 的范围在 [-64, 191]. 超过范围时, 进行修改
			// wrap if range is exceeded
			while (temp >= 192)
				temp -= 256;
			while (temp < -64)
				temp += 256;
			
			// 乘以 pitch 值, 再右移 6 位
			// multiply result by pitch,
			// shift off 6 bits, round to nearest
			temp = wavFreq * temp;
			rem = temp & 0x3F;
			temp >>= 6;
			if (rem >= 32)
				temp += 1;

			mod = temp;
		}
		
		return wavFreq + mod;
	}
	
	private void putOut(int vol_out) {
		if (vol_out > 32)
			vol_out = 32;
		
		// 最终输出
		if (!wavWrite)
			curOut = wave[(wavPhase >> 16) & 0x3F] * vol_out;
		
		// 音量的档位
		int v = 0;
		switch (masterVolume) {
		case 0:
			v = 30;
			break;
		case 1:
			v = 20;
			break;
		case 2:
			v = 15;
			break;
		case 3:
			v = 12;
			break;
		}
		
		v = (curOut * v) / 30;
		
		mix(v);
	}

}
