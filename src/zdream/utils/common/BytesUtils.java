package zdream.utils.common;

public class BytesUtils {

	/**
	 * <p>将 4 个 byte 数据转成 1 个 int 类型的数据, 按照高位在后的存储格式读取.
	 * <p>数据在数组中存储的格式为 [高位][低位]
	 * @param bs
	 *   数组
	 * @param offset
	 *   偏移量, 要转的 4 个 byte 数据中的第 1 个在数组中的索引值
	 * @return
	 */
	public static int bytes2Int(byte[] bs, int offset) {
		return (bs[offset] << 24)
				| (bs[offset + 1] & 0xff) << 16
				| (bs[offset + 2] & 0xff) << 8
				| (bs[offset + 3] & 0xff);
	}

}
