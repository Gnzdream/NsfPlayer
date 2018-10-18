package zdream.nsfplayer.xgm.device;

import zdream.nsfplayer.nsf.device.IDevice;

/**
 * 虚拟声卡
 * @author Zdream
 */
public interface ISoundChip extends IDevice, IRenderable0 {
	
	/**
	 * 设置芯片的时钟周期
	 * @param clock
	 *   时钟周期数
	 */
	public void setClock(double clock);
	
	/**
	 * 设置语言合成率
	 */
	public void setRate(double r);
	
	/**
     * Channel mask.
     */
	public void setMask(int mask);
	
	/**
     * Stereo mix.
     * @param mixl 左声道 = 0-256
     * @param mixr 右声道 = 0-256
     *     128 = neutral
     *     256 = double
     *     0 = nil
     *    <0 = inverted
     */
    public void setStereoMix(int trk, int mixl, int mixr);
    
    /**
     * Track info for keyboard view.
     * @param trk
     * @return
     *   默认返回 null
     */
    public ITrackInfo getTrackInfo(int trk);
    
}
