package zdream.nsfplayer.xgm.device;

/**
 * 可渲染的数据接口
 * @author Zdream
 */
public interface IRenderable0 {
	
	/**
	 * 声音的渲染
	 * @param bs
	 *   左频道和右频道的声音数据, 需要是 int[2]
	 * @return
	 *   合成了的数据的尺寸.
	 *   1:单声道
	 *   2:立体声
	 *   0:合成失败
	 */
	public int render(int[] bs);
	
	/**
	 * 芯片更新/操作现在绑定到 CPU 渲染(使用方法<code>render()</code>)
	 * 简单地混合并输出声音<br>
	 * chip update/operation is now bound to CPU clocks
	 * Render() now simply mixes and outputs sound
	 */
	public void tick(int clocks);
	
}
