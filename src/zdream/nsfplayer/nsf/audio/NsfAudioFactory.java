package zdream.nsfplayer.nsf.audio;

import java.io.IOException;

import zdream.utils.common.FileUtils;

/**
 * <p>用于生成 NSF 音频结构 (原则上不支持 NSFe)
 * @author Zdream
 * @version v0.1
 * @date 2018-01-16
 */
public class NsfAudioFactory {

	public NsfAudioFactory() {}
	
	public NsfAudio createFromFile(String path) throws IOException, NsfAudioException {
		return create(FileUtils.readFile(path));
	}
	
	public NsfAudio create(byte[] image) throws NsfAudioException {
		return create(image, 0);
	}
	
	/**
	 * 从 byte 数组中读取并生成 NSF 数据. 不支持 NSFe
	 * @param image
	 *   镜像 byte 数组
	 * @param offset
	 *   镜像 byte 数据从数组第几个数据开始, 默认 0
	 * @return
	 * @throws NsfAudioException
	 *   文件格式不匹配导致读取失败
	 */
	public NsfAudio create(byte[] image, int offset) throws NsfAudioException {
		
		if (image.length < 0x80) // 这里相当于检查 image == null
			throw new NsfAudioException("镜像数组太小");
		
		// 指向 image 的索引
		int ptr = offset;
		
		NsfAudio audio = new NsfAudio();
		
		// 检查开头的 4 个字节
		final byte[] HEAD = {'N', 'E', 'S', 'M'};
		for (int i = 0; i < HEAD.length; i++) {
			if (HEAD[i] != image[ptr++]) {
				throw new NsfAudioException("文件头标识有误");
			}
		}
		ptr++; // 第 5 字节忽略
		
		audio.version = (short) (image[ptr++] & 0xFF);
		audio.total_songs = (short) (image[ptr++] & 0xFF);
		audio.start = (short) (image[ptr++] & 0xFF);
		
		audio.load_address = (image[ptr] & 0xFF) | ((image[ptr + 1] & 0xFF) << 8);
		ptr += 2;
		audio.init_address = (image[ptr] & 0xFF) | ((image[ptr + 1] & 0xFF) << 8);
		ptr += 2;
		audio.play_address = (image[ptr] & 0xFF) | ((image[ptr + 1] & 0xFF) << 8);
		ptr += 2;
		
		int end;
		
		// 标题部分
		int nextPtr = ptr + 32; // [ptr, nextPtr) 这部分是写明 title 数据的
		for (end = ptr; end < nextPtr; end++) {
			if (image[end] == 0)
				break;
		}
		audio.title = new String(image, ptr, end - ptr);
		
		// 艺术家部分
		ptr = nextPtr;
		nextPtr = ptr + 32;
		for (end = ptr; end < nextPtr; end++) {
			if (image[end] == 0)
				break;
		}
		audio.artist = new String(image, ptr, end - ptr);
		
		// 版权声明部分
		ptr = nextPtr;
		nextPtr = ptr + 32;
		for (end = ptr; end < nextPtr; end++) {
			if (image[end] == 0)
				break;
		}
		audio.copyright = new String(image, ptr, end - ptr);
		
		ptr = nextPtr;
		audio.speed_ntsc = (image[ptr] & 0xFF) | ((image[ptr + 1] & 0xFF) << 8);
		ptr += 2;
		
		// 0x70
		for (int i = 0; i < 8; i++) {
			audio.bankswitch[i] = (short) (image[ptr++] & 0xFF);
		}
		
		audio.speed_pal = (image[ptr] & 0xFF) | ((image[ptr + 1] & 0xFF) << 8);
		ptr += 2;
		audio.pal_ntsc = (byte) (image[ptr++] & 0xFF);
		
		if (audio.speed_pal == 0)
			audio.speed_pal = 19997;
		if (audio.speed_ntsc == 0)
			audio.speed_ntsc = 16639;
		audio.soundchip = (byte) (image[ptr++] & 0xFF); // 0x7b

		// extra 占 4 byte
		ptr += 4;
		
		byte[] body = new byte[image.length - ptr]; // ptr == 0x80
		System.arraycopy(image, 0x80, body, 0, body.length);
		audio.body = body;
		
		return audio;

	}

}
