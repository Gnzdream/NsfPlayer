package com.zdream.nsfplayer.vcm;

/**
 * 映射到滑块控件作为数字
 * @author Zdream
 */
public class VT_SLIDER extends ValueCtrl {
	
	public int minValue, maxValue;
	public String minDesc, maxDesc;
	public int ticFreq, pageSize;
	
	public VT_SLIDER(String label, String desc, int min, int max,
			String min_desc, String max_desc, int tic_freq, int page_size) {
		super(label, desc, CT_SLIDER);
		this.minValue = min;
		this.maxValue = max;
		this.minDesc = min_desc;
		this.maxDesc = max_desc;
		this.ticFreq = tic_freq;
		this.pageSize = page_size;
		addConv(new VC_RANGE(min, max));
	}

}
