package com.zdream.nsfplayer.ftm.format;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;

import com.zdream.utils.common.CodeSpliter;

public class FtmAudioFactoryEntry {
	
	String txt;
	
	int line;
	
	Scanner scan;
	// 缓存上一行文本
	String bufText;
	
	static final String HEAD_SONG_INFO = "# Song information";
	static final String HEAD_SONG_COMMENT = "# Song comment";
	static final String HEAD_GLOBAL_SETTINGS = "# Global settings";
	static final String HEAD_MACROS = "# Macros";
	static final String HEAD_DPCM_SAMPLES = "# DPCM samples";
	static final String HEAD_INSTRUMENTS = "# Instruments";
	static final String HEAD_TRACKS = "# Tracks";
	static final String HEAD_END = "# End of export";
	
	static final String PROP_INST2A03 = "INST2A03";
	
	/**
	 * 要生成的音频类
	 */
	FtmAudio audio;
	
	FtmAudioFactoryEntry(String txt) {
		this.txt = txt;
	}
	
	/**
	 * 读取下一行文本, 行计数器加一
	 * @return
	 */
	String nextLine() {
		line++;
		if (bufText != null) {
			String text = bufText;
			bufText = null;
			return text;
		}
		return scan.nextLine();
	}
	
	/**
	 * 模拟回到上一行
	 * @param text
	 */
	void storeLine(String text) {
		line--;
		this.bufText = text;
	}
	
	/**
	 * 跳过 count 行空行.
	 * <p>如果跳过的行不是空行则抛出异常
	 * @throws FtmParseException
	 */
	void skipBlankLine(int count) {
		for (int i = 0; i < count; i++) {
			String str = nextLine();
			if (!str.equals("")) {
				throw new FtmParseException(line, "该行原本是空行, 但实际是: " + str);
			} 
		}
	}
	
	public FtmAudio createAudio() throws FtmParseException {
		scan = new Scanner(txt);
		line = 0;
		
		audio = new FtmAudio();
		audio.version = parseVersion();
		
		skipBlankLine(1);
		
		// do somrthing
		while (scan.hasNext()) {
			String str = nextLine();
			
			if (str.equals("")) {
				continue;
			}
			
			if (str.charAt(0) == '#') {
				int v = parseBlock(str);
				if (v == 1) {
					break;
				}
			} else {
				throw new FtmParseException(line, "无法解析该行, 这行开头应该是 '#'");
			}
		}
		
		scan.close();
		scan = null;
		
		return audio;
	}
	
	/**
	 * 检查文本第一行关于版本号的描述.<br>
	 * 第一行的文本应该是这样的:
	 * <blockquote>
	 * # FamiTracker text export 0.4.2
	 * </blockquote>
	 * @return
	 *   版本号文字, 比如 0.4.2
	 * @throws FtmParseException
	 *   版本号描述出错
	 */
	String parseVersion() throws FtmParseException {
		String firstLine = nextLine();
		if (!firstLine.startsWith("# FamiTracker text export ")) {
			throw new FtmParseException(line, "不是有效的文本导出文件");
		}
		return firstLine.substring("# FamiTracker text export ".length());
	}
	
	/**
	 * 开始解析一个块. 一个块的标题以 '#' 作为开头
	 * @param str
	 *   标题行, 以 '#' 开头的行
	 * @return
	 *   0 - 系统正常
	 *   1 - 检测到文本结束符号, 应该停止解析
	 * @throws FtmParseException
	 */
	int parseBlock(String str) throws FtmParseException {
		switch (str) {
		case HEAD_SONG_INFO:
			parseSongInfo();
			break;
			
		case HEAD_SONG_COMMENT:
			parseComment();
			break;
			
		case HEAD_GLOBAL_SETTINGS:
			parseGlobalSettings();
			break;
			
		case HEAD_MACROS:
			parseMacros();
			break;
			
		case HEAD_DPCM_SAMPLES:
			parseDpcms();
			break;
			
		case HEAD_INSTRUMENTS:
			parseInstruments();
			break;
			
		case HEAD_TRACKS:
			parseTracks();
			break;
			
		case HEAD_END:
			return 1;

		default:
			break;
		}
		
		return 0;
	}
	
