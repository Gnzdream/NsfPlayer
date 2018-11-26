package zdream.nsfplayer.nsf.device;

import zdream.nsfplayer.core.IResetable;
import zdream.nsfplayer.nsf.device.cpu.IntHolder;

/**
 * 用仪器、设备使用的装置的抽象
 * @author Zdream
 */
public interface IDevice extends IResetable {
	
	/**
	 * 数据写入
	 * @param adr
	 *   地址
	 * @param val
	 *   填写值
	 * @param id
	 *   设备识别信息.
	 *   一个设备复数的 IO 支持的时候等
	 *   这个值通常为 0
	 * @return
	 *   成功时 true, 失败时 false
	 */
	public boolean write(int adr, int val, int id);
	
	/**
	 * 从设备中读取数据
	 * @param adr
	 *   地址
	 * @param val
	 *   参数. 如果读到数据的话改写这个参数.
	 * @param id
	 *   设备识别信息.
	 *   一个设备复数的 IO 支持的时候等
	 *   这个值通常为 0
	 * @return
	 *   收到的值. 当无法读取或者读取失败时返回 null
	 *   成功时 true, 失败时 false
	 */
	public boolean read(int adr, IntHolder val, int id);
	
	/**
	 * 选项参数设置
	 * @param id
	 * @param value
	 */
	default void setOption(int id, int value) {
		// do nothing
	}
	
}
