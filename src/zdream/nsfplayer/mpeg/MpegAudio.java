package zdream.nsfplayer.mpeg;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Mpeg 格式的音频数据
 * <p>Mpeg 格式的音频, 最常见的就是 mp3 文件音频.<br>
 * 文件由 ID3V2(TAG_V2), Frame(Layer)[], ID3V1(TAG_V1) 组成.
 * <ul>
 *  <li>ID3V2 包含了作者, 作曲, 专辑等信息, 长度不固定, 扩展了 ID3V1 的信息量.<br>
 *  ID3V2 是可以没有的
 *  <li>Frame 一系列的帧，个数由文件大小和帧长决定<br>
 *  每个 Frame 的长度可能不固定，也可能固定，由比特率 bitrate 决定
 *  每个 Frame 又分为帧头和数据实体两部分<br>
 *  帧头记录了 mp3 的比特率，采样率，版本等信息，每个帧之间相互独立
 *  <li>ID3V1 包含了作者, 作曲, 专辑等信息, 长度固定为 128 bytes<br>
 *  ID3V1 是可以没有的
 * </ul>
 * 
 * @author Zdream
 * @since v0.1
 * @date 2018-01-16
 */
public class MpegAudio {

	public MpegAudio() {
		
	}
	
	/**
	 * 原始数据镜像 （摘掉 ID3V2 部分后的）
	 */
	byte[] datas;
	
	List<MpegFrame> frames = new ArrayList<>();

	public MpegFrame detectFrame() {
		MpegFrame frame = new MpegFrame(this, frames.size());
		frames.add(frame);
		
		return frame;
	}

}