	/**
	 * 解析歌曲信息部分.<br>
	 * 包括标题 (TITLE)、作家 (AUTHOR) 和版权 (COPYRIGHT).
	 * @throws FtmParseException
	 */
	void parseSongInfo() throws FtmParseException {
		for (int i = 0; i < 3; i++) {
			String str = nextLine();
			// 第一行是标题
			
			String check;
			switch (i) {
			case 0:
				check = "TITLE";
				break;
			case 1:
				check = "AUTHOR";
				break;
			case 2:
				check = "COPYRIGHT";
				break;
			default:
				continue;
			}
			
			if (!str.startsWith(check)) {
				throw new FtmParseException(line, "歌曲信息部分这里应该是 " + check);
			}
			if (str.charAt(str.length() - 1) != '"') {
				throw new FtmParseException(line, "歌曲信息部分这里应该以\"结尾");
			}
			
			int start = str.indexOf('"');
			// 这里如果写 TITLE " 的话会报异常, 不过本来就非法
			try {
				String value = str.substring(start + 1, str.length() - 1);
				
				switch (i) {
				case 0:
					audio.title = value;
					break;
				case 1:
					audio.author = value;
					break;
				case 2:
					audio.copyright = value;
					break;
				default:
					continue;
				}
			} catch (IndexOutOfBoundsException e) {
				throw new FtmParseException(line, "歌曲信息部分" + check + "为空", e);
			}
		}
	}
	
	/**
	 * <p>解析歌曲的评论、说明部分.
	 * <p>该部分分为多行, 每条评论占一行.
	 * @throws FtmParseException
	 */
	void parseComment() throws FtmParseException {
		ArrayList<String> comments = new ArrayList<>();
		
		while(true) {
			String str = nextLine();
			
			if (str.equals("")) {
				// 结束了
				break;
			} else if (str.startsWith("COMMENT \"")
					&& str.charAt(str.length() - 1) == '\"' && str.length() >= 10) {
				String comment = str.substring(9, str.length() - 1);
				comments.add(comment);
			} else {
				throw new FtmParseException(line, "评论部分为空");
			}
		}
		
		audio.comments = comments.toArray(new String[comments.size()]);
	}
	
	/**
	 * <p>解析全局设定部分
	 * @throws FtmParseException
	 */
	void parseGlobalSettings() throws FtmParseException {
		while (true) {
			String str = nextLine();
			if (str.equals("")) {
				break;
			}
			
			String key = str.substring(0, str.indexOf(' '));
			switch (key) {
			case "MACHINE":
				audio.machine = Byte.parseByte(str.substring(str.lastIndexOf(' ') + 1));
				break;
			case "FRAMERATE":
				audio.framerate = Integer.parseInt(str.substring(str.lastIndexOf(' ') + 1));
				break;
			case "EXPANSION": {
				int exp = Integer.parseInt(str.substring(str.lastIndexOf(' ') + 1));
				audio.useVcr6 = (exp & 1) > 0;
				audio.useVcr7 = (exp & 2) > 0;
				audio.useFds = (exp & 4) > 0;
				audio.useMmc5 = (exp & 8) > 0;
				audio.useN163 = (exp & 16) > 0;
				audio.useS5b = (exp & 32) > 0;
			} break;
			case "VIBRATO":
				audio.vibrato = Byte.parseByte(str.substring(str.lastIndexOf(' ') + 1));
				break;
			case "SPLIT":
				audio.split = Integer.parseInt(str.substring(str.lastIndexOf(' ') + 1));
				break;
			case "N163CHANNELS":
				// TODO 因为现在没考虑 N163, 所以暂时放一边
				break;
			default:
				break;
			}
			
		}
	}
	
	/**
	 * 
	 * @throws FtmParseException
	 */
	void parseMacros() throws FtmParseException {
		while (true) {
			String str = nextLine();
			if (str.equals("")) {
				break;
			}
			// TODO 现在的实例没有 macro, 暂不实现
		}
	}
	
	/**
	 * 
	 * @throws FtmParseException
	 */
	void parseDpcms() throws FtmParseException {
		while (true) {
			String str = nextLine();
			if (str.equals("")) {
				break;
			}
			// TODO 现在的实例没有 dpcm 采样, 暂不实现
		}
	}
	
