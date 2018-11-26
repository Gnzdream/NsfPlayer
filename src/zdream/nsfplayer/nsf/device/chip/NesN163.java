package zdream.nsfplayer.nsf.device.chip;

import java.util.Arrays;

import zdream.nsfplayer.nsf.device.AbstractSoundChip;
import zdream.nsfplayer.nsf.device.DeviceManager;
import zdream.nsfplayer.nsf.device.cpu.IntHolder;
import zdream.nsfplayer.nsf.renderer.NsfRuntime;
import zdream.nsfplayer.sound.SoundN163;

/**
 * N163 音频芯片, 管理输出 1 到 8 个 N163 轨道的音频
 * 
 * @author Zdream
 * @since v0.2.6
 */
public class NesN163 extends AbstractSoundChip {
	
	private final SoundN163[] n163s = new SoundN163[8];
	
	/**
	 * 上面的 N163 发声器是否处于打开状态
	 */
	private final boolean[] ons = new boolean[8];
	
	/**
	 * 上面的 N163 发声器的音量包络在 reg[] 中读取的索引
	 */
	private final int[] offsets = new int[8];
	
	/**
	 * 记录相关部分的实际数据.
	 * n163s 中 8 个轨道的数据是倒着放的, 第 1 个轨道在最后
	 */
	private final byte[] reg = new byte[0x80];
	/**
	 * 8 个轨道的总开关
	 */
	private boolean masterDisable;
	
	private int regSelect;
	private boolean regAdvance;
	
	public boolean isMasterDisable() {
		return masterDisable;
	}

	public NesN163(NsfRuntime runtime) {
		super(runtime);
		
		// 无论如何, N163 的第一个轨道一定是有的
		n163s[0] = new SoundN163();
		n163s[0].step = 15;
		ons[0] = true;
	}

	@Override
	public boolean write(int adr, int val, int id) {
		if (adr == 0xE000) { // 主 disable 参数
			masterDisable = ((val & 0x40) != 0);
			return true;
		} else if (adr == 0xF800) { // 选择
			regSelect = (val & 0x7F);
			regAdvance = (val & 0x80) != 0;
			return true;
		} else if (adr == 0x4800) { // 写入
			handleWrite(val);
			if (regAdvance)
				regSelect = (regSelect + 1) & 0x7F;
			return true;
		}
		return false;
	}

	@Override
	public boolean read(int adr, IntHolder val, int id) {
		if (adr == 0x4800) { // 选择读
			val.val = handleRead();
			if (regAdvance)
				regSelect = (regSelect + 1) & 0x7F;
			return true;
		}
		return false;
	}

	@Override
	public void reset() {
		for (int i = 0; i < n163s.length; i++) {
			SoundN163 sound = n163s[i];
			if (sound != null && ons[i]) {
				sound.reset();
			}
		}
		Arrays.fill(reg, (byte) 0);
		Arrays.fill(offsets, 0);
	}
	
	/**
	 * 外部 (一般是 {@link DeviceManager}) 调用, 强制更新 N163 的轨道数.
	 * 不自动处理与 mixer 的连接
	 * @param num
	 *   总轨道数, 范围 [1, 8]
	 */
	public void forceChannelCount(int num) {
		this.writeChannelCount(num, false);
	}

	@Override
	public SoundN163 getSound(byte channelCode) {
		switch (channelCode) {
		case CHANNEL_N163_1:
		case CHANNEL_N163_2:
		case CHANNEL_N163_3:
		case CHANNEL_N163_4:
		case CHANNEL_N163_5:
		case CHANNEL_N163_6:
		case CHANNEL_N163_7:
		case CHANNEL_N163_8:
			int index = channelCode - CHANNEL_N163_1;
			if (ons[index]) {
				return n163s[index];
			}
		}
		return null;
	}

	@Override
	public byte[] getAllChannelCodes() {
		byte[] bs = new byte[8];
		int count = 0;
		for (int i = 0; i < bs.length; i++) {
			SoundN163 sound = n163s[i];
			if (sound != null && ons[i]) {
				bs[count++] = (byte) (CHANNEL_N163_1 + i);
			}
		}
		
		if (count == 8) {
			return bs;
		}
		
		return Arrays.copyOf(bs, count);
	}

	/* **********
	 * 处理写入 *
	 ********** */
	
	/**
	 * 处理写入的结果
	 * @param val
	 *   值, 范围 [0, 0xFF]
	 */
	private void handleWrite(int val) {
		if (regSelect <= 0x3F) {
			// 修改包络部分
			writeEnvelop(regSelect, (byte) val);
		} else {
			// 修改参数部分
			
			// 0x7F 既是第一个轨道设置的音量的地方, 也是设置总轨道数的地方
			if (regSelect == 0x7F) {
				writeChannelCount(((val >> 4) & 0x07) + 1, true);
			}
			// 上面说过, 在 reg[] 中, 轨道号是从高到低的
			int x = 7 - ((regSelect - 0x40) >> 3);
			writeParamToSound(x, regSelect & 7, val);
		}
		
		reg[regSelect] = (byte) val;
	}
	
