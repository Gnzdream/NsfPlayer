package zdream.nsfplayer.ftm.executor.tools;

public class VibratoTable {
	
	private static final double[] VIBRATO_DEPTH = {
			0, 1.5, 2.5, 4.0, 5.0, 7.0, 10.0, 12.0, 14.0, 17.0, 22.0, 30.0, 44.0, 64.0, 96.0, 128.0
	};
	
	/**
	 * [depth][phase]
	 */
	private static final short[][] VIBRATO_TABLE = new short[16][16];
	
	static {
		for (int i = 1; i < VIBRATO_DEPTH.length; ++i) {	// depth, 忽略 0
			for (int j = 0; j < 16; ++j) {	// phase
				short value = 0;
				double angle = (j / 16.0) * (Math.PI / 2.0);
				value = (short) (Math.sin(angle) * VIBRATO_DEPTH[i] * 8);
				VIBRATO_TABLE[i][j] = value;
			}
		}
	}
	
	/**
	 * 返回颤音所在正弦波的值
	 * @param depth
	 *   深度数值 (4xy 的 x, 不等于振幅大小) 范围 [1, 15]
	 * @param phase
	 *   所在周期数. 范围全整数系列.
	 *   其中 [0, 15] 代表着均匀分布在 [0度, 90度] 区间内的 16 个采样
	 * @return
	 */
	public static int vibratoValue(int depth, int phase) {
		return vibratoValue0(depth, phase) / 8;
	}
	
	private static int vibratoValue0(int depth, int phase) {
		int x = (phase < 0) ? phase * -1 + 32 : phase;
		boolean negative = false;
		x %= 64;
		
		// 特殊值
		
		switch (x) {
		case 0: case 32:
			return 0;
		case 16:
			return (int) (VIBRATO_DEPTH[depth] * 8);
		case 48:
			return (int) (VIBRATO_DEPTH[depth] * -8);
		}
		
		// 其它
		if (x < 16) {
			// do nothing
		} else if (x < 32) {
			x = 32 - x;
		} else if (x < 48) {
			negative = true;
			x = x - 32;
		} else {
			negative = true;
			x = 64 - x;
		}
		
		int ret = VIBRATO_TABLE[depth][x];
		return (negative) ? -ret : ret;
	}
	
	/**
	 * 返回较为精确的颤音所在正弦波的值
	 * @param depth
	 *   深度数值 (4xy 的 x, 不等于振幅大小) 范围 [1, 16]
	 * @param phase
	 *   所在周期数. 范围全实数范围.
	 *   其中 [0, 16) 代表着均匀分布在 [0度, 90度) 区间内的采样
	 * @return
	 */
	public static int accurateVibratoValue(int depth, double phase) {
		int i = (int) Math.floor(phase);
		int inext = i + 1;
		
		int a = vibratoValue0(depth, i);
		int b = vibratoValue0(depth, inext);
		
		return (int) (a * (inext - phase) + b * (phase - i)) / 8;
	}
	
}
