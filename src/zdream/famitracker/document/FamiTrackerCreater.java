package zdream.famitracker.document;

import java.io.IOException;

/**
 * 用来将 FamiTracker 的文件 (.ftm) 转换成 {@link FamiTrackerDocument}
 * 允许将转成 .txt 的文件也能够解析.
 * @author Zdream
 */
public class FamiTrackerCreater {
	
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
	
	private static final String FILE_HEADER_ID = "FamiTracker Module";
	
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
		
		// TODO
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
	
}
