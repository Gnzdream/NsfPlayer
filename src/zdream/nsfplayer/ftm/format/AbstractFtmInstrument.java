package zdream.nsfplayer.ftm.format;

import zdream.nsfplayer.core.FtmChipType;

/**
 * <p>乐器接口.
 * 
 * <p>注意, 这部分的类因为访问频繁, 不再设置 get set 等封装方法
 * 
 * @author Zdream
 */
public abstract class AbstractFtmInstrument {
	
	/**
	 * 标识这个乐器的类型. 比如 2A03
	 * @return
	 */
	public abstract FtmChipType instType();
	
	/**
	 * 序号, 在同一个 NsfAudio 中, 同一种类型的乐器的 seq 是各不相同的.
	 * 这个值从 0 开始
	 */
	public int seq;
	
	public String name;
	
}
