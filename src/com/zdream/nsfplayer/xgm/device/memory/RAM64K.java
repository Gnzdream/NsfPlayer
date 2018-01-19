package com.zdream.nsfplayer.xgm.device.memory;

import java.util.Arrays;

import com.zdream.nsfplayer.nsf.device.IDevice;
import com.zdream.nsfplayer.xgm.device.IntHolder;

public class RAM64K implements IDevice {
	
	public final byte[] image = new byte[0x10000];
	
	public RAM64K() {
		// do nothing
	}

	@Override
	public void reset() {
		Arrays.fill(image, (byte) 0);
	}
	
	public boolean setImage (byte[] data, int offset, int size) {
		if (offset + size < 0x10000) {
			System.arraycopy(data, 0, image, offset, size);
		} else {
			System.arraycopy(data, 0, image, offset, 0x10000 - size);
		}
		return true;
	}

	@Override
	public boolean write(int addr, int val, int id) {
		image[addr & 0xFFFF] = (byte) (val & 0xFF);
		return true;
	}

	@Override
	public boolean read(int addr, IntHolder val, int id) {
		val.val = image[addr & 0xFFFF] & 0xFF;
		return true;
	}

	@Override
	public void setOption(int id, int value) {
		// do nothing
	}

}
