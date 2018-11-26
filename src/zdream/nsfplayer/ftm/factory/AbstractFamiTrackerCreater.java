package zdream.nsfplayer.ftm.factory;

import zdream.nsfplayer.ftm.audio.FamiTrackerHandler;
import zdream.nsfplayer.ftm.audio.FtmAudio;

/**
 * <p>抽象的 FamiTracker 数据创建工具, 用于创建、完善 {@link FtmAudio} 的信息.
 * <p>通过所给定的载体 T, 将载体中的数据读取、然后填充到 {@link FtmAudio} 中.
 * <p>该子类创建过程均不是线程安全的, 创建一个 {@link FtmAudio},
 * 工厂类都必须新建一个该类的实例.
 * </p>
 * @param <T>
 *   创建的 {@link FtmAudio} 所需要的数据载体
 * 
 * @author Zdream
 * @since v0.1
 */
public abstract class AbstractFamiTrackerCreater<T> {
	
	/* **********
	 *   创建   *
	 ********** */
	
	/**
	 * 利用数据载体 reader, 完善 {@link FtmAudio} 的信息
	 * @param reader
	 *   数据载体、数据源
	 * @param handler
	 *   FamiTracker 的音频的操作器.
	 *   通过该操作器可以向特定的 {@link FtmAudio} 写入数据
	 * @throws FamiTrackerFormatException
	 *   当 <code>reader</code> 提供的数据有误时
	 * @since v0.2.5
	 */
	public abstract void doCreate(T reader, FamiTrackerHandler handler)
			throws FamiTrackerFormatException;
	
	/* **********
	 * 错误处理 *
	 ********** */
	/*
	 * 当发现从数据来源 (上面所说的 T) 获得的数据是不正常的, 超出合理范围的数值,
	 * 就需要一个手段进行抛错处理.
	 */
	
	/**
	 * 错误处理. 只要进了该函数就一定会抛错.
	 * @param t
	 * @throws FamiTrackerFormatException
	 * @since v0.2.5
	 */
	protected abstract void handleException(T t, String msg) throws FamiTrackerFormatException;

}
