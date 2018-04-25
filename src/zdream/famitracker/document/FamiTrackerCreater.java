package zdream.famitracker.document;

import java.io.IOException;

/**
 * 用来将 FamiTracker 的文件 (.ftm) 转换成 {@link FamiTrackerDocument}
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
	public static final String FILE_BLOCK_PARAMS	 = "PARAMS";
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
	 * 创建 {@link FamiTrackerDocument} 文档
	 * @param filename
	 * @return
	 * @throws Exception
	 */
	public FamiTrackerDocument create(String filename) throws Exception {
		FamiTrackerDocument doc = new FamiTrackerDocument();
		
		doCreate(filename, doc);
		
		return doc;
	}
	
	/**
	 * 最低可以打开的文件版本, v0.1.
	 * 低于这个版本的文件将不再兼容
	 */
	public static final int COMPATIBLE_VER = 0x0200;
	
	private void doCreate(String filename, FamiTrackerDocument doc) throws IOException {
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
	
	private void doCreateNew(FamiTrackerDocument doc, DocumentReader openFile, int version) {
		// 1191 行
		
		if (version < 0x0210) {
			// This has to be done for older files
			doc.allocateTrack(0);
		}
		
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
			
			switch (block.id) {
			
			case FILE_END_ID: // 已经读取结束
				break LOOP;
			
			case FILE_BLOCK_PARAMS:
				//errorFlag = readBlock_Parameters(documentFile);
				break;
				
			case FILE_BLOCK_INFO: {
				/*byte[] bs = new byte[32];
				
				documentFile.getBlock(bs);
				m_strName = new String(bs, FamiTrackerApp.defCharset);
			
				documentFile.getBlock(bs);
				m_strArtist = new String(bs, FamiTrackerApp.defCharset);
			
				documentFile.getBlock(bs);
				m_strCopyright = new String(bs, FamiTrackerApp.defCharset);*/
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
			
			case FILE_BLOCK_HEADER: {
				//errorFlag = readBlock_Header(documentFile);
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
		block.size = openFile.readAsCInt();
		
		// TODO 原程序判断 version 和 size 的合法性, 这里跳过
		
		block.bs = new byte[block.size];
		bytesRead = openFile.read(block.bs);
		if (bytesRead != block.size) {
			throw new RuntimeException("块: " + block.id + " 大小为 " + block.size +
					" 但是只能读取 " + bytesRead + " 字节. 文件似乎已经损坏");
		}
		
		return block;
	}
	
}
