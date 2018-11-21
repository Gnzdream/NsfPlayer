package zdream.nsfplayer.ftm.renderer.context;

import zdream.nsfplayer.core.CycleCounter;
import zdream.nsfplayer.core.IResetable;
import zdream.nsfplayer.ftm.renderer.FamiTrackerParameter;

/**
 * <p>速率转换器
 * <p>用于计算与时钟相关的数据, 像每帧需要走多少时钟数这类数据.
 * 它会在每帧开始时, 通过每秒时钟数、播放速率等情况,
 * 最终计算出该帧的时钟数总量, 并将其写到 {@link FamiTrackerParameter} 中.
 * <p>它是 FamiTracker 工作环境下的一个工具.
 * </p>
 * 
 * @author Zdream
 * @since v0.2.9
 */
public class RateConverter implements IResetable {
	
	final FamiTrackerParameter param;
	
	public RateConverter(FamiTrackerParameter param) {
		this.param = param;
		this.counter = new CycleCounter();
	}
	
	/* **********
	 * 时钟参数 *
	 ********** */
	
	private CycleCounter counter;
	
	/* **********
	 * 公共方法 *
	 ********** */

	@Override
	public void reset() {
		counter.reset();
	}
	
	/**
	 * <p>如果参数变化, 需要调用该方法, 使该类重置.
	 * <p>以下参数发生改变, 需要调用该方法:
	 * <li>帧率
	 * <li>NES CPU 的时钟数 (FTM 恒定)
	 * <li>播放速度
	 * </li>
	 * </p>
	 */
	public void onParamUpdate() {
		int cycle = countCycle();
		counter.setParam(cycle, param.frameRate);
	}
	
	/**
	 * 直接传入帧率, 使该类重置.
	 * @param frameRate
	 *   帧率, 默认 60
	 */
	public void onParamUpdate(int frameRate) {
		param.frameRate = frameRate;
		
		int cycle = countCycle();
		counter.setParam(cycle, frameRate);
	}
	
	/**
	 * 直接传入帧率和每秒时钟数, 使该类重置.
	 * @param frameRate
	 *   帧率, 默认 60
	 * @param freqPerSec
	 *   每秒时钟数
	 */
	public void onParamUpdate(int frameRate, int freqPerSec) {
		param.frameRate = frameRate;
		param.freqPerSec = freqPerSec;
		
		int cycle = countCycle();
		counter.setParam(cycle, frameRate);
	}
	
	/**
	 * 计算本帧的与时钟相关的数据, 并写入 {@link FamiTrackerParameter} 中.
	 */
	public void doConvert() {
		int freqPerFrame = counter.tick();
		param.freqPerFrame = freqPerFrame;
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