	/**
	 * 设置总轨道数
	 * @param num
	 *   总轨道数, 范围 [1, 8]
	 * @param b
	 *   是否需要处理、更新和 mixer 的连接
	 */
	private void writeChannelCount(int num, boolean b) {
		int step = num * 15;
		
		for (int i = 0; i < n163s.length; i++) {
			boolean on = i < num;
			ons[i] = on;
			SoundN163 sound = n163s[i];
			if (on) {
				if (sound == null) {
					sound = n163s[i] = new SoundN163();
				}
				sound.step = step;
			} else {
				if (sound != null) {
					sound.step = 0;
					sound.reset();
				}
			}
		}
		
		if (b) {
			// 向 DeviceManager 报告现在的轨道数, 让它将 sound 和 mixer 相连
			getRuntime().manager.reattachN163(num);
		}
	}
	
	/**
	 * 设置各个发声器的参数
	 * @param x
	 *   发声器序号, 从 0 开始, 范围 [0, 7]
	 * @param address
	 *   地址, 范围 [0, 7]
	 * @param value
	 *   值, 范围 [0, 0xFF]
	 */
	private void writeParamToSound(int x, int address, int value) {
		SoundN163 sound = n163s[x];
		if (sound == null) {
			return;
		}
		
		switch (address) {
		case 0:
			sound.period = (sound.period & 0xFFF00) | (value & 0xFF);
			break;
		case 1:
			sound.phase = (sound.phase & 0xFFFF00) | (value & 0xFF);
			break;
		case 2:
			sound.period = (sound.period & 0xF00FF) | ((value & 0xFF) << 8);
			break;
		case 3:
			sound.phase = (sound.phase & 0xFF00FF) | ((value & 0xFF) << 8);
			break;
		case 4:
			sound.period = (sound.period & 0xFFFF) | ((value & 0x3) << 16);
			sound.length = 256 - (value & 0xFC);
			copyEnvelop(sound, this.offsets[x]);
			break;
		case 5:
			sound.phase = (sound.phase & 0xFFFF) | ((value & 0xFF) << 16);
			break;
		case 6:
			int offset = value & 0xFF;
			this.offsets[x] = offset;
			copyEnvelop(sound, offset);
			break;
		case 7:
			sound.volume = (value & 0xF);
			break;
		}
	}
	
	/**
	 * 将 reg[] 的音量包络数据整体拷贝到 sound.wave 中
	 */
	private void copyEnvelop(SoundN163 sound, int offset) {
		int len = Math.min(sound.length, (this.reg.length - offset) * 2);
		if (len > sound.wave.length) {
			len = sound.wave.length;
		}
		
		// TODO offset 为奇数, 一般不会发生, 但可能也是正常情况, 没有做
		int index = offset / 2; // 指向 this.reg
		
		// len 一定是 2 的倍数
		for (int i = 0; i < len;) {
			sound.wave[i++] = (byte) (this.reg[index] & 0xF); // 低位
			sound.wave[i++] = (byte) ((this.reg[index] >> 4) & 0xF); // 高位
			index++;
		}
	}
	
	/**
	 * 修改包络部分, 同时也修改各个 sound 中, 包含这个包络数据的对应包络部分 {@link SoundN163#wave}
	 */
	private void writeEnvelop(int address, byte value) {
		for (int i = 0; i < n163s.length; i++) {
			SoundN163 sound = n163s[i];
			if (sound == null || !ons[i]) {
				continue;
			}
			
			// TODO offset 为奇数的情况, 一般不会发生, 但可能也是正常情况, 没有做
			int offset = this.offsets[i] / 2;
			
			int length = sound.length;
			int index = (address - offset) * 2;
			
			if (index < length && index >= 0) {
				sound.wave[index++] = (byte) (value & 0xF); // 低位
				sound.wave[index] = (byte) ((value >> 4) & 0xF); // 高位
			}
		}
	}

	/* **********
	 * 处理读取 *
	 ********** */
	
	/**
	 * 处理读的结果
	 * @return
	 *   读的数据
	 */
	private int handleRead() {
		if (regSelect > 0x3F) {
			int x = 7 - ((regSelect - 0x40) >> 3);
			readParamFromSound(x, regSelect & 7);
		}
		
		return reg[regSelect] & 0xFF;
	}
	
	/**
	 * 从指定的发声器读取数据, 写回到 reg 数组中
	 * @param x
	 *   发声器序号, 从 0 开始, 范围 [0, 7]
	 * @param address
	 *   地址, 范围 [0, 7]
	 */
	private void readParamFromSound(int x, int address) {
		SoundN163 sound = n163s[x];
		if (sound == null) {
			return;
		}
		
		int value = 0;
		switch (address) {
		case 1:
			value = (sound.phase & 0xFF);
			break;
		case 3:
			value = (sound.phase & 0xFF00);
			break;
		case 5:
			value = (sound.phase & 0xFF0000);
			break;
		}
		
		reg[regSelect] = (byte) value;
	}

}
