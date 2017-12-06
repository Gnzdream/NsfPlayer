package com.zdream.nsfplayer.ftm.format;

/**
 * FTM 模式
 * @author Zdream
 */
public class FtmPattern {
	
	/**
	 * 切换音调
	 */
	public static final int EFFECT_NOTE_HEAD = 0x710000;
	
	/**
	 * 切换乐器
	 */
	public static final int EFFECT_INST_HEAD = 0x720000;
	
	/**
	 * 切换音量
	 */
	public static final int EFFECT_VOL_HEAD = 0x730000;
	
	/**
	 * 轨道静音标识<br>
	 * 对应于文本上的 "---"
	 */
	public static final int EFFECT_MUTE_HEAD = 0x700000;
	
	/**
	 * 轨道全静音标识<br>
	 * 对应于文本上的 "==="
	 */
	public static final int EFFECT_MUTE2_HEAD = 0x7F0000;
	
	/**
	 * 没有其它效果, 只是标识行号
	 */
	public static final int EFFECT_LINE_HEAD = 0x770000;
	
	/**
	 * <p>效果 '0'. Arpeggio 琵琶音
	 * <p>0xy, 在每个 tick 时刻改变音符的音高, with base + x and base + y (半音). 用 00 来禁用.
	 * <p>Example: 047 模拟大调和弦.
	 */
	public static final int EFFECT_0 = 0;
	
	/**
	 * <p>效果 '1'. Slide up 向上的滑音
	 * <p>1xx, 不停地向上滑音, 每个 tick 时刻改变 xx 个 pitch 音高. 用 00 来禁用.
	 */
	public static final int EFFECT_1 = 0x10000;
	
	/**
	 * 
	 */
	public static final int EFFECT_2 = 0x20000;
	public static final int EFFECT_3 = 0x30000;
	public static final int EFFECT_4 = 0x40000;
	public static final int EFFECT_7 = 0x70000;
	public static final int EFFECT_A = 0xA0000;
	public static final int EFFECT_B = 0xB0000;
	public static final int EFFECT_C = 0xC0000;
	public static final int EFFECT_D = 0xD0000;
	public static final int EFFECT_E = 0xE0000;
	public static final int EFFECT_F = 0xF0000;
	public static final int EFFECT_G = 0x100000;
	public static final int EFFECT_H = 0x110000;
	public static final int EFFECT_I = 0x120000;
	public static final int EFFECT_P = 0x190000;
	public static final int EFFECT_Q = 0x1A0000;
	public static final int EFFECT_R = 0x1B0000;
	public static final int EFFECT_S = 0x1C0000;
	public static final int EFFECT_V = 0x1F0000;
	
	/**
	 * 序号
	 */
	public int seq;
	
	/**
	 * <p>将表示音调的字符串转成切换音调的标识号. 转换有以下的可能:
	 * <li>当字符串类似为 "C-3" 时, 说明这是音调切换, 最后返回的值是 0x710000 + 音调对应的值(C-3 对应的是 36)<br>
	 * 音调对应的值从 C-0 (等于 0) 开始, 依次往上, C#0 是 1, D-0 是 2, D#0 是 3 ...
	 * <li>当字符串类似为 "2-#" 时, 说明这是音调切换, 不过只在 Noise 轨道上出现,<br>
	 * 返回的值是 0x710000 + 音调对应的值(C-3 对应的是 36)<br>
	 * 音调对应的值从 0-# (等于 0) 开始, 依次往上, 1-# 是 1, 2-# 是 2, 3-# 是 3 ...
	 * <li>当字符串为 "---" 时, 说明需要静音, 返回静音标识 {@link #EFFECT_MUTE_HEAD}
	 * <li>当字符串为 "===" 时, 说明需要全静音, 返回静音标识 {@link #EFFECT_MUTE2_HEAD}
	 * @param noteText
	 *   表示音调的字符串, 长度为 3
	 * @return
	 *   返回对应值, 如果解析失败返回 -1
	 */
	public static int parseNoteValue(String noteText) {
		if ("---".equals(noteText)) {
			return EFFECT_MUTE_HEAD;
		} else if ("===".equals(noteText)) {
			return EFFECT_MUTE2_HEAD;
		}
		
		if (noteText.charAt(2) == '#') {
			return EFFECT_NOTE_HEAD | Integer.parseInt(noteText.substring(0, 1), 16);
		}
		
		int v = EFFECT_NOTE_HEAD;
		switch (noteText.substring(0, 2)) {
		case "C-":
			v |= 0;
			break;
		case "C#":
			v |= 1;
			break;
		case "D-":
			v |= 2;
			break;
		case "D#":
			v |= 3;
			break;
		case "E-":
			v |= 4;
			break;
		case "F-":
			v |= 5;
			break;
		case "F#":
			v |= 6;
			break;
		case "G-":
			v |= 7;
			break;
		case "G#":
			v |= 8;
			break;
		case "A-":
			v |= 9;
			break;
		case "A#":
			v |= 10;
			break;
		case "B-":
			v |= 11;
			break;

		default:
			return -1;
		}
		
		try {
			return (v + Integer.parseInt(noteText.substring(2)) * 12);
		} catch (Exception e) {
			return -1;
		}
	}
	
	/**
	 * <p>将表示效果的字符串转成效果标识号
	 * <p>效果标识号是三个字符串组成的, axy. a 是效果名, xy 是数值.
	 * @param effectText
	 *   表示效果的字符串, 长度为 3
	 * @return
	 *   返回对应值, 如果解析失败返回 -1
	 */
	public static int parseEffectValue(String effectText) {
		char effect = effectText.charAt(0);
		int v;
		
		switch (effect) {
		case '0':
			v = EFFECT_0;
			break;
		case '1':
			v = EFFECT_1;
			break;
		case '2':
			v = EFFECT_2;
			break;
		case '3':
			v = EFFECT_3;
			break;
		case '4':
			v = EFFECT_4;
			break;
		case '7':
			v = EFFECT_7;
			break;
		case 'A':
			v = EFFECT_A;
			break;
		case 'B':
			v = EFFECT_B;
			break;
		case 'C':
			v = EFFECT_C;
			break;
		case 'D':
			v = EFFECT_D;
			break;
		case 'E':
			v = EFFECT_E;
			break;
		case 'F':
			v = EFFECT_F;
			break;
		case 'G':
			v = EFFECT_G;
			break;
		case 'H':
			v = EFFECT_H;
			break;
		case 'I':
			v = EFFECT_I;
			break;
		case 'P':
			v = EFFECT_P;
			break;
		case 'Q':
			v = EFFECT_Q;
			break;
		case 'R':
			v = EFFECT_R;
			break;
		case 'S':
			v = EFFECT_S;
			break;
		case 'V':
			v = EFFECT_V;
			break;

		default:
			return -1;
		}
		
		return v | Integer.parseInt(effectText.substring(1), 16);
	}
	
	/**
	 * 效果列表
	 */
	public int[] effects;
	
	/**
	 * 行目录
	 */
	public int[] lines, lineIdx;
}
