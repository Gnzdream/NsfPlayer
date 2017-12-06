package com.zdream.nsfplayer.ftm.format;

import com.zdream.nsfplayer.ftm.format.FtmAudio.Macro2A03;

/**
 * 2A03 乐器部分
 * @author Zdream
 */
public final class Inst2A03 implements IInst {

	@Override
	public int instType() {
		return INST_TYPE_2A03;
	}
	
	/**
	 * 序号, 在同一个 NsfAudio 中, 同一种类型的乐器的 seq 是各不相同的.
	 * 这个值从 0 开始
	 */
	public int seq;
	
	public Macro2A03 vol;
	public Macro2A03 arp;
	public Macro2A03 pit;
	public Macro2A03 hip;
	public Macro2A03 dut;
	public String name;
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(80);
		
		builder.append(name).append(':').append(' ').append('#').append(seq).append('\n');
		if (vol != null) {
			builder.append("vol").append(':').append(' ').append(vol).append('\n');
		}
		if (arp != null) {
			builder.append("arp").append(':').append(' ').append(arp).append('\n');
		}
		if (pit != null) {
			builder.append("pit").append(':').append(' ').append(pit).append('\n');
		}
		if (hip != null) {
			builder.append("hip").append(':').append(' ').append(hip).append('\n');
		}
		if (dut != null) {
			builder.append("dut").append(':').append(' ').append(dut).append('\n');
		}
		
		return builder.toString();
	}

}
