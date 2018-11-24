package zdream.nsfplayer.xgm.device.misc;

import zdream.nsfplayer.nsf.device.cpu.NesCPU;

public class RawNesCPU extends NesCPU {

	public RawNesCPU() {
		super();
	}

	public RawNesCPU(int clock) {
		super(clock);
	}

	protected CPULogger log_cpu;
	
	@Override
	protected void beforeStartUp() {
		if (log_cpu != null)
			log_cpu.play();
	}
	
	@Override
	protected void onInit(int a, int x, int y) {
		if (log_cpu != null)
			log_cpu.init(a, x);
	}
	
	public final void setLogger(CPULogger logger) {
		this.log_cpu = logger;
	}

}
