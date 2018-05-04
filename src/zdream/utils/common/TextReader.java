package zdream.utils.common;

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
	int line;
	
	public void set(String text) {
		this.text = text;
		reset();
	}
	
	public void reset() {
		
	}

}
