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
		
		// TODO 读取 ID3V2 部分
		
		audio.datas = image;
		
		return audio;
	}

}

