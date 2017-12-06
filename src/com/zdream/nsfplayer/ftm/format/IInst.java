package com.zdream.nsfplayer.ftm.format;

/**
 * <p>乐器接口.
 * 
 * <p>注意, 这部分的类因为访问频繁, 不再设置 get set 等封装方法
 * 
 * @author Zdream
 */
public interface IInst extends IInstParam {
	
	/**
	 * 标识这个乐器的类型
	 * @return
	 */
	public int instType();
	
}
