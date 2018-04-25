package zdream.famitracker.document;

/**
 * byte 数组读取器
 * @author Zdream
 * @date 2018-04-25
 */
public class BytesReader {
	
	protected byte[] bs; // 读取文件的全部数据在这里
	protected int offset;

	public BytesReader() {
		
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
	
	public boolean isFinished() {
		return offset >= bs.length;
	}

}
