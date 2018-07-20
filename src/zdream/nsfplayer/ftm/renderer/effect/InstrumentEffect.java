package zdream.nsfplayer.ftm.renderer.effect;

import zdream.nsfplayer.ftm.renderer.FamiTrackerRuntime;

/**
 * 修改乐器的效果
 * 
 * @author Zdream
 * @since 0.2.1
 */
public class InstrumentEffect implements IFtmEffect {
	
	public int inst;

	private InstrumentEffect(int inst) {
		this.inst = inst;
	}

	@Override
	public FtmEffectType type() {
		return FtmEffectType.INSTRUMENT;
	}
	
	/**
	 * 形成一个修改乐器的效果
	 * @param inst
	 *   乐器号码
	 * @return
	 *   效果实例
	 */
	public static InstrumentEffect of(int inst) {
		return new InstrumentEffect(inst);
	}
	
	@Override
	public void execute(byte channelCode, FamiTrackerRuntime runtime) {
		runtime.channels.get(channelCode).instrument = inst;
	}
	
	@Override
	public String toString() {
		return "Inst:" + Integer.toHexString(inst);
	}

}
