package zdream.nsfplayer.nsf.device;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import zdream.nsfplayer.xgm.device.IntHolder;

/**
 * 能够安装多个设备的总线, 提供复位, 写入, 读取等动作
 * @author Zdream
 */
public class Bus implements IDevice, Iterable<IDevice> {
	
	protected final List<IDevice> vd = new ArrayList<IDevice>();

	/**
	 * 对所有安装在总线的设备，都进行 <code>reset()</code> 操作.
	 * 调用次序就是设备所安装的顺序.
	 */
	public void reset() {
		for (Iterator<IDevice> it = vd.iterator(); it.hasNext();) {
			IDevice d = it.next();
			d.reset();
		}
	}
	
	/**
	 * 卸载所有安装在总线上的设备
	 */
	public void detachAll() {
		vd.clear();
	}
	
	/**
	 * 安装设备
	 * <p>在这个总线上安装装置.</p>
	 * @param d
	 *   要安装在总线上的设备
	 */
	public void attach(IDevice d) {
		if (d != null) {
			vd.add(d);
		}
	}

	/**
	 * 数据写入
	 * <p>对所有安装在总线的设备，都进行 <code>write()</code> 操作.
	 * 调用次序就是设备所安装的顺序.</p>
	 * @param id 在这个方法中会被忽略
	 */
	public boolean write(int addr, int value, int id) {
		boolean ret = false;
		for (Iterator<IDevice> it = vd.iterator(); it.hasNext();) {
			IDevice d = it.next();
			d.write(addr, value, 0);
		}
		return ret;
	}

	/**
	 * 数据读取
	 * <p>对所有安装在总线的设备，都进行 <code>write()</code> 操作.
	 * 调用次序就是设备所安装的顺序.</p>
	 * @return
	 *   当安装在该总线的所有的设备都正常读取, 返回 true
	 */
	public boolean read(int adr, IntHolder val, int id) {
		boolean ret = false;
		IntHolder vtmp = new IntHolder(0);
		
		val.val = 0;
		for (Iterator<IDevice> it = vd.iterator(); it.hasNext();) {
			IDevice d = it.next();
			
			if (d.read(adr, vtmp, 0)) {
				val.val |= vtmp.val;
				ret = true;
			}
		}
		return ret;
	}

	@Override
	public Iterator<IDevice> iterator() {
		return vd.iterator();
	}

}
