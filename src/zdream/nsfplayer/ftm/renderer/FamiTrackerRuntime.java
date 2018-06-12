package zdream.nsfplayer.ftm.renderer;

import java.util.HashMap;

import zdream.nsfplayer.ftm.document.FamiTrackerQuerier;
import zdream.nsfplayer.ftm.format.FtmNote;

/**
 * Famitracker 运行时状态
 * 
 * @author Zdream
 * @since v0.2.1
 */
public class FamiTrackerRuntime {
	
	/* **********
	 *   成员   *
	 ********** */
	public FtmRowFetcher fetcher;
	
	public final HashMap<Byte, AbstractFtmChannel> channels = new HashMap<>();
	
	/* **********
	 *   工具   *
	 ********** */

	/**
	 * 查询器.
	 * <br>由 Fetcher 生成, 管理
	 */
	public FamiTrackerQuerier querier;
	
	/* **********
	 *   数据   *
	 ********** */
	/**
	 * 放着正解释的行里面的所有键
	 */
	public HashMap<Byte, FtmNote> notes = new HashMap<>();

}
