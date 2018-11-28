package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.ftm.executor.AbstractFtmChannel;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;

/**
 * 噪声轨道专用, 修改噪声值的效果
 * 
 * @author Zdream
 * @since 0.2.2
 */
public class NoiseEffect implements IFtmEffect {
	
	/**
	 * 噪声值
	 */
	public final int noise;

	private NoiseEffect(int noise) {
		this.noise = noise;
	}

	@Override
	public FtmEffectType type() {
		return FtmEffectType.NOTE;
	}
	
	/**
	 * 形成一个修改噪声值的效果
	 * @param noise
	 *   噪声值. 必须在 [1, 16] 范围内, 0 是非法值
	 * @return
	 *   效果实例
	 * @throws IllegalArgumentException
	 *   当 <code>noise</code> 不在指定范围内时
	 */
	public static NoiseEffect of(int noise) throws IllegalArgumentException {
		if (noise > 16 || noise < 1) {
			throw new IllegalArgumentException("噪音值必须是 1 - 16 之间的整数数值");
		}
		return new NoiseEffect(noise);
	}
	
	@Override
	public void execute(byte channelCode, FamiTrackerRuntime runtime) {
		AbstractFtmChannel ch = runtime.channels.get(channelCode);
		
		ch.setMasterNote(noise);
		ch.turnOn();
	}
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("Noise:");
				
		switch (noise) {
		case 1: b.append("0-#"); break;
		case 2: b.append("1-#"); break;
		case 3: b.append("2-#"); break;
		case 4: b.append("3-#"); break;
		case 5: b.append("4-#"); break;
		case 6: b.append("5-#"); break;
		case 7: b.append("6-#"); break;
		case 8: b.append("7-#"); break;
		case 9: b.append("8-#"); break;
		case 10: b.append("9-#"); break;
		case 11: b.append("A-#"); break;
		case 12: b.append("B-#"); break;
		case 13: b.append("C-#"); break;
		case 14: b.append("D-#"); break;
		case 15: b.append("E-#"); break;
		case 16: b.append("F-#"); break;
		}
		
		return b.toString();
	}

}
