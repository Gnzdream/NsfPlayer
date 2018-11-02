package zdream.nsfplayer.ftm.factory;

import static zdream.nsfplayer.ftm.format.FtmSequence.SEQUENCE_COUNT;

import static zdream.nsfplayer.ftm.format.FtmStatic.*;
import static zdream.nsfplayer.core.FtmChipType.*;
import static zdream.nsfplayer.ftm.format.FtmNote.EF_PITCH;

import zdream.nsfplayer.core.FtmChipType;
import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.ftm.audio.FamiTrackerHandler;
import zdream.nsfplayer.ftm.audio.FtmAudio;
import zdream.nsfplayer.ftm.format.AbstractFtmInstrument;
import zdream.nsfplayer.ftm.format.FtmDPCMSample;
import zdream.nsfplayer.ftm.format.FtmInstrument2A03;
import zdream.nsfplayer.ftm.format.FtmInstrumentFDS;
import zdream.nsfplayer.ftm.format.FtmInstrumentVRC6;
import zdream.nsfplayer.ftm.format.FtmNote;
import zdream.nsfplayer.ftm.format.FtmPattern;
import zdream.nsfplayer.ftm.format.FtmSequence;
import zdream.nsfplayer.ftm.format.FtmSequenceType;
import zdream.nsfplayer.ftm.format.FtmTrack;
import zdream.utils.common.BytesReader;

/**
 * <p>用来将 FamiTracker 的文件 (.ftm) 利用 {@link FamiTrackerHandler}
 * 填充 {@link FtmAudio} 的数据
 * <p>一个该创建者实例只能填充一个 {@link FtmAudio} 的数据.
 * 如果要填充更多 {@link FtmAudio} 请新建更多该创建者实例.
 * </p>
 * @author Zdream
 * @since v0.1
 */
public class FamiTrackerCreater extends AbstractFamiTrackerCreater<BytesReader> {
	
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
	
	public static final String FILE_BLOCK_SEQUENCES_VRC6 = "SEQUENCES_VRC6";
	// 尚未使用的
	public static final String FILE_BLOCK_SEQUENCES_N163 = "SEQUENCES_N163";
	public static final String FILE_BLOCK_SEQUENCES_N106 = "SEQUENCES_N106";
	public static final String FILE_BLOCK_SEQUENCES_S5B = "SEQUENCES_S5B";
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
	
	/**
	 * <p>这个数值由于 FamiTracker 的版本兼容问题而导致.
	 * <p>FDS 的 ftm 文件在低版本（尚不清楚版本号）下, 由于原 FamiTracker 的开发者原因,
	 * 它的音阶数据会和实际音阶数据相差 2.
	 * <p>当检测到有该问题时, 该值就会置为 true, 引起后面的处理工作.
	 * </p>
	 */
	private boolean needAdjustFDSArpeggio;
	
	private void init() {
		trackCount = 0;
		effColumnCounts = null;
		needAdjustFDSArpeggio = false;
	}
	
	/**
	 * 最低可以打开的文件版本, v0.1.
	 * 低于这个版本的文件将不再兼容
	 */
	public static final int COMPATIBLE_VER = 0x0200;
	
	public void doCreate(BytesReader reader, FamiTrackerHandler doc) {
		init();
		
		validateHeader(reader);
		
		int version;
		version = reader.readAsCInt();

		if (version < 0x0200) {
			// 读取低版本的文件
			handleException(reader, EX_GENERAL_LOW_VERSION, version);
		} else if (version >= 0x0200) {
			try {
				doCreateNew(doc, reader, version);
			} catch (RuntimeException e) {
				handleException(reader, e);
			}
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
			
			case FILE_BLOCK_SEQUENCES_VRC6: {
				readBlockSequencesVRC6(doc, block);
			} break;
			
			// FILE_BLOCK_SEQUENCES_N106 是出于向后兼容的目的
			case FILE_BLOCK_SEQUENCES_N163: case FILE_BLOCK_SEQUENCES_N106: {
				// TODO 暂时无法处理 N163 部分
			} break;
			
			case FILE_BLOCK_SEQUENCES_S5B: {
				// TODO 暂时无法处理 S5B 部分
			} break;

			default:
				handleException(block, EX_BLOCK_UNKNOWED_ID);
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
			handleException(block, EX_PARAM_LOW_VERSION, version);
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
			handleException(block, EX_HEADER_LOW_VERSION, version);
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
			handleException(block, EX_INSTS_LOW_VERSION, version);
		}
		
		// 乐器中, 序号最大的值 + 1
		int max = block.readAsCInt();
		
		for (int i = 0; i < max; ++i) {
			// 乐器序号
			int index = block.readAsCInt();

			// 创建乐器实例
			byte type = block.readByte();
			AbstractFtmInstrument inst = createInstrument(ofInstrumentType(type), doc, block);
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
		} else {
			handleException(block, EX_SEQS_LOW_VERSION, version);
		}
	}
	
