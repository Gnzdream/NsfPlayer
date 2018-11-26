package zdream.nsfplayer.nsf.device.cpu;

import zdream.nsfplayer.nsf.device.IDevice;
import zdream.nsfplayer.nsf.device.cpu.K6502Context.ReadHandler;
import zdream.nsfplayer.nsf.device.cpu.K6502Context.WriteHandler;

/**
 * 模拟 NES 的 CPU
 * @author Zdream
 */
public class NesCPU implements IDevice {
	
	/**
	 * bits of fixed point for timing
	 * 16 causes overflow at low update rate values (~27 Hz)
	 * 15 should be sufficient for any NSF (~13.6 Hz), since the format only allows down to ~15.25 Hz
	 * 14 used here just for one extra bit of safety
	 */
	public static final int FRAME_FIXED = 14;
	
	protected int int_address;
	protected final K6502Context context = new K6502Context();
	protected boolean breaked;
	protected int clock_per_frame;
	protected int clock_of_frame;
	protected int frame_quarter;
	protected int breakpoint;
	protected IDevice bus;
	
	protected int pc_count = 0;
	
	public int NES_BASECYCLES;
	
	/**
	 * 默认初始化. 没有设定 NES_BASECYCLES, 它是没办法工作的
	 */
	public NesCPU() {
		this(-1);
	}
	
	public NesCPU(int clock) {
		NES_BASECYCLES = clock;
		bus = null;
		
		context.readByte = readByte;
		context.writeByte = writeByte;
	}
	
	protected void startup (int address) {
		breaked = false;
		context.pc = 0x4100;
		breakpoint = context.pc + 3;
		context.p = 0x26;                 // IRZ
		assert (bus != null);
		bus.write(context.pc + 0, 0x20, 0); // JSR
		bus.write(context.pc + 1, address & 0xff, 0);
		bus.write(context.pc + 2, address >> 8, 0);
		bus.write(context.pc + 3, 0x4c, 0); // JMP 04103H
		bus.write(context.pc + 4, breakpoint & 0xff, 0);
		bus.write(context.pc + 5, breakpoint >> 8, 0);
	}
	
	public int exec(int clock) {
		context.clock = 0;

		while ( context.clock < clock ) {
			if (!breaked) {
				pc_count++;
				context.exec();
				if (context.pc == breakpoint)
			        breaked = true;
			} else {
				if ((clock_of_frame >> FRAME_FIXED) < clock)
					context.clock = (clock_of_frame >> FRAME_FIXED) + 1;
				else
					context.clock = clock;
			}
			
			// wait for interrupt
			if ((clock_of_frame >> FRAME_FIXED) < context.clock) {
				if (breaked) {
					beforeStartUp();
					startup(int_address);
				}
				clock_of_frame += clock_per_frame;
			}
		}
		
		clock_of_frame -= (context.clock << FRAME_FIXED);

		return context.clock; // 返回实际运行的时钟数
	}
	
	public void setMemory (IDevice b) {
		bus = b;
	}

	@Override
	public boolean write(int adr, int val, int id) {
		if (bus != null)
			return bus.write(adr, val, id);
		else
			return false;
	}

	@Override
	public boolean read(int adr, IntHolder val, int id) {
		if (bus != null) {
			boolean result = bus.read (adr, val, id);
			// DEBUG OUT here
			return result;
		} else
			return false;
	}

	@Override
	public void setOption(int id, int value) {}

	@Override
	public void reset() {
		// KM6502 的重置
		context.readByte = readByte;
		context.writeByte = writeByte;
		context.iRequest = K6502Context.K6502_INIT;
		context.clock = 0;
		context.a = 0;
		context.x = 0;
		context.y = 0;
		context.s = 0xff;
		context.pc = breakpoint = 0xffff;
		context.illegal = 0;
		breaked = false;
		context.exec();
	}
	
	/**
	 * 
	 * @param start_adr
	 * @param int_adr
	 * @param int_freq
	 *   帧数, 默认值 60
	 * @param a
	 *   CPU 寄存器 A 初始值, 默认值 0
	 * @param x
	 *   CPU 寄存器 X 初始值, 默认值 0
	 * @param y
	 *   CPU 寄存器 Y 初始值, 默认值 0
	 */
	public void start(int start_adr, int int_adr, double int_freq, int a, int x, int y) {
		// approximate frame timing as an integer number of CPU clocks
		int_address = int_adr;
		clock_per_frame = (int) ((long) (1 << FRAME_FIXED) * NES_BASECYCLES / int_freq);
		clock_of_frame = 0;

		// count clock quarters
		frame_quarter = 3;

		onInit(a, x, y);

		context.a = a;
		context.x = x;
		context.y = y;
		startup(start_adr);

		for (int i = 0; (i < (NES_BASECYCLES / int_freq)) && !breaked; i++, context.exec()) {
			pc_count++;
			if (context.pc == breakpoint)
				breaked = true;
		}

		clock_of_frame = 0;
	}
	
	public final int getPC() {
		return context.pc;
	}
	
	ReadHandler readByte = new ReadHandler() {
		
		@Override
		public final int handler(int adr) {
			IntHolder val = new IntHolder(0);
			read(adr, val, 0);
			return val.val;
		}
	};
	
	WriteHandler writeByte = new WriteHandler() {
		
		@Override
		public void handler(int adr, int value) {
			write(adr, value, 0);
		}
	};
	
	/**
	 * 用于子类继承
	 * @since v0.2.9
	 */
	protected void beforeStartUp() {}
	
	/**
	 * 用于子类继承
	 * @since v0.2.9
	 */
	protected void onInit(int a, int x, int y) {}
}
