package com.zdream.nsfplayer.mpeg;

import java.io.IOException;

import com.zdream.utils.common.FileUtils;

/**
 * <p>Mpeg 格式的音频数据产生工厂
 * @author Zdream
 * @since v0.1
 * @date 2018-01-16
 */
public class MpegFactory {
	
	public MpegAudio createFromFile(String path) throws IOException, MpegAudioException {
		return create(FileUtils.readFile(path));
	}
	
	/**
	 * 从 byte 数组中读取并生成 mpeg 数据
	 * @param image
	 *   镜像 byte 数组
	 * @return
	 * @throws MpegAudioException
	 *   文件格式不匹配导致读取失败
	 */
	public MpegAudio create(byte[] image) throws MpegAudioException {
		MpegAudio audio = new MpegAudio();
		
		int offset = handleId3v2(image);
		
		System.out.println(System.currentTimeMillis());
		if (offset == 0) {
			audio.datas = image;
		} else {
			audio.datas = new byte[image.length - offset];
			System.out.println(audio.datas.length);
			System.arraycopy(image, offset, audio.datas, 0, audio.datas.length);
		}
		System.out.println(System.currentTimeMillis());
		
		return audio;
	}

	/**
	 * 读取并跳过 ID3V2 部分
	 * @param image
	 * @return
	 *   ID3V2 部分的数据长度, 如果没有 ID3V2 部分则返回 0.
	 */
	int handleId3v2(byte[] image) {
		if (image[0] == 'I' && image[1] == 'D' && image[2] == '3') {
			int size = (image[6] & 0x7F) << 21 | (image[7] & 0x7F) << 14 | (image[8] & 0x7F) << 7 | (image[9] & 0x7F);
			return size + 10; // 10 是 ID3V2 帧头的大小, size 没有包括它
		}
		return 0;
	}

}

