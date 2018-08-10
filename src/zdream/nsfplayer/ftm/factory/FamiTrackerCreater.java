package zdream.nsfplayer.ftm.factory;

import static zdream.nsfplayer.ftm.FamiTrackerSetting.MAX_DSAMPLES;
import static zdream.nsfplayer.ftm.FamiTrackerSetting.MAX_FRAMES;
import static zdream.nsfplayer.ftm.FamiTrackerSetting.MAX_INSTRUMENTS;
import static zdream.nsfplayer.ftm.FamiTrackerSetting.MAX_PATTERN;
import static zdream.nsfplayer.ftm.FamiTrackerSetting.MAX_PATTERN_LENGTH;
import static zdream.nsfplayer.ftm.FamiTrackerSetting.MAX_SEQUENCES;
import static zdream.nsfplayer.ftm.FamiTrackerSetting.MAX_TEMPO;
import static zdream.nsfplayer.ftm.format.FtmSequence.SEQUENCE_COUNT;

import zdream.nsfplayer.ftm.FamiTrackerSetting;
import zdream.nsfplayer.ftm.document.FamiTrackerHandler;
import zdream.nsfplayer.ftm.document.FtmAudio;
import zdream.nsfplayer.ftm.format.AbstractFtmInstrument;
import zdream.nsfplayer.ftm.format.FtmChipType;
import zdream.nsfplayer.ftm.format.FtmDPCMSample;
import zdream.nsfplayer.ftm.format.FtmInstrument2A03;
import zdream.nsfplayer.ftm.format.FtmInstrumentVRC6;
import zdream.nsfplayer.ftm.format.FtmNote;
import zdream.nsfplayer.ftm.format.FtmPattern;
import zdream.nsfplayer.ftm.format.FtmSequence;
import zdream.nsfplayer.ftm.format.FtmSequenceType;
import zdream.nsfplayer.ftm.format.FtmTrack;
import zdream.utils.common.BytesReader;

/**
 * 用来将 FamiTracker 的文件 (.ftm) 转换成 {@link FamiTrackerHandler}
 * 允许将转成 .txt 的文件也能够解析.
 * @author Zdream
 */
public class FamiTrackerCreater extends AbstractFamiTrackerCreater {
	
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
	
	/*
	 * 缓存的数据
	 */
	
	/**
	 * 总的曲目的数量
	 */
	private int trackCount;
	
	/**
	 * 每个轨道的效果列数
	 * [trackIdx 曲目号][channelNo 轨道序号]
	 */
	private int[][] effColumnCounts;

	/**
	 * 默认曲目播放的速度
	 */
	public static final int DEFAULT_SPEED = 6;
	
	private void reset() {
		trackCount = 0;
		effColumnCounts = null;
	}
	
	/**
	 * 最低可以打开的文件版本, v0.1.
	 * 低于这个版本的文件将不再兼容
	 */
	public static final int COMPATIBLE_VER = 0x0200;
	
	public void doCreate(BytesReader reader, FamiTrackerHandler doc) {
		reset();
		
		int version;
		
		if (!validateHeader(reader)) {
			throw new FamiTrackerFormatException("文件格式不正确: 文件头不匹配");
		}
		
		version = reader.readAsCInt();

		if (version < 0x0200) {
			// 读取低版本的文件
			throw new FamiTrackerFormatException("文件版本太低, 无法产生");
		} else if (version >= 0x0200) {
			doCreateNew(doc, reader, version);
		}
	}
	