	/**
	 * 解析乐器部分
	 * @throws FtmParseException
	 */
	void parseInstruments() throws FtmParseException {
		// 用来缓存乐器
		ArrayList<IInst> insts = new ArrayList<>();
		// 现在用来计数, 每个种类的乐器数量
		int i_2a03 = 0;
		
		while (true) {
			String str = nextLine();
			if (str.equals("")) {
				break;
			}
			
			String[] strs = CodeSpliter.split(str);
			if (strs.length == 0) {
				throw new FtmParseException(line, "乐器部分解析错误, 数据为空");
			}
			
			switch (strs[0]) {
			case PROP_INST2A03: {
				if (strs.length != 8) {
					throw new FtmParseException(line,
							"乐器部分解析错误, 2A03 乐器格式规定项数为 8, 但是这里只有 " + strs.length);
				}
				Inst2A03 ins = new Inst2A03();
				ins.seq = Integer.parseInt(strs[1]);
				
				int v;
				
				v = Integer.parseInt(strs[2]);
				if (v >= 0) {
					// TODO ins.vol = 
				}
				
				v = Integer.parseInt(strs[3]);
				if (v >= 0) {
					// TODO ins.arp = 
				}
				
				v = Integer.parseInt(strs[4]);
				if (v >= 0) {
					// TODO ins.pit = 
				}
				
				v = Integer.parseInt(strs[5]);
				if (v >= 0) {
					// TODO ins.hip = 
				}
				
				v = Integer.parseInt(strs[6]);
				if (v >= 0) {
					// TODO ins.dut = 
				}
				
				ins.name = strs[7];
				insts.add(ins);
				
				i_2a03++;
			} break;

			default:
				break;
			}
			
		}
		
		// 循环完成之后
		
		audio.inst2a03s = new Inst2A03[i_2a03];
		// 现在计数, 每个种类的乐器里, 成功放到 audio 中的乐器的数量
		i_2a03 = 0;
		
		for (IInst ins : insts) {
			switch (ins.instType()) {
			case FtmAudio.INST_TYPE_2A03:
				audio.inst2a03s[i_2a03++] = (Inst2A03) ins;
				break;

			default:
				break;
			}
		}
	}

	/**
	 * 解析乐曲部分
	 * @throws FtmParseException
	 */
	void parseTracks() throws FtmParseException {
		// 第一行一定是空行
		skipBlankLine(1);
		
		String str = this.nextLine();
		ArrayList<FtmTrack> tracks = new ArrayList<>();
		while (true) {
			if (str.startsWith("TRACK ")) {
				FtmTrack track = new FtmTrack();
				str = parseTrack(str, track);
				tracks.add(track);
			} else {
				storeLine(str);
				break;
			}
		}
		
		audio.tracks = tracks.toArray(new FtmTrack[tracks.size()]);
	}
	
