/**
 * <p>发声逻辑相关类所在包.
 * <p>该包里面放置很多由 {@link zdream.nsfplayer.sound.AbstractNsfSound} 的子类,
 * 它们作为执行构件的末端, 用于接收从执行构件核心发来的核心数据,
 * 向渲染构件发送音频数据.
 * <p>在版本 v0.2.x 中, 作为渲染构件主体的混音器放置在该包中.
 * 从 v0.3.0 开始, 这些类移到 {@link zdream.nsfplayer.mixer} 包中,
 * 于是执行构件和渲染构件正式分离.
 * </p>
 * 
 * @author Zdream
 * @since v0.1
 */
package zdream.nsfplayer.sound;