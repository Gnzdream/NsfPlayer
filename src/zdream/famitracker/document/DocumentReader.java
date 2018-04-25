package zdream.famitracker.document;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DocumentReader {
	
	public DocumentReader(String fileName) {
		file = new File(fileName);
	}
	
	File file;
	byte[] bs; // 读取文件的全部数据在这里
	int offset;
	
	/**
	 * 文件是否读完
	 */
	protected boolean finish;
	
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
	
	public int read(byte[] bs, int offset, int len) {
		int len0 = (len > this.bs.length - this.offset) ? this.bs.length - this.offset : len;
		System.arraycopy(this.bs, this.offset, bs, offset, len0);
		this.offset += len0;
		return len0;
	}

	public int read(byte[] bs) {
		int len = (bs.length > this.bs.length - this.offset) ? this.bs.length - this.offset : bs.length;
		System.arraycopy(this.bs, this.offset, bs, 0, len);
		offset += len;
		return len;
	}
	
	/**
	 * 按照 C 语言存放的 int 格式读取 int 数据.
	 * 数值高位放在后面, 低位放在前面. 4x8 位数据
	 * @return
	 */
	public int readAsCInt() {
		if (offset + 4 > bs.length) {
			throw new ArrayIndexOutOfBoundsException("还剩余 " + (bs.length - offset) + " 数据, 无法读取 4 个值");
		}
		int value = (bs[offset] & 0xFF) | ((bs[offset + 1] & 0xFF) << 8)
				| ((bs[offset + 2] & 0xFF) << 16) | ((bs[offset + 3] & 0xFF) << 24);
		offset += 4;
		return value;
	}
	
	public boolean isFinish() {
		return finish;
	}

}
