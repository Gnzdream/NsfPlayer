package zdream.nsfplayer.ftm.factory;

import java.util.ArrayList;

import zdream.nsfplayer.ftm.document.FamiTrackerHandler;
import zdream.nsfplayer.ftm.document.FtmAudio;
import zdream.nsfplayer.ftm.format.FtmInstrument2A03;
import zdream.nsfplayer.ftm.format.FtmNote;
import zdream.nsfplayer.ftm.format.FtmSequenceType;
import zdream.nsfplayer.ftm.format.FtmTrack;
import zdream.utils.common.CodeSpliter;
import zdream.utils.common.TextReader;

public class FamiTrackerTextCreater extends AbstractFamiTrackerCreater {
	
	/**
	 * 文本读取器
	 */
	TextReader reader;
	
	// 缓存数值
	
	/**
	 * 正在解析的曲目号
	 */
	int curTrackIdx = -1;
	
	/**
	 * 正在解析的曲目
	 */
	FtmTrack curTrack;
	
	/**
	 * 各列的效果列数
	 */
	int[] columns;
	
	/**
	 * order 的列表
	 */
	ArrayList<int[]> orders;
	
	/**
	 * pattern 部分
	 */
	int patternIdx = -1;
	
	/**
	 * 行数
	 */
	int rowIdx = 0;
	
	public FamiTrackerTextCreater() {
		// do nothing
	}
	
	/**
	 * 按照文本内容来生成 {@link FtmAudio}
	 * @param reader
	 * @param doc
	 * @throws FtmParseException
	 */
	public void doCreate(TextReader reader, FamiTrackerHandler doc) throws FtmParseException {
		this.reader = reader;
		reset();
		
		if (!reader.isFinished()) {
			while (reader.toNextValidLine() >= 0) {
				handleLine(reader, doc);
			}
		}
		
		reader.close();
	}
	
	public void reset() {
		
	}
	
	private void handleLine(TextReader reader, FamiTrackerHandler doc) {
		String[] strs = CodeSpliter.split(reader.thisLine());
		
		switch (strs[0]) {
		
		// Song information
		case "TITLE": {
			doc.audio.title = strs[1];
		} break;
		
		case "AUTHOR": {
			doc.audio.author = strs[1];
		} break;
		
		case "COPYRIGHT": {
			doc.audio.copyright = strs[1];
		} break;
		
		// Global settings
		case "MACHINE": {
			doc.setMechine(Byte.parseByte(strs[1]));
		} break;
		
		case "FRAMERATE": {
			doc.setFramerate(Integer.parseInt(strs[1]));
		} break;
		
		case "EXPANSION": {
			doc.setChip(Byte.parseByte(strs[1]));
		} break;
		
		case "VIBRATO": {
			doc.setVibrato(Byte.parseByte(strs[1]));
		} break;
		
		case "SPLIT": {
			doc.setSplit(Integer.parseInt(strs[1]));
		} break;
		
		// Instruments
		case "INST2A03": {
			parseInst2A03(reader, doc, strs);
		} break;
		
		// Tracks
		case "TRACK": {
			parseTrack(reader, doc, strs);
		} break;
		
		case "COLUMNS": {
			parseColumns(reader, doc, strs);
		} break;
		
		case "ORDER": {
			parseOrder(reader, doc, strs);
		} break;
		
		case "PATTERN": {
			parsePattern(reader, doc, strs);
		} break;
		
		case "ROW": {
			parseRow(reader, doc, strs);
		} break;

		default:
			break;
		}
	}
	
	private void parseInst2A03(TextReader reader, FamiTrackerHandler doc, String[] strs) {
		if (strs.length != 8) {
			throw new FtmParseException(reader.line(),
					"乐器部分解析错误, 2A03 乐器格式规定项数为 8, 但是这里只有 " + strs.length);
		}
		
		FtmInstrument2A03 inst = new FtmInstrument2A03();
		inst.seq = Integer.parseInt(strs[1]);
		inst.name = strs[7];
		
		inst.vol = Integer.parseInt(strs[2]);
		if (inst.vol != -1) {
			createSequence(doc, inst.vol, FtmSequenceType.VOLUME);
		}
		
		inst.arp = Integer.parseInt(strs[3]);
		if (inst.arp != -1) {
			createSequence(doc, inst.arp, FtmSequenceType.ARPEGGIO);
		}
		
		inst.pit = Integer.parseInt(strs[4]);
		if (inst.pit != -1) {
			createSequence(doc, inst.pit, FtmSequenceType.PITCH);
		}
		
		inst.hip = Integer.parseInt(strs[5]);
		if (inst.hip != -1) {
			createSequence(doc, inst.hip, FtmSequenceType.HI_PITCH);
		}
		
		inst.dut = Integer.parseInt(strs[6]);
		if (inst.dut != -1) {
			createSequence(doc, inst.dut, FtmSequenceType.DUTY);
		}

		doc.registerInstrument(inst);
	}
	
