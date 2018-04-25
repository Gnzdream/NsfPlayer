package zdream.nsfplayer.ftm.format;

import zdream.utils.common.FileUtils;

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
	 * 解析 Ftm 导出的文本文件, 生成 Ftm-Audio.
	 * @param txt
	 * @return
	 */
	public FtmAudio create(String txt) {
		FtmAudioFactoryEntry entry = new FtmAudioFactoryEntry(txt);
		return entry.createAudio();
	}
	
	public static void main(String[] args) throws Exception {
		String txt = FileUtils.readFileAsString("src\\assets\\test\\Editor_05.txt");
		
		FtmAudioFactory factory = new FtmAudioFactory();
		FtmAudio fa = factory.create(txt);
		System.out.println(fa);
	}

}
