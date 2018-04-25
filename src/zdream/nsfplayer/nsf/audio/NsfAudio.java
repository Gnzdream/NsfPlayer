package zdream.nsfplayer.nsf.audio;

/**
 * <p>NSF 音频文件内的数据
 * <p>这里将不再存放播放相关的数据
 * @author Zdream
 * @version v0.1
 * @date 2018-01-16
 */
public class NsfAudio {
	
	/**
	 * 当前 NSF 文件的版本号<br>
	 * 地址 0x000005, 单字节
	 */
	public short version;
	/**
	 * NSF 中乐曲数<br>
	 * 地址 0x000006, 单字节
	 */
	public short total_songs;
	/**
	 * 起始乐曲播放的号码<br>
	 * 地址 0x000007, 单字节
	 */
	public short start;
	
	/**
	 * 数据载入的内存地址, 范围 ($8000-$FFFF)<br>
	 * 地址 0x000008-0x000009, 双字节<br><br>
	 * 
	 * 这里说明了在游戏机 RAM 中的地址. 如果游戏放到内存中运行, 则 NSF 将放到内存中.
	 * 除去文件头 (地址 0x000000 至 0x00007F), 其它数据将放到 lenA 对应的地址中
	 */
	public int load_address;
	/**
	 * 初始化数据开始的地址, 范围($8000-$FFFF)<br>
	 * 地址 0x00000A-0x00000B, 双字节
	 */
	public int init_address;
	/**
	 * 乐曲播放地址, 范围($8000-$FFFF)<br>
	 * 地址 0x00000C-0x00000D, 双字节
	 */
	public int play_address;
	
	/*
	 * 游戏或乐曲的标题、曲作者或艺术家名称、版权部分,附加说明等略过
	 */
	/**
	 * NTSC 制式下乐曲循环播放速度, 常为 [16666]
	 */
	public int speed_ntsc;
	/**
	 * Bank 切换, 初始 8 bit 值<br>
	 * 
	 * 6502 汇编的寻址空间为 64K, 但是 NES 却只用 $8000-$FFFF, 共 32K, 对于像超级玛莉 1 这样的小游戏, 不用
	 * 考虑存储体 (Bank) 切换, 但是对于像魂斗罗 1, 2 代这样的游戏, 超过 32K, 就要进行存储体 (Bank) 切换,
	 * 大小可能不太一样, 有的是 16K, 有的是 32K, 有的是 8K 等等.
	 * 地址也不一样, $8000, $A000, $C000 都有可能.
	 * NSF 也会遇到空间不够的情况, 这时就要用到存储体 (Bank) 切换. NSF 存储体 (Bank) 切换大小为4K.
	 */
	public short bankswitch[] = new short[8];
	/**
	 * NTSC 制式下乐曲循环播放速度
	 */
	public int speed_pal;
	/**
	 * PAL/NTSC 制式选择<br>
	 * 
	 * <p>位开关, 数据从左（高）到右（低），前 6 位强制为 0
	 * <p>第 7 位如果为 1, NTSC/PAL, 此时第 8 位必须为 0; (= 2)<br>
	 * 否则第 7 位为 0, 第 8 位为 0, 为 NTSC 制式; 为 1, 为 PAL 制式
	 */
	public byte pal_ntsc;
	/**
	 * <b>特殊声音芯片</b><br>
	 * 
	 * 位开关, 数据从左（高）到右（低），前 2 位强制为 0<br>
	 * 第 3 位如果为 1, 使用 Sunsoft (FME7) 芯片;<br>
	 * 第 4 位如果为 1, 使用 Namcot (106) 芯片;<br>
	 * 第 5 位如果为 1, 使用 Nintendo (MMC5) 芯片;<br>
	 * 第 6 位如果为 1, 使用 Nintendo (FDS) 芯片;<br>
	 * 第 7 位如果为 1, 使用 Konami (VRC7) 芯片;<br>
	 * 第 8 位如果为 1, 使用 Konami (VRC6) 芯片;<br>
	 * 
	 * 如果 f2 == 0, 什么芯片也不用
	 */
	public byte soundchip;
	
	/**
	 * 标题
	 */
	public String title;
	
	/**
	 * 艺术家
	 */
	public String artist;
	
	/**
	 * 版权声明
	 */
	public String copyright;
	
	/**
	 * 剩余数据
	 */
	public byte[] body;
	
	// useVrc6, useVrc7, useFds, useMmc5, useN106, useFme7;
	
	public boolean useVrc6() {
		return (soundchip & 1) != 0;
	}
	
	public boolean useVrc7() {
		return (soundchip & 2) != 0;
	}
	
	public boolean useFds() {
		return (soundchip & 4) != 0;
	}
	
	public boolean useMmc5() {
		return (soundchip & 8) != 0;
	}
	
	/**
	 * <p>是否使用了 N163 芯片.
	 * <p>N163 (Namco 163) 又称为 N106.
	 * @return
	 */
	public boolean useN106() {
		return (soundchip & 16) != 0;
	}
	
	public boolean useFme7() {
		return (soundchip & 32) != 0;
	}
	
	NsfAudio() {
		
	}
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("NSF 乐曲 ").append(title).append(" - ").append(artist).append(" 总曲目: ").append(total_songs);
		
		return b.toString();
	}

}
