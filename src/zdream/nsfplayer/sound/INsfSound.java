package zdream.nsfplayer.sound;

public interface INsfSound {
	
	public String name();
	
	/**
	 * 输出音频大小, 一个采样数据, 振幅在 0 到 8191 之间
	 * @return
	 */
	default public int render() {
		return 0;
	}

}
