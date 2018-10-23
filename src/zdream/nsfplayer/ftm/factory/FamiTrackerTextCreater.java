package zdream.nsfplayer.ftm.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.ftm.document.FamiTrackerHandler;
import zdream.nsfplayer.ftm.document.FtmAudio;
import zdream.nsfplayer.ftm.format.FtmInstrument2A03;
import zdream.nsfplayer.ftm.format.FtmNote;
import zdream.nsfplayer.ftm.format.FtmPattern;
import zdream.nsfplayer.ftm.format.FtmSequenceType;
import zdream.nsfplayer.ftm.format.FtmTrack;
import zdream.nsfplayer.ftm.renderer.FamiTrackerConfig;
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
	
	/**
	 * 其它为 0
	 * 在 parse ORDER 的时候为 1
	 * 在 parse PATTERN(ROW) 的时候为 2
	 * 在 parse TRACK 的时候为 3
	 * 结束时 -1
	 */
	int status = 0;
	
	/**
	 * pattern 组的序号 - 一组 pattern
	 */
	HashMap<Integer, FtmPattern[]> patterns;
	
	/**
	 * 最大的 pattern 组序号
	 */
	int maxPatternIdx;
	
	/**
	 * 当前的 pattern 组
	 */
	FtmPattern[] curPatternGroup;
	
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
			while (reader.toNextValidLine() > 0) {
				handleLine(reader, doc);
			}
		}
		
		statusChange(-1, doc);
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
		
		statusChange(3, doc);
		
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
		
		statusChange(1, doc);
		
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
		
		statusChange(2, doc);
		
		this.patternIdx = Integer.parseInt(strs[1], 16);
		if (patternIdx > maxPatternIdx) {
			maxPatternIdx = patternIdx;
		}
		// 打包本次的 PATTERN 组
		this.patterns.put(this.patternIdx, curPatternGroup);
		
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
		
		// 解析部分
		int offset = 2;
		for (int column = 0; column < columns.length; column++) {
			int length = 4 + columns[column];
			parseColumn(reader, doc, strs, column, offset, length);
			offset += length;
		}
		
		rowIdx++;
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
					offset + 1, reader.line()));
		}
		
		boolean empty = true;
		FtmNote note = new FtmNote();
		
		// 音调部分
		String t = strs[offset + 1];
		
		if ("...".equals(t)) {
			note.note = FtmNote.NOTE_NONE;
			note.octave = 0;
		} else if ("---".equals(t)) {
			note.note = FtmNote.NOTE_HALT;
			note.octave = 0;
			empty = false;
		} else if ("===".equals(t)) {
			note.note = FtmNote.NOTE_RELEASE;
			note.octave = 0;
			empty = false;
		} else {
			if (doc.channelCode(column) == INsfChannelCode.CHANNEL_2A03_NOISE) {
				parseNoiseNote(t, note);
			} else {
				parseAudioNote(t, note);
			}
			empty = false;
		}
		
		// 乐器部分
		t = strs[offset + 2];
		if ("..".equals(t)) {
			note.instrument = FamiTrackerConfig.MAX_INSTRUMENTS;
		} else {
			note.instrument = Integer.parseInt(t, 16);
			empty = false;
		}
		
		// 音量部分
		t = strs[offset + 3];
		if (".".equals(t)) {
			note.vol = FamiTrackerConfig.MAX_VOLUMN;
		} else {
			note.vol = Byte.parseByte(t, 16);
			empty = false;
		}
		
		// 效果部分
		for (int idx = 4; idx < length; idx++) {
			t = strs[offset + idx];
			empty &= parseEffect(t, note, idx - 4);
		}
		
		if (!empty)
			this.curPatternGroup[column].notes[rowIdx] = note;
	}
	
	/**
	 * 非噪声部的音调与音阶解析
	 */
	private void parseAudioNote(String text, FtmNote note) {
		String tt = text.substring(0, 2);
		switch (tt) {
		case "C-": note.note = FtmNote.NOTE_C; break;
		case "C#": note.note = FtmNote.NOTE_CS; break;
		case "D-": note.note = FtmNote.NOTE_D; break;
		case "D#": note.note = FtmNote.NOTE_DS; break;
		case "E-": note.note = FtmNote.NOTE_E; break;
		case "F-": note.note = FtmNote.NOTE_F; break;
		case "F#": note.note = FtmNote.NOTE_FS; break;
		case "G-": note.note = FtmNote.NOTE_G; break;
		case "G#": note.note = FtmNote.NOTE_GS; break;
		case "A-": note.note = FtmNote.NOTE_A; break;
		case "A#": note.note = FtmNote.NOTE_AS; break;
		case "B-": note.note = FtmNote.NOTE_B; break;

		default:
			throw new FtmParseException(reader.line(),
				String.format("曲目部分解析错误, ROW 格式中第 '%s' 无法解析",
					tt, reader.line()));
		}
		
		// 音阶
		byte octave = Byte.parseByte(text.substring(2));
		if (octave < 0 || octave > 9) {
			throw new FtmParseException(reader.line(),
				String.format("曲目部分解析错误, ROW 格式中音阶 '%d' 无法解析",
					octave, reader.line()));
		}
		note.octave = octave;
	}
	
	/**
	 * 噪声部的音调解析
	 */
	private void parseNoiseNote(String text, FtmNote note) {
		switch (text) {
		case "0-#": note.note = FtmNote.NOTE_C; note.octave = 0; break;
		case "1-#": note.note = FtmNote.NOTE_CS; note.octave = 0; break;
		case "2-#": note.note = FtmNote.NOTE_D; note.octave = 0; break;
		case "3-#": note.note = FtmNote.NOTE_DS; note.octave = 0; break;
		case "4-#": note.note = FtmNote.NOTE_E; note.octave = 0; break;
		case "5-#": note.note = FtmNote.NOTE_F; note.octave = 0; break;
		case "6-#": note.note = FtmNote.NOTE_FS; note.octave = 0; break;
		case "7-#": note.note = FtmNote.NOTE_G; note.octave = 0; break;
		case "8-#": note.note = FtmNote.NOTE_GS; note.octave = 0; break;
		case "9-#": note.note = FtmNote.NOTE_A; note.octave = 0; break;
		case "A-#": note.note = FtmNote.NOTE_AS; note.octave = 0; break;
		case "B-#": note.note = FtmNote.NOTE_B; note.octave = 0; break;
		case "C-#": note.note = FtmNote.NOTE_C; note.octave = 1; break;
		case "D-#": note.note = FtmNote.NOTE_CS; note.octave = 1; break;
		case "E-#": note.note = FtmNote.NOTE_D; note.octave = 1; break;
		case "F-#": note.note = FtmNote.NOTE_DS; note.octave = 1; break;

		default:
			throw new FtmParseException(reader.line(),
				String.format("曲目部分解析错误, ROW 格式中第 '%s' 无法解析",
						text, reader.line()));
		}
	}
	
	/**
	 * 效果部分解析
	 */
	private boolean parseEffect(String text, FtmNote note, int index) {
		if ("...".equals(text)) {
			return true;
		}
		
		char head = text.charAt(0);
		byte eff = -1;
		
		char[] eff_chars = FtmNote.EFF_CHAR;
		for (byte i = 0; i < eff_chars.length; i++) {
			if (head == eff_chars[i]) {
				eff = i;
				break;
			}
		}
		
		if (eff == -1) {
			return true; // 或者报错
		}
		
		note.effNumber[index] = eff;
		note.effParam[index] = Short.parseShort(text.substring(1), 16);
		return false;
	}
	
	/**
	 * 状态转换
	 */
	private void statusChange(int status, FamiTrackerHandler doc) {
		switch (status) {
		case 1:
			
			break;
		case 2:
			if (this.status == 2) {
				
			} else if (this.status == 1) {
				// 第一次转到 PATTERN 上
				this.patterns = new HashMap<>();
				maxPatternIdx = 0;
				
				// 打包 order 数据, 并送到 curTrack 中
				int len = orders.size();
				curTrack.orders = new int[len][];
				for (int i = 0; i < len; i++) {
					curTrack.orders[i] = orders.get(i);
				}
			}
			curPatternGroup = new FtmPattern[columns.length];
			for (int i = 0; i < curPatternGroup.length; i++) {
				curPatternGroup[i] = new FtmPattern();
				curPatternGroup[i].notes = new FtmNote[curTrack.length];
			}
			break;
			
		case 3:
			if (this.status == 0) {
				// 那现在是 track 0 (第一首)
				
			} else {
				// 现在不是第一首
				
				// 需要打包上一个 track 的所有 pattern
				packPattern();
			}
			break;
			
		case -1:
			// 需要打包上一个 track 的所有 pattern
			packPattern();
			
			break;

		default:
			break;
		}
		
		this.status = status;
	}
	
	/**
	 * 打包上一个 track 的所有 pattern
	 */
	private void packPattern() {
		curTrack.patterns = new FtmPattern[maxPatternIdx + 1][];
		
		for (Iterator<Entry<Integer, FtmPattern[]>> it = this.patterns.entrySet().iterator(); it.hasNext();) {
			Entry<Integer, FtmPattern[]> entry = it.next();
			curTrack.patterns[entry.getKey()] = entry.getValue();
		}
	}
	
}