	/**
	 * 解析每一个乐曲
	 * @param firstLine
	 * @param track
	 *   解析的数据放到这个里面
	 * @return
	 */
	String parseTrack(final String firstLine, FtmTrack track) throws FtmParseException {
		String[] strs = CodeSpliter.split(firstLine);
		
		track.length = Integer.parseInt(strs[1]);
		track.speed = Integer.parseInt(strs[2]);
		track.tempo = Integer.parseInt(strs[3]);
		track.name = strs[4];
		
		// 下一行是 columns
		String str = nextLine();
		if (!str.startsWith("COLUMNS : ")) {
			throw new FtmParseException(line, "解析乐曲错误, 这行本应该是 COLUMNS");
		}
		strs = CodeSpliter.split(str.substring(10));
		int[] columns = new int[strs.length];
		for (int i = 0; i < columns.length; i++) {
			columns[i] = Integer.parseInt(strs[i]);
		}
		
		skipBlankLine(1);
		
		// 下一行是 ORDER
		int seq = 0; // 序号
		int seq_order; // 这是临时变量, 储存解析出来的序号, 用来和 seq 作对比的
		ArrayList<int[]> orders = new ArrayList<>();
		while (true) {
			str = nextLine();
			if ("".equals(str)) {
				break;
			}
			
			if (!str.startsWith("ORDER ")) {
				throw new FtmParseException(line, "解析乐曲错误, 这行本应该是 ORDER");
			}
			strs = CodeSpliter.split(str.substring(6));
			
			if (strs.length != columns.length + 2) {
				throw new FtmParseException(line,
						"解析乐曲错误, ORDER 数据量不正确, 应该是 " + columns.length + ", 而实际是 " + (strs.length - 2));
			}
			
			seq_order = Integer.parseInt(strs[0], 16);
			if (seq_order != seq) {
				throw new FtmParseException(line,
						"解析乐曲错误, ORDER 序号错误, 应该是 " + seq + ", 而实际是 " + seq_order);
			}
			seq++;
			if (!":".equals(strs[1])) {
				throw new FtmParseException(line, "解析乐曲错误, ORDER 部分 ':' 在期望的位置没有出现");
			}
			
			int[] os = new int[columns.length];
			for (int i = 0; i < os.length; i++) {
				os[i] = Integer.parseInt(strs[i + 2], 16);
			}
			orders.add(os);
		}
		
		// 下一行是 PATTERN
		ArrayList<FtmPattern[]> patterns = new ArrayList<>();
		while (true) {
			str = nextLine();
			if ("".equals(str)) {
				continue;
			}
			
			if (str.startsWith("PATTERN ")) {
				patterns.add(parsePattern(str, columns.length, track.length));
			} else {
				break;
			}
		}
		
		track.patterns = new FtmPattern[patterns.size()][];
		for (int i = 0; i < track.patterns.length; i++) {
			track.patterns[i] = patterns.get(i);
		}
		
		// Order 关联 Pattern
		track.orders = new FtmPattern[orders.size()][];
		for (int i = 0; i < track.orders.length; i++) {
			FtmPattern[] ps = new FtmPattern[columns.length];
			int[] os = orders.get(i);
			
			for (int j = 0; j < os.length; j++) {
				ps[j] = track.patterns[os[j]][j];
			}
			
			track.orders[i] = ps;
		}
		
		return str;
	}
	
