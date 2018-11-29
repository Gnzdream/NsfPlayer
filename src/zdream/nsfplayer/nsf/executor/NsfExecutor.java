package zdream.nsfplayer.nsf.executor;

import zdream.nsfplayer.core.AbstractNsfExecutor;
import zdream.nsfplayer.nsf.audio.NsfAudio;
import zdream.nsfplayer.nsf.renderer.NsfRendererConfig;
import zdream.nsfplayer.nsf.renderer.NsfRuntime;

/**
 * <p>Nsf 的执行构件.
 * <p>在 0.2.x 版本中, Nsf 的执行部分是直接写在 NsfRenderer 中的,
 * 从版本 0.3.0 开始, 执行构件从 renderer 中分离出来, 单独构成一个类.
 * 它交接了原本是需要 NsfRuntime 或 NsfRenderer 完成的任务中, 与执行相关的任务.
 * </p>
 * 
 * @author Zdream
 * @since v0.3.0
 */
public class NsfExecutor extends AbstractNsfExecutor<NsfAudio> {
	
	private final NsfRuntime runtime;
	
	public NsfExecutor() {
		this.runtime = new NsfRuntime();
		runtime.init();
	}
	
	/**
	 * TODO 待删除
	 * @return
	 */
	public NsfExecutor(NsfRendererConfig config) {
		this.runtime = new NsfRuntime(config);
		runtime.init();
	}
	
	/**
	 * TODO 待移交
	 * @return
	 */
	public NsfRuntime getRuntime() {
		return runtime;
	}
	
	/* **********
	 * 准备部分 *
	 ********** */
	
	/**
	 * 设置 tick() 的执行的速率.
	 * @param rate
	 *   执行速率. 一般这个值等于 sampleRate
	 */
	public void setRate(int rate) {
		runtime.param.sampleRate = rate; // 默认: 48000
	}

	@Override
	public void ready(NsfAudio audio) {
		// TODO Auto-generated method stub

	}
	
	/* **********
	 * 渲染部分 *
	 ********** */

	@Override
	public void tick() {
		// TODO Auto-generated method stub

	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}
	
	/* **********
	 *  监听器  *
	 ********** */
	
	/**
	 * 添加 N163 重连的监听器
	 * @param listener
	 */
	public void addN163ReattachListener(IN163ReattachListener listener) {
		runtime.n163Lsners.add(listener);
	}
	
	/**
	 * 删除 N163 重连的监听器
	 * @param listener
	 */
	public void removeReattachListener(IN163ReattachListener listener) {
		runtime.n163Lsners.remove(listener);
	}
	
	/**
	 * 清空 N163 重连的监听器
	 */
	public void clearReattachListeners() {
		runtime.n163Lsners.clear();
	}

}
