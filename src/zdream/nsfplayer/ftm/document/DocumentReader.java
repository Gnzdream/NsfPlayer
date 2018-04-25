package zdream.nsfplayer.ftm.document;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import zdream.utils.common.BytesReader;

public class DocumentReader extends BytesReader {
	
	public DocumentReader(String fileName) {
		file = new File(fileName);
	}
	
	File file;
	
	/**
	 * 打开并创建文件
	 * @param fileName
	 * @throws IOException
	 */
	public void open() throws IOException {
		FileInputStream reader = new FileInputStream(file);
		bs = new byte[(int) file.length()];
		reader.read(bs);
		offset = 0;
		
		reader.close();
	}
	
	public long length() {
		return file.length();
	}

}
