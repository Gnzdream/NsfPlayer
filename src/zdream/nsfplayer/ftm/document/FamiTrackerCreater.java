package zdream.nsfplayer.ftm.document;

import java.io.IOException;

import zdream.nsfplayer.ftm.FamiTrackerSetting;
import zdream.nsfplayer.ftm.document.format.AbstractFtmInstrument;
import zdream.nsfplayer.ftm.document.format.FtmChipType;
import zdream.nsfplayer.ftm.document.format.FtmDPCMSample;
import zdream.nsfplayer.ftm.document.format.FtmInstrument2A03;
import zdream.nsfplayer.ftm.document.format.FtmInstrumentVRC6;
import zdream.nsfplayer.ftm.document.format.FtmSequence2A03;
import zdream.nsfplayer.ftm.document.format.FtmSequenceType;
import zdream.nsfplayer.ftm.document.format.FtmSequenceVRC6;
import zdream.nsfplayer.ftm.document.format.FtmTrack;

/**
 * 用来将 FamiTracker 的文件 (.ftm) 转换成 {@link FamiTrackerHandler}
 * 允许将转成 .txt 的文件也能够解析.
 * @author Zdream
 */
public class FamiTrackerCreater {
	
	/*
	 * FTM 的每个块的 ID, 用于标识这个块的内容.
	 */
	
	/**
	 * FTM 整个文件的头标识
	 */
	public static final String FILE_HEADER_ID = "FamiTracker Module";
	public static final String FILE_BLOCK_PARAMS = "PARAMS";
	public static final String FILE_BLOCK_INFO = "INFO";
	public static final String FILE_BLOCK_INSTRUMENTS = "INSTRUMENTS";
	public static final String FILE_BLOCK_SEQUENCES = "SEQUENCES";
	public static final String FILE_BLOCK_FRAMES	 = "FRAMES";
	public static final String FILE_BLOCK_PATTERNS = "PATTERNS";
	public static final String FILE_BLOCK_DSAMPLES = "DPCM SAMPLES";
	public static final String FILE_BLOCK_HEADER	 = "HEADER";
	public static final String FILE_BLOCK_COMMENTS = "COMMENTS";
	/**
	 * FTM 整个文件的结束标识
	 */
	public static final String FILE_END_ID = "END";
	
	/**
	 * 创建 {@link FtmAudio} 文档
	 * @param filename
	 * @return
	 * @throws Exception
	 */
	public FtmAudio create(String filename) throws Exception {
		
		FtmAudio audio = new FtmAudio();
		FamiTrackerHandler doc = audio.handler;
		
		doCreate(filename, doc);
		
		return audio;
	}
	
	/**
	 * 最低可以打开的文件版本, v0.1.
	 * 低于这个版本的文件将不再兼容
	 */
	public static final int COMPATIBLE_VER = 0x0200;
	
	private void doCreate(String filename, FamiTrackerHandler doc) throws IOException {
		DocumentReader openFile = new DocumentReader(filename);
		int version;
		
		openFile.open();

		// 如果是空文件的话, 就直接报错
		if (openFile.length() == 0) {
			throw new IOException("文件: " + filename + " 是空文件");
		}
		
		if (!validateHeader(openFile)) {
			throw new FamiTrackerFormatException("文件格式不正确: 文件头不匹配");
		}
		
		version = openFile.readAsCInt();

		if (version < 0x0200) {
			// 读取低版本的文件
			throw new FamiTrackerFormatException("文件版本太低, 无法产生");
		} else if (version >= 0x0200) {
			doCreateNew(doc, openFile, version);
		}
	}
	
