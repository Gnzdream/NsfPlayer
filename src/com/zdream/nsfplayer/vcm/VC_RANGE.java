package com.zdream.nsfplayer.vcm;

/**
 * 最大和最小值的约束变换
 * @author Zdream
 */
public class VC_RANGE extends ValueConv {
	
	public int minValue, maxValue;

	public VC_RANGE(int min, int max) {
		super();
		this.minValue = min;
		this.maxValue = max;
	}
	
	public boolean getExportValue(ValueCtrl vt, Configuration cfg, final String id, final Value src_value, Value result) {
		int i = src_value.toInt();
		if (minValue <= i && i <= maxValue) {
			result = src_value;
			return true;
		} else {
			return false;
		}
	}
	
	public boolean getImportValue(ValueCtrl vt, Configuration cfg, final String id, final Value src_value, Value result) {
		int i = src_value.toInt();
		if (minValue <= i && i <= maxValue) {
			result = src_value;
			return true;
		} else {
			return false;
		}
	}

}