	private void doCreateNew(FamiTrackerHandler doc, BytesReader reader, int version) {
		
		doc.allocateTrack(1);
		
		/*
		 * Famitracker 产生的文件由多个块组成.
		 * 在读取的过程中就需要对逐个块进行处理.
		 */
		while (!reader.isFinished()) LOOP: {
			Block block = nextBlock(reader);
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
				readBlockSequences(doc, block);
			} break;
			
			case FILE_BLOCK_FRAMES: {
				readBlockFrames(doc, block);
			} break;
			
			case FILE_BLOCK_PATTERNS: {
				readBlockPatterns(doc, block, version);
			} break;
			
			case FILE_BLOCK_DSAMPLES: {
				readBlockDSamples(doc, block);
			} break;
			
			case FILE_BLOCK_COMMENTS: {
				// 直接忽略
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
		
		// 当 doc 建立完成之后, 开始进入检查部分
		revise(doc);
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
			throw new FtmParseException("PARAM 版本号: " + version + " 不支持");
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
			if (doc.audio.isUseN163()) {
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
			if (doc.audio.isUseN163()) {
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
				track.tempo = (doc.audio.getMachine() == FtmAudio.MACHINE_NTSC) ?
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
			throw new FtmParseException("HEADER 版本号: " + version + " 不支持");
		}
		
		if (version == 1) {
			// 版本 1 只支持单曲
			trackCount = 1;
			
			int channelCount = doc.channelCount();
			effColumnCounts = new int[trackCount][channelCount];
			for (int i = 0; i < channelCount; ++i) {
				block.skip(1); // channelType 忽略
				effColumnCounts[0][i] = block.readByte();
			}
		} else {
			trackCount = block.readUnsignedByte() + 1;  // 0 就是只有 1 个曲子
			doc.allocateTrack(trackCount);
			
			int channelCount = doc.channelCount();
			effColumnCounts = new int[trackCount][channelCount];
			
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
					effColumnCounts[j][i] = block.readByte();
				}
			}
			
			// Highlight 忽略
		}
	}

