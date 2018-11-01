package zdream.nsfplayer.ftm.factory;

import zdream.utils.common.BytesReader;

/**
 * FTM 的文件块
 * @author Zdream
 * @date 2018-04-25
 */
class Block extends BytesReader {
	/**
	 * 文件头标识
	 */
	String id;
	public void setId(byte[] id) {
		// 将数组后面的空白和 \0 全部忽略
		int end = id.length - 1;
		for (; end > 0; end--) {
			if (id[end] == '\0' || id[end] == ' ') {
				continue;
			}
			break;
		}
		
		this.id = new String(id, 0, end + 1);
	}
	/**
	 * 块版本号
	 */
	int version;
	/**
	 * 块大小 (字节)
	 */
	int size;
	
	/**
	 * <p>该文件块在整个文件数据的位置. 即该块的 [0] 号单位在整个文件数据的索引值.
	 * <p>这个值有助于在检查到数据错误时, 能够在抛出错误时指定错误的位置
	 * </p>
	 * @since v0.2.5
	 */
	int blockOffset;
	
	public void setSize(int size) {
		this.size = size;
		bs = new byte[size];
	}
}