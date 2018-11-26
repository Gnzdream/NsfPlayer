package zdream.nsfplayer.nsf.device.memory;

import zdream.nsfplayer.nsf.audio.NsfAudio;
import zdream.nsfplayer.nsf.device.IDevice;
import zdream.nsfplayer.nsf.device.cpu.IntHolder;

/**
 * 模拟 NES 的虚拟内存.
 * <p>开始的时候设置 0x10000 个 bytes 的内存.
 * @author Zdream
 */
public class NesMem implements IDevice {
	
	protected byte[] image;
	
	/**
	 * 只与 FDS 芯片相关
	 */
	protected boolean fdsEnable = false;
	
	public NesMem() {
		this.image = new byte[0x10000];
	}

	@Override
	public void reset() {
		for (int i = 0; i < 0x800; i++) {
			image[i] = (byte) 0;
		}
		fdsEnable = false;
	}
	
	/**
	 * 将数据放入虚拟内存
	 * @param data
	 *   NSF 除去头部之后的所有数据
	 * @param offset
	 *   就是 {@link NsfAudio#load_address}
	 */
	public final boolean setImage(byte[] data, int offset, int size) {
		if (offset + size < 0x10000) {
			System.arraycopy(data, 0, image, offset, size);
		} else {
			int length = 0x10000 - offset;
			System.arraycopy(data, 0, image, offset, length);
		}
		return true;
	}

	@Override
	public boolean write(int addr, int value, int id) {
		if (0x0000 <= addr && addr < 0x2000) {
			image[addr & 0x7ff] = (byte) (value & 0xff);
			return true;
		}
		if (0x6000 <= addr && addr < 0x8000) {
			image[addr] = (byte) (value & 0xff);
			return true;
		}
		if (0x4100 <= addr && addr < 0x4110) {
			image[addr] = (byte) (value & 0xff);
			return true;
		}
		if (fdsEnable && 0x8000 <= addr && addr < 0xe000) {
			image[addr] = (byte) (value & 0xff);
		}
		return false;
	}

	@Override
	public boolean read(int addr, IntHolder val, int id) {
		if (0x0000 <= addr && addr < 0x2000) {
			val.val = image[addr & 0x7ff] & 0xff;
			return true;
		}
		if (0x4100 <= addr && addr < 0x4110) {
			val.val = image[addr] & 0xff;
			return true;
		}
		if (0x6000 <= addr && addr < 0x10000) {
			val.val = image[addr] & 0xff;
			return true;
		}
		return false;
	}
	
	/**
	 * 复制内存的数据至 bs 数组中
	 * @param bs
	 *   盛放数据的数组
	 * @param offset
	 *   bs 的 offset
	 * @param length
	 *   复制的数据个数
	 * @param address
	 *   内存的复制起点位置
	 * @since v0.2.4
	 */
	public void read(byte[] bs, int offset, int length, int address) {
		System.arraycopy(image, address, bs, offset, length);
	}
	
	public final void setFDSMode(boolean t) {
		fdsEnable = t;
	}

}