	/**
	 * <p>处理乐器.
	 * <p>里面的数据内容有:
	 * <li>乐器序号
	 * <li>乐器的所含序列的序号 (类似于指针)
	 * <li>乐器名称
	 * </li>
	 * </p>
	 * 
	 * @param doc
	 * @param block
	 */
	private void readBlockInstruments(FamiTrackerHandler doc, Block block) {
		int version = block.version;
		if (version < 1) {
			throw new FtmParseException("INSTRUMENTS 版本号: " + version + " 不支持");
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
	 * <p>处理序列 (2A03 & MMC5).
	 * <br>根据文件里面写明的 sequences 的块版本号, 确定 {@code block} 里面的文件格式:
	 * 
	 * @param doc
	 * @param block
	 */
	private void readBlockSequences(FamiTrackerHandler doc, Block block) {
		int version = block.version;
		int count = block.readAsCInt();
		
		if (version >= 3) {
			int[] indices = new int[MAX_SEQUENCES * SEQUENCE_COUNT];
			int[] types = new int[MAX_SEQUENCES * SEQUENCE_COUNT];

			for (int i = 0; i < count; ++i) {
				int index = block.readAsCInt();
				int type = block.readAsCInt();
				int seqCount = block.readUnsignedByte(); // 序列的数组长度
				int loopPoint = block.readAsCInt(); // Loop 的点的位置

				// Work-around for some older files
				if (loopPoint == seqCount)
					loopPoint = -1;

				indices[i] = index;
				types[i] = type;

				FtmSequence seq = doc.getOrCreateSequence2A03(FtmSequenceType.get(type), index);

				seq.clear();
				seq.loopPoint = loopPoint;

				/*
				 * 版本 4 之后加入了 release 点位和 setting 数据项
				 */
				if (version == 4) {
					seq.releasePoint = block.readAsCInt();
					seq.settings = (byte) block.readAsCInt();
				}

				byte[] data = new byte[seqCount];
				block.read(data);
				seq.data = data;
			}

			if (version == 5) {
				/*
				 * 根据源代码, 版本 5 中的 release 点位在保存时出现问题, 这个问题在版本 6 时修复.
				 */
				for (int i = 0; i < MAX_SEQUENCES; ++i) {
					for (int j = 0; j < SEQUENCE_COUNT; ++j) {
						int releasePoint = block.readAsCInt();
						int settings = block.readAsCInt();
						
						FtmSequence seq = doc.getSequence2A03(FtmSequenceType.get(j), i);
						if (seq == null) {
							continue;
						}
						
						seq.releasePoint = releasePoint;
						seq.settings = (byte) settings;
					}
				}
			} else if (version >= 6) {
				// Read release points correctly stored
				/*
				 * 版本 6 的 release 点位数据能够正确地存放
				 */
				for (int i = 0; i < count; ++i) {
					int releasePoint = block.readAsCInt();
					int settings = block.readAsCInt();
					int index = indices[i];
					int type = types[i];

					FtmSequence seq = doc.getSequence2A03(FtmSequenceType.get(type), index);
					seq.releasePoint = releasePoint;
					seq.settings = (byte) settings;
				}
			}
		} 
		else {
			throw new FtmParseException("Sequences 部分暂时不支持老版本");
		}
	}

	/**
	 * <p>处理 Frames.
	 * <br>根据文件里面写明的 FRAMES 的块版本号, 确定 {@code block} 里面的文件格式:
	 * 
	 * <p>当<b>块版本为 1 </b>时不支持
	 * 
	 * <p>当<b>块版本为 2 </b>时, 每个曲目有以下信息:
	 * <li>曲目段落数
	 * <li>曲目播放速度 speed
	 * <li>段落的最大行数
	 * <li>每个轨道、每段的 order
	 * </li>
	 * 
	 * <p>当<b>块版本为 3 </b>时, 每个曲目有以下信息:
	 * <li>曲目段落数
	 * <li>曲目播放速度 speed
	 * <li>曲目播放节奏值 tempo
	 * <li>段落的最大行数
	 * <li>每个轨道、每段的 order
	 * </li>
	 * </p>
	 * 
	 * @param doc
	 * @param block
	 */
	private void readBlockFrames(FamiTrackerHandler doc, Block block) {
		int version = block.version;
		if (version <= 1) {
			throw new FtmParseException("FRAMES 版本号: " + version + " 不支持");
		}
		
		if (version > 1) {
			int trackIdx = 0;
			
			for (; trackIdx < trackCount; ++trackIdx) {
				// 曲目的所有段数 Frame
				int frameCount = block.readAsCInt();
				if (frameCount <= 0 || frameCount > MAX_FRAMES) {
					throw new FtmParseException("曲目 " + trackIdx + " 的 Frame 数量: " + frameCount + " 不合法");
				}
					
				int speed = block.readAsCInt();
				if (speed <= 0) {
					throw new FtmParseException("曲目 " + trackIdx + " 的 speed: " + speed + " 不合法");
				}
				
				FtmTrack track = doc.getOrCreateTrack(trackIdx);
				// pTrack.setFrameCount(frameCount);

				if (version == 3) {
					int tempo = block.readAsCInt();
					if (tempo <= 0 || tempo > MAX_TEMPO) {
						throw new FtmParseException("曲目 " + trackIdx + " 的 tempo: " + tempo + " 不合法");
					}
					track.tempo = tempo;
					track.speed = speed;
					
				} else {
					if (speed < 20) {
						int tempo = (doc.audio.getMachine() == FtmAudio.MACHINE_NTSC) ?
								FtmTrack.DEFAULT_NTSC_TEMPO : FtmTrack.DEFAULT_PAL_TEMPO;
						track.tempo = tempo;
						track.speed = speed;
					} else {
						if (speed > MAX_TEMPO) {
							throw new FtmParseException("曲目 " + trackIdx + " 的 speed: " + speed + " 不合法");
						}
						track.tempo = speed;
						track.speed = DEFAULT_SPEED;
					}
				}

				// 每个段落的行数
				int rowCount = block.readAsCInt();
				if (rowCount <= 0 || rowCount > MAX_PATTERN_LENGTH) {
					throw new FtmParseException("曲目 " + trackIdx + " 的 patternLength: " + rowCount + " 不合法");
				}
				
				track.length = rowCount;
				int channelsCount = doc.channelCount();
				track.orders = new int[frameCount][channelsCount];
				
				for (int frameIdx = 0; frameIdx < frameCount; ++frameIdx) {
					for (int channelIdx = 0; channelIdx < channelsCount; ++channelIdx) {
						// order 就类似于索引指针, 告诉你某个曲目第 x 段应该播放第几号段落.
						int order = block.readUnsignedByte();
						if (order < 0 || order >= MAX_PATTERN) {
							throw new FtmParseException(
									String.format("曲目 %d, Frame %d, 轨道 %d 的 order: %d 不合法",
											trackIdx, frameIdx, channelIdx, order));
						}
						
						track.orders[frameIdx][channelIdx] = order;
					}
				}
			}
			
		} else {
			throw new FtmParseException("Frame 部分暂时不支持老版本");
		}
	}

	/**
	 * <p>处理段 (Pattern).
	 * <br>根据文件里面写明的 PATTERNS 的块版本号, 确定 {@code block} 里面的文件格式
	 * <p>当<b>块版本为 1 </b>时不支持.
	 * 
	 * <p>每个段 (pattern) 都含以下数据:
	 * <li>轨道号
	 * <li>段号 pattern
	 * <li>键数据个数 note count
	 * <li>该段的所有键数据
	 * </li>
	 * 
	 * <p>每个键 (note) 都含以下数据:
	 * <li>行号
	 * <li>音调
	 * <li>音阶
	 * <li>所用的乐器序号
	 * <li>音量
	 * <li>效果项与效果参数 (有 1 或 4 个, 取决于 fileVersion)
	 * </li>
	 * </p>
	 * 
	 * @param doc
	 * @param block
	 * @param fileVersion
	 *   文件的版本号
	 */
	private void readBlockPatterns(FamiTrackerHandler doc, Block block, int fileVersion) {
		int version = block.version;
		if (version <= 1) {
			throw new FtmParseException("PATTERNS 版本号: " + version + " 不支持");
		}
		
		while (!block.isFinished()) {
			int trackIdx = block.readAsCInt();
			
			int channelIdx = block.readAsCInt();
			int patternIdx = block.readAsCInt();
			
			/*
			 * 有效数据个数.
			 * 在一个轨道上, 如果某一行的数据不为全空, 有曲调、音量、乐器、效果等数据, 就认为这个是有效数据
			 */
			int items = block.readAsCInt();
			
			if (channelIdx < 0) {
				throw new FtmParseException(String.format("PATTERNS: 曲目 %d 的轨道数 %d 不合法", trackIdx, channelIdx));
			}
			if (patternIdx < 0 || patternIdx >= MAX_PATTERN) {
				throw new FtmParseException(String.format("PATTERNS: 曲目 %d 的 pattern %d 不合法", trackIdx, patternIdx));
			}
			if (items <= 0 || items >= MAX_PATTERN_LENGTH) {
				throw new FtmParseException(String.format("PATTERNS: 曲目 %d 的 items %d 不合法", trackIdx, items));
			}
			
			for (int i = 0; i < items; ++i) {
				int row;
				if (fileVersion == 0x0200)
					row = block.readUnsignedByte();
				else
					row = block.readAsCInt();
				
				if (row >= MAX_PATTERN_LENGTH) {
					throw new FtmParseException(String.format("PATTERNS: 曲目 %d 的第 %d 个数据的行数 %d 不合法",
							trackIdx, i, row));
				}
				FtmNote note = doc.getOrCreateNote(trackIdx, patternIdx, channelIdx, row);

				note.note = block.readByte();
				note.octave = block.readByte();
				note.instrument = block.readUnsignedByte();
				note.vol = block.readByte();

				if (fileVersion == 0x0200) {
					byte effectNumber;
					short effectParam;
					effectNumber = block.readByte();
					effectParam = (short) block.readUnsignedByte();
					if (version < 3) {
						if (effectNumber == FtmNote.EF_PORTAOFF) {
							effectNumber = FtmNote.EF_PORTAMENTO;
							effectParam = 0;
						} else if (effectNumber == FtmNote.EF_PORTAMENTO) {
							if (effectParam < 0xFF)
								effectParam++;
						}
					}

					note.effNumber[0] = effectNumber;
					note.effParam[0] = effectParam;
				} else {
					int effColumnCount = effColumnCounts[trackIdx][channelIdx] + 1; // 默认就有 1 列
					for (int n = 0; n < effColumnCount; ++n) {
						byte effectNumber;
						int effectParam;
						effectNumber = block.readByte();
						effectParam = block.readUnsignedByte();

						if (version < 3) {
							if (effectNumber == FtmNote.EF_PORTAOFF) {
								effectNumber = FtmNote.EF_PORTAMENTO;
								effectParam = 0;
							} else if (effectNumber == FtmNote.EF_PORTAMENTO) {
								if (effectParam < 0xFF)
									effectParam++;
							}
						}

						note.effNumber[n] = effectNumber;
						note.effParam[n] = (short) (effectParam & 0xFF);
					}
				}

				if (note.vol > FtmNote.MAX_VOLUME) {
					throw new FtmParseException(String.format("曲目 %d, 轨道 %d, 段号 %d, 行号 %d, 音量 %d 不合法",
							trackIdx, channelIdx, patternIdx, row, note.vol));
				}

				// Specific for version 2.0
				if (fileVersion == 0x0200) {
					if (note.effNumber[0] == FtmNote.EF_SPEED && note.effParam[0] < 20)
						note.effParam[0]++;
					
					if (note.vol == 0)
						note.vol = FtmNote.MAX_VOLUME;
					else {
						note.vol--;
						note.vol &= 0x0F;
					}

					if (note.note == 0)
						note.instrument = MAX_INSTRUMENTS;
				}

				if (version == 3) {
					// Fix for VRC7 portamento TODO
					/*if (expansionEnabled(SNDCHIP_VRC7) && channel > 4) {
						for (int n = 0; n < MAX_EFFECT_COLUMNS; ++n) {
							switch (note.effNumber[n]) {
								case EF_PORTA_DOWN:
									note.effNumber[n] = EF_PORTA_UP;
									break;
								case EF_PORTA_UP:
									note.effNumber[n] = EF_PORTA_DOWN;
									break;
							}
						}
					}*/
					// TODO FDS pitch effect fix
					
				}
			}
		}
	}
	
	/**
	 * <p>处理 DSamples.
	 * <br>根据文件里面写明的 DSAMPLES 的块版本号, 确定 {@code block} 里面的文件格式:
	 * 
	 * <p>每个采样 (note) 都含以下数据:
	 * <li>序号
	 * <li>名称
	 * <li>数据
	 * </li>
	 * </p>
	 * 
	 * @param doc
	 * @param block
	 */
	private void readBlockDSamples(FamiTrackerHandler doc, Block block) {
		int count = block.readUnsignedByte();
		if (count < 0 || count >= MAX_DSAMPLES) {
			throw new FtmParseException(String.format("DSAMPLES: 数量 %d 不合法", count));
		}
		
		for (int i = 0; i < count; ++i) {
			int index = block.readUnsignedByte();
			if (index < 0 || index >= MAX_DSAMPLES) {
				throw new FtmParseException(String.format("DSAMPLES: 第 %d 个采样的序号 %d 不合法", i, index));
			}
			
			FtmDPCMSample sample = doc.getOrCreateDPCMSample(index);
			int len = block.readAsCInt();
			
			// 名称
			sample.name = block.readAsString(len);
			
			// 数据
			int size = block.readAsCInt();
			if (size < 0 || size >= 0x8000) {
				throw new FtmParseException(String.format("DSAMPLES: 第 %d 个采样的数据长度 %d 不合法", i, size));
			}
			byte[] bs = new byte[size];
			int relSize = block.read(bs);
			if (relSize != size) {
				throw new FtmParseException(String.format("DSAMPLES: 第 %d 个采样的数据长度 %d 不合法, 实际只能读取 %d",
						i, size, relSize));
			}
			sample.data = bs;
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

		// TODO 除了 2A03 和 VRC6 的其它的乐器
			
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
			
			createSequence(doc, index, type);
			switch (type) {
			case 0:
				inst.vol = index;
				break;
			case 1:
				inst.arp = index;
				break;
			case 2:
				inst.pit = index;
				break;
			case 3:
				inst.hip = index;
				break;
			case 4:
				inst.dut = index;
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
				
				FtmDPCMSample sample = doc.getOrCreateDPCMSample(index);
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
			
			createSeqVRC6(doc, index, type);
			switch (type) {
			case 0:
				inst.vol = index;
				break;
			case 1:
				inst.arp = index;
				break;
			case 2:
				inst.pit = index;
				break;
			case 3:
				inst.hip = index;
				break;
			case 4:
				inst.dut = index;
				break;

			default:
				break;
			}
		}
		return inst;
	}
	
	/* **********
	 *   检查   *
	 ********** */
	
	/**
	 * 检查和尝试修复
	 */
	void revise(FamiTrackerHandler doc) {
		reviseNotes(doc);
		// TODO 其它检查项
	}
	
	/**
	 * 检查乐器, 还有每个音键使用的乐器是否在正确的范围内.
	 * 修复: 将不在正确的范围内的乐器号码修改成统一值 -1
	 * @param doc
	 */
	private void reviseNotes(FamiTrackerHandler doc) {
		FtmAudio audio = doc.audio;
		int instMax = audio.instrumentCount();
		
		int trackLen = audio.getTrackCount();
		for (int i = 0; i < trackLen; i++) {
			FtmTrack track = audio.getTrack(i);
			FtmPattern[][] ps = track.patterns;
			
			for (int x = 0; x < ps.length; x++) {
				FtmPattern[] ys = ps[x];
				if (ys == null) {
					continue;
				}
				
				for (int y = 0; y < ys.length; y++) {
					FtmPattern p = ys[y];
					if (p == null) {
						continue;
					}
					
					FtmNote[] notes = p.notes;
					for (int j = 0; j < notes.length; j++) {
						reviseNote(notes[j], instMax);
					}
				}
			}
			
		}
	}
	
	private void reviseNote(FtmNote note, int instMax) {
		if (note == null) {
			return;
		}
		
		if (note.instrument < 0 || note.instrument >= instMax) {
			note.instrument = -1;
		}
	}
	
	/* **********
	 *   其它   *
	 ********** */

	/**
	 * 检查头部 ID
	 * @param reader
	 * @return
	 */
	boolean validateHeader(BytesReader reader) {
		int len = FILE_HEADER_ID.length();
		byte[] bs_head = new byte[len];
		int i = reader.read(bs_head);
		
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
	public Block nextBlock(BytesReader reader) {
		Block block = new Block();
		
		byte[] bs = new byte[16];
		int bytesRead = reader.read(bs);
		
		if (bytesRead == 0) {
			// 读取不到数据, 意味着文件已经读取完成
			return block; // 这个 block 就是没有设置 id 的
		}
		
		block.setId(bs);
		if (FILE_END_ID.equals(block.id)) {
			return block; // 结束标识, 没有版本、大小、数据
		}
		
		block.version = reader.readAsCInt();
		block.setSize(reader.readAsCInt());
		
		// TODO 原程序判断 version 和 size 的合法性, 这里跳过
		
		bytesRead = reader.read(block.bytes());
		if (bytesRead != block.size) {
			throw new RuntimeException("块: " + block.id + " 大小为 " + block.size +
					" 但是只能读取 " + bytesRead + " 字节. 文件似乎已经损坏");
		}
		
		return block;
	}
	
}
