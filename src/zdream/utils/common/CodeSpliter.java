package zdream.utils.common;

import java.util.ArrayList;
import java.util.List;

/**
 * 根据命令行的方式将字符串切开<br>
 * 
 * @since v0.1
 * @author Zdream
 * @date 2016-04-16
 * @version v0.1
 */
public class CodeSpliter {
	
	public static String[] split(String str) {
		List<String> list = new ArrayList<String>();
		char[] chars = str.toCharArray();
		
		int index = 0; // chars 的索引
		int size = str.length(); // chars 的总长度
		
		int head = 0; // 子字符串的开头索引
		int tail = 0; // 子字符串的结尾索引
		int status = 0;
		
		for (; index < size; index++) {
			char ch = chars[index];
			switch (status) {
			case 0: // 没有开始检索
				if (Character.isSpaceChar(ch)) {
					continue;
				} else if (ch == '\"') {
					head = index;
					status = 3;
				} else {
					head = index;
					status = 1;
				}
				break;
				
			case 1: // 已经找到头（非双引号开头的）
				if (Character.isSpaceChar(ch)) {
					// 可以截断
					tail = index;
					list.add(new String(chars, head, tail - head));
					status = 0;
				} else {
					continue;
				}
				
			case 3: // 已经找到头（双引号开头的）
				if (ch == '\"') {
					if (index > 0 && chars[index - 1] != '\\') {
						// 可以截断
						tail = index;
						String ss = new String(chars, head + 1, tail - head - 1);
						ss.replace("\\\"", "\"");
						list.add(ss);
						status = 0;
					}
				} else {
					continue;
				}
			}
		}
		
		if (status == 1) {
			tail = index;
			list.add(new String(chars, head, tail - head));
		}
		
		return list.toArray(new String[list.size()]);
	}
	
	/**
	 * 将 "123" 这类文本, 将两端的引号去掉, 中间格式作处理之后, 返回
	 * @since v0.2.5
	 */
	public static String extract(String raw) {
		int head = 0; // 包括
		int tail = raw.length(); // 不包括
		
		if (raw.charAt(0) == '"') {
			head ++;
		}
		if (raw.charAt(tail - 1) == '"') {
			tail --;
		}
		if (tail <= head) {
			return "";
		}

		char[] chars = raw.toCharArray();
		int status = 0;
		int index = head;
		StringBuilder b = new StringBuilder(tail - head);
		
		for (; index < tail; index++) {
			char ch = chars[index];
			switch (status) {
			case 0: // 没有开始检索
				if (ch == '\\') {
					status = 1;
				} else {
					b.append(ch);
				}
				break;
				
			case 1: // 已经找到 '\', 有以下匹配:
				// '\"' 转成 '"'
				// '\\' 转成 '\'
				// '\ ' 转成 ' '
				// 其它情况, '\' 仍然视为有效字符 '\'
				
				if (Character.isSpaceChar(ch) || ch == '"' || ch == '\\') {
					b.append(ch);
				} else {
					b.append('\\').append(ch);
				}
				status = 0;
			}
		}
		
		// 结尾
		if (status == 1) {
			b.append('\\');
		}
		
		return b.toString();
	}
	
}
