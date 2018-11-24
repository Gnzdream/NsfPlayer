package zdream.nsfplayer.ftm.factory;

import static zdream.nsfplayer.core.NsfChannelCode.typeOfChannel;

import static zdream.nsfplayer.core.INsfChannelCode.*;
import static zdream.nsfplayer.ftm.format.FtmNote.*;
import static zdream.nsfplayer.ftm.format.FtmStatic.*;
import static zdream.nsfplayer.core.FtmChipType.*;
import static zdream.nsfplayer.ftm.format.FtmSequenceType.*;
import static zdream.utils.common.CodeSpliter.extract;
import static zdream.utils.common.CodeSpliter.split;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import zdream.nsfplayer.core.FtmChipType;
import zdream.nsfplayer.ftm.audio.FamiTrackerHandler;
import zdream.nsfplayer.ftm.audio.FtmAudio;
import zdream.nsfplayer.ftm.format.FtmDPCMSample;
import zdream.nsfplayer.ftm.format.FtmInstrument2A03;
import zdream.nsfplayer.ftm.format.FtmInstrumentFDS;
import zdream.nsfplayer.ftm.format.FtmInstrumentN163;
import zdream.nsfplayer.ftm.format.FtmInstrumentVRC6;
import zdream.nsfplayer.ftm.format.FtmInstrumentVRC7;
import zdream.nsfplayer.ftm.format.FtmNote;
import zdream.nsfplayer.ftm.format.FtmPattern;
import zdream.nsfplayer.ftm.format.FtmSequence;
import zdream.nsfplayer.ftm.format.FtmSequenceType;
import zdream.nsfplayer.ftm.format.FtmTrack;
import zdream.utils.common.TextReader;

/**
 * <p>用来将 FamiTracker 的导出的文本文件 (.txt) 利用 {@link FamiTrackerHandler}
 * 填充 {@link FtmAudio} 的数据
 * <p>一个该创建者实例只能填充一个 {@link FtmAudio} 的数据.
 * 如果要填充更多 {@link FtmAudio} 请新建更多该创建者实例.
 * </p>
 * @author Zdream
 * @since v0.1
 */
public class FamiTrackerTextCreater extends AbstractFamiTrackerCreater<TextReader> {
	
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
	 * 在 txt 的列号对应到 FtmAudio 中的轨道序号的对应关系.
	 * 在含 N163 轨道时, FTM 会忽略 namco 轨道数的参数, 直接写入 8 个轨道的数据
	 * 所以需要这个映射
	 * @since v0.2.6
	 */
	int[] channelIndexs;
	
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
	
	/*
	 * DPCM 数据读取的暂存状态
	 * 
	 * 顺序: DPCMDEF -> DPCM -> DPCM ... DPCM -> 其它
	 */
	/**
	 * 当前读取的 DPCM 的采样数据
	 */
	byte[] dpcmBytes;
	/**
	 * 当前读取的 DPCM 的位置, 相当于指向 dpcmBytes 的索引
	 */
	int dpcmOffset;
	
	public FamiTrackerTextCreater() {
		// do nothing
	}
	
	/**
	 * 按照文本内容来生成 {@link FtmAudio}
	 */
	public void doCreate(TextReader reader, FamiTrackerHandler doc) throws FamiTrackerFormatException {
		this.reader = reader;
		
		if (!reader.isFinished()) {
			while (reader.toNextValidLine() > 0) {
				handleLine(reader, doc);
			}
		}
		
		statusChange(-1, doc);
		reader.close();
	}
	
