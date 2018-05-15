package zdream.utils.common;

import java.util.Scanner;

/**
 * byte 数组读取器
 * @author Zdream
 * @date 2018-04-25
 * @since v0.1
 */
public class TextReader {

	String text;
	
	/*
	 * 行号
	 */
	int l = 0;
	
	Scanner scanner;
	
	/**
	 * 储存本行文本
	 */
	String lineBuf;
	
	public TextReader(String text) {
		this.text = text;
		scanner = new Scanner(text);
	}
	
	// 基本操作
	
	/**
	 * 下一行文本
	 * @return
	 */
	public String nextLine() {
		if (!scanner.hasNextLine()) {
			lineBuf = null;
			l = 0;
		}
		
		lineBuf = scanner.nextLine();
		l++;
		
		return lineBuf;
	}
	
	/**
	 * 本行文本
	 * @return
	 */
	public String thisLine() {
		return lineBuf;
	}
	
	/**
	 * @return
	 *  本行行号, 第一行为 1. 还没读取或已经读取完毕时第一行时为 0
	 */
	public int line() {
		return l;
	}
	
	public void close() {
		scanner.close();
	}
	
	public boolean isFinished() {
		return !scanner.hasNextLine();
	}
	
	// 进阶操作
	
	/**
	 * 询问现在这行是否为有效行.
	 * 只要是空行、以 # 开头的行, 都不是有效行
	 */
	public boolean isValidLine() {
		if (lineBuf.trim().equals("")) {
			return false;
		}
		if (lineBuf.charAt(0) == '#') {
			return false;
		}
		return true;
	}
	
	/**
	 * 跳转到下一个有效的行.
	 * @return
	 *   当没有下一个有效行时, 返回 0
	 */
	public int toNextValidLine() {
		while (scanner.hasNextLine()) {
			nextLine();
			
			if (isValidLine()) {
				return l;
			}
		}
		return 0;
	}
}
