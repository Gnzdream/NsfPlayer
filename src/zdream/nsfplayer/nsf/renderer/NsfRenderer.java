package zdream.nsfplayer.nsf.renderer;

import zdream.nsfplayer.ftm.audio.FtmAudio;
import zdream.nsfplayer.nsf.audio.NsfAudio;

/**
 * <p>NSF 渲染器.
 * <p>该类在 v0.2.3 版本以前基本处于不可用的状态, 直到 v0.2.4 版本进行了大量的改造.
 * </p>
 * 
 * @author Zdream
 * @since v0.1
 */
public class NsfRenderer {
	
	public NsfRenderer() {
		this.runtime = new NsfRuntime();
	}
	
	/**
	 * 读取 Nsf 音频, TODO 并以默认曲目进行准备
	 * @param audio
	 * @throws NullPointerException
	 *   当 audio 为 null 时
	 */
	public void ready(NsfAudio audio) {
		if (audio == null) {
			throw new NullPointerException("audio = null");
		}
		
		runtime.audio = audio;
		runtime.reload();
	}

	/**
	 * 读取 Nsf 音频, TODO 以指定曲目进行准备
	 * @param audio
	 *   Nsf 音频实例
	 * @param track
	 *   曲目号
	 * @throws NullPointerException
	 *   当 audio 为 null 时
	 * @throws IllegalArgumentException
	 *   当曲目号 track 在范围 [0, audio.total_songs) 之外时.
	 */
	public void ready(NsfAudio audio, int track) {
		if (audio == null) {
			throw new NullPointerException("audio = null");
		}
		if (track < 0 || track >= audio.total_songs) {
			throw new IllegalArgumentException(
					"曲目号 track 需要在范围 [0, " + audio.total_songs + ") 内");
		}
		
		runtime.audio = audio;
		// TODO track
		runtime.reload();
	}
	
	/**
	 * <p>在不更改 Nsf 音频的同时, 切换到指定曲目的开头.
	 * <p>第一次播放时需要指定 Nsf 音频数据.
	 * 因此第一次需要调用含 {@link NsfAudio} 参数的重载方法
	 * </p>
	 * @param track
	 *   曲目号, 从 0 开始
	 * @throws NullPointerException
	 *   当调用该方法前未指定 {@link FtmAudio} 音频时
	 * @throws IllegalArgumentException
	 *   当曲目号 track 在范围 [0, audio.total_songs) 之外时.
	 */
	public void ready(int track) throws NullPointerException {
		// TODO
	}

	/**
	 * 渲染 Nsf 音频, 将音频采样的数据放入到 b 数组中
	 * 需要之前 load 过.
	 * @param b
	 *   需要填充的 byte 数组
	 * @param offset
	 * @param size
	 * @return
	 *   实际填充的 byte 数据量
	 */
	public int render(byte[] b, int offset, int size) {
		// TODO
		return 0;
	}

	/* **********
	 * 所含数据 *
	 ********** */
	
	private NsfRuntime runtime;

}
