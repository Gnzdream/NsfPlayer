package com.zdream.nsfplayer.xgm.device;

import java.util.Iterator;

/**
 * 图层
 * 
 * <p>与 Bus 类似, 但不把读写的工作也不让全设备传播.</p>
 * 
 * @author Zdream
 *
 */
public class Layer extends Bus {
	
	/**
	 * 数据写入
	 * <p>对安装在总线的设备，进行 <code>write()</code> 操作.
	 * 调用次序就是设备所安装的顺序.
	 * 如果其中一个设备写入成功后直接 <code>return</code></p>
	 * @param id 在这个方法中会被忽略
	 */
	public boolean write(int addr, int value, int id) {
		boolean ret;
		for (Iterator<IDevice> it = vd.iterator(); it.hasNext();) {
			IDevice d = it.next();
			ret = d.write(addr, value, 0);
			if (ret) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 数据读取
	 * <p>对安装在总线的设备，进行 <code>read()</code> 操作.
	 * 调用次序就是设备所安装的顺序.
	 * 如果其中一个设备读取成功后直接 <code>return</code></p>
	 * @param id 在这个方法中会被忽略
	 */
	public boolean read(int adr, IntHolder val, int id) {
		val.val = 0;
		for (Iterator<IDevice> it = vd.iterator(); it.hasNext();) {
			IDevice d = it.next();
			if (d.read(adr, val, 0))
				return true;
		}
		return false;
	}

}
