package zdream.nsfplayer.xgm.device.memory;

import java.util.Arrays;

import zdream.nsfplayer.nsf.device.IDevice;
import zdream.nsfplayer.xgm.device.IntHolder;

/**
 * 4 KB * 16 的空间
 * @author Zdream
 */
public class NesBank implements IDevice {
	
	/**
	 * 注意 banks 中每一个元素都是指向 image 的某个地址（索引）
	 * 和 cpp 文件中的指针意义不相同
	 */
	int[] bank = new int[256];
	byte[] image;
	byte[] nullBank = new byte[0x1000];
	
	int[] bankswitch = new int[16];
	int[] bankdefault = new int[16];
	
	boolean fdsEnable = false;
	int bankMax;
	
	/**
	 * <p>每块 bank 的大小.</p>
	 * bank 是一个存储单元, 它在 NES 机器里面有多个, 用于放入卡带的镜像数据.
	 * 因为每个 bank 大小固定, 而且卡带的镜像数据通常大于 bank 大小,
	 * 因此需要多个 bank 来存储.<br>
	 * 
	 * 利用 bank 进行对镜像数据的获取公式是:<br>
	 * 第 x 块 bank 上第 y 个数据: bank[x][y] = image[BANK_BYTES_IN_IMAGE * x + y];
	 */
	public static final int BANK_BYTES_IN_IMAGE = 0x1000;
	
	public NesBank() {}
	
	public void setBankDefault(int bank, int value) {
		bankdefault[bank] = value;
	}
	
	public boolean setImage(byte[] data, int offset, int size) {
		// 开始的 bankDefault 值都是无效的 -1
		Arrays.fill(bankdefault, -1);

		int totalSize = ((offset & 0xfff) + data.length);
		bankMax = (totalSize >> 12); // count of full banks
		
		if ((totalSize & 0xfff) != 0) {
			bankMax += 1; // include last partial bank
		}
		if (bankMax > 256) {
			return false;
		}

		image = new byte[0x1000 * bankMax];
		int idx = offset & 0xfff;
		System.arraycopy(data, 0, image, idx, size);
		
		for (int i = 0; i < bankMax; i++) {
			bank[i] = (BANK_BYTES_IN_IMAGE * i);
		}
		for (int i = bankMax; i < bank.length; i++) {
			bank[i] = -1; // 全部置为无效值
		}

		return true;
	}

	@Override
	public void reset() {
		Arrays.fill(nullBank, (byte) 0);
		for (int i = 0; i < 16; i++) {
			bankswitch[i] = bankdefault[i];
		}
	}

	@Override
	public boolean write(int addr, int value, int id) {
		if (0x5ff8 <= addr && addr < 0x6000) {
			bankswitch[(addr & 7) + 8] = value & 0xff;
			return true;
		}
		
		if (fdsEnable) {
			if (0x5ff6 <= addr && addr < 0x5ff8) {
				bankswitch[addr & 7] = value & 0xff;
				return true;
			}
	    }
		
		if (0 <= bankswitch[addr >> 12] && 0x6000 <= addr && addr < 0xe000) {
			int idx = bank[bankswitch[addr >> 12]];
			if (idx == -1) {
				return false;
			}
			image[idx + (addr & 0x0fff)] = (byte) (value & 0xff);
	        return true;
		}
		
		return false;
	}

	@Override
	public boolean read(int addr, IntHolder val, int id) {
		if (0x5ff8 <= addr && addr < 0x5fff) {
			val.val = bankswitch[(addr & 7) + 8];
			return true;
		}

		if (0 <= bankswitch[addr >> 12] && 0x8000 <= addr && addr < 0x10000) {
			int idx = bank[bankswitch[addr >> 12]];
			if (idx == -1) {
				val.val = 0;
			} else {
				val.val = (image[idx + (addr & 0xfff)] & 0xff); // 取出的数转化为正数
			}
			return true;
		}

		if (fdsEnable) {
			if (0x5ff6 <= addr && addr < 0x5ff8) {
				val.val = bankswitch[addr & 7];
				return true;
			}

			if (0 <= bankswitch[addr >> 12] && 0x6000 <= addr && addr < 0x8000) {
				int idx = bank[bankswitch[addr >> 12]];
				if (idx == -1) {
					val.val = 0;
				} else {
					val.val = (image[idx + addr & 0xfff] & 0xff); // 取出的数转化为正数
				}
				return true;
			}
		}

		return false;
	}

	@Override
	public void setOption(int id, int value) {
		// do nothing
	}
	
	public final void setFDSMode (boolean t) {
	    fdsEnable = t;
	}

}
