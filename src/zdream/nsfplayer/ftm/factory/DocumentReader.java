package zdream.nsfplayer.ftm.factory;

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
	 * 打开并创建文件, 将文件的所有数据以 byte 数组的方式读入.
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
	
	@Override
	public int length() {
		return (int) file.length();
	}
	
	@Override
	public void set(byte[] bs) {
		// do-nothing
	}

}
