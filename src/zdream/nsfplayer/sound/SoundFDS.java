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
	 * 波形信封表部分:
	 *   [0x4040, 0x407F]
	 * 其它参数部分:
	 * 00 号位: 0x4080 波形信封控制
	 * 01 号位: 0x4081 无
	 * 02 号位: 0x4082 波形频率参数的低 8 位（共 12 位）
	 * 03 号位: 0x4083 波形频率参数的高 4 位（共 12 位）, 禁用标识
	 * 04 号位: 0x4084 调制信封控制
	 * 05 号位: 0x4085 调制相位
	 * 06 号位: 0x4086 调制频率参数的低 8 位（共 12 位）
	 * 07 号位: 0x4087 调制频率参数的高 4 位（共 12 位）, 禁用标识
	 * 08 号位: 0x4088 调制信封表输入
	 * 09 号位: 0x4089 波形写入标识, 总音量
	 * 10 号位: 0x408A 信封播放速度
	 */
	
	/**
	 * <p>[0x4040, 0x407F]
	 * <p>波形信封表, 每个单元的值的范围 [0, 63]
	 * </p>
	 */
	public final byte[] wave = new byte[64];
	
	/**
	 * <p>08 号位: 00000xxx 累积值的表数据
	 * <p>调制信封表, 每个单元的值的范围 [0, 7].
	 * <p>推荐使用 {@link #writeMods(byte)} 方法来写入该值
	 * </p>
	 */
	public final byte[] mods = new byte[64];
	
	/**
	 * <p>00 号位: x0000000
	 * <p>波形信封禁用标志.
	 * <p>为 1 时为 true, 此时波形信封被禁用; 为 0 时为 false, 此时启用波形信封
	 * </p>
	 */
	public boolean wavEnvDisable;
	
	/**
	 * <p>00 号位: 0x000000
	 * <p>波形信封模式
	 * <p>为 1 时为 true, 为 0 时为 false
	 * </p>
	 */
	public boolean wavEnvMode;
	
	/**
	 * <p>00 号位: 00xxxxxx
	 * <p>波形信封播放速度
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
	 * <p>信封暂停标志
	 * <p>为 1 时为 true, 信封暂停; 为 0 时为 false
	 * </p>
	 */
	public boolean envHalt;
	
	/**
	 * <p>04 号位: x0000000
	 * <p>调制信封禁用标志.
	 * <p>为 1 时为 true, 此时调制信封被禁用; 为 0 时为 false, 此时启用调制信封
	 * </p>
	 */
	public boolean modEnvDisable;
	
	/**
	 * <p>04 号位: 0x000000
	 * <p>调制信封模式
	 * <p>为 1 时为 true, 为 0 时为 false
	 * </p>
	 */
	public boolean modEnvMode;
	
	/**
	 * <p>04 号位: 00xxxxxx
	 * <p>调制信封播放速度
	 * <p>范围 [0, 63]
	 * </p>
	 */
	public int modEnvSpeed;
	
	/**
	 * <p>05 号位: 0xxxxxxx
	 * <p>调制信封相位
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
	 * <p>波形写入标志
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
	 * <p>总信封速度
	 * <p>范围 [0, 255]
	 * </p>
	 */
	public int masterEnvSpeed;
	
	// TODO
	
	/* **********
	 * 输入方法 *
	 ********** */
	
	/**
	 * <p>写入 mod 值到调制信封表中.
	 * <p>在原 NSF 程序中, 通过写入 0x4088 单元来控制该表, 而该方法就模拟这个行为.
	 * </p>
	 * @param mod
	 *   范围 [0, 7]
	 */
	public void writeMods(byte mod) {
		// TODO
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
		// TODO
		
		super.reset();
		
		// 还要补充的
		// TODO write(0x408A, 0xE8, 0);
		// 0x4080
		// 0x4083
		// 0x4084
		// 0x4085
		// 0x4087
		
	}

	@Override
	protected void onProcess(int time) {
		// TODO Auto-generated method stub

	}

}