	private void readBlockSequencesVRC6(FamiTrackerHandler doc, Block block) {
		int version = block.version;
		int count = block.readAsCInt();

		if (version < 4) {
			for (int i = 0; i < count; ++i) {
				int index = block.readAsCInt();
				int type = block.readAsCInt();
				int seqCount = block.readUnsignedByte();
				int loopPoint = block.readAsCInt();
				
				FtmSequence seq = doc.getOrCreateSequenceVRC6(FtmSequenceType.get(type), index);
				seq.clear();
				
				seq.loopPoint = loopPoint;
				
				byte[] bs = new byte[seqCount];
				block.read(bs);
				seq.data = bs;
			}
		} else {
			int[] indices = new int[MAX_SEQUENCES];
			int[] types = new int[MAX_SEQUENCES];
			int releasePoint = -1, settings = 0;

			for (int i = 0; i < count; ++i) {
				int index = block.readAsCInt();
				int type = block.readAsCInt();
				int seqCount = block.readUnsignedByte();
				int loopPoint = block.readAsCInt();

				indices[i] = index;
				types[i] = type;
				
				// 检查 index
				if (index >= MAX_SEQUENCES) {
					handleException(block, EX_SEQSVRC6_MAX_SEQUENCES, index);
				}

				FtmSequence seq = doc.getOrCreateSequenceVRC6(FtmSequenceType.get(type), index);
				seq.clear();

				seq.loopPoint = loopPoint;
				
				if (version == 4) {
					seq.releasePoint = block.readAsCInt();
					seq.settings = (byte) block.readAsCInt();
				}
				
				byte[] data = new byte[seqCount];
				block.read(data);
				seq.data = data;
			}

			if (version == 5) {
				// 根据源代码, 版本 5 中的 release 点位在保存时出现问题, 这个问题在版本 6 时修复.
				for (int i = 0; i < MAX_SEQUENCES; ++i) {
					for (int j = 0; j < SEQUENCE_COUNT; ++j) {
						releasePoint = block.readAsCInt();
						settings = block.readAsCInt();
						
						FtmSequence seq = doc.getSequenceVRC6(FtmSequenceType.get(j), i);
						if (seq == null) {
							continue;
						}
						
						seq.releasePoint = releasePoint;
						seq.settings = (byte) settings;
					}
				}
			} else if (version >= 6) {
				for (int i = 0; i < count; ++i) {
					releasePoint = block.readAsCInt();
					settings = block.readAsCInt();
					int index = indices[i];
					int type = types[i];
					
					FtmSequence seq = doc.getOrCreateSequenceVRC6(FtmSequenceType.get(type), index);
					
					seq.releasePoint = releasePoint;
					seq.settings = (byte) settings;
				}
			}
		}
	}