	private void handleLine(TextReader reader, FamiTrackerHandler doc) {
		String[] strs = split(reader.thisLine());
		
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
		
		case "N163CHANNELS": {
			doc.setNamcoChannels(Integer.parseInt(strs[1]));
		} break;
		
		// Macros
		case "MACRO": { // 就是 sequence
			parseMacro(reader, doc, strs);
		} break;
		
		case "MACROVRC6": {
			parseMacroVRC6(reader, doc, strs);
		} break;
		
		case "MACRON163": {
			parseMacroN163(reader, doc, strs);
		} break;
		
		// DPCM samples
		case "DPCMDEF": {
			parseDPCMDefine(reader, doc, strs);
		} break;
		
		case "DPCM": {
			parseDPCM(reader, doc, strs);
		} break;
		
		// Instruments
		case "INST2A03": {
			parseInst2A03(reader, doc, strs);
		} break;
		
		case "KEYDPCM": {
			parseInstKeyDPCM(reader, doc, strs);
		} break;

		case "INSTVRC6": {
			parseInstVRC6(reader, doc, strs);
		} break;
		
		case "FDSWAVE": {
			parseFDSWave(reader, doc, strs);
		} break;
		
		case "FDSMOD": {
			parseFDSMod(reader, doc, strs);
		} break;
		
		case "FDSMACRO": {
			parseFDSMacro(reader, doc, strs);
		} break;
		
		case "INSTN163": {
			parseInstN163(reader, doc, strs);
		} break;
		
		case "N163WAVE": {
			parseN163Wave(reader, doc, strs);
		} break;
		
		case "INSTVRC7": {
			parseInstVRC7(reader, doc, strs);
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
	
	/**
	 * <p>解析 Macro 部分, 即 sequence 部分
	 * <p>示例:
	 * <blockquote><pre>
     *     MACRO       0   4  -1   3   0 : 9 5 3 2 1 1 0
     * </pre></blockquote>
	 * 各个参数的意义是:
	 * <blockquote><pre>
     *     MACRO &lt;类型&gt; &lt;序号&gt; &lt;循环点位置&gt; &lt;释放点位置&gt; &lt;辅助参数&gt; : &lt;序列&gt; ...
     * </pre></blockquote>
     * 辅助参数, 见 {@link FtmSequence#settings}
	 * </p>
	 * 
	 * @since v0.2.5
	 */
	private void parseMacro(TextReader reader, FamiTrackerHandler doc, String[] strs) {
		if (strs.length < 7) {
			handleException(reader, EX_MACRO_WRONG_ITEMS, strs.length);
		}
		if (!":".equals(strs[6])) {
			handleException(reader, EX_MACRO_WRONG_TOKEN);
		}
		
		parseMacro0(reader, doc, strs, _2A03);
	}
	
	/**
	 * <p>解析 MacroVRC6 部分, 即 VRC6 sequence 部分
	 * <p>示例:
	 * <blockquote><pre>
     *     MACROVRC6   0   1  -1   1   0 : 11 9 5 2 0
     * </pre></blockquote>
	 * 各个参数的意义是:
	 * <blockquote><pre>
     *     MACRO &lt;类型&gt; &lt;序号&gt; &lt;循环点位置&gt; &lt;释放点位置&gt; &lt;辅助参数&gt; : &lt;序列&gt; ...
     * </pre></blockquote>
     * 辅助参数, 见 {@link FtmSequence#settings}
     * <p>可以看到, 和 Macro 基本没区别
	 * </p>
	 * 
	 * @since v0.2.5
	 */
	private void parseMacroVRC6(TextReader reader, FamiTrackerHandler doc, String[] strs) {
		if (strs.length < 7) {
			handleException(reader, EX_MACROVRC6_WRONG_ITEMS, strs.length);
		}
		if (!":".equals(strs[6])) {
			handleException(reader, EX_MACROVRC6_WRONG_TOKEN);
		}
		
		parseMacro0(reader, doc, strs, VRC6);
	}
	
	/**
	 * <p>解析 MacroN163 部分, 即 N163 sequence 部分
	 * <p>示例:
	 * <blockquote><pre>
     *     MACRON163   0   4  -1  -1   0 : 15 13 12
     * </pre></blockquote>
	 * 各个参数的意义是:
	 * <blockquote><pre>
     *     MACRON163 &lt;类型&gt; &lt;序号&gt; &lt;循环点位置&gt; &lt;释放点位置&gt; &lt;辅助参数&gt; : &lt;序列&gt; ...
     * </pre></blockquote>
     * 辅助参数, 见 {@link FtmSequence#settings}
     * <p>可以看到, 和 Macro 基本没区别
	 * </p>
	 * 
	 * @since v0.2.6
	 */
	private void parseMacroN163(TextReader reader, FamiTrackerHandler doc, String[] strs) {
		if (strs.length < 7) {
			handleException(reader, EX_MACRON163_WRONG_ITEMS, strs.length);
		}
		if (!":".equals(strs[6])) {
			handleException(reader, EX_MACRON163_WRONG_TOKEN);
		}
		
		parseMacro0(reader, doc, strs, N163);
	}
	
	private void parseMacro0(TextReader reader, FamiTrackerHandler doc, String[] strs, FtmChipType chip) {
		int type = Integer.parseInt(strs[1]);
		int index = Integer.parseInt(strs[2]);
		int loop = Integer.parseInt(strs[3]);
		int release = Integer.parseInt(strs[4]);
		int settings = Integer.parseInt(strs[5]);
		
		FtmSequence seq = doc.getOrCreateSequence(chip, FtmSequenceType.get(type), index);
		seq.loopPoint = loop;
		seq.releasePoint = release;
		seq.settings = (byte) settings;
		
		// 序列存储
		final int length = strs.length - 7;
		byte[] data = new byte[length];
		for (int i = 0; i < length; i++) {
			data[i] = Byte.parseByte(strs[i + 7]);
		}
		seq.data = data;
	}
	
	/**
	 * <p>解析 DPCMDEF 部分, 即 DPCM define 部分
	 * <p>示例:
	 * <blockquote><pre>
     *     DPCMDEF   0   897 "fsharp"
     * </pre></blockquote>
	 * 各个参数的意义是:
	 * <blockquote><pre>
     *     DPCMDEF &lt;序号&gt; &lt;数据大小&gt; &lt;名称&gt;
     * </pre></blockquote>
	 * </p>
	 * 
	 * @since v0.2.5
	 */
	private void parseDPCMDefine(TextReader reader, FamiTrackerHandler doc, String[] strs) {
		if (strs.length != 4) {
			handleException(reader, EX_DPCMDEF_WRONG_ITEMS, strs.length);
		}
		
		int index = Integer.parseInt(strs[1]);
		int length = Integer.parseInt(strs[2]);
		
		FtmDPCMSample dsample = doc.getOrCreateDPCMSample(index);
		dsample.data = this.dpcmBytes = new byte[length];
		dsample.name = extract(strs[3]);
		dpcmOffset = 0;
	}
	
	/**
	 * <p>解析 DPCM 部分, 即 DPCM 采样数据部分
	 * <p>示例:
	 * <blockquote><pre>
     *     DPCM : D5 FF FD 00 00 FF 01 1C 01 F0 E7 0F 00 FE FF 03 00 80 57 FF F1 FD 0F 00 40 FC FF FF 01 FE 08 80
     * </pre></blockquote>
	 * 各个参数的意义是:
	 * <blockquote><pre>
     *     DPCM : &lt;采样数据&gt; ...
     * </pre></blockquote>
     * 其中采样数据的个数在 [1, 32] 范围内, 均以 16 进制文本的形式呈现
	 * </p>
	 * 
	 * @since v0.2.5
	 */
	private void parseDPCM(TextReader reader, FamiTrackerHandler doc, String[] strs) {
		if (!":".equals(strs[1])) {
			handleException(reader, EX_DPCM_WRONG_TOKEN, strs.length);
		}
		
		for (int i = 2; i < strs.length; i++) {
			this.dpcmBytes[this.dpcmOffset++] = (byte) Integer.parseInt(strs[i], 16);
		}
	}
	
	private void parseInst2A03(TextReader reader, FamiTrackerHandler doc, String[] strs) {
		if (strs.length != 8) {
			handleException(reader, EX_INST2A03_WRONG_ITEMS, strs.length);
		}
		
		FtmInstrument2A03 inst = new FtmInstrument2A03();
		inst.seq = Integer.parseInt(strs[1]);
		inst.name = extract(strs[7]);
		
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
	
	/**
	 * <p>解析 KEYDPCM 部分, 即 2A03 Instrument 的 DPCM 参数部分
	 * <p>示例:
	 * <blockquote><pre>
     *     KEYDPCM   2   3   2     3  15   0     0  -1
     * </pre></blockquote>
	 * 各个参数的意义是:
	 * <blockquote><pre>
     *     KEYDPCM &lt;乐器序号&gt; &lt;音阶&gt; &lt;音高&gt; &lt;DPCM 采样序号&gt;
     *     &lt;采样音高&gt; &lt;是否循环&gt; &lt;位置参数&gt; &lt;采样 delta 值&gt;
     * </pre></blockquote>
     * 音高指 pitchOfOctave;
     * <br>是否循环, 1 为循环, 0 为不循环
     * <br>采样音高, 见 {@link FtmInstrument2A03#samplePitches}
     * <br>采样 delta 值, 见 {@link FtmInstrument2A03#sampleDeltas}
	 * </p>
	 * 
	 * @since v0.2.5
	 */
	private void parseInstKeyDPCM(TextReader reader, FamiTrackerHandler doc, String[] strs) {
		if (strs.length != 9) {
			handleException(reader, EX_KEYDPCM_WRONG_ITEMS, strs.length);
		}
		
		int index = Integer.parseInt(strs[1]);
		int octave = Integer.parseInt(strs[2]);
		int pitchOfOctave = Integer.parseInt(strs[3]);
		int sIndex = Integer.parseInt(strs[4]);
		byte samplePitch = Byte.parseByte(strs[5]);
		boolean loop = Integer.parseInt(strs[6]) != 0;
		// strs[7] 忽略
		byte sampleDelta = Byte.parseByte(strs[8]);
		
		FtmInstrument2A03 inst = (FtmInstrument2A03) doc.audio.getInstrument(index);
		FtmDPCMSample sample = doc.getOrCreateDPCMSample(sIndex);
		
		inst.samples[octave][pitchOfOctave] = sample;
		inst.samplePitches[octave][pitchOfOctave] = (byte) (samplePitch | (loop ? 0x80 : 0));
		inst.sampleDeltas[octave][pitchOfOctave] = sampleDelta;
	}
	
	/**
	 * <p>解析 InstVRC6 部分, 即 VRC6 乐器部分
	 * <p>示例:
	 * <blockquote><pre>
     *     INSTVRC6   7     1  -1  -1  -1   0 "VRC6 lead 1"
     * </pre></blockquote>
	 * 各个参数的意义是:
	 * <blockquote><pre>
     *     INSTVRC6 &lt;序号&gt; &lt;音量序列号&gt; &lt;琶音序列号&gt; &lt;音高序列号&gt;
     *     &lt;大音高序列号&gt; &lt;音色序列号&gt; &lt;乐器名称&gt;
     * </pre></blockquote>
	 * </p>
	 * 
	 * @since v0.2.5
	 */
	private void parseInstVRC6(TextReader reader, FamiTrackerHandler doc, String[] strs) {
		if (strs.length != 8) {
			handleException(reader, EX_INSTVRC6_WRONG_ITEMS, strs.length);
		}
		
		FtmInstrumentVRC6 inst = new FtmInstrumentVRC6();
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
	
	/**
	 * <p>解析 FDSWave 部分, 即 FDS 音量包络数据部分
	 * <p>示例:
	 * <blockquote><pre>
     *     FDSWAVE    2 : 35 39 42 41 39 45 48 45 42 45 46 ...
     * </pre></blockquote>
     * 注, 冒号后面的数字个数固定为 64 个.
	 * <br>各个参数的意义是:
	 * <blockquote><pre>
     *     FDSWAVE &lt;乐器序号&gt; : &lt;64 个包络值&gt;
     * </pre></blockquote>
	 * </p>
	 * 
	 * @since v0.2.5
	 */
	private void parseFDSWave(TextReader reader, FamiTrackerHandler doc, String[] strs) {
		if (strs.length != 67) {
			handleException(reader, EX_FDSWAVE_WRONG_ITEMS, strs.length);
		}
		if (!":".equals(strs[2])) {
			handleException(reader, EX_FDSWAVE_WRONG_TOKEN);
		}
		
		int index = Integer.parseInt(strs[1]);
		FtmInstrumentFDS instfds = doc.getOrCreateInstrumentFDS(index);
		byte[] bs = instfds.samples;
		
		for (int i = 0; i < bs.length; i++) {
			bs[i] = Byte.parseByte(strs[i + 3]);
		}
	}
	
	/**
	 * <p>解析 FDSMod 部分, 即 FDS 调制数据部分
	 * <p>示例:
	 * <blockquote><pre>
     *     FDSMOD    2 : 35 39 42 41 39 45 48 45 42 45 46 ...
     * </pre></blockquote>
     * 注, 冒号后面的数字个数固定为 32 个.
	 * <br>各个参数的意义是:
	 * <blockquote><pre>
     *     FDSMOD &lt;乐器序号&gt; : &lt;32 个调制值&gt;
     * </pre></blockquote>
	 * </p>
	 * 
	 * @since v0.2.5
	 */
	private void parseFDSMod(TextReader reader, FamiTrackerHandler doc, String[] strs) {
		if (strs.length != 35) {
			handleException(reader, EX_FDSMOD_WRONG_ITEMS, strs.length);
		}
		if (!":".equals(strs[2])) {
			handleException(reader, EX_FDSMOD_WRONG_TOKEN);
		}
		
		int index = Integer.parseInt(strs[1]);
		FtmInstrumentFDS instfds = doc.getOrCreateInstrumentFDS(index);
		byte[] bs = instfds.modulation;
		
		for (int i = 0; i < bs.length; i++) {
			bs[i] = Byte.parseByte(strs[i + 3]);
		}
	}
	
	/**
	 * <p>解析 FDSMacro 部分, 即 FDS 序列部分
	 * <p>示例:
	 * <blockquote><pre>
     *     FDSMACRO   2   0  -1  -1   0 : 24 18 16 14 13 12
     * </pre></blockquote>
	 * 各个参数的意义是:
	 * <blockquote><pre>
     *     FDSMACRO &lt;乐器序号&gt; &lt;类型&gt; &lt;循环点位置&gt; &lt;释放点位置&gt; &lt;辅助参数&gt; : &lt;序列&gt; ...
     * </pre></blockquote>
	 * </p>
	 * 
	 * @since v0.2.5
	 */
	private void parseFDSMacro(TextReader reader, FamiTrackerHandler doc, String[] strs) {
		if (!":".equals(strs[6])) {
			handleException(reader, EX_FDSMACRO_WRONG_TOKEN);
		}
		
		int index = Integer.parseInt(strs[1]);
		FtmInstrumentFDS instfds = doc.getOrCreateInstrumentFDS(index);
		
		int type = Integer.parseInt(strs[2]);
		FtmSequence seq = new FtmSequence(FtmSequenceType.get(type));
		seq.loopPoint = Integer.parseInt(strs[3]);
		seq.releasePoint = Integer.parseInt(strs[4]);
		seq.settings = Byte.parseByte(strs[5]);
		
		// 序列存储
		final int length = strs.length - 7;
		byte[] data = new byte[length];
		for (int i = 0; i < length; i++) {
			data[i] = Byte.parseByte(strs[i + 7]);
		}
		seq.data = data;
		
		switch (type) {
		case 0:
			instfds.seqVolume = seq;
			break;
		case 1:
			instfds.seqArpeggio = seq;
			break;
		case 2:
			instfds.seqPitch = seq;
			break;
		}
	}
	
	/**
	 * <p>解析 INSTN163 部分, 即 N163 乐器部分
	 * <p>示例:
	 * <blockquote><pre>
     *     INSTN163   6     0  -1  -1  -1  -1  32  96   1 "Choir"
     * </pre></blockquote>
	 * 各个参数的意义是:
	 * <blockquote><pre>
     *     INSTN163 &lt;序号&gt;
     *     &lt;音量序列号&gt; &lt;琶音序列号&gt; &lt;音高序列号&gt; &lt;大音高序列号&gt; &lt;音色序列号&gt;
     *     &lt;波形长度&gt; &lt;波形位置&gt; &lt;波形个数&gt; &lt;乐器名称&gt;
     * </pre></blockquote>
	 * </p>
	 * 
	 * @since v0.2.6
	 */
	private void parseInstN163(TextReader reader2, FamiTrackerHandler doc, String[] strs) {
		if (strs.length != 11) {
			handleException(reader, EX_INSTN163_WRONG_ITEMS, strs.length);
		}
		
		int seq = Integer.parseInt(strs[1]);
		FtmInstrumentN163 inst = doc.getOrCreateInstrumentN163(seq);
		inst.name = strs[10];
		
		inst.vol = Integer.parseInt(strs[2]);
		if (inst.vol != -1) {
			doc.getOrCreateSequence(N163, VOLUME, inst.vol);
		}
		
		inst.arp = Integer.parseInt(strs[3]);
		if (inst.arp != -1) {
			doc.getOrCreateSequence(N163, ARPEGGIO, inst.arp);
		}
		
		inst.pit = Integer.parseInt(strs[4]);
		if (inst.pit != -1) {
			doc.getOrCreateSequence(N163, PITCH, inst.pit);
		}
		
		inst.hip = Integer.parseInt(strs[5]);
		if (inst.hip != -1) {
			doc.getOrCreateSequence(N163, HI_PITCH, inst.hip);
		}
		
		inst.dut = Integer.parseInt(strs[6]);
		if (inst.dut != -1) {
			doc.getOrCreateSequence(N163, DUTY, inst.dut);
		}
		
		int waveSize = Integer.parseInt(strs[7]);
		int waveCount = Integer.parseInt(strs[9]);
		inst.wavePos = Integer.parseInt(strs[8]);
		
		inst.waves = new byte[waveCount][waveSize];
		doc.registerInstrument(inst);
	}
	
	/**
	 * <p>解析 N163WAVE 部分, 即 N163 波形 (包络) 数据部分
	 * <p>示例:
	 * <blockquote><pre>
     *     N163WAVE   6   0 : 4 2 2 2 1 1 0 0 1 5 8 11 13 14 15 15 15 14 13 10 7 3 1 1 0 0 0 0 1 2 4 5
     * </pre></blockquote>
	 * 各个参数的意义是:
	 * <blockquote><pre>
     *     N163WAVE &lt;乐器序号&gt; &lt;波形序号&gt; : &lt;波形数据&gt; ...
     * </pre></blockquote>
     * 波形数据个数不定, 一般有 32 个
	 * </p>
	 * 
	 * @since v0.2.6
	 */
	private void parseN163Wave(TextReader reader2, FamiTrackerHandler doc, String[] strs) {
		if (strs.length < 5) {
			handleException(reader, EX_N163WAVE_WRONG_ITEMS, strs.length);
		}
		if (!":".equals(strs[3])) {
			handleException(reader, EX_FDSMACRO_WRONG_TOKEN);
		}
		
		int instSeq = Integer.parseInt(strs[1]);
		int waveSeq = Integer.parseInt(strs[2]);
		
		FtmInstrumentN163 inst = doc.getOrCreateInstrumentN163(instSeq);
		
		// 该函数运行前, 一定运行过 parseInstN163, 所以 inst.waves 一定不为 null
		byte[] waves = inst.waves[waveSeq];
		for (int i = 0; i < waves.length; i++) {
			waves[i] = Byte.parseByte(strs[i + 4]);
		}
	}

	/**
	 * <p>解析 INSTVRC7 部分, 即 VRC7 乐器数据部分
	 * <p>示例:
	 * <blockquote><pre>
     *     INSTVRC7   5     0 01 00 70 1A 12 20 8F F0 "intro"
     * </pre></blockquote>
	 * 各个参数的意义是:
	 * <blockquote><pre>
     *     INSTVRC7 &lt;乐器序号&gt; &lt;patch 号&gt; &lt;8 个乐器参数&gt; &lt;乐器名称&gt; 
     * </pre></blockquote>
     * 乐器参数共有 8 个, 以 16 进制格式写明
	 * </p>
	 * 
	 * @since v0.2.7
	 */
	private void parseInstVRC7(TextReader reader2, FamiTrackerHandler doc, String[] strs) {
		if (strs.length != 12) {
			handleException(reader, EX_INSTVRC7_WRONG_ITEMS, strs.length);
		}
		
		FtmInstrumentVRC7 inst = new FtmInstrumentVRC7();
		inst.seq = Integer.parseInt(strs[1]);
		inst.name = strs[11];
		inst.patchNum = Integer.parseInt(strs[2]);
		
		for (int i = 0; i < 8; i++) {
			inst.regs[i] = Short.parseShort(strs[3 + i], 16);
		}

		doc.registerInstrument(inst);
	}

	private void parseTrack(TextReader reader, FamiTrackerHandler doc, String[] strs) {
		if (strs.length != 5) {
			handleException(reader, EX_TRACK_WRONG_ITEMS, strs.length);
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
			handleException(reader, EX_COLUMNS_WRONG_TOKEN);
		}
		
		columns = new int[strs.length - 2];
		int length = strs.length - 2;
		for (int i = 0; i < length; i++) {
			columns[i] = Integer.parseInt(strs[i + 2]);
		}
		
		// N163
		// 如果该 txt 含 N163 芯片时, 会把所有 8 个轨道写上去. 因此这里需要做处理
		channelIndexs = new int[columns.length];
		if (doc.audio.isUseN163()) {
			int namcoCount = doc.audio.getNamcoChannels();
			int namco1Index = -1;
			int i = 5;
			for (; i < columns.length; i++) {
				if (doc.channelCode(i) == CHANNEL_N163_1) {
					namco1Index = i;
					break;
				}
			}
			
			int end = namco1Index + namcoCount;
			for (i = 0; i < end; i++) {
				channelIndexs[i] = i;
			}
			int nextVal = i; // i = end
			
			end = namco1Index + 8;
			for (; i < end; i++) {
				channelIndexs[i] = -1;
			}
			for (; i < channelIndexs.length; i++, nextVal++) {
				channelIndexs[i] = nextVal;
			}
		} else {
			for (int i = 0; i < channelIndexs.length; i++) {
				channelIndexs[i] = i;
			}
		}
	}
	
	private void parseOrder(TextReader reader, FamiTrackerHandler doc, String[] strs) {
		if (strs.length != 3 + columns.length) {
			handleException(reader, EX_OREDR_WRONG_ITEMS, 3 + columns.length, strs.length);
		}
		if (!":".equals(strs[2])) {
			handleException(reader, EX_OREDR_WRONG_TOKEN);
		}
		
		int index = Integer.parseInt(strs[1], 16);
		if (index != orders.size()) {
			handleException(reader, EX_OREDR_WRONG_OREDR_NO, index, orders.size());
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
			handleException(reader, EX_PATTERN_WRONG_TOKEN);
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
			handleException(reader, EX_ROW_WRONG_ITEMS, lenExp, strs.length);
		}
		
		// 行数
		int row = Integer.parseInt(strs[1], 16);
		if (row != rowIdx) {
			handleException(reader, EX_ROW_WRONG_ROW_NO, row, rowIdx);
		}
		
		// 解析部分
		int offset = 2;
		for (int column = 0; column < columns.length; column++) {
			int length = 4 + columns[column];
			
			// realColume 是在 FtmAudio 中的列数. 存在 N163 轨道时, realColume 可能不等于 column
			int realColume = channelIndexs[column];
			if (realColume != -1) {
				parseColumnInRow(reader, doc, strs, realColume, offset, length);
			}
			
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
	private void parseColumnInRow(
			TextReader reader,
			FamiTrackerHandler doc,
			String[] strs,
			final int column,
			final int offset,
			final int length) {
		// 第一个元素必须是 ":"
		if (!":".equals(strs[offset])) {
			handleException(reader, EX_ROW_WRONG_TOKEN_IN_COLUMN, column);
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
			if (doc.channelCode(column) == CHANNEL_2A03_NOISE) {
				parseNoiseNote(t, note);
			} else {
				parseAudioNote(t, note);
			}
			empty = false;
		}
		
		// 乐器部分
		t = strs[offset + 2];
		if ("..".equals(t)) {
			note.instrument = MAX_INSTRUMENTS;
		} else {
			note.instrument = Integer.parseInt(t, 16);
			empty = false;
		}
		
		// 音量部分
		t = strs[offset + 3];
		if (".".equals(t)) {
			note.vol = MAX_VOLUMN;
		} else {
			note.vol = Byte.parseByte(t, 16);
			empty = false;
		}
		
		// 效果部分
		byte channelCode = doc.channelCode(column);
		for (int idx = 4; idx < length; idx++) {
			t = strs[offset + idx];
			empty &= parseEffect(t, note, idx - 4, channelCode);
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
			handleException(reader, EX_ROW_WRONG_NOTE, tt);
		}
		
		// 音阶
		byte octave = Byte.parseByte(text.substring(2));
		if (octave < 0 || octave > 9) {
			handleException(reader, EX_ROW_WRONG_OCTAVE, octave);
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
			handleException(reader, EX_ROW_WRONG_NOISE, text);
		}
	}
	
	/**
	 * 效果部分解析
	 * @param channelCode
	 *   现在处理的轨道号 (不是轨道序号)
	 */
	private boolean parseEffect(String text, FtmNote note, int index, byte channelCode) {
		if ("...".equals(text)) {
			return true;
		}
		
		char head = text.charAt(0);
		byte eff = this.conventEffectToCode(head, channelCode);
		
		if (eff == -1) {
			return true; // 找不到效果是什么, 可以选择报错, 但是这里不这样做
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
	
	/* **********
	 *   工具   *
	 ********** */
	
	/*
	 * 下面是所有效果字符和对应的解释.
	'.',	// None
	'F',	// Speed
	'B',	// Jump 
	'D',	// Skip 
	'C',	// Halt
	'E',	// Volume
	'3',	// Porta on
	 0,		// Porta off // 已弃用
	'H',	// Sweep up
	'I',	// Sweep down
	'0',	// Arpeggio 琶音
	'4',	// Vibrato
	'7',	// Tremolo
	'P',	// Pitch
	'G',	// Note delay
	'Z',	// DAC setting
	'1',	// Portamento up
	'2',	// Portamento down
	'V',	// Duty cycle
	'Y',	// Sample offset
	'Q',	// Slide up
	'R',	// Slide down
	'A',	// Volume slide
	'S',	// Note cut
	'X',	// DPCM retrigger						 
	 0,		// 已弃用
	'H',	// FDS modulation depth
	'I',	// FDS modulation speed hi
	'J',	// FDS modulation speed lo
	'W',	// DPCM Pitch
	'H',	// Sunsoft envelope low
	'I',	// Sunsoft envelope high
	'J',	// Sunsoft envelope type
	'9',	// Targeted volume slide
	'H',	// VRC7 modulator
	'I',	// VRC7 carrier
	'J',	// VRC7 modulator/feedback level
	 */
	
	/**
	 * 将所给的效果的字符, 转换成效果码
	 * @param ch
	 *   在 txt 文档中写明的、所给的效果的字符
	 * @param channelCode
	 *   所在的轨道号
	 * @return
	 *   效果码. 这个码在 {@link FtmNote} 中定义.
	 *   如果找不到对应的效果码, 返回 -1.
	 * @since v0.2.5
	 */
	private byte conventEffectToCode(char ch, byte channelCode) {
		byte channelType = typeOfChannel(channelCode);
		
		switch (ch) {
		// 全局效果
		case 'F': return EF_SPEED;
		case 'B': return EF_JUMP;
		case 'D': return EF_SKIP;
		case 'C': return EF_HALT;
		
		// 通用轨道效果
		case 'E': return EF_VOLUME; // 已弃用
		case '3': return EF_PORTAMENTO;
		// TODO Sweep up 和 Sweep down 这里暂时未实现
		case '0': return EF_ARPEGGIO;
		case '4': return EF_VIBRATO;
		case '7': return EF_TREMOLO;
		case 'P': return EF_PITCH;
		case 'G': return EF_DELAY;
		case '1': return EF_PORTA_UP;
		case '2': return EF_PORTA_DOWN;
		case 'V': return EF_DUTY_CYCLE;
		case 'Q': return EF_SLIDE_UP;
		case 'R': return EF_SLIDE_DOWN;
		case 'A': return EF_VOLUME_SLIDE;
		case 'S': return EF_NOTE_CUT;
//		case '9': return ?; // Targeted volume slide, 该效果 FTM 工程未实现
		
		// OCC 新效果含 T, O, L, M, 尚不清楚作用
		
		// DPCM 轨道专用效果
		case 'Z': if (channelCode == CHANNEL_2A03_DPCM) return EF_DAC; break;
		case 'Y': if (channelCode == CHANNEL_2A03_DPCM) return EF_SAMPLE_OFFSET; break;
		case 'X': if (channelCode == CHANNEL_2A03_DPCM) return EF_RETRIGGER; break;
		case 'W': if (channelCode == CHANNEL_2A03_DPCM) return EF_DPCM_PITCH; break;
		
		// 特殊轨道专用效果
		case 'H': {
			// 2A03 sweep
			// FDS modulation depth
			// Sunsoft envelope low
			// TODO VRC7 modulator, 未实现 'H' 效果
			switch (channelType) {
			case CHANNEL_TYPE_PULSE:
				return EF_SWEEPUP;
			case CHANNEL_TYPE_FDS:
				return EF_FDS_MOD_DEPTH;
			case CHANNEL_TYPE_S5B:
				return EF_SUNSOFT_ENV_LO;
//			case CHANNEL_TYPE_VRC7:
//				return ?;
			}
		} break;
		case 'I': {
			// 2A03 sweep
			// FDS modulation speed hi
			// Sunsoft envelope high
			// TODO VRC7 carrier, 未实现 'I' 效果
			switch (channelType) {
			case CHANNEL_TYPE_PULSE:
				return EF_SWEEPDOWN;
			case CHANNEL_TYPE_FDS:
				return EF_FDS_MOD_SPEED_HI;
			case CHANNEL_TYPE_S5B:
				return EF_SUNSOFT_ENV_HI;
//			case CHANNEL_TYPE_VRC7:
//				return ?;
			}
		} break;
		case 'J': {
			// FDS modulation speed lo
			// Sunsoft envelope type
			// TODO VRC7 modulator/feedback level, 未实现 'I' 效果
			switch (channelType) {
			case CHANNEL_TYPE_FDS:
				return EF_FDS_MOD_SPEED_LO;
			case CHANNEL_TYPE_S5B:
				return EF_SUNSOFT_ENV_TYPE;
//			case CHANNEL_TYPE_VRC7:
//				return ?;
			}
		} break;

		}
		return -1;
	}
	
	/* **********
	 * 错误处理 *
	 ********** */
	
	/*
	 * 产生的消息错误列表
	 */
	static final String EX_INST2A03_WRONG_ITEMS = "乐器部分解析错误, 2A03 乐器格式规定项数为 8, 但是这里只有 %d";
	static final String EX_KEYDPCM_WRONG_ITEMS = "乐器部分解析错误, KEYDPCM 乐器格式规定项数为 9, 但是这里只有 %d";
	static final String EX_INSTVRC6_WRONG_ITEMS = "乐器部分解析错误, VRC6 乐器格式规定项数为 8, 但是这里只有 %d";
	static final String EX_FDSWAVE_WRONG_ITEMS = "乐器部分解析错误, FDSWAVE 格式规定项数为 67, 但是这里只有 %d";
	static final String EX_FDSWAVE_WRONG_TOKEN = "FDSWAVE 部分解析错误";
	static final String EX_FDSMOD_WRONG_ITEMS = "乐器部分解析错误, FDSMOD 格式规定项数为 67, 但是这里只有 %d";
	static final String EX_FDSMOD_WRONG_TOKEN = "FDSMOD 部分解析错误";
	static final String EX_FDSMACRO_WRONG_TOKEN = "FDSMACRO 部分解析错误";
	static final String EX_INSTN163_WRONG_ITEMS = "乐器部分解析错误, N163 乐器格式规定项数为 11, 但是这里只有 %d";
	static final String EX_N163WAVE_WRONG_ITEMS = "乐器部分解析错误, N163 波形数据格式规定项数至少为 5, 但是这里只有 %d";
	static final String EX_N163WAVE_WRONG_TOKEN = " N163WAVE 波形数据解析错误";
	static final String EX_INSTVRC7_WRONG_ITEMS = "乐器部分解析错误, VRC7 乐器格式规定项数为 12, 但是这里只有 %d";
	static final String EX_DPCMDEF_WRONG_ITEMS = "曲目部分解析错误, DPCMDEF 格式规定项数为 4, 但是这里只有 %d";
	static final String EX_DPCM_WRONG_TOKEN = "MACRO 部分解析错误";
	static final String EX_MACRO_WRONG_ITEMS = "曲目部分解析错误, MACRO 格式规定项数至少为 8, 但是这里只有 %d";
	static final String EX_MACRO_WRONG_TOKEN = "MACRO 部分解析错误";
	static final String EX_MACROVRC6_WRONG_ITEMS = "曲目部分解析错误, MACROVRC6 格式规定项数至少为 8, 但是这里只有 %d";
	static final String EX_MACROVRC6_WRONG_TOKEN = "MACROVRC6 部分解析错误";
	static final String EX_MACRON163_WRONG_ITEMS = "曲目部分解析错误, MACRON163 格式规定项数至少为 8, 但是这里只有 %d";
	static final String EX_MACRON163_WRONG_TOKEN = "MACRON163 部分解析错误";
	static final String EX_TRACK_WRONG_ITEMS = "曲目部分解析错误, TRACK 格式规定项数为 5, 但是这里只有 %d";
	static final String EX_COLUMNS_WRONG_TOKEN = "COLUMNS 部分解析错误";
	static final String EX_OREDR_WRONG_ITEMS = "曲目部分解析错误, ORDER 格式规定项数为 %d, 但是这里只有 %d";
	static final String EX_OREDR_WRONG_TOKEN = "OREDR 部分解析错误";
	static final String EX_OREDR_WRONG_OREDR_NO = "OREDR 序号不匹配, 值为 %d, 原期望为 %d";
	static final String EX_PATTERN_WRONG_TOKEN = "PATTERN 部分解析错误";
	static final String EX_ROW_WRONG_ITEMS = "曲目部分解析错误, ROW 格式规定项数为 %d, 但是这里只有 %d";
	static final String EX_ROW_WRONG_ROW_NO = "ROW 序号不匹配, 值为 %d, 原期望为 %d";
	static final String EX_ROW_WRONG_TOKEN_IN_COLUMN = "轨道序号为 %d 的 ROW 部分解析错误";
	static final String EX_ROW_WRONG_NOTE = "曲目部分解析错误, ROW 格式中音符 '%s' 无法解析";
	static final String EX_ROW_WRONG_OCTAVE = "曲目部分解析错误, ROW 格式中音阶 %d 错误";
	static final String EX_ROW_WRONG_NOISE = "曲目部分解析错误, ROW 格式中噪音轨道中的 '%s' 无法解析";
	
	@Override
	protected void handleException(TextReader reader, String msg) throws FamiTrackerFormatException {
		String msg0 = String.format("行号 %d 发现错误: %s", reader.line(), msg);
		
		throw new FamiTrackerFormatException(msg0);
	}
	
	protected void handleException(TextReader reader, String msg, Object... args) throws FamiTrackerFormatException {
		handleException(reader, String.format(msg, args));
	}
	
}
