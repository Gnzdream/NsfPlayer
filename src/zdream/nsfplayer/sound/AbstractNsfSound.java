package zdream.nsfplayer.sound;

import zdream.nsfplayer.sound.mixer.IMixerChannel;

/**
 * 音频发声器的超类
 * @author Zdream
 * @since 0.2.1
 */
public abstract class AbstractNsfSound implements IResetable {

	/**
	 * 通向合成器 Buffer 的管道
	 */
	protected IMixerChannel out;
	
	/**
	 * 记录已经渲染的时间.
	 * 每帧结束时, 调用 {@link #endFrame()} 重置为 0
	 */
	protected int time;
	
	/**
	 * 是否启用的标志位
	 */
	private boolean enable;
	
	/**
	 * 每帧结束时调用
	 */
	public void endFrame() {
		time = 0;
	}
	
	/**
	 * @param out
	 *   {@link #out}
	 */
	public void setOut(IMixerChannel out) {
		this.out = out;
	}
	
	/**
	 * @return
	 *   {@link #enable}
	 */
	public boolean isEnable() {
		return enable;
	}
	
	/**
	 * @param enable
	 *   {@link #enable}
	 */
	public void setEnable(boolean enable) {
		this.enable = enable;
	}
	
	@Override
	public void reset() {
		this.endFrame();
	}
	
	/**
	 * <p>开始运作 time 的时间数.
	 * <p>如果该发声器启用 ({@link #enable} == true) 则会向 {@link #out} 传递音频相关数据
	 * 否则将直接跳过这段时间.
	 * </p>
	 * @param time
	 */
	public final void process(int time) {
		if (enable) {
			onProcess(time);
		}
		this.time += time;
	}
	
	/**
	 * 发声器工作, 并向 {@link #out} 传递音频相关数据
	 * @param time
	 */
	protected abstract void onProcess(int time);

}
