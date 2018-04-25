package zdream.nsfplayer.xgm.player;

/**
 * 序列类型性能数据
 * @author Zdream
 */
public class SoundData {

    /**
     * @return 标题
     */
	public String getTitle() {
		return "";
	}

    /**
     * 设置标题文字
     * @param title 新标题文字, 不超过 255 字
     */
    public void setTitle(String title) {}

    /**
     * 音频演奏的总时间
     */
	public int getLength() {
		return 3 * 60 * 1000;
	}

    /**
     * 设置音频演奏时间
     */
    public void setLength(int time_in_ms) {}

}
