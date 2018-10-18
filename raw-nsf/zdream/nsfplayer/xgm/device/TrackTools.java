package zdream.nsfplayer.xgm.device;

public class TrackTools {
	
	/**
	 * 将频率转换为音符编号. 0x60 是 o4c (中央 C)
	 * @param freq
	 * @return
	 *   0 为无效值
	 */
	public static int getNote(double freq) {
		final double LOG2_440 = 8.7813597135246596040696824762152;
		final double LOG_2 = 0.69314718055994530941723212145818;
		final int NOTE_440HZ = 0x69;

		if (freq > 1.0)
			return (int) ((12 * (Math.log(freq) / LOG_2 - LOG2_440) + NOTE_440HZ + 0.5));
		else
			return 0;
	}
}
