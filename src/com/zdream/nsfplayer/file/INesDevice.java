package com.zdream.nsfplayer.file;

public interface INesDevice {
	
	/**
	 * 将数据放入虚拟内存
	 * @param data
	 *   数据源
	 * @param offset
	 *   前面多少位置被 skip
	 * @param size
	 *   数据量
	 * @return
	 *   是否成功
	 */
	public boolean setImage(byte[] data, int offset, int size);

}
