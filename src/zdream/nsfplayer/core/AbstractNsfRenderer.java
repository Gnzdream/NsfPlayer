package zdream.nsfplayer.core;

/**
 * 抽象的 NSF 音源的渲染器, 用于输出以 byte / short 数组组织的 PCM 音频数据
 * 
 * @version 0.3.2
 *   相关方法独立抽出接口, 
 * 
 * @author Zdream
 * @since v0.2.4
 */
public abstract class AbstractNsfRenderer<T extends AbstractNsfAudio> extends AbstractRenderer<T>
	implements INsfRendererHandler<T> {

}
