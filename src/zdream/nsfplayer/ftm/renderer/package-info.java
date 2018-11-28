/**
 * <p>FamiTracker 里面的渲染部分, 其主要任务就是运行 FTM 文件, 尝试输出音频采样数据.
 * 该包依赖 zdream.nsfplayer.sound 包来进行音频的渲染.
 * 从版本 v0.3.0 开始, 绝大部分的执行构件移动到包 zdream.nsfplayer.ftm.executor 下.
 * 
 * <p>如果需要对 FTM 文件进行渲染播放的话, 这里推荐使用
 * {@link zdream.nsfplayer.ftm.renderer.FamiTrackerRenderer} 类.
 * <p>如果需要对 FTM 文件进行执行的话, 这里推荐使用
 * {@link zdream.nsfplayer.ftm.executor.FamiTrackerExecutor} 类.
 * </p>
 * @author Zdream
 * @since v0.2.1
 */
package zdream.nsfplayer.ftm.renderer;