	private void doCreateNew(FamiTrackerHandler doc, DocumentReader openFile, int version) {
		
		doc.allocateTrack(1);
		
		/*
		 * Famitracker 产生的文件由多个块组成.
		 * 在读取的过程中就需要对逐个块进行处理.
		 */
		while (!openFile.isFinished()) LOOP: {
			Block block = nextBlock(openFile);
			if (block.id == null) {
				// 已经读取结束
				break;
			}
			
			System.out.println(block.id);
			switch (block.id) {
			
			case FILE_END_ID: // 已经读取结束
				break LOOP;
			
			case FILE_BLOCK_PARAMS:
				readBlockParameters(doc, block, version);
				break;
				
			case FILE_BLOCK_INFO: {
				readBlockInfo(doc, block);
			} break;
			
			case FILE_BLOCK_HEADER: {
				readBlockHeader(doc, block);
			} break;
			
			case FILE_BLOCK_INSTRUMENTS: {
				readBlockInstruments(doc, block);
			} break;
			
			case FILE_BLOCK_SEQUENCES: {
				//errorFlag = readBlock_Sequences(documentFile);
			} break;
			
			case FILE_BLOCK_FRAMES: {
				//errorFlag = readBlock_Frames(documentFile);
			} break;
			
			case FILE_BLOCK_PATTERNS: {
				//errorFlag = readBlock_Patterns(documentFile);
			} break;
			
			case FILE_BLOCK_DSAMPLES: {
				//errorFlag = readBlock_DSamples(documentFile);
			} break;
			
			case FILE_BLOCK_COMMENTS: {
				//errorFlag = readBlock_Comments(documentFile);
			} break;
			
			/*case FILE_BLOCK_SEQUENCES_VRC6: {
				errorFlag = readBlock_SequencesVRC6(documentFile);
			} break;
			
			// FILE_BLOCK_SEQUENCES_N106 是出于向后兼容的目的
			case FILE_BLOCK_SEQUENCES_N163: case FILE_BLOCK_SEQUENCES_N106: {
				errorFlag = readBlock_SequencesN163(documentFile);
			} break;
			
			case FILE_BLOCK_SEQUENCES_S5B: {
				errorFlag = readBlock_SequencesS5B(documentFile);
			} break;
			
			case "END": {
				fileFinished = true;
			} break;*/

			default:
				System.err.println("出现了未知的 blockID: " + block.id);
				//errorFlag = true;
				break;
			}
		}
	}

