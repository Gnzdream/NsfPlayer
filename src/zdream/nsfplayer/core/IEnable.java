package zdream.nsfplayer.core;

/**
 * 可以开关、开启 / 禁用的类接口
 * 
 * @author Zdream
 * @since v0.2.3
 */
public interface IEnable {
	
	/**
	 * 设置是否开启
	 * @param enable
	 */
	public void setEnable(boolean enable);
	
	/**
	 * 询问是否开启
	 * @return
	 */
	public boolean isEnable();

}