	/**
	 * <p>处理段 Frames.
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
			handleException(block, EX_FRAMES_LOW_VERSION, version);
		}
		
		int trackIdx = 0;
		
		for (; trackIdx < trackCount; ++trackIdx) {
			// 曲目的所有段数 Frame
			int frameCount = block.readAsCInt();
			if (frameCount <= 0 || frameCount > MAX_FRAMES) {
				handleException(block, EX_FRAMES_WRONG_FRAME_COUNT, trackIdx, frameCount);
			}
				
			int speed = block.readAsCInt();
			if (speed <= 0) {
				handleException(block, EX_FRAMES_WRONG_SPEED, trackIdx, speed);
			}
			
			FtmTrack track = doc.getOrCreateTrack(trackIdx);

			if (version == 3) {
				int tempo = block.readAsCInt();
				if (tempo <= 0 || tempo > MAX_TEMPO) {
					handleException(block, EX_FRAMES_WRONG_TEMPO, trackIdx, tempo);
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
						handleException(block, EX_FRAMES_WRONG_SPEED, trackIdx, speed);
					}
					track.tempo = speed;
					track.speed = DEFAULT_SPEED;
				}
			}

			// 每个段落的行数
			int rowCount = block.readAsCInt();
			if (rowCount <= 0 || rowCount > MAX_PATTERN_LENGTH) {
				handleException(block, EX_FRAMES_WRONG_ROW_NO, trackIdx, rowCount);
			}
			
			track.length = rowCount;
			int channelsCount = doc.channelCount();
			track.orders = new int[frameCount][channelsCount];
			
			for (int frameIdx = 0; frameIdx < frameCount; ++frameIdx) {
				for (int channelIdx = 0; channelIdx < channelsCount; ++channelIdx) {
					// order 就类似于索引指针, 告诉你某个曲目第 x 段应该播放第几号段落.
					int order = block.readUnsignedByte();
					if (order < 0 || order >= MAX_PATTERN) {
						handleException(block, EX_FRAMES_WRONG_ORDER_NO,
								trackIdx, frameIdx, channelIdx, order);
					}
					
					track.orders[frameIdx][channelIdx] = order;
				}
			}
		}
	}

	/**
	 * <p>处理模式 (Pattern).
	 * <br>根据文件里面写明的 PATTERNS 的块版本号, 确定 {@code block} 里面的文件格式
	 * <p>当<b>块版本为 1 </b>时不支持.
	 * 
	 * <p>每个模式 (pattern) 都含以下数据:
	 * <li>轨道号
	 * <li>模式号 pattern
	 * <li>键数据个数 note count
	 * <li>该模式的所有键数据
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
			handleException(block, EX_PAT_LOW_VERSION, version);
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
				handleException(block, EX_PAT_WRONG_CHANNEL_NO, trackIdx, channelIdx);
			}
			if (patternIdx < 0 || patternIdx >= MAX_PATTERN) {
				handleException(block, EX_PAT_WRONG_PATTERN_NO, trackIdx, patternIdx);
			}
			if (items <= 0 || items >= MAX_PATTERN_LENGTH) {
				handleException(block, EX_PAT_WRONG_NOTE_AMOUNT, trackIdx, items);
			}
			
			for (int i = 0; i < items; ++i) {
				int row;
				if (fileVersion == 0x0200)
					row = block.readUnsignedByte();
				else
					row = block.readAsCInt();
				
				FtmNote note;
				if (row >= MAX_PATTERN_LENGTH) {
					handleException(block, EX_PAT_WRONG_ROW_NO, trackIdx, patternIdx, channelIdx, i, row);
				}
				
				if (row >= doc.audio.getTrack(trackIdx).length) {
					// 这个 note 不会加到 doc 中去
					note = new FtmNote();
				} else {
					note = doc.getOrCreateNote(trackIdx, patternIdx, channelIdx, row);
				}

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
					handleException(block, EX_PAT_WRONG_VOLUME, trackIdx, patternIdx, channelIdx, row, note.vol);
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
					
					// 如果该轨道是 FDS 轨道
					/* else */ if (doc.audio.isUseFds()
							&& doc.channelCode(channelIdx) == INsfChannelCode.CHANNEL_FDS) {
						for (int n = 0; n < MAX_EFFECT_COLUMNS; ++n) {
							if (note.effNumber[n] == EF_PITCH) {
								if (note.effParam[n] != 0x80)
									note.effParam[n] = (short) ((0x100 - note.effParam[n]) & 0xFF);
							}
						}
					}
					
				}
				
				if (version < 5) {
					// FDS 的音阶在以前的版本是低两个值的
					if (doc.audio.isUseFds() && doc.channelCode(channelIdx) == INsfChannelCode.CHANNEL_FDS
							&& note.octave < 6) {
						note.octave += 2;
						needAdjustFDSArpeggio = true;
					}
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
			handleException(block, EX_DSMP_WRONG_AMOUNT, count);
		}
		
		for (int i = 0; i < count; ++i) {
			int index = block.readUnsignedByte();
			if (index < 0 || index >= MAX_DSAMPLES) {
				handleException(block, EX_DSMP_WRONG_INDEX, i, index);
			}
			
			FtmDPCMSample sample = doc.getOrCreateDPCMSample(index);
			int len = block.readAsCInt();
			
			// 名称
			sample.name = block.readAsString(len);
			
			// 数据
			int size = block.readAsCInt();
			if (size < 0 || size >= 0x8000) {
				handleException(block, EX_DSMP_WRONG_SIZE, i, size);
			}
			byte[] bs = new byte[size];
			int relSize = block.read(bs);
			if (relSize != size) {
				handleException(block, EX_DSMP_NOT_REACH_END, i, size, relSize);
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
			
		case FDS:
			return createFDSInstrument(doc, block);

		// TODO 其它芯片 N163 VRC7 S5B
			
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
		int octaves = (version == 1) ? 6 : OCTAVE_RANGE;

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
				
				FtmDPCMSample sample = doc.getOrCreateDPCMSample(index - 1);
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
	
	private FtmInstrumentFDS createFDSInstrument(FamiTrackerHandler doc, Block block) {
		FtmInstrumentFDS inst = new FtmInstrumentFDS();
		
		for (int i = 0; i < FtmInstrumentFDS.SAMPLE_LENGTH; ++i) {
			inst.samples[i] = block.readByte();
		}

		for (int i = 0; i < FtmInstrumentFDS.MODULATION_LENGTH; ++i) {
			inst.modulation[i] = block.readByte();
		}
		
		inst.modulationSpeed = block.readAsCInt();
		inst.modulationDepth = block.readAsCInt();
		inst.modulationDelay = block.readAsCInt();

		/*
		 * 这里用后面的数据推断下面是哪个部分
		 */
		int a = block.readAsCInt() & 0x7FFFFFFF; // unsigned
		int b = block.readAsCInt() & 0x7FFFFFFF; // unsigned
		block.rollback(8);

		if (a < 256 && (b & 0xFF) != 0x00) {
			// 什么都不做
		} else {
			inst.seqVolume = createFDSSequence(doc, block, FtmSequenceType.VOLUME);
			inst.seqArpeggio = createFDSSequence(doc, block, FtmSequenceType.ARPEGGIO);
			//
			// 下面的文本来自原 FamiTracker 工程.
			// Note: Remove this line when files are unable to load 
			// (if a file contains FDS instruments but FDS is disabled)
			// this was a problem in an earlier version.
			//
			if (block.version > 2) {
				inst.seqPitch = createFDSSequence(doc, block, FtmSequenceType.PITCH);
			} else {
				inst.seqPitch = createEmptySequence(FtmSequenceType.PITCH);
			}
		}

		// 原始版本音量范围是 [0, 15], 现在是 [0, 31]
		// Older files was 0-15, new is 0-31
		if (block.version <= 3) {
			for (int i = 0; i < inst.seqVolume.length(); ++i) {
				inst.seqVolume.data[i] *= 2; 
			}
		}
		
		return inst;
	}
	
	private FtmSequence createFDSSequence(FamiTrackerHandler doc, Block block, FtmSequenceType type) {
		// 原工程里面下面四个值均为 unsigned
		// 但是我认为 loopPoint 和 releasePoint 非法值是 -1
		// 所以这里这两个值不强制转为 unsigned
		final int seqCount = block.readUnsignedByte();
		int loopPoint = block.readAsCInt();
		int releasePoint = block.readAsCInt();
		int settings = block.readAsCInt(); // 仅 Arpeggio 序列使用

		if (seqCount > MAX_SEQUENCES) {
			handleException(block, EX_INSTS_WRONG_SEQ_AMOUNT, seqCount);
		}
		FtmSequence seq = new FtmSequence(type);

		// seq.  setItemCount(seqCount);
		seq.loopPoint = loopPoint;
		seq.releasePoint = releasePoint;
		seq.settings = (byte) settings;

		seq.data = new byte[seqCount];
		for (int i = 0; i < seqCount; ++i) {
			int value = block.readUnsignedByte();
			seq.data[i] = (byte) value;
		}

		return seq;
	}
	
	private FtmSequence createEmptySequence(FtmSequenceType type) {
		FtmSequence seq = new FtmSequence(type);
		seq.clear();
		return seq;
	}
	
	/* **********
	 *   检查   *
	 ********** */
	
	/**
	 * 检查和尝试修复
	 */
	void revise(FamiTrackerHandler doc) {
		reviseNotes(doc);
		
		// FDS 乐器兼容问题
		if (needAdjustFDSArpeggio) {
			adjustFDSArpeggio(doc);
		}
		
		// TODO 其它检查项
	}

	private void adjustFDSArpeggio(FamiTrackerHandler doc) {
		for (int i = 0; i < doc.audio.instrumentCount(); ++i) {
			AbstractFtmInstrument inst = doc.audio.getInstrument(i);
			if (inst == null || inst.instType() != FtmChipType.FDS) {
				continue;
			}
			
			FtmInstrumentFDS instfds = (FtmInstrumentFDS) inst;
			FtmSequence seq = instfds.seqArpeggio;
			
			if (seq.length() > 0 && seq.settings == FtmSequence.ARP_SETTING_FIXED) {
				final int length = seq.length();
				for (int j = 0; j < length; ++j) {
					seq.data[j] += 24;
				}
			}
		}
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
			if (ps == null) {
				track.patterns = new FtmPattern[1][doc.channelCount()];
				continue;
			}
			
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
	 * 错误处理 *
	 ********** */
	
	/*
	 * 产生的消息错误列表
	 */
	static final String EX_GENERAL_WRONG_HEAD = "FTM 文件格式不正确, 文件头不匹配";
	static final String EX_GENERAL_LOW_VERSION = "FTM 文件版本 %x 太低, 无法解析";
	static final String EX_BLOCK_UNKNOWED_ID = "未知的块 ID";
	// Parameters
	static final String EX_PARAM_LOW_VERSION = "FTM Parameters 块版本 %d 太低, 无法解析";
	// Header
	static final String EX_HEADER_LOW_VERSION = "FTM Header 块版本 %d 太低, 无法解析";
	// Instruments
	static final String EX_INSTS_LOW_VERSION = "FTM Instruments 块版本 %d 太低, 无法解析";
	static final String EX_INSTS_WRONG_SEQ_AMOUNT = "乐器序列的数量: %d 有误";
	// Sequences
	static final String EX_SEQS_LOW_VERSION = "FTM Sequences 块版本 %d 太低, 无法解析";
	// Seq VRC6
	static final String EX_SEQSVRC6_MAX_SEQUENCES = "VRC6 序列的序号 %d 异常";
	// FRAMES
	static final String EX_FRAMES_LOW_VERSION = "FTM Frames 块版本 %d 太低, 无法解析";
	static final String EX_FRAMES_WRONG_FRAME_COUNT = "曲目 %d 的段 Frame 数量: %d 有误";
	static final String EX_FRAMES_WRONG_SPEED = "曲目 %d 的速度值 speed: %d 有误";
	static final String EX_FRAMES_WRONG_TEMPO = "曲目 %d 的节奏值 tempo: %d 有误";
	static final String EX_FRAMES_WRONG_ROW_NO = "曲目 %d 的行号: %d 有误";
	static final String EX_FRAMES_WRONG_ORDER_NO = "曲目 %d, 段 (Frame) 号 %d, 轨道序号 %d 的顺序号 order: %d 有误";
	// PATTERNS
	static final String EX_PAT_LOW_VERSION = "FTM Patterns 块版本 %d 太低, 无法解析";
	static final String EX_PAT_WRONG_CHANNEL_NO = "曲目 %d 的轨道序号: %d 有误";
	static final String EX_PAT_WRONG_PATTERN_NO = "曲目 %d 的模式序号: %d 有误";
	static final String EX_PAT_WRONG_NOTE_AMOUNT = "曲目 %d 的键个数: %d 有误";
	static final String EX_PAT_WRONG_ROW_NO = "曲目 %d, 模式号 %d, 轨道序号 %d, 第 %d 个键的行号: %d 有误";
	static final String EX_PAT_WRONG_VOLUME = "曲目 %d, 模式号 %d, 轨道序号 %d, 行号 %d 的键的音量: %d 有误";
	// DSamples
	static final String EX_DSMP_WRONG_AMOUNT = "DPCM 采样的数量: %d 有误";
	static final String EX_DSMP_WRONG_INDEX = "第 %d 个采样序号: %d 有误";
	static final String EX_DSMP_WRONG_SIZE = "第 %d 个采样的数据长度: %d 有误";
	static final String EX_DSMP_NOT_REACH_END = "第 %d 个采样的数据长度: %d 不合法, 实际只能读取 %d";
	
	/**
	 * @param block
	 *   当前的错误块
	 * @param msg
	 *   错误消息内容
	 * @throws FamiTrackerFormatException
	 * @since v0.2.5
	 */
	protected void handleException(Block block, String msg) throws FamiTrackerFormatException {
		String msg0 = String.format("位置 0x%x [0x%x + 0x%x] (%s) 版本号 %d, 发现错误: %s",
				block.getOffset() + block.blockOffset, block.getOffset(), block.blockOffset,
				block.id, block.version, msg);
		
		throw new FamiTrackerFormatException(msg0);
	}

	protected void handleException(Block block, String msg, Object... args) throws FamiTrackerFormatException {
		handleException(block, String.format(msg, args));
	}
	
	/**
	 * @param reader
	 *   当前 byte[] 的数据载体
	 * @param msg
	 *   错误消息内容
	 * @throws FamiTrackerFormatException
	 * @since v0.2.5
	 */
	protected void handleException(BytesReader reader, String msg) throws FamiTrackerFormatException {
		String msg0 = String.format("位置 0x%x 发现错误: %s", reader.getOffset(), msg);
		
		throw new FamiTrackerFormatException(msg0);
	}

	protected void handleException(BytesReader reader, String msg, Object... args) throws FamiTrackerFormatException {
		handleException(reader, String.format(msg, args));
	}
	
	protected void handleException(BytesReader reader, RuntimeException exp)
			throws FamiTrackerFormatException, RuntimeException {
		if (exp instanceof FamiTrackerFormatException) {
			throw exp;
		} else {
			String msg0 = String.format("位置 $%x 发现错误 %s: %s", reader.getOffset(),
					exp.getClass().getSimpleName(), exp.getMessage());
			throw new FamiTrackerFormatException(msg0, exp);
		}
	}
	
	/* **********
	 *   其它   *
	 ********** */

	/**
	 * 检查头部 ID. 检查到出现问题时, 抛出 {@link FamiTrackerFormatException}
	 * @param reader
	 */
	void validateHeader(BytesReader reader) {
		int len = FILE_HEADER_ID.length();
		byte[] bs_head = new byte[len];
		int i = reader.read(bs_head);
		
		if (i != len) {
			handleException(reader, EX_GENERAL_WRONG_HEAD);
		}
		
		byte[] id_head = FILE_HEADER_ID.getBytes();
		for (int j = 0; j < bs_head.length; j++) {
			if (id_head[j] != bs_head[j]) {
				handleException(reader, EX_GENERAL_WRONG_HEAD);
			}
		}
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
		block.blockOffset = reader.getOffset();
		
		// TODO 原程序判断 version 和 size 的合法性, 这里跳过
		
		bytesRead = reader.read(block.bytes());
		if (bytesRead != block.size) {
			throw new FamiTrackerFormatException("块: " + block.id + " 大小为 " + block.size +
					" 但是只能读取 " + bytesRead + " 字节. 文件似乎已经损坏");
		}
		
		return block;
	}
	
	/**
	 * 用所给的 (TODO) 类型号确定它是 Nsf 的什么芯片
	 * @param type
	 * @return
	 */
	public FtmChipType ofInstrumentType(int type) {
		switch (type) {
		case 1: return _2A03;
		case 2: return VRC6;
		case 3: return VRC7;
		case 4: return FDS;
		case 5: return N163;
		case 6: return S5B;

		default: return null;
		}
	}
	
}
