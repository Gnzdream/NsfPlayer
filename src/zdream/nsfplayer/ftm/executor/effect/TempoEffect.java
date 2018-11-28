package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;

/**
 * <p>修改速度 (tempo) 的效果, Fxx
 * <p>属全局效果
 * </p>
 * 
 * @author Zdream
 * @since 0.2.1
 */
public class TempoEffect implements IFtmEffect {
	
	public final int tempo;

	private TempoEffect(int tempo) {
		this.tempo = tempo;
	}

	@Override
	public FtmEffectType type() {
		return FtmEffectType.TEMPO;
	}
	
	/**
	 * 形成一个修改节奏值的效果
	 * @param tempo
	 *   节奏值. 节奏值必须是正数
	 * @return
	 *   效果实例
	 * @throws IllegalArgumentException
	 *   当节奏值 <code>tempo</code> 不在指定范围内时
	 */
	public static TempoEffect of(int tempo) throws IllegalArgumentException {
		if (tempo <= 0) {
			throw new IllegalArgumentException("音量必须是 0 - 15 之间的整数数值");
		}
		return new TempoEffect(tempo);
	}
	
	@Override
	public void execute(byte channelCode, FamiTrackerRuntime runtime) {
		runtime.fetcher.setTempo(tempo);
	}
	
	@Override
	public String toString() {
		return "Tempo:" + tempo;
	}

}
