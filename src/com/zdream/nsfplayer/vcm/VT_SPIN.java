package com.zdream.nsfplayer.vcm;

/**
 * 以数字形式映射到文本区域
 * @author Zdream
 */
public class VT_SPIN extends ValueCtrl {
	
	public int minValue, maxValue;

	public VT_SPIN(String l, String d, int min, int max) {
		super(l, d, CT_SPIN);
		this.minValue = min;
		this.maxValue = max;
		addConv(new VC_RANGE(min, max));
	}

}
