package zdream.nsfplayer.mpeg;

import zdream.utils.common.BytesUtils;

/**
 * <p>mpeg 解码器.
 * <p>该解码器指定将 Mpeg 格式的音频转化成 byte 数组
 * @author Zdream
 * @since v0.1
 * @date 2018-01-16
 */
public class MpegDecoder{
	
	static final int HEADER_MASK = 0xffe00000;
	
	/**
	 * 记录正在解码的位置, 这个值是指向 audio.datas 的索引.
	 */
	private int pos;
	private int endPos;
	
	private boolean end;
	
	/**
	 * 保留第一帧的头数据, (只保留用来检查的几位), 作为判断后面帧头是否合法的依据
	 */
	private int firstH;
	
	/**
	 * 用于解码的 layer
	 */
	private AbstractLayer layer;
	
	private MpegFrameHead header = new MpegFrameHead();
	// private AbstractLayer layer;
	
	private MpegAudio audio;
	
	public void ready(MpegAudio audio) {
		this.audio = audio;
		
		pos = 0; // TODO 应该跳过 ID3V2
		endPos = audio.datas.length; // TODO 应该跳过 ID3V1
		firstH = 0;
		end = false;
	}
	
	/**
	 * 解码器的主方法. 每次调用这个方法时, 解码器解码下一帧
	 */
	public byte[] decode() {
		// 寻找帧头
		if (!detectFrameHead()) {
			if (end) {
				// 向 audio 记录到末尾的情况
			}
			return null;
		}
		
		// 解码的主要方法
		return decode0();
	}
	
	boolean detectTerminal() {
		if (pos >= endPos) {
			// TODO 流读取时不是这样, 而是再读一段数据
			return end = true;
		}
		
		return false;
	}
	
	public boolean isEnd() {
		return end;
	}
	
	/**
	 * <p>从 pos 指向的位置开始 (包括 pos), 寻找帧头的位置.
	 * <p>寻找到之后, 将改变成员变量 pos 的位置, 让它指向帧头;<br>
	 * 并读取帧头数据
	 * @return
	 *   如果没有找到帧头, 则返回 false, 找到则返回 true
	 */
	boolean detectFrameHead() {
		
		if (end) {
			return false;
		}
		
		while (true) {
			if (audio.datas[pos] == -1) { // 0xFF -> -1
				if (isLegalHead()) { // 该方法自己会解析 header
					break;
				}
			}
			pos++;
			
			// 帧头一定有 4 字节的. 如果不满 4 字节一定不是帧头
			if (endPos - pos < 4) {
				return false;
			}
		}
		
		// 现在 pos 指向 audio.datas 的帧头
		
		// TODO 现在能拿到帧长度. 如果用流来缓存的话, 检查该帧是否已经完全读出来了
		// 帧长度: header.getFrameSize();
		
		// 如果已经发现到了最后的话, 那就说明没有数据字段了, 这个肯定不能算
		return true;
	}
	
	/**
	 * 检查 audio.datas[pos] 是不是指向一个合法的帧头的位置
	 * @return
	 *   如果是, 返回 true, 并且 header 已经解析完帧头的数据, 否则返回 false
	 */
	boolean isLegalHead() {
		int h = BytesUtils.bytes2Int(audio.datas, pos);
		if ((h & HEADER_MASK) == HEADER_MASK
				&& ((h >> 19) & 3) != 1 // version ID:  '01' - reserved
				&& ((h >> 17) & 3) != 0 // Layer index: '00' - reserved
				&& ((h >> 12) & 15) != 15 // Bitrate Index: '1111' - reserved
				&& ((h >> 12) & 15) != 0 // Bitrate Index: '0000' - free
				&& ((h >> 10) & 3) != 3) {// Sampling Rate Index: '11' - reserved
			
			// 下面按照第一帧的帧头来判断是否合法
			if (firstH == 0) {
				// 设置第一帧的头信息
				firstH = 0xffe00000 |
						(h & 0x180000) | // version ID
						(h & 0x60000) | // Layer index
						(h & 0xc00); // sampling_frequency
			} else {
				// 以第一帧头信息为检查对象, 检查这个头信息
				int mask = 0xffe00000; 		// syncword
				mask |= h & 0x180000;	// version ID
				mask |= h & 0x60000;	// Layer index
				mask |= h & 0xc00;		// sampling_frequency
				// mode, mode_extension 不是每帧都相同
				
				if (firstH != mask) {
					return false;
				}
			}
			
			byte l = header == null ? 0 : header.getLayer();
			
			// 帧头解码
			header.decode(h);
			
			// 看帧头信息, 是否需要更换 Layer
			if (l != header.getLayer()) {
				if (header.isLayer3()) {
					layer = new Layer3();
				} else if (header.isLayer2()) {
					
				} else if (header.isLayer1()) {
					
				}
			}
			
			return true;
		}
		return false;
	}
	
	/**
	 * 用相应的解码器进行解码
	 */
	byte[] decode0() {
		// 现在 pos 指向帧头
		
		int ptr = pos + 4; // 跳过帧头
		if (header.isProtected())
			ptr += 2; // 忽略 CRC-word 字段
		
		int length = header.getMainDataSize();
		
		byte[] ret = null;
		
		if (ptr + length <= audio.datas.length && layer != null) {
			layer.ready(header);
			ret = layer.decode(audio.datas, ptr, length);
		}
		
		// 现在 pos 指向该帧帧尾, 可能是下一帧的帧头
		pos += header.getFrameSize();
		detectTerminal();
		
		return ret;
	}

}
