package zdream.nsfplayer.core;

/**
 * <p>Frame Sequence 接口.
 * <p>2A03 (+ 2A07) 相关轨道需要实现部分时间步长少于 1 帧的工作.
 * 比如:
 * <li>Pulse 轨道的 Envelope 包络更新, 大约每 0.25 帧 (240 Hz) 触发一次;
 * <li>Pulse 轨道的 Sweep 扫音, 大约每 0.5 帧 (120 Hz) 触发一次;
 * <li>Triangle 轨道的 Length Counter 计数器更新, 大约每 0.25 帧 (240 Hz) 触发一次;
 * <li>Noise 轨道的 Envelope 包络更新, 大约每 0.25 帧 (240 Hz) 触发一次;
 * <li>Noise 轨道的 Length Counter 计数器更新, 大约每 0.5 帧 (120 Hz) 触发一次;
 * </li>
 * </p>
 * 
 * @author Zdream
 * @since v0.2.8
 */
public interface IFrameSequence {
	
	public static final int SEQUENCE_STEP_NTSC = 7458;
	public static final int SEQUENCE_STEP_PAL = 8314;
	
	/**
	 * 设置每次 Frame Sequence 更新时的时钟数
	 * @param clock
	 *   每个 Frame Sequence 的时钟数, 这个时钟数大概对应 NTSC 60Hz 下的 0.25 帧.
	 *   在不同的制式下该值的取值也不同.
	 * @see #SEQUENCE_STEP_NTSC
	 * @see #SEQUENCE_STEP_PAL
	 */
	public void setSequenceStep(int clock);

}