	private void parseTrack(TextReader reader, FamiTrackerHandler doc, String[] strs) {
		if (strs.length != 5) {
			throw new FtmParseException(reader.line(),
					"曲目部分解析错误, TRACK 格式规定项数为 5, 但是这里只有 " + strs.length);
		}
		
		this.curTrackIdx++;
		FtmTrack track = doc.createTrack();
		curTrack = track;
		
		track.length = Integer.parseInt(strs[1]);
		track.speed = Integer.parseInt(strs[2]);
		track.tempo = Integer.parseInt(strs[3]);
		track.name = strs[4];
		
		orders = new ArrayList<>();
		patternIdx = -1;
	}
	
	private void parseColumns(TextReader reader, FamiTrackerHandler doc, String[] strs) {
		if (!":".equals(strs[1])) {
			throw new FtmParseException(reader.line(),
					"曲目部分解析错误, COLUMNS 部分");
		}
		
		columns = new int[strs.length - 2];
		int length = strs.length - 2;
		for (int i = 0; i < length; i++) {
			columns[i] = Integer.parseInt(strs[i + 2]);
		}
	}
	
	private void parseOrder(TextReader reader, FamiTrackerHandler doc, String[] strs) {
		if (strs.length != 3 + columns.length) {
			throw new FtmParseException(reader.line(),
					String.format("曲目部分解析错误, ORDER 格式规定项数为 %d, 但是这里只有 %d",
							3 + columns.length, strs.length));
		}
		if (!":".equals(strs[2])) {
			throw new FtmParseException(reader.line(),
					"曲目部分解析错误, COLUMNS 部分");
		}
		
		int index = Integer.parseInt(strs[1], 16);
		if (index != orders.size()) {
			throw new FtmParseException(reader.line(),
					"曲目部分解析错误, ORDER 部分序号不匹配: " + index + " != " + orders.size());
		}
		
		int[] order = new int[columns.length];
		for (int i = 0; i < order.length; i++) {
			order[i] = Integer.parseInt(strs[3 + i], 16);
		}
		orders.add(order);
	}
	
	private void parsePattern(TextReader reader, FamiTrackerHandler doc, String[] strs) {
		if (strs.length != 2) {
			throw new FtmParseException(reader.line(),
					"曲目部分解析错误, PATTERN 部分");
		}
		
		int patternIdx = Integer.parseInt(strs[1], 16);
		
		if (patternIdx == 0) {
			int len = orders.size();
			curTrack.orders = new int[len][];
			for (int i = 0; i < len; i++) {
				curTrack.orders[i] = orders.get(i);
			}
		}
		
		rowIdx = 0; // 行数从 0 开始计
	}
	
	private void parseRow(TextReader reader, FamiTrackerHandler doc, String[] strs) {
		// 预计的 strs 的数组长度
		int lenExp = 2;
		for (int i = 0; i < columns.length; i++) {
			lenExp += (4 + columns[i]);
		}
		
		if (strs.length != lenExp) {
			throw new FtmParseException(reader.line(),
				String.format("曲目部分解析错误, ROW 格式规定项数为 %d, 但是这里只有 %d",
					lenExp, strs.length));
		}
		
		// 行数
		int row = Integer.parseInt(strs[1], 16);
		if (row != rowIdx) {
			throw new FtmParseException(reader.line(),
				String.format("曲目部分解析错误, ROW 格式中预计行数为 %d, 但是是 %d",
					rowIdx, reader.line()));
		}
		rowIdx++;
		
		// 解析部分
		int offset = 2;
		for (int column = 0; column < columns.length; column++) {
			int length = 3 + columns[column];
			parseColumn(reader, doc, strs, column, offset, length);
		}
		
		// TODO
	}
	
