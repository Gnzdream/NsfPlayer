package zdream.nsfplayer.core;

/**
 * Nsf 抽象音频执行构件
 * 
 * @param <T>
 *   音频数据
 *
 * @author Zdream
 * @since v0.3.0
 */
public abstract class AbstractNsfExecutor<T extends AbstractNsfAudio> implements INsfExecutor<T>, INsfChannelCode {
	
	public AbstractNsfExecutor() {
		
	}
	
	/* **********
	 * 公共方法 *
	 ********** */
	
	private boolean enable;
	
	@Override
	public final boolean isEnable() {
		return enable;
	}

	@Override
	public final void setEnable(boolean enable) {
		this.enable = enable;
	}

}
