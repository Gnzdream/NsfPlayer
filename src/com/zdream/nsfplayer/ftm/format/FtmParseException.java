package com.zdream.nsfplayer.ftm.format;

public class FtmParseException extends RuntimeException {

	private static final long serialVersionUID = 9126958051285118053L;
	
	public FtmParseException(int line, String message) {
		super(String.format("第 %d 行, %s", line, message));
	}
	
	public FtmParseException(String message) {
		super(message);
	}

	public FtmParseException(int line, String message, Throwable cause) {
		super(String.format("第 %d 行, %s", line, message), cause);
	}
	
	public FtmParseException(String message, Throwable cause) {
		super(message, cause);
	}

}