	/**
	 * 解析一个 note 部分, 产生 {@link FtmNote} 并存到 doc 部分
	 * @param reader
	 * @param doc
	 * @param strs
	 * @param column
	 *   第几列
	 * @param offset
	 *   输入该列的 strs 是从第几个元素开始起
	 * @param length
	 *   属于该列的 strs 的长度, 至少是 5
	 *   <br>(第一个是 ':', 第二个是音调, 第三个是乐器, 第四个是音量, 第五个以及后面都是效果)
	 */
	private void parseColumn(
			TextReader reader,
			FamiTrackerHandler doc,
			String[] strs,
			final int column,
			final int offset,
			final int length) {
		// 第一个元素必须是 ":"
		if (!":".equals(strs[offset])) {
			throw new FtmParseException(reader.line(),
				String.format("曲目部分解析错误, ROW 格式中第 %d 个元素必须是 ':'",
					offset, reader.line()));
		}
		
		FtmNote note = new FtmNote();
		
		// 音调部分
		String t = strs[offset + 1];
		
		if ("...".equals(t)) {
			note.note = FtmNote.NOTE_NONE;
			note.octave = 0;
		} else if ("---".equals(t)) {
			note.note = FtmNote.NOTE_HALT;
			note.octave = 0;
		} else if ("===".equals(t)) {
			note.note = FtmNote.NOTE_RELEASE;
			note.octave = 0;
		} else {
			String tt = t.substring(0, 2);
			
			
			
			System.out.println(tt);
		}
		
	}
	
	


	
	/*//**
	 * 开始解析一个块. 一个块的标题以 '#' 作为开头
	 * @param str
	 *   标题行, 以 '#' 开头的行
	 * @return
	 *   0 - 系统正常
	 *   1 - 检测到文本结束符号, 应该停止解析
	 * @throws FtmParseException
	 *//*
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
	
	*//**
	 * 解析歌曲信息部分.<br>
	 * 包括标题 (TITLE)、作家 (AUTHOR) 和版权 (COPYRIGHT).
	 * @throws FtmParseException
	 *//*
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
	
	*//**
	 * <p>解析歌曲的评论、说明部分.
	 * <p>该部分分为多行, 每条评论占一行.
	 * @throws FtmParseException
	 *//*
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
	
	*//**
	 * <p>解析全局设定部分
	 * @throws FtmParseException
	 *//*
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
	
	*//**
	 * 
	 * @throws FtmParseException
	 *//*
	void parseMacros() throws FtmParseException {
		while (true) {
			String str = nextLine();
			if (str.equals("")) {
				break;
			}
			// TODO 现在的实例没有 macro, 暂不实现
		}
	}
	
	*//**
	 * 
	 * @throws FtmParseException
	 *//*
	void parseDpcms() throws FtmParseException {
		while (true) {
			String str = nextLine();
			if (str.equals("")) {
				break;
			}
			// TODO 现在的实例没有 dpcm 采样, 暂不实现
		}
	}
	
	*//**
	 * 解析乐器部分
	 * @throws FtmParseException
	 *//*
	void parseInstruments() throws FtmParseException {
		// 用来缓存乐器
		ArrayList<AbstractFtmInstrument> insts = new ArrayList<>();
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
				FtmInstrument2A03 ins = new FtmInstrument2A03();
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
		
		//audio.inst2a03s = new FtmInstrument2A03[i_2a03];
		// 现在计数, 每个种类的乐器里, 成功放到 audio 中的乐器的数量
		i_2a03 = 0;
		
		for (AbstractFtmInstrument ins : insts) {
			switch (ins.instType()) {
			case _2A03:
				//audio.inst2a03s[i_2a03++] = (FtmInstrument2A03) ins;
				break;

			default:
				break;
			}
		}
	}

	*//**
	 * 解析乐曲部分
	 * @throws FtmParseException
	 *//*
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
		
		audio.tracks = tracks;
	}
	
	*//**
	 * 解析每一个乐曲
	 * @param firstLine
	 * @param track
	 *   解析的数据放到这个里面
	 * @return
	 *//*
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
		track.orders = new int[orders.size()][];
		for (int i = 0; i < track.orders.length; i++) {
			track.orders[i] = orders.get(i);
		}
		
		return str;
	}
	
	*//**
	 * 解析 Track - Pattern 部分
	 * @param fristLine
	 *   第一行文本, 类似于 "PATTERN 00"
	 * @param column
	 *   期望这个 Pattern 有几个轨道
	 * @param maxRow
	 *   最多的行数
	 * @return
	 * @throws FtmParseException
	 *//*
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
		
		
		 * 是否标识过本行的行号.
		 * 
		 * 因为数据存储是这样的:
		 * 行号0, 改变音调, 改变音量, 行号2, 改变乐器, 改变音量, P效果, 行号6, 改变音量...
		 * 不是所有的行号都会放上去. 如果本行有要改变的东西, 会先放行号标识;
		 * 如果本行已经标识过, 就不需要再标识了, 所以需要这个变量记录是否本行已经标识过.
		 
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
		for (int j = 0; j < column; j++) {
			StringBuilder builder = new StringBuilder(100);
			builder.append("row ").append(j).append(": ");
			
			for (Iterator<Integer> it = lists.get(j).iterator(); it.hasNext();) {
				int v = it.next();
				builder.append(Integer.toHexString(v)).append(' ');
			}
			System.out.println(builder.toString());
		}
		// end print out
		
		// 下面需要修改 TODO
		FtmNote[] patterns = new FtmNote[column];
		for (int i = 0; i < patterns.length; i++) {
			FtmNote pattern = new FtmNote();
			pattern.seq = seq;
			
			LinkedList<Integer> list = lists.get(i);
			int[] effects = new int[list.size()];
			int j = 0;
			// 记录出现行号的个数
			int count = 0;
			
			for (Iterator<Integer> it = list.iterator(); it.hasNext();) {
				int v = it.next();
				
				effects[j] = v;
				if ((v & 0x7FFF0000) == FtmNote.EFFECT_LINE_HEAD) {
					count++;
				}
				j++;
			}
			
			j = 0;
			int[] lines = new int[count], lineIdx = new int[count];
			
			for (int idx = 0; idx < effects.length; idx++) {
				int v = effects[idx];
				if ((v & 0x7FFF0000) == FtmNote.EFFECT_LINE_HEAD) {
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
		
		// return patterns;
		return null;
	}*/
	
}
