package zdream.utils.common;

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
	
	/**
	 * 获取一个 byte 数据
	 * @return
	 */
	public byte readByte() {
		if (offset + 1 > bs.length) {
			throw new ArrayIndexOutOfBoundsException("还剩余 " + (bs.length - offset) + " 数据, 无法读取 4 个值");
		}
		return bs[offset++];
	}
	
	/**
	 * 获取一个转成正数的 byte 数据
	 * @return
	 */
	public int readUnsignedByte() {
		if (offset + 1 > bs.length) {
			throw new ArrayIndexOutOfBoundsException("还剩余 " + (bs.length - offset) + " 数据, 无法读取 4 个值");
		}
		return (bs[offset++] & 0xFF);
	}
	
	/**
	 * <p>将后面的 length 长度的数据当成字符串数据, 转换成字符串.
	 * <p>如果字符串后面有一大堆 '\0', 则转换时会去掉这些数据.
	 * <p>当最后读到数据最底端时, 不会抛数组越界错误.
	 * @param length
	 * @return
	 */
	public String readAsString(int length) {
		byte[] bs = new byte[length];
		int byteReads = read(bs);
		
		int end = byteReads - 1;
		for (; end >= 0; end--) {
			if (bs[end] == '\0') {
				continue;
			}
			break;
		}
		
		return new String(bs, 0, end + 1);
	}
	
	public boolean isFinished() {
		return offset >= bs.length;
	}
	
	public final byte[] bytes() {
		return bs;
	}

}