	/**
	 * <p>处理参数项.
	 * <br>根据文件里面写明的 param 的块版本号, 确定 {@code block} 里面的文件格式:
	 * 
	 * <p>当<b>块版本为 1 </b>时:
	 * <li>track[0] 的 speed
	 * <li>所用的轨道数
	 * <li>制式
	 * <li>刷新率
	 * </li>
	 * 
	 * <p>当<b>块版本为 2 </b>时:
	 * <li>扩展芯片码
	 * <li>所用的轨道数
	 * <li>制式
	 * <li>刷新率
	 * </li>
	 * 
	 * <p>当<b>块版本为 3 </b>时:
	 * <li>扩展芯片码
	 * <li>所用的轨道数
	 * <li>制式
	 * <li>刷新率
	 * <li>震动模式 (忽略)
	 * </li>
	 * 
	 * <p>当<b>块版本为 4 或 5 </b>时:
	 * <li>扩展芯片码
	 * <li>所用的轨道数
	 * <li>制式
	 * <li>刷新率
	 * <li>震动模式 (忽略)
	 * <li>小节间隔 (忽略)
	 * <li>拍间隔 (忽略)
	 * </li>
	 * 
	 * <p>当<b>块版本为 6 </b>时:
	 * <li>扩展芯片码
	 * <li>所用的轨道数
	 * <li>制式
	 * <li>刷新率
	 * <li>震动模式 (忽略)
	 * <li>小节间隔 (忽略)
	 * <li>拍间隔 (忽略)
	 * <li>Namco 轨道数 (当上面的扩展芯片码说该音乐使用了 Namco 音源时存在)
	 * </li>
	 * 
	 * <p><b>其它高于 6 的版本</b>时:
	 * <li>扩展芯片码
	 * <li>所用的轨道数
	 * <li>制式
	 * <li>刷新率
	 * <li>震动模式 (忽略)
	 * <li>小节间隔 (忽略)
	 * <li>拍间隔 (忽略)
	 * <li>Namco 轨道数 (当上面的扩展芯片码说该音乐使用了 Namco 音源时存在)
	 * <li>节奏与速度的分割值
	 * </li>
	 * 
	 * <p><b>注意</b>:
	 * <li>轨道数. 2A03 为 5, 2A03+VRC6 为 8, 等等.
	 * <br>由于后面轨道数可以用统计的方法累加计算出来, 所以【所用的轨道数】这个数据并不会被记录.
	 * <li>制式包含 NTSC 和 PAL
	 * </li>
	 * </p>
	 * 
	 * @param doc
	 * @param block
	 * @param fileVersion
	 *   整个文件的版本号
	 */
	private void readBlockParameters(FamiTrackerHandler doc, Block block, int fileVersion) {
		int version = block.version;
		if (version < 1) {
			throw new FtmParseException("PARAM 版本号错误: " + version);
		}

		FtmTrack track = doc.audio.getTrack(0);
		
		switch (version) {
		case 1:
			track.speed = block.readAsCInt();
			block.skip(4); // 轨道数, 忽略
			doc.setMechine((byte) block.readAsCInt());
			doc.setFramerate(block.readAsCInt());
			doc.setDefaultSplit();
			
			break;
			
		case 2:
			doc.setChip(block.readByte());
			block.skip(4); // 轨道数, 忽略
			doc.setMechine((byte) block.readAsCInt());
			doc.setFramerate(block.readAsCInt());
			doc.setDefaultSplit();
			
			break;
			
		case 3:
			doc.setChip(block.readByte());
			block.skip(4); // 轨道数, 忽略
			doc.setMechine((byte) block.readAsCInt());
			doc.setFramerate(block.readAsCInt());
			block.skip(4); // 震动模式, 忽略
			doc.setDefaultSplit();
			
			break;
			
		case 4: case 5:
			doc.setChip(block.readByte());
			block.skip(4); // 轨道数, 忽略
			doc.setMechine((byte) block.readAsCInt());
			doc.setFramerate(block.readAsCInt());
			block.skip(4); // 震动模式, 忽略
			block.skip(4); // 小节间隔 忽略
			block.skip(4); // 拍间隔, 忽略
			doc.setDefaultSplit();
			
			break;
			
		case 6:
			doc.setChip(block.readByte());
			block.skip(4); // 轨道数, 忽略
			doc.setMechine((byte) block.readAsCInt());
			doc.setFramerate(block.readAsCInt());
			block.skip(4); // 震动模式, 忽略
			block.skip(4); // 小节间隔 忽略
			block.skip(4); // 拍间隔, 忽略
			if (doc.audio.useN163) {
				doc.setNamcoChannels(block.readAsCInt());
			}
			doc.setDefaultSplit();
			
			break;
			
		default:
			doc.setChip(block.readByte());
			block.skip(4); // 轨道数, 忽略
			doc.setMechine((byte) block.readAsCInt());
			doc.setFramerate(block.readAsCInt());
			block.skip(4); // 震动模式, 忽略
			block.skip(4); // 小节间隔 忽略
			block.skip(4); // 拍间隔, 忽略
			if (doc.audio.useN163) {
				doc.setNamcoChannels(block.readAsCInt());
			}
			doc.setSplit(block.readAsCInt());
			
			break;
		}
		
		if (fileVersion == 0x0200) {
			int speed = track.speed;
			if (speed < 20)
				track.speed = speed + 1;
		}
		
		if (version == 1) {
			if (track.speed > 19) {
				track.tempo = track.speed;
				track.speed = 6;
			} else {
				track.tempo = (doc.audio.machine == FtmAudio.MACHINE_NTSC) ?
						FtmTrack.DEFAULT_NTSC_TEMPO : FtmTrack.DEFAULT_PAL_TEMPO;
			}
		}
	}
	
	/**
	 * 产生音乐的消息, 包含标题、作家和版权说明等
	 * @param doc
	 * @param block
	 */
	private void readBlockInfo(FamiTrackerHandler doc, Block block) {
		doc.audio.title = block.readAsString(32);
		doc.audio.author = block.readAsString(32);
		doc.audio.copyright = block.readAsString(32);
	}
	
