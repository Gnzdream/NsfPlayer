package zdream.nsfplayer.xgm.player;

import zdream.nsfplayer.vcm.Configuration;

/**
 * 用于管理语音合成系统设置的类
 * @author Zdream
 */
public abstract class PlayerConfig extends Configuration {

	public abstract boolean load(String file, String sect);

	public abstract boolean save(String file, String sect);

	public abstract boolean load(String file, String sect, String name);

	public abstract boolean save(String file, String sect, String name);
	
}
