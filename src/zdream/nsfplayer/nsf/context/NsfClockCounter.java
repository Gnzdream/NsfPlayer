package zdream.nsfplayer.nsf.context;

import zdream.nsfplayer.core.FloatCycleCounter;
import zdream.nsfplayer.core.IResetable;
import zdream.nsfplayer.nsf.renderer.NsfParameter;

/**
 * <p>NSF 速率转换器
 * <p>用于计算与时钟相关的数据, 像每个采样需要走多少时钟数这类数据.
 * 它会在每帧开始时, 通过每秒时钟数、播放速率等情况,
 * 最终计算出该采样对应的时钟数总量, 并将其写到 {@link NsfParameter} 中.
 * <p>它是 NSF 工作环境下的一个工具.
 * </p>
 * 
 * @author Zdream
 * @since v0.2.9
 */
public class NsfClockCounter implements IResetable {
	
	final NsfParameter param;
	
	public NsfClockCounter(NsfParameter param) {
		this.param = param;
		this.cpuCounter = new FloatCycleCounter();
		this.apuCounter = new FloatCycleCounter();
	}
	
	/* **********
	 * 时钟参数 *
	 ********** */
	
	private final FloatCycleCounter cpuCounter;
	private final FloatCycleCounter apuCounter;
	
	/* **********
	 * 公共方法 *
	 ********** */

	@Override
	public void reset() {
		cpuCounter.reset();
		apuCounter.reset();
	}
	
	/**
	 * <p>仅重置 APU 的参数
	 * <p>当播放速度发生变化时需要调用该函数
	 * </p>
	 */
	public void onAPUParamUpdate() {
		int cycle = countCycle();
		apuCounter.setParam(cycle, param.sampleRate);
	}
	
	/**
	 * <p>直接传入采样率和每秒时钟数, 使该类重置. APU 和 CPU 的计数器均会重置.
	 * <p>当音频重载的时候需要调用该函数
	 * </p>
	 * @param frameRate
	 *   采样率, 默认 48000
	 * @param freqPerSec
	 *   每秒时钟数
	 */
	public void onParamUpdate(int sampleRate, int freqPerSec) {
		param.sampleRate = sampleRate;
		param.freqPerSec = freqPerSec;
		
		cpuCounter.setParam(param.freqPerSec, sampleRate);
		int cycle = countCycle();
		apuCounter.setParam(cycle, sampleRate);
	}
	
	/**
	 * 计算本帧的与时钟相关的数据, 并写入 {@link NsfParameter} 中.
	 */
	public void doConvert() {
		int cpuClockInCurSample = cpuCounter.tick();
		param.cpuClockInCurSample = cpuClockInCurSample;
		
		int apuClockInCurSample = apuCounter.tick();
		param.apuClockInCurSample = apuClockInCurSample;
	}
	
	/* **********
	 * 私有方法 *
	 ********** */
	
	private int countCycle() {
		int cycle = param.freqPerSec;
		if (param.speed != 1 && param.speed > 0) {
			cycle = (int) (cycle / param.speed);
		}
		return cycle;
	}

}
