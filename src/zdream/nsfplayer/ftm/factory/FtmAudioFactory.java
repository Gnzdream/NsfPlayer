package zdream.nsfplayer.ftm.factory;

import java.io.IOException;

import zdream.nsfplayer.ftm.document.FtmAudio;
import zdream.utils.common.BytesReader;
import zdream.utils.common.FileUtils;
import zdream.utils.common.TextReader;

/**
 * <p>Famitracker 文本文件的工厂, 负责解析 FTM txt, 生成 FtmAudio 类.
 * <p>一般而言, 用法是将文本的 {@link String}, {@link CharSequence} 或者 byte[] 流作为参数
 * 输入. 至于怎么读文件就不是这个工厂类的工作了.
 * <p>解析 Ftm 导出的文本文件, 使用的方法 {@link #create(String)}
 * 
 * @author Zdream
 */
public class FtmAudioFactory {
	
	/**
	 * 解析 Ftm 文件, 生成 Ftm-Audio.
	 * @param bs
	 *   文件数据
	 * @return
	 */
	public FtmAudio create(byte[] bs) throws FtmParseException {
		BytesReader reader = new BytesReader(bs);
		return createFtm(reader);
	}
	
	/**
	 * 解析 Ftm 文件, 生成 Ftm-Audio.
	 * @param filepath
	 *   文件路径
	 * @return
	 */
	public FtmAudio create(String filepath) throws IOException, FtmParseException {
		DocumentReader openFile = new DocumentReader(filepath);
		
		openFile.open();
		
		// 如果是空文件的话, 就直接报错
		if (openFile.length() == 0) {
			throw new IOException("文件: " + filepath + " 是空文件");
		}
		
		return createFtm(openFile);
	}
	
	private FtmAudio createFtm(BytesReader reader) throws FtmParseException {
		FamiTrackerCreater creater = new FamiTrackerCreater();
		
		FtmAudio audio = new FtmAudio();
		creater.doCreate(reader, audio.handler);
		
		return audio;
	}
	
	/**
	 * 解析 Ftm 导出的文本文件, 生成 Ftm-Audio.
	 * @param txt
	 *   文件数据, 任意字符串类型
	 * @return
	 */
	public FtmAudio createFromText(String txt) throws FtmParseException {
		FamiTrackerTextCreater creater = new FamiTrackerTextCreater();
		
		TextReader reader = new TextReader(txt);
		FtmAudio audio = new FtmAudio();
		creater.doCreate(reader, audio.handler);
		
		return audio;
	}
	
	/**
	 * 解析 Ftm 导出的文本文件, 生成 Ftm-Audio.
	 * @param filepath
	 *   文件路径
	 * @return
	 */
	public FtmAudio createFromTextPath(String path) throws IOException, FtmParseException {
		return createFromText(FileUtils.readFileAsString(path));
	}
	
	public static void main(String[] args) throws Exception {
		String txt = FileUtils.readFileAsString("src\\assets\\test\\Editor_05.txt");
		
		FtmAudioFactory factory = new FtmAudioFactory();
		FtmAudio fa = factory.createFromText(txt);
		System.out.println(fa);
	}

}
