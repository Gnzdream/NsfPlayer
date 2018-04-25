package zdream.nsfplayer.ftm.document;

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
		
		this.id = new String(id, 0, end);
	}
	/**
	 * 块版本号
	 */
	int version;
	/**
	 * 块大小 (字节)
	 */
	int size;
	
	public void setSize(int size) {
		this.size = size;
		bs = new byte[size];
	}
}