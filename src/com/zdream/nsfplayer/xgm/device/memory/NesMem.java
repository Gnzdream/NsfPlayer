package com.zdream.nsfplayer.xgm.device.memory;

import com.zdream.nsfplayer.xgm.device.IDevice;
import com.zdream.nsfplayer.xgm.device.IntHolder;
import com.zdream.nsfplayer.xgm.player.nsf.NsfAudio;

/**
 * 模拟 NES 的虚拟内存
 * @author Zdream
 */
public class NesMem implements IDevice {
	
	protected byte[] image;
	protected boolean fdsEnable = false;
	
	public NesMem() {
		// do nothing
	}

	@Override
	public void reset() {
		for (int i = 0; i < 0x800; i++) {
			image[i] = (byte) 0;
		}
	}
	
	/**
	 * 将数据放入虚拟内存
	 * @param data
	 *   NSF 除去头部之后的所有数据
	 * @param offset
	 *   就是 {@link NsfAudio#load_address}
	 */
	public final boolean setImage(byte[] data, int offset, int size) {
		this.image = new byte[0x10000];
		if (offset + size < 0x10000) {
			for (int i = 0; i < size; i++) {
				this.image[i + offset] = data[i];
			}
		} else {
			int length = 0x10000 - offset;
			for (int i = 0; i < length; i++) {
				this.image[i + offset] = data[i];
			}
		}
		return true;
	}

	@Override
	public boolean write(int addr, int value, int id) {
		// System.out.println(String.format("write %d->index:%d  (%d)", value, addr, id));
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
		// System.out.println("write fail");
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
	
	public final void setFDSMode(boolean t) {
		fdsEnable = t;
	}

	@Override
	public void setOption(int id, int value) {
		// do nothing
	}

}
