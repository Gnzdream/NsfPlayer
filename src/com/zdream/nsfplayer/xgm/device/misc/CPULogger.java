package com.zdream.nsfplayer.xgm.device.misc;

import java.io.FileWriter;
import java.io.IOException;

import com.zdream.nsfplayer.nsf.device.IDevice;
import com.zdream.nsfplayer.xgm.device.IntHolder;
import com.zdream.nsfplayer.xgm.device.cpu.NesCPU;
import com.zdream.nsfplayer.xgm.player.nsf.NsfAudio;

/**
 * <b>log_level</b><br>
 * 0 - no logging<br>
 * 1 - log APU writes only<br>
 * 2 - log all writes<br>
 * 3 - log all writes and reads<br>
 * 4 - log only bank register writes<br>
 * 
 * @author Zdream
 */
public class CPULogger implements IDevice {
	
	protected int log_level;
	protected int soundchip; // unsigned
	// protected File file; // FILE* 这个是输出日志的文件
	protected String filename;
	protected int frame_count; // unsigned
	protected NesCPU cpu;
	protected NsfAudio nsf;
	protected int[] bank = new int[8]; // unsigned
	
	// 补充
	FileWriter writer;
	
	@Override
	protected void finalize() throws Throwable {
		if (writer != null)
		try {
			writer.close();
		} catch (Exception e) { } finally {
			writer = null;
		}
	}

	@Override
	public void reset() { }

	@Override
	public boolean write(int adr, int val, int id) {
		if (log_level < 1) return false;
	    if (log_level < 2) {
	        boolean apu = false;
	        if (adr >= 0x4000 && adr <= 0x4013) apu = true;
	        if (adr == 0x4015)                  apu = true;
	        if (adr == 0x4017)                  apu = true;
	        if ((soundchip &  1) != 0 && adr >= 0x9000 && adr <= 0x9003) apu = true; // vrc6
	        if ((soundchip &  1) != 0 && adr >= 0xA000 && adr <= 0xA002) apu = true; // vrc6
	        if ((soundchip &  1) != 0 && adr >= 0xB000 && adr <= 0xB002) apu = true; // vrc6
	        if ((soundchip &  2) != 0 && adr == 0x9010)                  apu = true; // vrc7
	        if ((soundchip &  2) != 0 && adr == 0x9030)                  apu = true; // vrc7
	        if ((soundchip &  4) != 0 && adr >= 0x4040 && adr <= 0x4092) apu = true; // fds
	        if ((soundchip &  8) != 0 && adr >= 0x5000 && adr <= 0x5013) apu = true; // mmc5
	        if ((soundchip &  8) != 0 && adr == 0x5015)                  apu = true; // mmc5
	        if ((soundchip & 16) != 0 && adr == 0x4800)                  apu = true; // n163
	        if ((soundchip & 16) != 0 && adr == 0xF800)                  apu = true; // n163
	        if ((soundchip & 32) != 0 && adr == 0xC000)                  apu = true; // 5b
	        if ((soundchip & 32) != 0 && adr == 0xE000)                  apu = true; // 5b
	        if (!apu) return false;
	    }
	    try {
			if (log_level == 4 && writer != null) {
				// log bank switching
				if (adr >= 0x5FF8 && adr <= 0x5FFF) {
					// log PC when bank switching
					if (cpu != null) {
						int pc = cpu.getPC(); // unsigned
						int b = 0xFF; // unsigned
						if (nsf != null && pc >= 0x8000)
							b = bank[(pc - 0x8000) / 0x1000];
						writer.append(String.format("SWITCH_PC(%04X,%02X)\n", pc, b));
					}
					writer.append(String.format("BANK(%1X000,%02X)\n", (adr - 0x5FF8) + 8, val));
					return false;
				}
				// only log RAM writes
				else if (adr >= 0x8000)
					return false;
				else if (adr >= 0x0800 && adr < 0x6000)
					return false;
			}
			if (writer != null) {
				writer.append(String.format("WRITE(%04X,%02X)\n", adr, val));
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		return false;
	}

	@Override
	public boolean read(int adr, IntHolder val, int id) {
		if (log_level == 4) { // only RAM reads
	        if (adr >= 0x8000) return false;
	        if (adr >= 0x800 && adr < 0x6000) return false;
	    }
	    if (log_level > 2 && writer != null) {
	    	try {
	    		writer.append(String.format("READ(%04X)\n", adr));
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
	    }
	    return false;
	}

	@Override
	public void setOption(int id, int value) {
		switch (id) {
	    case 0:
	        log_level = value;
	        break;
	    default:
	        break;
	    }
	}
	
	public final int getLogLevel() {
	    return log_level;
	}
	
	/**
	 * @param soundchip_
	 *   unsigned byte
	 */
	public final void setSoundchip(int soundchip_) {
	    soundchip = soundchip_;
	}
	
	public final void setFilename(String filename_) {
		if (this.writer != null) {
			try {
				writer.close();
			} catch (Exception e) {}
			this.writer = null;
		}
		
		this.filename = filename_;
	}

	public final void begin(String title) {
	    if (this.writer != null) {
	    	try {
				writer.close();
			} catch (Exception e) {}
			this.writer = null;
	    }
	    try {
			if (filename != null) {
				writer = new FileWriter(filename);
			}

			if (writer != null) {
				writer.append(String.format("BEGIN(\"%s\")\n", title));
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	    frame_count = 0;
	}

	public final void init(int reg_a, int reg_x) {
		if (writer != null) {
			try {
				if (log_level == 4 && nsf != null) {
					for (int i = 0; i < 8; ++i) {
						bank[i] = nsf.bankswitch[i];

						writer.append(String.format("BANK(%1X000,%02X)\n", i + 8, bank[i]));
					}
				}
				writer.append(String.format("INIT(%02X,%02X)\n", reg_a, reg_x));
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}
	}

	public final void play() {
		if (writer != null && (log_level != 4 || frame_count == 0)) {
			try {
				writer.append(String.format("PLAY(%d)\n", frame_count));
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}
	    ++frame_count;
	}
	
	public final void setCPU (NesCPU c) {
	    cpu = c;
	}

	public final void setNSF (NsfAudio n) {
	    nsf = n;
	}

}
