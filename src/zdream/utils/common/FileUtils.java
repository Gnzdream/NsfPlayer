package zdream.utils.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 工具类, 用于读取、写入文件
 * @author Zdream
 *
 */
public class FileUtils {

	public static void writeFile(String path, String content) {
		File file = new File(path);
		FileWriter w = null;
		try {
			w = new FileWriter(file);
			w.write(content);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (w != null) {
				try {
					w.close();
				} catch (IOException e) {}
			}
		}
	}
	
	/**
	 * 将文件作为 byte[] 流读出.
	 * @param fn
	 *   file name 文件名
	 * @return
	 * @throws IOException
	 */
	public static byte[] readFile(String fn) throws IOException {
		File f = new File(fn);
		FileInputStream r = new FileInputStream(f);
		byte[] bs = new byte[(int) f.length()];
		r.read(bs);
		r.close();
		
		return bs;
	}
	
	/**
	 * 将文件作为 String 读出. 字符集默认为 UTF-8
	 * @param fn
	 *   file name 文件名
	 * @return
	 * @throws IOException
	 */
	public static String readFileAsString(String fn) throws IOException {
		return readFileAsString(fn, "UTF-8");
	}
	
	/**
	 * 将文件作为 String 读出. 指定字符集
	 * @param fn
	 *   file name 文件名
	 * @param charset
	 *   字符集
	 * @return
	 * @throws IOException
	 */
	public static String readFileAsString(String fn, String charset) throws IOException {
		byte[] bs = readFile(fn);
		return new String(bs, charset);
	}

}
