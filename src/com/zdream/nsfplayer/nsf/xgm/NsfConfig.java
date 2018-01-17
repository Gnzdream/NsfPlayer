package com.zdream.nsfplayer.nsf.xgm;

import java.io.IOException;
import java.util.Properties;

public class NsfConfig {

	public NsfConfig() {
		pro = new Properties();
		try {
			pro.load(getClass().getResourceAsStream("nsf_config.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println(pro);
	}
	
	public void reset() {
		
	}
	
	Properties pro;
	
	public String get(String key) {
		return pro.getProperty(key);
	}
	
	public String get(String key, String def) {
		String v = pro.getProperty(key);
		return (v == null) ? def : v;
	}
	
	public int getInt(String key, int def) {
		String v = pro.getProperty(key);
		if (v == null) {
			return def;
		}
		return Integer.parseInt(v);
	}

}