	/**
	 * <p>处理标识头.
	 * <br>根据文件里面写明的 header 的块版本号, 确定 {@code block} 里面的文件格式:
	 * 
	 * <p>当<b>块版本为 1 </b>时:
	 * <li>每个轨道的效果列数
	 * </li>
	 * 
	 * <p>当<b>块版本为 2 </b>时:
	 * <li>(总乐曲数 - 1)
	 * <li>各个乐曲、每个轨道的效果列数
	 * </li>
	 * 
	 * <p>当<b>块版本为 3 及以上</b>时:
	 * <li>(总乐曲数 - 1)
	 * <li>各个乐曲名称
	 * <li>各个乐曲、每个轨道的效果列数
	 * </li>
	 * </p>
	 * 
	 * @param doc
	 * @param block
	 */
	private void readBlockHeader(FamiTrackerHandler doc, Block block) {
		int version = block.version;
		if (version < 1) {
			throw new FtmParseException("HEADER 版本号错误: " + version);
		}
		
		if (version == 1) {
			// 版本 1 只支持单曲
			int channelCount = doc.channelCount();
			for (int i = 0; i < channelCount; ++i) {
				block.skip(1); // channelType 忽略
				doc.setEffectColumn(0, i, block.readByte());
			}
		} else {
			int trackCount = block.readUnsignedByte() + 1;  // 0 就是只有 1 个曲子
			doc.allocateTrack(trackCount);
			
			int channelCount = doc.channelCount();
			
			// Track 名称
			if (version >= 3) {
				for (int i = 0; i < trackCount; ++i) {
					doc.audio.getTrack(i).name = block.readAsString();
				}
			}
			
			// Effect Column Count
			for (int i = 0; i < channelCount; ++i) {
				block.skip(1); // channelType 忽略
				for (int j = 0; j < trackCount; ++j) {
					doc.setEffectColumn(j, i, block.readByte());
				}
			}
			
			// Highlight 忽略
		}
	}

	/**
	 * <p>处理乐器.
	 * <p>里面的数据内容有:
	 * <li>
	 * </li>
	 * </p>
	 * 
	 * @param doc
	 * @param block
	 */
	private void readBlockInstruments(FamiTrackerHandler doc, Block block) {
		int version = block.version;
		if (version < 1) {
			throw new FtmParseException("HEADER 版本号错误: " + version);
		}
		
		// 乐器中, 序号最大的值 + 1
		int max = block.readAsCInt();
		
		for (int i = 0; i < max; ++i) {
			// 乐器序号
			int index = block.readAsCInt();

			// 创建乐器实例
			byte type = block.readByte();
			AbstractFtmInstrument inst = createInstrument(FtmChipType.ofInstrumentType(type), doc, block);
			inst.seq = index;

			// 读取乐器名称
			int size = block.readAsCInt();
			inst.name = block.readAsString(size);

			// 保存乐器到 FtmAudio 中
			doc.registerInstrument(inst);
		}
	}

	/**
	 * 创建乐器
	 * @param type
	 * @param doc
	 * @param block
	 * @return
	 */
	private AbstractFtmInstrument createInstrument(FtmChipType type, FamiTrackerHandler doc, Block block) {
		switch (type) {
		case _2A03:
			return create2A03Instrument(doc, block);
			
		case VRC6:
			return createVRC6Instrument(doc, block);

		// TODO
			
		default:
			break;
		}
		return null;
	}
	
	private FtmInstrument2A03 create2A03Instrument(FamiTrackerHandler doc, Block block) {
		int version = block.version;
		FtmInstrument2A03 inst = new FtmInstrument2A03();
		
		int seqCount = block.readAsCInt();

		for (byte type = 0; type < seqCount; ++type) {
			boolean enable = block.readByte() != 0;
			int index = block.readUnsignedByte();
			
			if (!enable) {
				continue;
			}
			
			switch (type) {
			case 0:
				inst.vol = createSeq2A03(doc, block, index, type);
				break;
			case 1:
				inst.arp = createSeq2A03(doc, block, index, type);
				break;
			case 2:
				inst.pit = createSeq2A03(doc, block, index, type);
				break;
			case 3:
				inst.hip = createSeq2A03(doc, block, index, type);
				break;
			case 4:
				inst.dut = createSeq2A03(doc, block, index, type);
				break;

			default:
				break;
			}
		}

		// DPCM 部分
		int octaves = (version == 1) ? 6 : FamiTrackerSetting.OCTAVE_RANGE;

		for (int i = 0; i < octaves; ++i) {
			for (int j = 0; j < 12; ++j) {
				int index = block.readUnsignedByte();
				/*
				 * 没有采用 DPCM 的, index 都为 0.
				 * 下面如果检测到采用的 DPCM 为 0 时, 就直接跳过.
				 */
				if (index == 0) {
					inst.setEmptySample(i, j);
					block.skip((version > 5) ? 2 : 1);
					continue;
				}
				
				FtmDPCMSample sample = createDSample(doc, index);
				byte pitch = block.readByte();
				byte delta;
				if (version > 5) {
					delta = block.readByte();
					if (delta < 0) {
						delta = -1;
					}
				} else {
					delta = -1;
				}
				inst.setSample(i, j, sample, pitch, delta);
			}
		}
		
		return inst;
	}
	