	/**
	 * 解析 Track - Pattern 部分
	 * @param fristLine
	 *   第一行文本, 类似于 "PATTERN 00"
	 * @param column
	 *   期望这个 Pattern 有几个轨道
	 * @param maxRow
	 *   最多的行数
	 * @return
	 * @throws FtmParseException
	 */
	FtmPattern[] parsePattern(final String fristLine, final int column, final int maxRow)
			throws FtmParseException {
		// 第一行
		final int seq = Integer.parseInt(fristLine.substring(8), 16);
		
		ArrayList<LinkedList<Integer>> lists = new ArrayList<>(column);
		for (int i = 0; i < column; i++) {
			lists.add(new LinkedList<>());
		}
		
		int row = 0; // 期望的行数
		int row_pattern; // 这是临时变量, 储存解析出来的行数, 用来和 row 作对比的
		
		/*
		 * 是否标识过本行的行号.
		 * 
		 * 因为数据存储是这样的:
		 * 行号0, 改变音调, 改变音量, 行号2, 改变乐器, 改变音量, P效果, 行号6, 改变音量...
		 * 不是所有的行号都会放上去. 如果本行有要改变的东西, 会先放行号标识;
		 * 如果本行已经标识过, 就不需要再标识了, 所以需要这个变量记录是否本行已经标识过.
		 */
		boolean sign;
		
		while (true) {
			String str = nextLine();
			if ("".equals(str)) {
				break;
			}
			
			if (!str.startsWith("ROW ")) {
				throw new FtmParseException(line, "解析乐曲错误, 这行本应该是 ROW");
			}
			String[] strs0 = str.substring(4).split(" : ");
			if (strs0.length != column + 1) {
				throw new FtmParseException(line,
						"解析乐曲错误, ROW 数据量不正确, 应该是 " + column + ", 而实际是 " + (strs0.length - 1));
			}
			
			row_pattern = Integer.parseInt(strs0[0], 16);
			if (row != row_pattern) {
				throw new FtmParseException(line,
						"解析乐曲错误, ROW 行数不正确, 应该是 " + row + ", 而实际是 " + row_pattern);
			}
			
			// 解析每个轨道, 开发用的轨道号: i-1
			
			for (int i = 1; i < strs0.length; i++) {
				String[] strs = CodeSpliter.split(strs0[i]);
				
				if (strs.length < 4) {
					throw new FtmParseException(line,
							String.format("解析乐曲错误, 第 %d 轨道只有 %d 个数据, 但是实际应该超过 4", i, strs.length));
				}
				sign = false;
				
				// 0 号数据, note
				String s = strs[0];
				CHECK_NOTE: {
					if ("...".equals(s)) {
						break CHECK_NOTE;
					}
					
					int v = FtmPattern.parseNoteValue(s);
					if (v == -1) {
						throw new FtmParseException(line,
								String.format("解析乐曲错误, 第 %d 轨道的 %s 无法解析", i, s));
					}
					// 标识行号
					lists.get(i - 1).add(FtmPattern.EFFECT_LINE_HEAD | row);
					sign = true;
					
					lists.get(i - 1).add(v);
				}
				
				// 1 号数据, 乐器
				s = strs[1];
				CHECK_INST: {
					if ("..".equals(s)) {
						break CHECK_INST;
					}
					// 标识行号
					if (!sign) {
						lists.get(i - 1).add(FtmPattern.EFFECT_LINE_HEAD | row);
						sign = true;
					}
					
					lists.get(i - 1).add(FtmPattern.EFFECT_INST_HEAD | Integer.parseInt(s, 16));
				}
				
				// 2 号数据, 音量
				s = strs[2];
				CHECK_VOL: {
					if (".".equals(s)) {
						break CHECK_VOL;
					}
					// 标识行号
					if (!sign) {
						lists.get(i - 1).add(FtmPattern.EFFECT_LINE_HEAD | row);
						sign = true;
					}
					
					lists.get(i - 1).add(FtmPattern.EFFECT_VOL_HEAD | Integer.parseInt(s, 16));
				}
				
				// 其它数据, 效果
				for (int j = 3; j < strs.length; j++) {
					s = strs[j];
					
					if ("...".equals(s)) {
						continue;
					}
					// 标识行号
					if (!sign) {
						lists.get(i - 1).add(FtmPattern.EFFECT_LINE_HEAD | row);
						sign = true;
					}
					
					lists.get(i - 1).add(FtmPattern.parseEffectValue(s));
				}
				
			}
			
			row++;
		}
		
		// print out
		/*for (int j = 0; j < column; j++) {
			StringBuilder builder = new StringBuilder(100);
			builder.append("row ").append(j).append(": ");
			
			for (Iterator<Integer> it = lists.get(j).iterator(); it.hasNext();) {
				int v = it.next();
				builder.append(Integer.toHexString(v)).append(' ');
			}
			System.out.println(builder.toString());
		}*/
		// end print out
		
		FtmPattern[] patterns = new FtmPattern[column];
		for (int i = 0; i < patterns.length; i++) {
			FtmPattern pattern = new FtmPattern();
			pattern.seq = seq;
			
			LinkedList<Integer> list = lists.get(i);
			int[] effects = new int[list.size()];
			int j = 0;
			// 记录出现行号的个数
			int count = 0;
			
			for (Iterator<Integer> it = list.iterator(); it.hasNext();) {
				int v = it.next();
				
				effects[j] = v;
				if ((v & 0x7FFF0000) == FtmPattern.EFFECT_LINE_HEAD) {
					count++;
				}
				j++;
			}
			
			j = 0;
			int[] lines = new int[count], lineIdx = new int[count];
			
			for (int idx = 0; idx < effects.length; idx++) {
				int v = effects[idx];
				if ((v & 0x7FFF0000) == FtmPattern.EFFECT_LINE_HEAD) {
					lines[j] = v & 0xFFFF;
					lineIdx[j] = idx;
					
					j++;
				}
			}
			
			pattern.effects = effects;
			pattern.lines = lines;
			pattern.lineIdx = lineIdx;
			patterns[i] = pattern;
		}
		
		return patterns;
	}
	
}
