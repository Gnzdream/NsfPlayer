package com.zdream.utils.common;

import java.io.File;
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

}