	private FtmInstrumentVRC6 createVRC6Instrument(FamiTrackerHandler doc, Block block) {
		FtmInstrumentVRC6 inst = new FtmInstrumentVRC6();
		
		int seqCount = block.readAsCInt();

		for (byte type = 0; type < seqCount; ++type) {
			boolean enable = block.readByte() != 0;
			int index = block.readUnsignedByte();
			
			if (!enable) {
				continue;
			}
			
			switch (type) {
			case 0:
				inst.vol = createSeqVRC6(doc, block, index, type);
				break;
			case 1:
				inst.arp = createSeqVRC6(doc, block, index, type);
				break;
			case 2:
				inst.pit = createSeqVRC6(doc, block, index, type);
				break;
			case 3:
				inst.hip = createSeqVRC6(doc, block, index, type);
				break;
			case 4:
				inst.dut = createSeqVRC6(doc, block, index, type);
				break;

			default:
				break;
			}
		}
		return inst;
	}
	
	/**
	 * 创建 2A03 的序列
	 */
	private FtmSequence2A03 createSeq2A03(FamiTrackerHandler doc, Block block, int index, byte type) {
		FtmSequence2A03 s = new FtmSequence2A03();
		s.index = index;
		s.type = FtmSequenceType.get(type);
		
		// 将序列注册到 Ftm 中
		doc.registerSequence(s);
		
		return s;
	}
	
	/**
	 * 创建 2A03 的序列
	 */
	private FtmSequenceVRC6 createSeqVRC6(FamiTrackerHandler doc, Block block, int index, byte type) {
		FtmSequenceVRC6 s = new FtmSequenceVRC6();
		s.index = index;
		s.type = FtmSequenceType.get(type);
		
		// 将序列注册到 Ftm 中
		doc.registerSequence(s);
		
		return s;
	}
	
	/**
	 * 创建 DPCM 采样的数据. 只是一个外壳, 先不读取数据
	 * @return
	 */
	private FtmDPCMSample createDSample(FamiTrackerHandler doc, int index) {
		FtmDPCMSample sample = new FtmDPCMSample();
		sample.index = index;
		
		// 将采样注册到 Ftm 中
		doc.registerDPCMSample(sample);
		
		return sample;
	}

	/**
	 * 检查头部 ID
	 * @param openFile
	 * @return
	 */
	private boolean validateHeader(DocumentReader openFile) {
		int len = FILE_HEADER_ID.length();
		byte[] bs_head = new byte[len];
		int i = openFile.read(bs_head);
		
		if (i != len) {
			return false;
		}
		
		byte[] id_head = FILE_HEADER_ID.getBytes();
		for (int j = 0; j < bs_head.length; j++) {
			if (id_head[j] != bs_head[j]) {
				return false;
			}
		}
		
		return true;
	}
	

	
	/**
	 * 是否还有下一个 block.
	 * <p>除了最后结尾的 block (END) 以外, 所有的 block 都是由:
	 * <li>16 字节的块标识
	 * <li>4 字节的无符号数字, 表示块版本号</li>
	 * <li>4 字节的无符号数字, 表示块大小</li>
	 * <li>任意大小的数据</li>
	 */
	public Block nextBlock(DocumentReader openFile) {
		Block block = new Block();
		
		byte[] bs = new byte[16];
		int bytesRead = openFile.read(bs);
		
		if (bytesRead == 0) {
			// 读取不到数据, 意味着文件已经读取完成
			return block; // 这个 block 就是没有设置 id 的
		}
		
		block.setId(bs);
		if (FILE_END_ID.equals(block.id)) {
			return block; // 结束标识, 没有版本、大小、数据
		}
		
		block.version = openFile.readAsCInt();
		block.setSize(openFile.readAsCInt());
		
		// TODO 原程序判断 version 和 size 的合法性, 这里跳过
		
		bytesRead = openFile.read(block.bytes());
		if (bytesRead != block.size) {
			throw new RuntimeException("块: " + block.id + " 大小为 " + block.size +
					" 但是只能读取 " + bytesRead + " 字节. 文件似乎已经损坏");
		}
		
		return block;
	}
	
}
