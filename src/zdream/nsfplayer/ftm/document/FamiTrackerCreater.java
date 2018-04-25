package zdream.nsfplayer.ftm.document;

import java.io.IOException;

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
				//errorFlag = readBlock_Header(documentFile);
			} break;
			
			case FILE_BLOCK_INSTRUMENTS: {
				//errorFlag = readBlock_Instruments(documentFile);
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
	 * <p>当<b>块版本为 1 或 2 </b>时:
	 * <li>track[0] 的 speed
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
			throw new FtmParseException("版本号错误: " + version);
		}
		
		FtmTrack track = doc.createTrack();
		
		switch (version) {
		case 1: case 2:
			track.speed = block.readAsCInt();
			block.readAsCInt(); // 轨道数, 忽略
			doc.setMechine((byte) block.readAsCInt());
			doc.setFramerate(block.readAsCInt());
			doc.setDefaultSplit();
			
			break;
			
		case 3:
			doc.setChip(block.readByte());
			block.readAsCInt(); // 轨道数, 忽略
			doc.setMechine((byte) block.readAsCInt());
			doc.setFramerate(block.readAsCInt());
			block.readAsCInt(); // 震动模式, 忽略
			doc.setDefaultSplit();
			
			break;
			
		case 4: case 5:
			doc.setChip(block.readByte());
			block.readAsCInt(); // 轨道数, 忽略
			doc.setMechine((byte) block.readAsCInt());
			doc.setFramerate(block.readAsCInt());
			block.readAsCInt(); // 震动模式, 忽略
			block.readAsCInt(); // 小节间隔 忽略
			block.readAsCInt(); // 拍间隔, 忽略
			doc.setDefaultSplit();
			
			break;
			
		case 6:
			doc.setChip(block.readByte());
			block.readAsCInt(); // 轨道数, 忽略
			doc.setMechine((byte) block.readAsCInt());
			doc.setFramerate(block.readAsCInt());
			block.readAsCInt(); // 震动模式, 忽略
			block.readAsCInt(); // 小节间隔 忽略
			block.readAsCInt(); // 拍间隔, 忽略
			if (doc.audio.useN163) {
				doc.setNamcoChannels(block.readAsCInt());
			}
			doc.setDefaultSplit();
			
			break;
			
		default:
			doc.setChip(block.readByte());
			block.readAsCInt(); // 轨道数, 忽略
			doc.setMechine((byte) block.readAsCInt());
			doc.setFramerate(block.readAsCInt());
			block.readAsCInt(); // 震动模式, 忽略
			block.readAsCInt(); // 小节间隔 忽略
			block.readAsCInt(); // 拍间隔, 忽略
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
